package os_simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TerminalGUI extends JFrame {

    JTextArea terminal;
    Process process;
    BufferedWriter writer;
    BufferedReader reader;
    BufferedReader errorReader;
    int promptPosition = 0; // علشان نعرف امتى يبدأ الأمر

    public TerminalGUI() {
        
        setTitle("Terminal - Zero Trace");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        terminal = new JTextArea();
        terminal.setBackground(Color.BLACK);
        terminal.setForeground(Color.GREEN);
        terminal.setCaretColor(Color.GREEN);
        terminal.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(terminal);
        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);

        startShell();
        handleKeyInput();
    }

    private void startShell() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "-i", "priceless_germain", "bash"
            );
            pb.redirectErrorStream(false);
            process = pb.start();

            writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

            appendText("Connected to Zero Trace OS\n$ ");
            readOutput();
            readError();

        } catch (Exception e) {
            appendText("Error: " + e.getMessage() + "\n");
        }
    }

    private void handleKeyInput() {
        terminal.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                // لما يضغط Enter
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume(); // امنع الـ Enter الافتراضي

                    String text = terminal.getText();
                    String command = text.substring(promptPosition).trim();

                    appendText("\n");

                    if (!command.isEmpty()) {
                        try {
                            writer.write(command + "\n");
                            writer.flush();
                        } catch (Exception ex) {
                            appendText("Error: " + ex.getMessage() + "\n");
                        }
                    } else {
                        appendText("$ ");
                        promptPosition = terminal.getText().length();
                    }
                }

                // امنع المستخدم من مسح الـ prompt
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (terminal.getCaretPosition() <= promptPosition) {
                        e.consume();
                    }
                }

                // امنع مسح النص القديم
                if (terminal.getCaretPosition() < promptPosition) {
                    if (e.getKeyCode() != KeyEvent.VK_RIGHT &&
                        e.getKeyCode() != KeyEvent.VK_LEFT &&
                        e.getKeyCode() != KeyEvent.VK_UP &&
                        e.getKeyCode() != KeyEvent.VK_DOWN) {
                        terminal.setCaretPosition(terminal.getText().length());
                    }
                }
            }
        });
    }

    private void appendText(String text) {
        SwingUtilities.invokeLater(() -> {
            terminal.append(text);
            terminal.setCaretPosition(terminal.getText().length());
            promptPosition = terminal.getText().length();
        });
    }

    private void readOutput() {
        new Thread(() -> {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    final String out = line;
                    SwingUtilities.invokeLater(() -> {
                        terminal.append(out + "\n");
                        terminal.append("$ ");
                        terminal.setCaretPosition(terminal.getText().length());
                        promptPosition = terminal.getText().length();
                    });
                }
            } catch (Exception e) {
                appendText("\nStream closed\n");
            }
        }).start();
    }

    private void readError() {
        new Thread(() -> {
            String line;
            try {
                while ((line = errorReader.readLine()) != null) {
                    final String err = line;
                    SwingUtilities.invokeLater(() -> {
                        terminal.append(err + "\n");
                        terminal.append("$ ");
                        terminal.setCaretPosition(terminal.getText().length());
                        promptPosition = terminal.getText().length();
                    });
                }
            } catch (Exception e) {
                // ignore
            }
        }).start();
    }
}
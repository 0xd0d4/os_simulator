package os_simulator;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.*;

public class TextEditorFrame extends JFrame {

    private JTextArea textArea;
    private File currentFile;

    // الألوان
    private static final Color BG_DARK     = new Color(26, 18, 48);   // خلفية التيكست
    private static final Color BG_MENU     = new Color(38, 33, 92);   // شريط القوائم
    private static final Color BG_TITLE    = new Color(60, 52, 137);  // التايتل بار
    private static final Color FG_TEXT     = new Color(232, 224, 255); // لون الكتابة
    private static final Color FG_MUTED    = new Color(175, 169, 236); // لون ثانوي

    public TextEditorFrame() {
        setupUI();
        setupMenuBar();
        setTitle("Text Editor");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setupUI() {
        textArea = new JTextArea();
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(BG_DARK);
        textArea.setForeground(FG_TEXT);
        textArea.setCaretColor(FG_MUTED);
        textArea.setSelectionColor(new Color(83, 74, 255));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(null);
        add(scroll);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_MENU);
        menuBar.setBorderPainted(false);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(FG_MUTED);

        JMenuItem newFile  = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");

        for (JMenuItem item : new JMenuItem[]{newFile, openFile, saveFile}) {
            item.setBackground(BG_MENU);
            item.setForeground(FG_TEXT);
            fileMenu.add(item);
        }

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // New
        newFile.addActionListener(e -> {
            textArea.setText("");
            currentFile = null;
            setTitle("Text Editor — untitled");
        });

        // Open
        openFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    textArea.read(reader, null);
                    reader.close();
                    currentFile = file;
                    setTitle("Text Editor — " + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error opening file:\n" + ex.getMessage());
                }
            }
        });

        // Save  ← هنا كانت المشكلة الأساسية
        saveFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (currentFile != null) {
                chooser.setSelectedFile(currentFile);
            }
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    FileWriter writer = new FileWriter(file);
                    writer.write(textArea.getText());
                    writer.close();
                    currentFile = file;
                    setTitle("Text Editor — " + file.getName());
                    JOptionPane.showMessageDialog(this, "Saved successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error saving file:\n" + ex.getMessage());
                }
            }
        });
    }

    public void openFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            textArea.read(reader, null);
            reader.close();
            currentFile = file;
            setTitle("Text Editor — " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
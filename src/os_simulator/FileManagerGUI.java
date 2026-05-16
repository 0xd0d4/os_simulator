package os_simulator;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;

public class FileManagerGUI extends JFrame {

    private JList<File> fileList;
    private DefaultListModel<File> model;
    private JLabel pathLabel;

    private java.util.Stack<File> backStack = new java.util.Stack<>();
    private java.util.Stack<File> forwardStack = new java.util.Stack<>();
    private File currentDir;

    public FileManagerGUI() {
        setTitle("📂 File Manager");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(5, 0, 15));

        // ====== Top Bar ======
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(10, 0, 25));
        topBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        pathLabel = new JLabel();
        pathLabel.setForeground(new Color(180, 0, 255));
        pathLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton backBtn = createButton("⬅");
        JButton forwardBtn = createButton("➡");

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBackground(new Color(10, 0, 25));
        navPanel.add(backBtn);
        navPanel.add(forwardBtn);

        topBar.add(navPanel, BorderLayout.WEST);
        topBar.add(pathLabel, BorderLayout.CENTER);

        // ====== File List ======
        model = new DefaultListModel<>();
        fileList = new JList<>(model);

        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                File file = (File) value;
                label.setText((file.isDirectory() ? "📁 " : "📄 ") + file.getName());
                label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

                return label;
            }
        });

        fileList.setBackground(new Color(10, 0, 20));
        fileList.setForeground(new Color(0, 180, 255));
        fileList.setSelectionBackground(new Color(80, 0, 120));

        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 0, 120)));

        // ====== Bottom Buttons ======
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setBackground(new Color(5, 0, 15));

        JButton openBtn = createButton("📂 Open");
        JButton deleteBtn = createButton("🗑 Delete");
        JButton newBtn = createButton("➕ New Folder");

        bottomBar.add(openBtn);
        bottomBar.add(deleteBtn);
        bottomBar.add(newBtn);

        // ====== Actions ======

        // Open
        openBtn.addActionListener(e -> {
            File selected = fileList.getSelectedValue();
            if (selected == null) return;

            if (selected.isDirectory()) {
                backStack.push(currentDir);
                forwardStack.clear();
                loadFiles(selected);
            } else {
                JOptionPane.showMessageDialog(this,
                        "📄 File: " + selected.getName());
            }
        });

        // Delete
        deleteBtn.addActionListener(e -> {
            File selected = fileList.getSelectedValue();
            if (selected == null) return;

            selected.delete();
            loadFiles(currentDir);
        });

        // New Folder
        newBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Folder name:");
            if (name == null || name.isEmpty()) return;

            new File(currentDir, name).mkdir();
            loadFiles(currentDir);
        });

        // Double Click
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openBtn.doClick();
                }
            }
        });

        // Back
        backBtn.addActionListener(e -> {
            if (!backStack.isEmpty()) {
                forwardStack.push(currentDir);
                loadFiles(backStack.pop());
            }
        });

        // Forward
        forwardBtn.addActionListener(e -> {
            if (!forwardStack.isEmpty()) {
                backStack.push(currentDir);
                loadFiles(forwardStack.pop());
            }
        });

        // ====== Add ======
        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // ====== Start ======
        loadFiles(new File(System.getProperty("user.home")));
    }

    // ====== Load Files ======
    private void loadFiles(File dir) {
        if (dir == null) return;

        currentDir = dir;
        model.clear();

        pathLabel.setText("📍 " + dir.getAbsolutePath());

        File[] files = dir.listFiles();

        if (files != null) {
            java.util.Arrays.sort(files, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });

            for (File f : files) {
                model.addElement(f);
            }
        }
    }

    // ====== Button Style ======
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 40, 80));
        btn.setForeground(new Color(0, 180, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 255)));
        return btn;
    }
}
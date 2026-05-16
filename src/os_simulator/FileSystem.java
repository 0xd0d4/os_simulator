package os_simulator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class FileSystem extends JFrame {

    JTable fileTable;
    DefaultTableModel tableModel;
    JLabel pathLabel, statusLabel;
    JTextField searchField;
    String currentPath;
    java.util.Stack<String> history = new java.util.Stack<>();

    public FileSystem(String startPath) {
        this.currentPath = startPath;
        initializeUI();
        runCommand("mkdir -p /root/.trash");
        loadDirectory(currentPath);
    }

    public FileSystem() {
        this("/root");
    }

    private void initializeUI() {
        setTitle("File System - Zero Trace");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        Color bgColor = Color.BLACK;
        Color green   = new Color(0, 255, 0);
        Color cyan    = new Color(0, 255, 255);

        getContentPane().setBackground(bgColor);

        // ====== TOP ======
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 5, 10));

        JButton backBtn = makeBtn("◀ Back", new Color(20, 20, 20), cyan);
        JButton homeBtn = makeBtn("⌂ Home", new Color(20, 20, 20), green);

        pathLabel = new JLabel("  📂 " + currentPath);
        pathLabel.setForeground(Color.YELLOW);
        pathLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        pathLabel.setOpaque(true);
        pathLabel.setBackground(new Color(15, 15, 15));
        pathLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 30));
        searchField.setBackground(new Color(15, 15, 15));
        searchField.setForeground(green);
        searchField.setCaretColor(green);
        searchField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(green),
            "🔍 Search", 0, 0, null, green));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        navPanel.setBackground(bgColor);
        navPanel.add(backBtn);
        navPanel.add(homeBtn);

        topPanel.add(navPanel, BorderLayout.WEST);
        topPanel.add(pathLabel, BorderLayout.CENTER);
        topPanel.add(searchField, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ====== LEFT ======
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(130, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(
            0, 0, 0, 1, new Color(0, 80, 0)));

        JLabel quickTitle = new JLabel("  QUICK ACCESS");
        quickTitle.setForeground(Color.GRAY);
        quickTitle.setFont(new Font("Monospaced", Font.BOLD, 11));
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(quickTitle);
        leftPanel.add(Box.createVerticalStrut(5));

        String[][] quickDirs = {
            {"⌂ Home",   "/root"},
            {"📁 Etc",   "/etc"},
            {"📁 Var",   "/var"},
            {"📁 Tmp",   "/tmp"},
            {"📁 Usr",   "/usr"},
            {"📁 Bin",   "/bin"},
            {"📁 Proc",  "/proc"},
            {"🗑 Trash", "/root/.trash"}
        };

        for (String[] dir : quickDirs) {
            JButton btn = new JButton(dir[0]);
            btn.setBackground(new Color(10, 10, 10));
            btn.setForeground(green);
            btn.setFont(new Font("Monospaced", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            final String path = dir[1];
            btn.addActionListener(e -> navigateTo(path));
            leftPanel.add(btn);
        }
        add(leftPanel, BorderLayout.WEST);

        // ====== CENTER: Table ======
        String[] cols = {"", "Name", "Size", "Type", "Permissions", "Modified"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        fileTable = new JTable(tableModel);
        fileTable.setBackground(bgColor);
        fileTable.setForeground(green);
        fileTable.setGridColor(new Color(0, 50, 0));
        fileTable.setSelectionBackground(new Color(0, 100, 0));
        fileTable.setSelectionForeground(Color.WHITE);
        fileTable.setRowHeight(24);
        fileTable.setShowHorizontalLines(true);
        fileTable.setShowVerticalLines(false);
        fileTable.setFillsViewportHeight(true);

        fileTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        fileTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        JTableHeader header = fileTable.getTableHeader();
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(green);
        header.setFont(new Font("Monospaced", Font.BOLD, 13));

        fileTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = fileTable.getSelectedRow();
                    if (row == -1) return;
                    String type = tableModel.getValueAt(row, 3).toString();
                    String name = tableModel.getValueAt(row, 1).toString();
                    if (type.equals("DIR")) {
                        navigateTo(currentPath.endsWith("/")
                            ? currentPath + name
                            : currentPath + "/" + name);
                    } else {
                        viewFile(name);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.setBackground(bgColor);
        scroll.getViewport().setBackground(bgColor);
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(0, 50, 0), 1));
        add(scroll, BorderLayout.CENTER);

        // ====== BOTTOM ======
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 8, 10));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setBackground(bgColor);

        JButton newFolderBtn = makeBtn("📁 New Folder", new Color(0, 50, 0),   green);
        JButton newFileBtn   = makeBtn("📄 New File",   new Color(0, 30, 60),  cyan);
        JButton delBtn       = makeBtn("🗑 Delete",     new Color(80, 0, 0),   Color.RED);
        JButton emptyBtn     = makeBtn("🧹 Empty Trash",new Color(50, 0, 50),  Color.MAGENTA);
        JButton refreshBtn   = makeBtn("🔄 Refresh",    new Color(20, 20, 20), green);

        // ── New Folder ──
        newFolderBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(
                this, "Folder Name:", "New Folder");
            if (name == null || name.trim().isEmpty()) return;

            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "docker", "exec", "priceless_germain",
                        "bash", "-c",
                        "mkdir -p \"" + currentPath + "/" + name
                            + "\" && echo SUCCESS"
                    );
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                    String output = r.readLine();
                    p.waitFor();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("SUCCESS".equals(output)
                            ? "✅ Created: " + name : "❌ Failed");
                        loadDirectory(currentPath);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        statusLabel.setText("❌ " + ex.getMessage()));
                }
            }).start();
        });

        // ── New File ──
        newFileBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(
                this, "File Name:", "newfile.txt");
            if (name == null || name.trim().isEmpty()) return;

            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "docker", "exec", "priceless_germain",
                        "bash", "-c",
                        "touch \"" + currentPath + "/" + name
                            + "\" && echo SUCCESS"
                    );
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                    String output = r.readLine();
                    p.waitFor();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("SUCCESS".equals(output)
                            ? "✅ Created: " + name : "❌ Failed");
                        loadDirectory(currentPath);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        statusLabel.setText("❌ " + ex.getMessage()));
                }
            }).start();
        });

        // ── Delete ──
        delBtn.addActionListener(e -> deleteAction());

        // ── Empty Trash ──
        emptyBtn.addActionListener(e -> emptyTrashAction());

        // ── Refresh ──
        refreshBtn.addActionListener(e -> loadDirectory(currentPath));

        // ── Back ──
        backBtn.addActionListener(e -> {
            if (!history.isEmpty()) {
                currentPath = history.pop();
                loadDirectory(currentPath);
            }
        });

        // ── Home ──
        homeBtn.addActionListener(e -> navigateTo("/root"));

        // ── Search ──
        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) searchFiles(query);
        });

        btnPanel.add(newFolderBtn);
        btnPanel.add(newFileBtn);
        btnPanel.add(delBtn);
        btnPanel.add(emptyBtn);
        btnPanel.add(refreshBtn);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(fg));
        return btn;
    }

    public void navigateTo(String path) {
        history.push(currentPath);
        currentPath = path;
        loadDirectory(path);
    }

    private void deleteAction() {
        int row = fileTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "اختار ملف الأول!");
            return;
        }
        String name = tableModel.getValueAt(row, 1).toString();
        String path = currentPath.endsWith("/")
            ? currentPath + name
            : currentPath + "/" + name;

        int confirm = JOptionPane.showConfirmDialog(
            this, "Move \"" + name + "\" to Trash?",
            "Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                if (runCommand("mv \"" + path + "\" /root/.trash/"))
                    SwingUtilities.invokeLater(() ->
                        loadDirectory(currentPath));
            }).start();
        }
    }

    private void emptyTrashAction() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "Clear Trash permanently?",
            "Empty Trash", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                if (runCommand("rm -rf /root/.trash/*"))
                    SwingUtilities.invokeLater(() ->
                        loadDirectory(currentPath));
            }).start();
        }
    }

    private void loadDirectory(String path) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "ls -la --time-style=long-iso \"" + path + "\""
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                String line;
                boolean first = true;

                while ((line = r.readLine()) != null) {
                    if (first || line.startsWith("total")) {
                        first = false;
                        continue;
                    }
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 8) continue;

                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 7; i < parts.length; i++)
                        nameBuilder.append(parts[i])
                            .append(i == parts.length - 1 ? "" : " ");

                    String name = nameBuilder.toString();
                    if (name.equals(".") || name.equals("..")) continue;

                    boolean isDir = parts[0].startsWith("d");
                    rows.add(new Object[]{
                        isDir ? "📁" : "📄",
                        name,
                        isDir ? "--" : parts[4],
                        isDir ? "DIR" : "FILE",
                        parts[0],
                        parts[5] + " " + parts[6]
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                    pathLabel.setText("  📂 " + path);
                    statusLabel.setText(rows.size() + " items");
                    currentPath = path;
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }

    private void viewFile(String name) {
        new Thread(() -> {
            try {
                String path = currentPath.endsWith("/")
                    ? currentPath + name
                    : currentPath + "/" + name;

                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "cat \"" + path + "\""
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null)
                    sb.append(line).append("\n");

                SwingUtilities.invokeLater(() -> {
                    JTextArea ta = new JTextArea(sb.toString());
                    ta.setEditable(false);
                    ta.setBackground(Color.BLACK);
                    ta.setForeground(new Color(0, 255, 0));
                    ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
                    ta.setCaretColor(new Color(0, 255, 0));

                    JScrollPane sp = new JScrollPane(ta);

                    JDialog d = new JDialog(this, "📄 " + name, true);
                    d.setSize(600, 400);
                    d.setLocationRelativeTo(this);
                    d.getContentPane().setBackground(Color.BLACK);
                    d.add(sp);
                    d.setVisible(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("❌ Cannot read: " + name));
            }
        }).start();
    }

    private void searchFiles(String query) {
        statusLabel.setText("Searching...");
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "find / -name \"*" + query
                        + "*\" 2>/dev/null | head -50"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                String line;
                while ((line = r.readLine()) != null) {
                    String name = line.substring(
                        line.lastIndexOf("/") + 1);
                    rows.add(new Object[]{
                        "📄", name, "--", "FILE", "--", line});
                }

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                    statusLabel.setText(
                        "Found: " + rows.size() + " results");
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Search error"));
            }
        }).start();
    }

    private boolean runCommand(String cmd) {
        try {
            return new ProcessBuilder(
                "docker", "exec", "priceless_germain",
                "bash", "-c", cmd)
                .start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
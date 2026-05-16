/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os_simulator;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
public class IODevicesManager extends MainDesktop {

    JTable deviceTable;
    DefaultTableModel tableModel;
    JLabel statusLabel;
    JTabbedPane tabs;
    Timer refreshTimer;

    public IODevicesManager() {
        
        setTitle("I/O Devices - Zero Trace");
        setSize(850, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        Color bgColor = Color.BLACK;
        Color green   = new Color(0, 255, 0);
        Color cyan    = new Color(0, 255, 255);
        Color magenta = new Color(255, 0, 255);
        Color yellow  = Color.YELLOW;

        getContentPane().setBackground(bgColor);

        // ====== Header ======
        JLabel title = new JLabel("⚡ I/O DEVICES MANAGER", SwingConstants.CENTER);
        title.setForeground(green);
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        title.setBackground(bgColor);
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // ====== Tabs ======
        tabs = new JTabbedPane();
        tabs.setBackground(bgColor);
        tabs.setForeground(green);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 13));

        // Tab 1: Disks
        tabs.addTab("💾 Disks", makeDisksPanel(bgColor, green, yellow));

        // Tab 2: Storage Usage
        tabs.addTab("📊 Storage", makeStoragePanel(bgColor, green, cyan));

        // Tab 3: Network
        tabs.addTab("🌐 Network", makeNetworkPanel(bgColor, green, cyan));

        // Tab 4: CPU Info
        tabs.addTab("⚙ CPU", makeCPUPanel(bgColor, green, magenta));

        add(tabs, BorderLayout.CENTER);

        // ====== Bottom ======
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 8, 10));

        statusLabel = new JLabel("Loading...");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton refreshBtn = makeBtn("🔄 Refresh All", new Color(0, 40, 0), green);
        refreshBtn.addActionListener(e -> loadAllData());

        JLabel autoLabel = new JLabel("Auto-refresh: 5s  ");
        autoLabel.setForeground(Color.GRAY);
        autoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setBackground(bgColor);
        btnPanel.add(autoLabel);
        btnPanel.add(refreshBtn);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ====== Start ======
        loadAllData();
        refreshTimer = new Timer(5000, e -> loadAllData());
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                refreshTimer.stop();
            }
        });
    }

    // ==================== DISKS TAB ====================
    JTextArea disksArea;

    private JPanel makeDisksPanel(Color bg, Color green, Color yellow) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Device", "Type", "Size", "Mount Point", "Read Only"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = makeTable(model, bg, green);
        table.setName("disksTable");

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(bg);
        scroll.setBackground(bg);

        disksArea = makeTextArea(bg, yellow);
        JScrollPane detailScroll = new JScrollPane(disksArea);
        detailScroll.setBackground(bg);
        detailScroll.getViewport().setBackground(bg);
        detailScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(yellow),
            "lsblk Details",
            0, 0,
            new Font("Monospaced", Font.PLAIN, 11), yellow));

        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, scroll, detailScroll);
        split.setDividerLocation(200);
        split.setBackground(bg);

        panel.add(split, BorderLayout.CENTER);
        panel.putClientProperty("tableModel", model);
        return panel;
    }

    // ==================== STORAGE TAB ====================
    JPanel storageCardsPanel;

    private JPanel makeStoragePanel(Color bg, Color green, Color cyan) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("DISK USAGE (df -h)", SwingConstants.LEFT);
        lbl.setForeground(cyan);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 14));

        storageCardsPanel = new JPanel();
        storageCardsPanel.setLayout(new BoxLayout(
            storageCardsPanel, BoxLayout.Y_AXIS));
        storageCardsPanel.setBackground(bg);

        JScrollPane scroll = new JScrollPane(storageCardsPanel);
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================== NETWORK TAB ====================
    JTextArea networkArea;

    private JPanel makeNetworkPanel(Color bg, Color green, Color cyan) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Interface", "IP Address", "RX Bytes", "TX Bytes", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = makeTable(model, bg, green);
        table.setName("networkTable");

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(bg);
        scroll.setBackground(bg);

        networkArea = makeTextArea(bg, cyan);
        JScrollPane detailScroll = new JScrollPane(networkArea);
        detailScroll.setBackground(bg);
        detailScroll.getViewport().setBackground(bg);
        detailScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(cyan),
            "Network Details (ip addr)",
            0, 0,
            new Font("Monospaced", Font.PLAIN, 11), cyan));

        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, scroll, detailScroll);
        split.setDividerLocation(180);
        split.setBackground(bg);

        panel.add(split, BorderLayout.CENTER);
        panel.putClientProperty("networkModel", model);
        return panel;
    }

    // ==================== CPU TAB ====================
    JTextArea cpuArea;

    private JPanel makeCPUPanel(Color bg, Color green, Color magenta) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("CPU INFORMATION", SwingConstants.LEFT);
        lbl.setForeground(magenta);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 14));

        cpuArea = makeTextArea(bg, magenta);

        JScrollPane scroll = new JScrollPane(cpuArea);
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================== LOAD DATA ====================
    private void loadAllData() {
        statusLabel.setText("Refreshing...");
        loadDisks();
        loadStorage();
        loadNetwork();
        loadCPU();
    }

    private void loadDisks() {
        new Thread(() -> {
            try {
                // lsblk
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "lsblk -o NAME,TYPE,SIZE,MOUNTPOINT,RO"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                StringBuilder raw = new StringBuilder();
                String line;
                boolean first = true;

                while ((line = r.readLine()) != null) {
                    raw.append(line).append("\n");
                    if (first) { first = false; continue; }
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 4) {
                        String name  = parts[0];
                        String type  = parts[1];
                        String size  = parts[2];
                        String mount = parts.length > 3 ? parts[3] : "--";
                        String ro    = parts.length > 4 ? parts[4] : "--";

                        String icon = type.equals("disk") ? "💾"
                            : type.equals("part") ? "📀" : "📁";

                        rows.add(new Object[]{icon + " " + name,
                            type, size, mount, ro});
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    JPanel disksPanel = (JPanel) tabs.getComponentAt(0);
                    JSplitPane split =
                        (JSplitPane) disksPanel.getComponent(0);
                    JScrollPane tableScroll =
                        (JScrollPane) split.getTopComponent();
                    JTable table =
                        (JTable) tableScroll.getViewport().getView();
                    DefaultTableModel model =
                        (DefaultTableModel) table.getModel();
                    model.setRowCount(0);
                    for (Object[] row : rows) model.addRow(row);
                    disksArea.setText(raw.toString());
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    disksArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void loadStorage() {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "df -h"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                java.util.List<String[]> entries =
                    new java.util.ArrayList<>();
                String line;
                boolean first = true;

                while ((line = r.readLine()) != null) {
                    if (first) { first = false; continue; }
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 6) {
                        entries.add(parts);
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    storageCardsPanel.removeAll();
                    for (String[] parts : entries) {
                        String fs      = parts[0];
                        String size    = parts[1];
                        String used    = parts[2];
                        String avail   = parts[3];
                        String usePerc = parts[4].replace("%", "");
                        String mount   = parts[5];

                        JPanel card = new JPanel(new BorderLayout(5, 5));
                        card.setBackground(new Color(10, 10, 10));
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                new Color(0, 80, 0), 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                        card.setMaximumSize(
                            new Dimension(Integer.MAX_VALUE, 80));
                        card.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JLabel info = new JLabel(
                            "💾 " + fs + "  →  " + mount +
                            "   [" + used + " / " + size + "]");
                        info.setForeground(new Color(0, 255, 0));
                        info.setFont(new Font("Monospaced", Font.BOLD, 12));

                        int perc = 0;
                        try { perc = Integer.parseInt(usePerc); }
                        catch (Exception ignored) {}

                        JProgressBar bar = new JProgressBar(0, 100);
                        bar.setValue(perc);
                        bar.setString(usePerc + "%  (Free: " + avail + ")");
                        bar.setStringPainted(true);
                        bar.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        bar.setBackground(new Color(20, 20, 20));
                        bar.setForeground(perc > 80 ? Color.RED
                            : perc > 60 ? Color.YELLOW
                            : new Color(0, 200, 0));

                        card.add(info, BorderLayout.NORTH);
                        card.add(bar, BorderLayout.CENTER);

                        storageCardsPanel.add(card);
                        storageCardsPanel.add(Box.createVerticalStrut(8));
                    }
                    storageCardsPanel.revalidate();
                    storageCardsPanel.repaint();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadNetwork() {
        new Thread(() -> {
            try {
                // ip addr
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "ip addr show"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                StringBuilder raw = new StringBuilder();
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                String line;
                String currentIface = "";
                String currentIP = "--";

                while ((line = r.readLine()) != null) {
                    raw.append(line).append("\n");
                    if (line.matches("^\\d+:.*")) {
                        if (!currentIface.isEmpty()) {
                            rows.add(new Object[]{
                                "🌐 " + currentIface,
                                currentIP, "--", "--",
                                "UP"
                            });
                        }
                        currentIface = line.split(":")[1].trim();
                        currentIP = "--";
                    } else if (line.trim().startsWith("inet ")) {
                        currentIP = line.trim().split("\\s+")[1];
                    }
                }
                if (!currentIface.isEmpty()) {
                    rows.add(new Object[]{
                        "🌐 " + currentIface,
                        currentIP, "--", "--", "UP"
                    });
                }

                // /proc/net/dev للـ RX/TX
                ProcessBuilder pb2 = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "cat /proc/net/dev"
                );
                Process p2 = pb2.start();
                BufferedReader r2 = new BufferedReader(
                    new InputStreamReader(p2.getInputStream()));

                java.util.Map<String, String[]> netStats =
                    new java.util.HashMap<>();
                String line2;
                boolean skip = true;
                while ((line2 = r2.readLine()) != null) {
                    if (skip) { skip = false; continue; }
                    if (line2.contains("|")) continue;
                    String[] parts = line2.trim().split("\\s+");
                    if (parts.length > 9) {
                        String iface = parts[0].replace(":", "");
                        netStats.put(iface, new String[]{
                            formatBytes(parts[1]),
                            formatBytes(parts[9])
                        });
                    }
                }

                // دمج الـ RX/TX مع الـ rows
                for (int i = 0; i < rows.size(); i++) {
                    String iface = rows.get(i)[0].toString()
                        .replace("🌐 ", "").trim();
                    if (netStats.containsKey(iface)) {
                        rows.get(i)[2] = netStats.get(iface)[0];
                        rows.get(i)[3] = netStats.get(iface)[1];
                    }
                }

                final String rawStr = raw.toString();
                final java.util.List<Object[]> finalRows = rows;

                SwingUtilities.invokeLater(() -> {
                    JPanel netPanel = (JPanel) tabs.getComponentAt(2);
                    JSplitPane split =
                        (JSplitPane) netPanel.getComponent(0);
                    JScrollPane tableScroll =
                        (JScrollPane) split.getTopComponent();
                    JTable table =
                        (JTable) tableScroll.getViewport().getView();
                    DefaultTableModel model =
                        (DefaultTableModel) table.getModel();
                    model.setRowCount(0);
                    for (Object[] row : finalRows) model.addRow(row);
                    networkArea.setText(rawStr);
                    statusLabel.setText("Updated: " +
                        new java.util.Date().toString()
                        .substring(11, 19));
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    networkArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void loadCPU() {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "cat /proc/cpuinfo | grep -E " +
                    "'model name|cpu MHz|cache size|cpu cores|vendor_id'" +
                    " | sort -u"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                StringBuilder sb = new StringBuilder();
                sb.append("══════════════════════════════\n");
                sb.append("       CPU INFORMATION\n");
                sb.append("══════════════════════════════\n\n");

                String line;
                while ((line = r.readLine()) != null) {
                    sb.append("  ").append(line).append("\n");
                }

                // عدد الـ cores
                ProcessBuilder pb2 = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "nproc"
                );
                Process p2 = pb2.start();
                BufferedReader r2 = new BufferedReader(
                    new InputStreamReader(p2.getInputStream()));
                String cores = r2.readLine();
                sb.append("\n  CPU Cores Available: ")
                  .append(cores).append("\n");

                // uptime
                ProcessBuilder pb3 = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "uptime"
                );
                Process p3 = pb3.start();
                BufferedReader r3 = new BufferedReader(
                    new InputStreamReader(p3.getInputStream()));
                String uptime = r3.readLine();
                sb.append("\n  System Uptime:\n  ")
                  .append(uptime).append("\n");

                final String result = sb.toString();
                SwingUtilities.invokeLater(() -> cpuArea.setText(result));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    cpuArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== HELPERS ====================
    private JTable makeTable(DefaultTableModel model, Color bg, Color fg) {
        JTable table = new JTable(model);
        table.setBackground(bg);
        table.setForeground(fg);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setGridColor(new Color(0, 60, 0));
        table.setSelectionBackground(new Color(0, 100, 0));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(24);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(fg);
        header.setFont(new Font("Monospaced", Font.BOLD, 13));
        return table;
    }

    private JTextArea makeTextArea(Color bg, Color fg) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(bg);
        area.setForeground(fg);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setCaretColor(fg);
        return area;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(fg));
        return btn;
    }

    private String formatBytes(String bytes) {
        try {
            long b = Long.parseLong(bytes);
            if (b < 1024) return b + " B";
            if (b < 1048576) return (b / 1024) + " KB";
            if (b < 1073741824) return (b / 1048576) + " MB";
            return (b / 1073741824) + " GB";
        } catch (Exception e) {
            return bytes;
        }
    }
}


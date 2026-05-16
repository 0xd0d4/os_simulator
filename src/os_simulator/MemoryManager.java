package os_simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MemoryManager extends MainDesktop {

    JProgressBar ramBar, swapBar;
    JLabel ramLabel, swapLabel, totalLabel, usedLabel, freeLabel, cachedLabel;
    JTable memTable;
    DefaultTableModel tableModel;
    Timer refreshTimer;

    public MemoryManager() {
        setTitle("Memory Manager - Zero Trace");
        setSize(700, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        Color bgColor = Color.BLACK;
        Color green = new Color(0, 255, 0);
        Color cyan = new Color(0, 255, 255);
        Color magenta = new Color(255, 0, 255);

        getContentPane().setBackground(bgColor);

        // ====== Header ======
        JLabel title = new JLabel("⚡ MEMORY MANAGER", SwingConstants.CENTER);
        title.setForeground(green);
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        title.setBackground(bgColor);
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ====== Center Panel ======
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(bgColor);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- RAM Bar ---
        JLabel ramTitle = new JLabel("RAM USAGE");
        ramTitle.setForeground(cyan);
        ramTitle.setFont(new Font("Monospaced", Font.BOLD, 14));
        ramTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        ramBar = new JProgressBar(0, 100);
        ramBar.setStringPainted(true);
        ramBar.setForeground(green);
        ramBar.setBackground(new Color(20, 20, 20));
        ramBar.setBorder(BorderFactory.createLineBorder(green));
        ramBar.setFont(new Font("Monospaced", Font.BOLD, 13));
        ramBar.setPreferredSize(new Dimension(600, 30));
        ramBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ramBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        ramLabel = new JLabel("Used: -- / Total: --");
        ramLabel.setForeground(green);
        ramLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- SWAP Bar ---
        JLabel swapTitle = new JLabel("SWAP USAGE");
        swapTitle.setForeground(magenta);
        swapTitle.setFont(new Font("Monospaced", Font.BOLD, 14));
        swapTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        swapBar = new JProgressBar(0, 100);
        swapBar.setStringPainted(true);
        swapBar.setForeground(magenta);
        swapBar.setBackground(new Color(20, 20, 20));
        swapBar.setBorder(BorderFactory.createLineBorder(magenta));
        swapBar.setFont(new Font("Monospaced", Font.BOLD, 13));
        swapBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        swapBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        swapLabel = new JLabel("Used: -- / Total: --");
        swapLabel.setForeground(magenta);
        swapLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        swapLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Stats Cards ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBackground(bgColor);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalLabel  = makeCard("TOTAL",  "--", new Color(0, 150, 255));
        usedLabel   = makeCard("USED",   "--", Color.RED);
        freeLabel   = makeCard("FREE",   "--", green);
        cachedLabel = makeCard("CACHED", "--", Color.YELLOW);

        statsPanel.add(totalLabel);
        statsPanel.add(usedLabel);
        statsPanel.add(freeLabel);
        statsPanel.add(cachedLabel);

        // --- Table: /proc/meminfo ---
        String[] cols = {"Memory Type", "Size"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        memTable = new JTable(tableModel);
        memTable.setBackground(bgColor);
        memTable.setForeground(green);
        memTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        memTable.setGridColor(new Color(0, 60, 0));
        memTable.setSelectionBackground(new Color(0, 100, 0));
        memTable.setRowHeight(20);

        JTableHeader header = memTable.getTableHeader();
        header.setBackground(new Color(20, 20, 20));
        header.setForeground(green);
        header.setFont(new Font("Monospaced", Font.BOLD, 13));

        JScrollPane tableScroll = new JScrollPane(memTable);
        tableScroll.setBackground(bgColor);
        tableScroll.getViewport().setBackground(bgColor);
        tableScroll.setBorder(BorderFactory.createLineBorder(green));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        // أضف كل حاجة للـ centerPanel
        centerPanel.add(ramTitle);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(ramBar);
        centerPanel.add(Box.createVerticalStrut(3));
        centerPanel.add(ramLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(swapTitle);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(swapBar);
        centerPanel.add(Box.createVerticalStrut(3));
        centerPanel.add(swapLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(statsPanel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(tableScroll);

        add(centerPanel, BorderLayout.CENTER);

        // ====== Bottom Buttons ======
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(bgColor);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(0, 40, 0));
        refreshBtn.setForeground(green);
        refreshBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createLineBorder(green));
        refreshBtn.addActionListener(e -> loadMemoryData());

        JLabel autoLabel = new JLabel("Auto-refresh: 3s  ");
        autoLabel.setForeground(Color.GRAY);
        autoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        btnPanel.add(autoLabel);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ====== Start ======
        loadMemoryData();
        refreshTimer = new Timer(3000, e -> loadMemoryData());
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                refreshTimer.stop();
            }
        });
    }

    // Card صغيرة للـ stats
    private JLabel makeCard(String title, String value, Color color) {
        JLabel lbl = new JLabel(
            "<html><center><b style='color:gray'>" + title +
            "</b><br><span style='font-size:16px'>" + value +
            "</span></center></html>",
            SwingConstants.CENTER
        );
        lbl.setForeground(color);
        lbl.setBackground(new Color(15, 15, 15));
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(color));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return lbl;
    }

    private void updateCard(JLabel lbl, String title, String value, Color color) {
        lbl.setText(
            "<html><center><b style='color:gray'>" + title +
            "</b><br><span style='font-size:16px'>" + value +
            "</span></center></html>"
        );
        lbl.setForeground(color);
    }

    private void loadMemoryData() {
        new Thread(() -> {
            try {
                // جيب بيانات الـ free
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c", "free -m"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                String line;
                long ramTotal = 0, ramUsed = 0, ramFree = 0, ramCached = 0;
                long swapTotal = 0, swapUsed = 0;

                while ((line = r.readLine()) != null) {
                    if (line.startsWith("Mem:")) {
                        String[] parts = line.trim().split("\\s+");
                        ramTotal  = Long.parseLong(parts[1]);
                        ramUsed   = Long.parseLong(parts[2]);
                        ramFree   = Long.parseLong(parts[3]);
                        if (parts.length > 5)
                            ramCached = Long.parseLong(parts[5]);
                    } else if (line.startsWith("Swap:")) {
                        String[] parts = line.trim().split("\\s+");
                        swapTotal = Long.parseLong(parts[1]);
                        swapUsed  = Long.parseLong(parts[2]);
                    }
                }

                // جيب تفاصيل /proc/meminfo
                ProcessBuilder pb2 = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "cat /proc/meminfo | head -20"
                );
                Process p2 = pb2.start();
                BufferedReader r2 = new BufferedReader(
                    new InputStreamReader(p2.getInputStream()));

                java.util.List<String[]> memInfo = new java.util.ArrayList<>();
                String line2;
                while ((line2 = r2.readLine()) != null) {
                    String[] parts = line2.split(":");
                    if (parts.length == 2) {
                        memInfo.add(new String[]{
                            parts[0].trim(),
                            parts[1].trim()
                        });
                    }
                }

                // حدّث الـ UI
                final long fRamTotal = ramTotal, fRamUsed = ramUsed;
                final long fRamFree = ramFree, fRamCached = ramCached;
                final long fSwapTotal = swapTotal, fSwapUsed = swapUsed;

                SwingUtilities.invokeLater(() -> {
                    // RAM Bar
                    int ramPercent = fRamTotal > 0
                        ? (int)((fRamUsed * 100) / fRamTotal) : 0;
                    ramBar.setValue(ramPercent);
                    ramBar.setString(ramPercent + "%");
                    ramLabel.setText("Used: " + fRamUsed + " MB  /  Total: "
                        + fRamTotal + " MB");

                    // لون الـ bar حسب النسبة
                    if (ramPercent > 80)
                        ramBar.setForeground(Color.RED);
                    else if (ramPercent > 60)
                        ramBar.setForeground(Color.YELLOW);
                    else
                        ramBar.setForeground(new Color(0, 255, 0));

                    // SWAP Bar
                    int swapPercent = fSwapTotal > 0
                        ? (int)((fSwapUsed * 100) / fSwapTotal) : 0;
                    swapBar.setValue(swapPercent);
                    swapBar.setString(swapPercent + "%");
                    swapLabel.setText("Used: " + fSwapUsed + " MB  /  Total: "
                        + fSwapTotal + " MB");

                    // Cards
                    updateCard(totalLabel,  "TOTAL",
                        fRamTotal + " MB",  new Color(0, 150, 255));
                    updateCard(usedLabel,   "USED",
                        fRamUsed + " MB",   Color.RED);
                    updateCard(freeLabel,   "FREE",
                        fRamFree + " MB",   new Color(0, 255, 0));
                    updateCard(cachedLabel, "CACHED",
                        fRamCached + " MB", Color.YELLOW);

                    // Table
                    tableModel.setRowCount(0);
                    for (String[] row : memInfo) {
                        tableModel.addRow(row);
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
}
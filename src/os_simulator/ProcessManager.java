
package os_simulator;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
public class ProcessManager extends MainDesktop{
    
    JTable processTable;
    DefaultTableModel tableModel;
    JLabel cpuLabel, memLabel;
    Timer refreshTimer;
    Process process;
    BufferedWriter writer;
    BufferedReader reader;

    public ProcessManager() {
        
        setTitle("Process Manager - Zero Trace");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ====== الألوان ======
        Color bgColor = Color.BLACK;
        Color fgColor = new Color(0, 255, 0);
        Color tableHeader = new Color(20, 20, 20);

        // ====== Header ======
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(bgColor);

        JLabel title = new JLabel("⚙ PROCESS MANAGER");
        title.setForeground(fgColor);
        title.setFont(new Font("Monospaced", Font.BOLD, 16));

        cpuLabel = new JLabel("  CPU: --");
        cpuLabel.setForeground(Color.CYAN);
        cpuLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));

        memLabel = new JLabel("  MEM: --");
        memLabel.setForeground(Color.MAGENTA);
        memLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));

        headerPanel.add(title);
        headerPanel.add(cpuLabel);
        headerPanel.add(memLabel);
        add(headerPanel, BorderLayout.NORTH);

        // ====== Table ======
        String[] columns = {"PID", "USER", "CPU%", "MEM%", "STATUS", "COMMAND"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        processTable = new JTable(tableModel);
        processTable.setBackground(bgColor);
        processTable.setForeground(fgColor);
        processTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        processTable.setGridColor(new Color(0, 80, 0));
        processTable.setSelectionBackground(new Color(0, 100, 0));
        processTable.setSelectionForeground(Color.WHITE);
        processTable.setRowHeight(22);

        // Header style
        JTableHeader header = processTable.getTableHeader();
        header.setBackground(tableHeader);
        header.setForeground(fgColor);
        header.setFont(new Font("Monospaced", Font.BOLD, 13));

        // Column widths
        processTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        processTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        processTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        processTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        processTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        processTable.getColumnModel().getColumn(5).setPreferredWidth(350);

        JScrollPane scroll = new JScrollPane(processTable);
        scroll.setBackground(bgColor);
        scroll.getViewport().setBackground(bgColor);
        add(scroll, BorderLayout.CENTER);

        // ====== Buttons ======
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(bgColor);

        JButton killBtn = new JButton("⛔ Kill Process");
        killBtn.setBackground(new Color(80, 0, 0));
        killBtn.setForeground(Color.RED);
        killBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        killBtn.setFocusPainted(false);
        killBtn.setBorder(BorderFactory.createLineBorder(Color.RED));

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(0, 40, 0));
        refreshBtn.setForeground(fgColor);
        refreshBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createLineBorder(fgColor));

        // Kill button action
        killBtn.addActionListener(e -> {
            int row = processTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                    "اختار process الأول!",
                    "تحذير",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String pid = tableModel.getValueAt(row, 0).toString();
            String cmd = tableModel.getValueAt(row, 5).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                "هتوقف: " + cmd + " (PID: " + pid + ")?",
                "تأكيد",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                runDockerCommand("kill -9 " + pid);
                loadProcesses();
            }
        });

        // Refresh button action
        refreshBtn.addActionListener(e -> loadProcesses());

        btnPanel.add(killBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ====== Start ======
        connectToDocker();
        loadProcesses();

        // Auto refresh كل 3 ثواني
        refreshTimer = new Timer(3000, e -> loadProcesses());
        refreshTimer.start();

        // لما تقفل الشاشة يوقف الـ timer
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                refreshTimer.stop();
            }
        });
    }

    private void connectToDocker() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "-i", "priceless_germain", "bash"
            );
            process = pb.start();
            writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "فشل الاتصال بـ Docker!\n" + e.getMessage(),
                "خطأ",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProcesses() {
        new Thread(() -> {
            try {
                // جيب العمليات
                ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "ps", "aux", "--no-headers"
                );
                Process p = pb.start();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

                // جيب الـ CPU و Memory الكلية
                ProcessBuilder pbTop = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "top -bn1 | grep '%Cpu' | awk '{print $2}'"
                );
                Process pTop = pbTop.start();
                BufferedReader rTop = new BufferedReader(
                    new InputStreamReader(pTop.getInputStream()));
                String cpuLine = rTop.readLine();

                ProcessBuilder pbMem = new ProcessBuilder(
                    "docker", "exec", "priceless_germain",
                    "bash", "-c",
                    "free | grep Mem | awk '{printf \"%.1f\", $3/$2*100}'"
                );
                Process pMem = pbMem.start();
                BufferedReader rMem = new BufferedReader(
                    new InputStreamReader(pMem.getInputStream()));
                String memLine = rMem.readLine();

                // حدّث الـ labels
                SwingUtilities.invokeLater(() -> {
                    cpuLabel.setText("  CPU: " +
                        (cpuLine != null ? cpuLine + "%" : "--"));
                    memLabel.setText("  MEM: " +
                        (memLine != null ? memLine + "%" : "--"));
                });

                // امسح الجدول القديم
                SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));

                String line;
                while ((line = r.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+", 11);
                    if (parts.length >= 11) {
                        String pid  = parts[1];
                        String user = parts[0];
                        String cpu  = parts[2];
                        String mem  = parts[3];
                        String stat = parts[7];
                        String cmd  = parts[10];

                        // لون حسب الـ status
                        SwingUtilities.invokeLater(() ->
                            tableModel.addRow(new Object[]{
                                pid, user, cpu, mem, stat, cmd
                            })
                        );
                    }
                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
                        "خطأ",
                        JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void runDockerCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "priceless_germain",
                "bash", "-c", command
            );
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    


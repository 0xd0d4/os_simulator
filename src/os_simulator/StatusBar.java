
package os_simulator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class StatusBar extends JPanel{ 
    
   
    // === حالة كل أيقونة ===
    private boolean bellOn = true;
    private boolean wifiOn = true;
    private boolean volMuted = false;
    private int batLevel = 2; // 0=10%, 1=30%, 2=75%, 3=100%

    private JLabel toastLabel;
    private Timer toastTimer;

    // ألوان
    private final Color BG = new Color(26, 26, 26);
    private final Color ICON_COLOR = Color.WHITE;
    private final Color RED = new Color(255, 59, 48);
    private final Color ORANGE = new Color(255, 149, 0);
    private final Color GREEN = new Color(48, 209, 88);

    // أزرار الأيقونات
    private IconButton bellBtn, wifiBtn, volBtn, batBtn;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setPreferredSize(new Dimension(320, 60));

        // شريط الأيقونات
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 8));
        iconPanel.setBackground(BG);
        iconPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 20)));

        bellBtn = new IconButton("bell");
        wifiBtn = new IconButton("wifi");
        volBtn  = new IconButton("vol");
        batBtn  = new IconButton("bat");

        bellBtn.addActionListener(e -> toggleBell());
        wifiBtn.addActionListener(e -> toggleWifi());
        volBtn.addActionListener(e -> toggleVolume());
        batBtn.addActionListener(e -> cycleBattery());

        iconPanel.add(bellBtn);
        iconPanel.add(wifiBtn);
        iconPanel.add(volBtn);
        iconPanel.add(batBtn);

        add(iconPanel, BorderLayout.NORTH);

        // Toast
        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        toastLabel.setOpaque(true);
        toastLabel.setBackground(new Color(40, 40, 40));
        toastLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        toastLabel.setVisible(false);

        JPanel toastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toastPanel.setBackground(BG);
        toastPanel.add(toastLabel);
        add(toastPanel, BorderLayout.CENTER);

        toastTimer = new Timer(1800, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        updateIcons();
    }

    // ===== دوال التحكم =====

    private void toggleBell() {
        bellOn = !bellOn;
        updateIcons();
        showToast(bellOn ? "🔔 الإشعارات شغالة" : "🔕 الإشعارات متوقفة");
    }

    private void toggleWifi() {
        wifiOn = !wifiOn;
        updateIcons();
        showToast(wifiOn ? "📶 واي فاي متصل" : "📵 واي فاي مقطوع");
    }

    private void toggleVolume() {
        volMuted = !volMuted;
        updateIcons();
        showToast(volMuted ? "🔇 الصوت متوقف" : "🔊 الصوت شغال");
    }

    private void cycleBattery() {
        batLevel = (batLevel + 1) % 4;
        updateIcons();
        String[] labels = {"🔴 ١٠٪", "🟠 ٣٠٪", "🟢 ٧٥٪", "🟢 ١٠٠٪"};
        showToast("🔋 البطارية " + labels[batLevel]);
    }

    private void updateIcons() {
        bellBtn.setState(bellOn, bellOn ? ICON_COLOR : RED);
        wifiBtn.setState(wifiOn, wifiOn ? ICON_COLOR : RED);
        volBtn.setState(!volMuted, volMuted ? RED : ICON_COLOR);
        batBtn.setBatLevel(batLevel);
        repaint();
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        toastLabel.setVisible(true);
        toastTimer.restart();
    }

    // ===== كلاس الأيقونة =====

    class IconButton extends JButton {
        private String type;
        private boolean active = true;
        private Color iconColor = Color.WHITE;
        private int batLvl = 2;

        IconButton(String type) {
            this.type = type;
            setPreferredSize(new Dimension(36, 36));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(new Color(255,255,255,20)); repaint(); }
                public void mouseExited(MouseEvent e)  { setBackground(null); repaint(); }
            });
        }

        void setState(boolean on, Color color) {
            this.active = on;
            this.iconColor = color;
            repaint();
        }

        void setBatLevel(int lvl) {
            this.batLvl = lvl;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // خلفية hover
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
            }

            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            switch (type) {
                case "bell":  drawBell(g2, cx, cy);  break;
                case "wifi":  drawWifi(g2, cx, cy);  break;
                case "vol":   drawVol(g2, cx, cy);   break;
                case "bat":   drawBat(g2, cx, cy);   break;
            }

            // خط أحمر لما يكون inactive
            if (!active && !type.equals("bat")) {
                g2.setColor(RED);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 8, cy - 8, cx + 8, cy + 8);
            }

            g2.dispose();
        }

        private void drawBell(Graphics2D g2, int cx, int cy) {
            // جسم الجرس
            int bx = cx - 7, by = cy - 8;
            g2.drawArc(bx, by, 14, 12, 0, 180);
            g2.drawLine(bx, by + 6, bx, cy + 4);
            g2.drawLine(bx + 14, by + 6, bx + 14, cy + 4);
            g2.drawLine(bx - 2, cy + 4, bx + 16, cy + 4);
            // أسفل الجرس
            g2.drawArc(cx - 3, cy + 4, 6, 4, 180, 180);
            // نقطة حمراء لو bellOn
            if (active) {
                g2.setColor(RED);
                g2.fillOval(cx + 4, cy - 10, 5, 5);
                g2.setColor(iconColor);
            }
        }

        private void drawWifi(Graphics2D g2, int cx, int cy) {
            // 3 أقواس الواي فاي
            int[] radii = {10, 7, 4};
            for (int r : radii) {
                g2.drawArc(cx - r, cy - r + 2, r * 2, r * 2, 30, 120);
            }
            // النقطة
            g2.fillOval(cx - 2, cy + 5, 4, 4);
        }

        private void drawVol(Graphics2D g2, int cx, int cy) {
            // مثلث السماعة
            int[] xs = {cx - 7, cx - 2, cx - 2};
            int[] ys = {cy, cy - 5, cy + 5};
            g2.fillPolygon(xs, ys, 3);
            // مستطيل السماعة
            g2.fillRect(cx - 9, cy - 3, 3, 6);
            // موجات الصوت لو مش muted
            if (!volMuted) {
                g2.drawArc(cx - 1, cy - 5, 8, 10, -50, 100);
                g2.drawArc(cx + 2, cy - 8, 10, 16, -50, 100);
            }
        }

        private void drawBat(Graphics2D g2, int cx, int cy) {
            // إطار البطارية
            g2.drawRoundRect(cx - 9, cy - 6, 16, 12, 2, 2);
            // طرف البطارية
            g2.drawLine(cx + 7, cy - 3, cx + 9, cy - 3);
            g2.drawLine(cx + 9, cy - 3, cx + 9, cy + 3);
            g2.drawLine(cx + 7, cy + 3, cx + 9, cy + 3);

            // ملء حسب المستوى
            int[] widths = {2, 5, 9, 12};
            Color[] colors = {RED, ORANGE, GREEN, GREEN};
            int fw = widths[batLvl];
            g2.setColor(colors[batLvl]);
            g2.fillRoundRect(cx - 8, cy - 4, fw, 8, 1, 1);
        }
    }

    // ===== main لتجربة الكود =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Status Bar - iPhone Style");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setBackground(new Color(26, 26, 26));

            // محاكاة شاشة الموبايل
            JPanel phone = new JPanel(new BorderLayout());
            phone.setBackground(new Color(26, 26, 26));
            phone.setPreferredSize(new Dimension(320, 500));

            StatusBar bar = new StatusBar();
            phone.add(bar, BorderLayout.NORTH);

            // محتوى وهمي تحت الـ status bar
            JPanel content = new JPanel();
            content.setBackground(new Color(20, 20, 20));
            content.add(new JLabel("<html><font color='gray' size='3'>محتوى التطبيق هنا</font></html>"));
            phone.add(content, BorderLayout.CENTER);

            frame.add(phone);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}



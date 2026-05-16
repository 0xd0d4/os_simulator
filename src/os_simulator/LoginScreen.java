package os_simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    JPasswordField passField;
    JTextField userField;
    JLabel statusLabel;
    JLabel timeLabel;
    Timer clockTimer;
    Timer matrixTimer;
    char[][] matrixChars;
    int[] matrixY;
    JPanel matrixPanel;

    String[][] users = {
        {"root", "1234"},
        {"doha", "1810"},
        {"admin", "admin"}
    };

    // ألوان موحدة
    final Color PURPLE      = new Color(180, 0, 255);
    final Color DARK_PURPLE = new Color(80, 0, 120);
    final Color BLUE        = new Color(0, 180, 255);
    final Color BG          = Color.BLACK;

    public LoginScreen() {

        setTitle("Zero Trace OS - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ====== Matrix Rain Background ======
        matrixPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
                if (matrixChars != null) {
                    for (int i = 0; i < matrixChars.length; i++) {
                        for (int j = 0; j < matrixChars[i].length; j++) {
                            float alpha = (float)(matrixChars[i].length - j)
                                / matrixChars[i].length;
                            // بنفسجي
                            g2.setColor(new Color(150, 0, 255,
                                (int)(alpha * 200)));
                            g2.drawString(
                                String.valueOf(matrixChars[i][j]),
                                i * 20, matrixY[i] - (j * 20));
                        }
                    }
                }
            }
        };
        matrixPanel.setBackground(BG);
        matrixPanel.setLayout(new GridBagLayout());
        add(matrixPanel, BorderLayout.CENTER);

        // ====== Login Card ======
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 210));
                g2.fill(new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight(), 20, 20));
                // border بنفسجي
                g2.setColor(new Color(150, 0, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(
                    0, 0, getWidth()-1, getHeight()-1, 20, 20));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(420, 510));

        // Logo
        JLabel logo = new JLabel("◈ ZERO TRACE OS ◈", SwingConstants.CENTER);
        logo.setForeground(PURPLE);
        logo.setFont(new Font("Monospaced", Font.BOLD, 22));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

//        // Skull
        JLabel skull = new JLabel("", SwingConstants.CENTER);
        skull.setFont(new Font("Monospaced", Font.PLAIN, 60));
        skull.setAlignmentX(Component.CENTER_ALIGNMENT);


        //__________________________

        // Subtitle
        JLabel subtitle = new JLabel("SECURE ACCESS REQUIRED",
            SwingConstants.CENTER);
        subtitle.setForeground(new Color(120, 0, 180));
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JLabel divider = new JLabel(
            "──────────────────────", SwingConstants.CENTER);
        divider.setForeground(DARK_PURPLE);
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── USERNAME  ──
        JLabel userLbl = new JLabel("USERNAME          ");
        userLbl.setForeground(BLUE);
        userLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        userLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        userLbl.setHorizontalAlignment(SwingConstants.LEFT);

        userField = new JTextField();
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setBackground(new Color(15, 0, 25));
        userField.setForeground(PURPLE);
        userField.setCaretColor(PURPLE);
        userField.setFont(new Font("Monospaced", Font.PLAIN, 15));
        userField.setHorizontalAlignment(JTextField.LEFT);
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_PURPLE),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // ── PASSWORD ──
        JLabel passLbl = new JLabel("PASSWORD          ");
        passLbl.setForeground(BLUE);
        passLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        passLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        passLbl.setHorizontalAlignment(SwingConstants.LEFT);

        passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setBackground(new Color(15, 0, 25));
        passField.setForeground(PURPLE);
        passField.setCaretColor(PURPLE);
        passField.setFont(new Font("Monospaced", Font.PLAIN, 15));
        passField.setEchoChar('*');
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARK_PURPLE),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        // Status
        statusLabel = new JLabel(" ", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Login Button - بنفسجي/أزرق ──
        JButton loginBtn = new JButton("[ AUTHENTICATE ]");
        loginBtn.setBackground(new Color(50, 0, 80));
        loginBtn.setForeground(PURPLE);
        loginBtn.setFont(new Font("Monospaced", Font.BOLD, 15));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createLineBorder(PURPLE, 2));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginBtn.setBackground(new Color(80, 0, 130));
                loginBtn.setBorder(BorderFactory.createLineBorder(BLUE, 2));
            }
            public void mouseExited(MouseEvent e) {
                loginBtn.setBackground(new Color(50, 0, 80));
                loginBtn.setBorder(BorderFactory.createLineBorder(PURPLE, 2));
            }
        });

        ActionListener loginAction = e -> authenticate();
        loginBtn.addActionListener(loginAction);
        passField.addActionListener(loginAction);
        userField.addActionListener(e -> passField.requestFocus());

        // Hint
        JLabel hint = new JLabel("default: root / 1234");
        hint.setForeground(new Color(100, 0, 150));
        hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // أضف للـ card
        card.add(skull);
        card.add(Box.createVerticalStrut(5));
        card.add(logo);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(10));
        card.add(divider);
        card.add(Box.createVerticalStrut(20));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(passField);
        card.add(Box.createVerticalStrut(20));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);

        matrixPanel.add(card);

        // ====== Top Bar ======
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(5, 0, 15));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JLabel osLabel = new JLabel("ZERO TRACE OS  v1.0");
        osLabel.setForeground(new Color(120, 0, 180));
        osLabel.setFont(new Font("Monospaced", Font.BOLD, 13));

        timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setForeground(PURPLE);
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        topBar.add(osLabel, BorderLayout.WEST);
        topBar.add(timeLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ====== Bottom Bar ======
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setBackground(new Color(5, 0, 15));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        JLabel bottomLbl = new JLabel(
            "[ NO IDENTITY LEFT BEHIND ]  •  ZERO TRACE COMPLETE");
        bottomLbl.setForeground(new Color(100, 0, 150));
        bottomLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bottomBar.add(bottomLbl);
        add(bottomBar, BorderLayout.SOUTH);

        startMatrixRain();
        startClock();

        setVisible(true);
        userField.requestFocus();
    }

    private void authenticate() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        statusLabel.setForeground(BLUE);
        statusLabel.setText(" AUTHENTICATING...");

        Timer delay = new Timer(1000, e -> {
            boolean found = false;
            for (String[] u : users) {
                if (u[0].equals(user) && u[1].equals(pass)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                statusLabel.setForeground(PURPLE);
                statusLabel.setText("✔ ACCESS GRANTED");

                Timer open = new Timer(800, ev -> {
                    clockTimer.stop();
                    matrixTimer.stop();

                    // ====== الحل: افتح الـ MainDesktop الأول ======
                    MainDesktop desktop = new MainDesktop();
                    desktop.setVisible(true);

                    // بعدين اقفل الـ Login
                    dispose();
                });
                open.setRepeats(false);
                open.start();

            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("✘ ACCESS DENIED - INVALID CREDENTIALS");
                passField.setText("");
                shakeWindow();
            }
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void shakeWindow() {
        final int[] count = {0};
        Point origin = getLocation();
        Timer shake = new Timer(30, e -> {
            int dx = (count[0] % 2 == 0) ? 10 : -10;
            setLocation(origin.x + dx, origin.y);
            count[0]++;
            if (count[0] > 8) {
                ((Timer) e.getSource()).stop();
                setLocation(origin);
            }
        });
        shake.start();
    }

    private void startMatrixRain() {
        int cols = 100;
        matrixChars = new char[cols][10];
        matrixY = new int[cols];
        String chars = "01アイウエオカキクケコZERO TRACE";

        for (int i = 0; i < cols; i++) {
            matrixY[i] = (int)(Math.random() * 800);
        }

        matrixTimer = new Timer(80, e -> {
            for (int i = 0; i < cols; i++) {
                for (int j = matrixChars[i].length - 1; j > 0; j--) {
                    matrixChars[i][j] = matrixChars[i][j-1];
                }
                matrixChars[i][0] = chars.charAt(
                    (int)(Math.random() * chars.length()));
                matrixY[i] += 20;
                if (matrixY[i] > getHeight() + 200) {
                    matrixY[i] = 0;
                }
            }
            matrixPanel.repaint();
        });
        matrixTimer.start();
    }

    private void startClock() {
        clockTimer = new Timer(1000, e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            timeLabel.setText(String.format(
                "%02d:%02d:%02d  |  %02d/%02d/%04d",
                now.getHour(), now.getMinute(), now.getSecond(),
                now.getDayOfMonth(), now.getMonthValue(), now.getYear()));
        });
        clockTimer.start();
        clockTimer.getActionListeners()[0].actionPerformed(null);
    }
}
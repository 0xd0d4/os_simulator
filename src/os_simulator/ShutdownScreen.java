package os_simulator;

import javax.swing.*;
import java.awt.*;

public class ShutdownScreen extends JFrame {

    Timer fadeTimer;

    public ShutdownScreen() {
        
        setUndecorated(true);
        //  fullscreen مش حجم ثابت 
//        setSize(500, 350);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // في المنتصف
        setLocationRelativeTo(null); 
        setBackground(new Color(0, 0, 0));
        getContentPane().setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        //  Panel المنتصف 
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // أيقونة
        JLabel icon = new JLabel("⏻", SwingConstants.CENTER);
        icon.setForeground(new Color(180, 0, 255));
        icon.setFont(new Font("Monospaced", Font.BOLD, 70));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // النص الرئيسي
        JLabel msg = new JLabel("SHUTTING DOWN", SwingConstants.CENTER);
        msg.setForeground(new Color(180, 0, 255));
        msg.setFont(new Font("Monospaced", Font.BOLD, 28));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // نقط متحركة
        JLabel dots = new JLabel(".", SwingConstants.CENTER);
        dots.setForeground(new Color(0, 180, 255));
        dots.setFont(new Font("Monospaced", Font.BOLD, 28));
        dots.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sub text
        JLabel sub = new JLabel("Zero Trace OS - Goodbye", SwingConstants.CENTER);
        sub.setForeground(new Color(0, 180, 255));
        sub.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setForeground(new Color(180, 0, 255));
        bar.setBackground(new Color(20, 0, 30));
        bar.setBorder(BorderFactory.createLineBorder(new Color(80, 0, 120)));
        bar.setPreferredSize(new Dimension(300, 8));
        bar.setMaximumSize(new Dimension(300, 8));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        bar.setStringPainted(false);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(5));
        panel.add(dots);
        panel.add(Box.createVerticalStrut(15));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(25));
        panel.add(bar);

        add(panel);
        setVisible(true);

        // ====== نقط متحركة ======
        String[] dotFrames = {".", ". .", ". . ."};
        final int[] dotIndex = {0};
        Timer dotsTimer = new Timer(400, e -> {
            dots.setText(dotFrames[dotIndex[0] % 3]);
            dotIndex[0]++;
        });
        dotsTimer.start();

        // ====== Progress bar ======
        final int[] progress = {0};
        Timer progressTimer = new Timer(25, e -> {
            progress[0]++;
            bar.setValue(progress[0]);
            if (progress[0] >= 100) {
                ((Timer) e.getSource()).stop();
                dotsTimer.stop();
                startFade();
            }
        });
        progressTimer.start();
    }

    private void startFade() {
        float[] alpha = {1.0f};
        fadeTimer = new Timer(40, e -> {
            alpha[0] -= 0.03f;
            if (alpha[0] <= 0) {
                fadeTimer.stop();
                dispose();
                //  يقفل البرنامج خالص
                System.exit(0);
            }
            try {
                setOpacity(alpha[0]);
            } catch (Exception ex) {
                fadeTimer.stop();
                dispose();
                System.exit(0);
            }
        });
        fadeTimer.start();
    }
}
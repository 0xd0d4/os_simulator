package os_simulator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class setupContextMenu extends JFrame {

    private JLabel jLabel2 = new JLabel();
    private JPanel jPanel1 = new JPanel();

    public setupContextMenu() {
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        jLabel2.setBounds(0, 0, 800, 600);
        jLabel2.setOpaque(true);
        jLabel2.setBackground(Color.BLACK);

        add(jLabel2);

        setupContextMenu(); // تشغيل المينيو
    }

    // ====== Context Menu ======
    private void setupContextMenu() {
        setupContextMenu self = this;
        JLabel desktop = jLabel2;

        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem newFile = new JMenuItem("New File");
        JMenuItem refresh = new JMenuItem("Refresh");
        JMenuItem shutdown = new JMenuItem("Shutdown");

        newFile.addActionListener(e -> showCreateDialog());
        refresh.addActionListener(e -> desktop.repaint());

        shutdown.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(self, "Shutdown?");
            if (c == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        contextMenu.add(newFile);
        contextMenu.add(refresh);
        contextMenu.add(shutdown);

        desktop.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { check(e); }
            public void mouseReleased(MouseEvent e) { check(e); }

            void check(MouseEvent e) {
                if (e.isPopupTrigger())
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    // ====== Create Dialog ======
    private void showCreateDialog() {
        String name = JOptionPane.showInputDialog(this, "File name:");
        if (name == null) return;

        JPanel icon = new JPanel();
        icon.setBounds(50, 50 + jLabel2.getComponentCount() * 60, 80, 60);
        icon.setBackground(Color.DARK_GRAY);

        JLabel lbl = new JLabel(name);
        lbl.setForeground(Color.WHITE);

        icon.add(lbl);
        jLabel2.add(icon);

        jLabel2.revalidate();
        jLabel2.repaint();
    }

    public static void main(String[] args) {
        new setupContextMenu().setVisible(true);
    }
}
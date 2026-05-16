package os_simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AppMenu extends JPopupMenu {

    public AppMenu() {

        setBackground(new Color(5, 5, 5));
        setBorder(BorderFactory.createLineBorder(new Color(0, 255, 0), 1));
        
        
JMenuItem item1 = createItem("🖥 Root Terminal", "terminal");
item1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
add(item1);

JMenuItem item2 = createItem("📁 Zero Trace Files", "filesys");
item2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
add(item2);



//        add(createItem("  Root Terminal", "terminal"));
//        add(createItem("  Zero Trace Files", "filesys"));
//        add(createItem("   Nmap Scanner", "nmap"));
//        add(createItem("  Metasploit", "metasploit"));
        
        addSeparator();
        
JMenuItem exitItem = createItem("🚪 Logout System", "exit");
exitItem.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
exitItem.setForeground(Color.RED);
add(exitItem);
    }

    private JMenuItem createItem(String text, String action) {
        JMenuItem item = new JMenuItem(text);
        
        // ستايل العناصر
        item.setBackground(new Color(5, 5, 5));
        item.setForeground(new Color(0, 255, 0));
        item.setFont(new Font("Monospaced", Font.BOLD, 13));
        item.setPreferredSize(new Dimension(230, 40));
        item.setOpaque(true);

        // تأثير الـ Hover (الهكر ستايل)
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(0, 255, 0));
                item.setForeground(Color.BLACK);
            }
            public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(5, 5, 5));
                item.setForeground(new Color(0, 255, 0));
            }
        });

        // الأكشنز (هنا الربط الصح)
        item.addActionListener(e -> {
            if (action.equals("filesys")) {
                // كدة مش هيجيب ايرور لو عملتي خطوة رقم 1
                new FileSystem("/").setVisible(true);
            } else if (action.equals("terminal")) {
                new TerminalGUI().setVisible(true);
            } else if (action.equals("exit")) {
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, "Starting simulation for: " + text);
            }
        });

        return item;
    }
}
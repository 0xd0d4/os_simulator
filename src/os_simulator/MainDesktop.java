
package os_simulator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;


public class MainDesktop extends javax.swing.JFrame {
        private JLabel desktop;


    public MainDesktop() {
    initComponents();
    // ← ربط الـjLabel2
    desktop = jLabel2; 
    setupContextMenu();

//______________________________________________________________________________

jPanel1.setLayout(new BorderLayout());
StatusBar bar = new StatusBar();
jPanel1.add(bar, BorderLayout.EAST);
bar.setPreferredSize(new Dimension(300, 50));


       
    addShakeEffect(jButHome);
    addShakeEffect(jButFileSystem1);
    addShakeEffect(jButTrash);
    
    new javax.swing.Timer(1000, e -> {
    java.util.Date now = new java.util.Date();

    // نزود ساعة
    now.setTime(now.getTime() + (60 * 60 * 1000));

    java.text.SimpleDateFormat timeFormat =
        new java.text.SimpleDateFormat("hh:mm:ss a");

    java.text.SimpleDateFormat dateFormat =
        new java.text.SimpleDateFormat("dd MMM yyyy");

    timeLabel.setText(timeFormat.format(now));
    dateLabel.setText(dateFormat.format(now));

}).start();
    

  }
 //___________________________________________________________________________
    //methods >>desktop Menu
     private void setupContextMenu() {
        MainDesktop self = this;

        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.setBackground(new Color(10, 0, 20));
        contextMenu.setBorder(BorderFactory.createLineBorder(
            new Color(180, 0, 255), 1));

        JMenuItem newFolder = makeMenuItem("  New Folder", new Color(0, 50, 255));
        JMenuItem newFile   = makeMenuItem("  New File",   new Color(0, 50, 255));
        JMenuItem terminal  = makeMenuItem("  Openn Terminal",   new Color(180, 0, 255));
        JMenuItem filesys   = makeMenuItem("  File System",new Color(180, 0, 255));
        JMenuItem refresh   = makeMenuItem("  Refresh",    new Color(0, 255, 180));
        JMenuItem shutdown  = makeMenuItem("  Shutdown",    Color.RED);

        newFolder.addActionListener(e -> showCreateDialog(false));
        newFile.addActionListener(e ->   showCreateDialog(true));

        terminal.addActionListener(e -> {
            new TerminalGUI().setVisible(true);
        });

        filesys.addActionListener(e -> {
            new FileSystem().setVisible(true);
        });

        refresh.addActionListener(e -> {
            desktop.revalidate();
            desktop.repaint();
        });

        shutdown.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(self,
                "Shutdown Zero Trace OS?", "Shutdown",
                JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                self.dispose();
                new ShutdownScreen();
            }
        });

        JSeparator sep1 = new JSeparator();
        sep1.setBackground(new Color(80, 0, 120));
        JSeparator sep2 = new JSeparator();
        sep2.setBackground(new Color(80, 0, 120));

        contextMenu.add(newFolder);
        contextMenu.add(newFile);
        contextMenu.add(sep1);
        contextMenu.add(terminal);
        contextMenu.add(filesys);
        contextMenu.add(sep2);
        contextMenu.add(refresh);
        contextMenu.add(shutdown);

        self.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { check(e); }
            public void mouseReleased(MouseEvent e) { check(e); }
            void check(MouseEvent e) {
                if (e.isPopupTrigger())
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        desktop.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { check(e); }
            public void mouseReleased(MouseEvent e) { check(e); }
            void check(MouseEvent e) {
                if (e.isPopupTrigger())
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void showCreateDialog(boolean isFile) {
        String type = isFile ? "File" : "Folder";
        String[] options = {
            "  My Computer",
            "  Docker Container",
            "  GUI Desktop"
        };

        int choice = JOptionPane.showOptionDialog(
            this,
            "Where to create the " + type + "?",
            " Choose Location",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]
        );

        if (choice == -1) return;

        switch (choice) {
            case 0: createOnWindows(isFile); break;
            case 1: createOnDocker(isFile);  break;
            case 2: createOnGUI(isFile);     break;
        }
    }

    private void createOnWindows(boolean isFile) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(
            System.getProperty("user.home") + "/Desktop"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        String name = JOptionPane.showInputDialog(this,
            (isFile ? "File" : "Folder") + " name:",
            isFile ? "newfile.txt" : "New Folder");
        if (name == null || name.trim().isEmpty()) return;

        File item = new File(dir, name);
        try {
            if (isFile) {
                item.createNewFile();
                openEditor(item.getAbsolutePath(), "windows", item);
            } else {
                item.mkdir();
                JOptionPane.showMessageDialog(this,
                    " Created:\n" + item.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Error: " + ex.getMessage());
        }
    }

    private void createOnDocker(boolean isFile) {
        String[] paths = {"/root", "/tmp", "/home", "/var", "/etc"};

        String location = (String) JOptionPane.showInputDialog(
            this, "Choose Docker location:",
            " Docker", JOptionPane.PLAIN_MESSAGE,
            null, paths, paths[0]);
        if (location == null) return;

        String name = JOptionPane.showInputDialog(this,
            (isFile ? "File" : "Folder") + " name:",
            isFile ? "newfile.txt" : "NewFolder");
        if (name == null || name.trim().isEmpty()) return;

        String fullPath = location + "/" + name;

        new Thread(() -> {
            if (isFile) {
                runDockerCommand("touch \"" + fullPath + "\"");
                SwingUtilities.invokeLater(() ->
                    openEditor(fullPath, "docker", null));
            } else {
                runDockerCommand("mkdir -p \"" + fullPath + "\"");
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        " Created in Docker:\n" + fullPath));
            }
        }).start();
    }


    private void createOnGUI(boolean isFile) {
    String name = JOptionPane.showInputDialog(this,
        (isFile ? "File" : "Folder") + " name:",
        isFile ? "newfile.txt" : "New Folder");
    if (name == null || name.trim().isEmpty()) return;

    int existingCount = desktop.getComponentCount();
    int xPos = 15;
    int yPos = 80 + (existingCount * 95);

    if (yPos > desktop.getHeight() - 100) {
        xPos = 15 + (existingCount / 6) * 85;
        yPos = 80 + ((existingCount % 6) * 95);
    }

    addDesktopIcon(name, isFile, xPos, yPos);
}

// ====== الميثود الرئيسية للأيقونة ======
private void addDesktopIcon(String name, boolean isFile, int xPos, int yPos) {

    JPanel iconPanel = new JPanel(null);
    iconPanel.setOpaque(false);
    iconPanel.setBounds(xPos, yPos, 75, 90);

    JLabel iconLbl = new JLabel(isFile ? "📄" : "📁", SwingConstants.CENTER);
    iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
    iconLbl.setForeground(isFile
        ? new Color(0, 180, 255) : new Color(255, 200, 0));
    iconLbl.setBounds(0, 0, 75, 50);

    JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
    nameLbl.setForeground(Color.WHITE);
    nameLbl.setFont(new Font("Arial", Font.BOLD, 11));
    nameLbl.setOpaque(true);
    nameLbl.setBackground(new Color(0, 0, 0, 120));
    nameLbl.setBounds(0, 52, 75, 35);

    iconPanel.add(iconLbl);
    iconPanel.add(nameLbl);

    // ====== Drag ======
    final int[] dragStart = {0, 0};

    iconPanel.addMouseListener(new MouseAdapter() {

        public void mousePressed(MouseEvent e) {
            dragStart[0] = e.getX();
            dragStart[1] = e.getY();

            // ====== Right Click Menu ======
            if (SwingUtilities.isRightMouseButton(e)) {
                JPopupMenu iconMenu = new JPopupMenu();
                iconMenu.setBackground(new Color(10, 0, 20));
                iconMenu.setBorder(BorderFactory.createLineBorder(
                    new Color(180, 0, 255), 1));

                // ── Open (لو فايل) ──
                if (isFile) {
                    JMenuItem open = makeMenuItem(
                        "📄  Open", new Color(0, 180, 255));
                    open.addActionListener(ev ->
                        openEditor(name, "gui", null));
                    iconMenu.add(open);
                    iconMenu.add(makeSeparator());
                }

                // ── Rename ──
                JMenuItem rename = makeMenuItem(
                    "  Rename", new Color(0, 180, 255));
                rename.addActionListener(ev -> {
                    String newName = JOptionPane.showInputDialog(
                        MainDesktop.this, "New name:", name);
                    if (newName != null && !newName.trim().isEmpty()) {
                        nameLbl.setText(newName);
                        iconPanel.repaint();
                    }
                });

                // ── Properties ──
                JMenuItem props = makeMenuItem(
                    "  Properties", new Color(0, 180, 255));
                props.addActionListener(ev -> {
                    JOptionPane.showMessageDialog(MainDesktop.this,
                        "Name: " + nameLbl.getText() + "\n" +
                        "Type: " + (isFile ? "File" : "Folder") + "\n" +
                        "Location: GUI Desktop\n" +
                        "Position: " + iconPanel.getX()
                            + ", " + iconPanel.getY(),
                        "Properties",
                        JOptionPane.INFORMATION_MESSAGE);
                });

                iconMenu.add(makeSeparator());

                // ── Delete ──
                JMenuItem delete = makeMenuItem("  Delete", Color.RED);
                delete.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(
                        MainDesktop.this,
                        "Delete \"" + nameLbl.getText() + "\"?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        desktop.remove(iconPanel);
                        desktop.revalidate();
                        desktop.repaint();
                    }
                });

                iconMenu.add(rename);
                iconMenu.add(props);
                iconMenu.add(makeSeparator());
                iconMenu.add(delete);

                iconMenu.show(iconPanel, e.getX(), e.getY());
            }
        }

        public void mouseClicked(MouseEvent e) {
            // تحديد - لون مختلف
            if (SwingUtilities.isLeftMouseButton(e)) {
                // reset كل الأيقونات
                for (Component c : desktop.getComponents()) {
                    if (c instanceof JPanel) c.setBackground(null);
                    ((JPanel)c).setOpaque(false);
                }
                // highlight الأيقونة دي
                iconPanel.setOpaque(true);
                iconPanel.setBackground(new Color(100, 100, 255, 50));
                iconPanel.repaint();
            }

            // دوبل كليك يفتح
            if (e.getClickCount() == 2
                    && SwingUtilities.isLeftMouseButton(e)) {
                if (isFile) openEditor(name, "gui", null);
            }
        }
    });

    // ====== Mouse Motion للـ Drag ======
    iconPanel.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
            // حساب المكان الجديد
            int newX = iconPanel.getX() + e.getX() - dragStart[0];
            int newY = iconPanel.getY() + e.getY() - dragStart[1];

            // منع الخروج من حدود الـ desktop
            newX = Math.max(0, Math.min(newX,
                desktop.getWidth() - iconPanel.getWidth()));
            newY = Math.max(0, Math.min(newY,
                desktop.getHeight() - iconPanel.getHeight()));

            iconPanel.setLocation(newX, newY);
            desktop.repaint();
        }
    });

    desktop.add(iconPanel);
    desktop.revalidate();
    desktop.repaint();
}

// ====== Helper ======
private JSeparator makeSeparator() {
    JSeparator sep = new JSeparator();
    sep.setBackground(new Color(80, 0, 120));
    sep.setForeground(new Color(80, 0, 120));
    return sep;
}

    private void openEditor(String path, String mode, File windowsFile) {
        JFrame editor = new JFrame("📄 " + path);
        editor.setSize(650, 480);
        editor.setLocationRelativeTo(this);
        editor.setLayout(new BorderLayout());
        editor.getContentPane().setBackground(Color.BLACK);

        JTextArea textArea = new JTextArea();
        textArea.setBackground(new Color(10, 0, 20));
        textArea.setForeground(mode.equals("docker")
            ? new Color(0, 255, 0) : new Color(0, 180, 255));
        textArea.setCaretColor(new Color(180, 0, 255));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);

        if (mode.equals("docker")) {
            new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "docker", "exec", "priceless_germain",
                        "bash", "-c", "cat \"" + path + "\"");
                    Process p = pb.start();
                    BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null)
                        sb.append(line).append("\n");
                    SwingUtilities.invokeLater(() ->
                        textArea.setText(sb.toString()));
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(80, 0, 120)));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(5, 0, 15));

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new Color(0, 40, 80));
        saveBtn.setForeground(new Color(0, 180, 255));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createLineBorder(
            new Color(0, 180, 255)));

        JButton closeBtn = new JButton("✖ Close");
        closeBtn.setBackground(new Color(60, 0, 0));
        closeBtn.setForeground(Color.RED);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createLineBorder(Color.RED));

saveBtn.addActionListener(e -> {
    try {
        if (mode.equals("windows") && windowsFile != null) {
            // ← التغيير هنا بس
            java.io.FileWriter fw = new java.io.FileWriter(windowsFile);
            fw.write(textArea.getText());
            fw.close();

        } else if (mode.equals("docker")) {
            String content = textArea.getText()
                .replace("'", "'\\''");
            new Thread(() -> runDockerCommand(
                "printf '%s' '" + content
                + "' > \"" + path + "\"")).start();
        }
        JOptionPane.showMessageDialog(editor, "✅ Saved!");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(editor,
            "❌ " + ex.getMessage());
    }
});

        closeBtn.addActionListener(e -> editor.dispose());

        JLabel pathLbl = new JLabel("  " +
            (mode.equals("docker") ? "🐳" :
             mode.equals("windows") ? "🖥️" : "🖼️")
            + "  " + path);
        pathLbl.setForeground(new Color(100, 0, 150));
        pathLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        pathLbl.setBackground(new Color(5, 0, 15));
        pathLbl.setOpaque(true);

        toolbar.add(saveBtn);
        toolbar.add(closeBtn);
        editor.add(toolbar, BorderLayout.NORTH);
        editor.add(scroll, BorderLayout.CENTER);
        editor.add(pathLbl, BorderLayout.SOUTH);
        editor.setVisible(true);
    }

    private JMenuItem makeMenuItem(String text, Color color) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(new Color(10, 0, 20));
        item.setForeground(color);
        item.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return item;
    }

    private void runDockerCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", "priceless_germain",
                "bash", "-c", command);
            pb.start().waitFor();
        } catch (Exception e) { e.printStackTrace(); }
    }

    
//_____________________________________________________________________________
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButHome = new javax.swing.JButton();
        jButTrash = new javax.swing.JButton();
        jButTrash1 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton15 = new javax.swing.JButton();
        jButFileSystem1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTxTime = new javax.swing.JLabel();
        jTxTime1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButShutdown = new javax.swing.JButton();
        jButLock = new javax.swing.JButton();
        timeLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        jButStartContainer = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButFileManager = new javax.swing.JButton();
        jButtextEditor = new javax.swing.JButton();
        jButtonGoogle = new javax.swing.JButton();
        jButtonTerminal = new javax.swing.JButton();
        jButProcessManager = new javax.swing.JButton();
        jButMemoryManager = new javax.swing.JButton();
        jButFileSystem = new javax.swing.JButton();
        jButDevicesManager = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Desktop_Page");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("myfile.txt");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(1800, 630, 70, 20));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Youtube");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1800, 500, 60, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("File System");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1790, 290, 80, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Home");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1810, 180, 60, -1));

        jPanel1.setBackground(new java.awt.Color(36, 36, 36));
        jPanel1.setPreferredSize(new java.awt.Dimension(370, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 0, 190, 60));

        jButHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/home.png"))); // NOI18N
        jButHome.setBorderPainted(false);
        jButHome.setContentAreaFilled(false);
        jButHome.setFocusPainted(false);
        jButHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButHomeActionPerformed(evt);
            }
        });
        getContentPane().add(jButHome, new org.netbeans.lib.awtextra.AbsoluteConstraints(1780, 110, -1, -1));

        jButTrash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/youtube.png"))); // NOI18N
        jButTrash.setBorderPainted(false);
        jButTrash.setContentAreaFilled(false);
        jButTrash.setFocusPainted(false);
        jButTrash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButTrashActionPerformed(evt);
            }
        });
        getContentPane().add(jButTrash, new org.netbeans.lib.awtextra.AbsoluteConstraints(1770, 430, 110, -1));

        jButTrash1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/trash.png"))); // NOI18N
        jButTrash1.setBorderPainted(false);
        jButTrash1.setContentAreaFilled(false);
        jButTrash1.setFocusPainted(false);
        jButTrash1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButTrash1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButTrash1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1780, 320, 80, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Trash");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(1800, 400, 60, -1));

        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/file.png"))); // NOI18N
        jButton15.setBorderPainted(false);
        jButton15.setContentAreaFilled(false);
        jButton15.setFocusPainted(false);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(1780, 550, -1, -1));

        jButFileSystem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/folder.png"))); // NOI18N
        jButFileSystem1.setBorderPainted(false);
        jButFileSystem1.setContentAreaFilled(false);
        jButFileSystem1.setDefaultCapable(false);
        jButFileSystem1.setFocusPainted(false);
        jButFileSystem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButFileSystem1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButFileSystem1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1770, 210, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Desktop1.png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1900, 950));

        jTxTime.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        getContentPane().add(jTxTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jTxTime1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        getContentPane().add(jTxTime1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanel2.setBackground(new java.awt.Color(36, 36, 36));

        jButShutdown.setBackground(new java.awt.Color(102, 102, 102));
        jButShutdown.setForeground(new java.awt.Color(102, 102, 102));
        jButShutdown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/logout.png"))); // NOI18N
        jButShutdown.setBorderPainted(false);
        jButShutdown.setContentAreaFilled(false);
        jButShutdown.setFocusPainted(false);
        jButShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButShutdownActionPerformed(evt);
            }
        });

        jButLock.setBackground(new java.awt.Color(102, 102, 102));
        jButLock.setForeground(new java.awt.Color(102, 102, 102));
        jButLock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/lock.png"))); // NOI18N
        jButLock.setBorderPainted(false);
        jButLock.setContentAreaFilled(false);
        jButLock.setFocusPainted(false);
        jButLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButLockActionPerformed(evt);
            }
        });

        timeLabel.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        timeLabel.setForeground(new java.awt.Color(255, 255, 255));

        dateLabel.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(255, 255, 255));

        jButStartContainer.setBackground(new java.awt.Color(102, 102, 102));
        jButStartContainer.setForeground(new java.awt.Color(102, 102, 102));
        jButStartContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/startDockerIcon.png"))); // NOI18N
        jButStartContainer.setBorderPainted(false);
        jButStartContainer.setContentAreaFilled(false);
        jButStartContainer.setFocusPainted(false);
        jButStartContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButStartContainerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 189, Short.MAX_VALUE)
                .addComponent(dateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(timeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButStartContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jButLock, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButShutdown, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(timeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                        .addComponent(dateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButLock, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                    .addComponent(jButShutdown, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                    .addComponent(jButStartContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1250, 0, 650, 70));

        jPanel3.setBackground(new java.awt.Color(36, 36, 36));

        jButton7.setBackground(new java.awt.Color(102, 102, 102));
        jButton7.setForeground(new java.awt.Color(102, 102, 102));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/logo1_1.png"))); // NOI18N
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(102, 102, 102));
        jButton8.setForeground(new java.awt.Color(102, 102, 102));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Minimize.png"))); // NOI18N
        jButton8.setBorderPainted(false);
        jButton8.setContentAreaFilled(false);
        jButton8.setFocusPainted(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButFileManager.setBackground(new java.awt.Color(102, 102, 102));
        jButFileManager.setForeground(new java.awt.Color(102, 102, 102));
        jButFileManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/file manager.png"))); // NOI18N
        jButFileManager.setBorderPainted(false);
        jButFileManager.setContentAreaFilled(false);
        jButFileManager.setFocusPainted(false);
        jButFileManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButFileManagerActionPerformed(evt);
            }
        });

        jButtextEditor.setBackground(new java.awt.Color(102, 102, 102));
        jButtextEditor.setForeground(new java.awt.Color(102, 102, 102));
        jButtextEditor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Text-Editor.png"))); // NOI18N
        jButtextEditor.setBorderPainted(false);
        jButtextEditor.setContentAreaFilled(false);
        jButtextEditor.setFocusPainted(false);
        jButtextEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtextEditorActionPerformed(evt);
            }
        });

        jButtonGoogle.setBackground(new java.awt.Color(102, 102, 102));
        jButtonGoogle.setForeground(new java.awt.Color(102, 102, 102));
        jButtonGoogle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/google-chrome.png"))); // NOI18N
        jButtonGoogle.setBorderPainted(false);
        jButtonGoogle.setContentAreaFilled(false);
        jButtonGoogle.setFocusPainted(false);
        jButtonGoogle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGoogleActionPerformed(evt);
            }
        });

        jButtonTerminal.setBackground(new java.awt.Color(102, 102, 102));
        jButtonTerminal.setForeground(new java.awt.Color(102, 102, 102));
        jButtonTerminal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/terminal.png"))); // NOI18N
        jButtonTerminal.setBorderPainted(false);
        jButtonTerminal.setContentAreaFilled(false);
        jButtonTerminal.setFocusPainted(false);
        jButtonTerminal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTerminalActionPerformed(evt);
            }
        });

        jButProcessManager.setBackground(new java.awt.Color(102, 102, 102));
        jButProcessManager.setForeground(new java.awt.Color(102, 102, 102));
        jButProcessManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Process Manager.png"))); // NOI18N
        jButProcessManager.setBorderPainted(false);
        jButProcessManager.setContentAreaFilled(false);
        jButProcessManager.setFocusPainted(false);
        jButProcessManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButProcessManagerActionPerformed(evt);
            }
        });

        jButMemoryManager.setBackground(new java.awt.Color(102, 102, 102));
        jButMemoryManager.setForeground(new java.awt.Color(102, 102, 102));
        jButMemoryManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Memory Manager.png"))); // NOI18N
        jButMemoryManager.setBorderPainted(false);
        jButMemoryManager.setContentAreaFilled(false);
        jButMemoryManager.setFocusPainted(false);
        jButMemoryManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButMemoryManagerActionPerformed(evt);
            }
        });

        jButFileSystem.setBackground(new java.awt.Color(102, 102, 102));
        jButFileSystem.setForeground(new java.awt.Color(102, 102, 102));
        jButFileSystem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/File System.png"))); // NOI18N
        jButFileSystem.setBorderPainted(false);
        jButFileSystem.setContentAreaFilled(false);
        jButFileSystem.setFocusPainted(false);
        jButFileSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButFileSystemActionPerformed(evt);
            }
        });

        jButDevicesManager.setBackground(new java.awt.Color(102, 102, 102));
        jButDevicesManager.setForeground(new java.awt.Color(102, 102, 102));
        jButDevicesManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/Devices Manager.png"))); // NOI18N
        jButDevicesManager.setBorderPainted(false);
        jButDevicesManager.setContentAreaFilled(false);
        jButDevicesManager.setFocusPainted(false);
        jButDevicesManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButDevicesManagerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButFileManager, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtextEditor, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonGoogle, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(165, 165, 165)
                .addComponent(jButtonTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButProcessManager, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButMemoryManager, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButFileSystem, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButDevicesManager, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButDevicesManager, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButFileSystem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButMemoryManager, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButProcessManager, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jButtonTerminal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonGoogle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtextEditor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButFileManager, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1070, 60));

        setSize(new java.awt.Dimension(1917, 1047));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
// استدعاء المنيو الهكر
    AppMenu menu = new AppMenu();
    
    // إظهارها تحت أيقونة اللوجو (jButton7)
    menu.show(jButton7, 0, jButton7.getHeight());
    
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButtonTerminalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTerminalActionPerformed
    // TODO add your handling code here:
    TerminalGUI t = new TerminalGUI();
    t.setVisible(true);
    }//GEN-LAST:event_jButtonTerminalActionPerformed

    
    
    private void jButHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButHomeActionPerformed
    // TODO add your handling code here:
//    os_simulator.FileSystem fs = new os_simulator.FileSystem();
//    
//    // 2. استدعاء ميثود التنقل للمسار المطلوب
//    fs.navigateTo("/root");
//    
//    // 3. إظهار الفريم
//    fs.setVisible(true);
    new FileSystem("/root").setVisible(true);

  
    }//GEN-LAST:event_jButHomeActionPerformed

    private void jButTrashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButTrashActionPerformed
    // TODO add your handling code here:
try {
        Desktop.getDesktop().browse(new URI("https://www.youtube.com"));
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    }//GEN-LAST:event_jButTrashActionPerformed

    private void jButFileSystem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFileSystem1ActionPerformed
    // TODO add your handling code here:
//    FileSystem fs = new FileSystem();
//        fs.setVisible(true);
    new FileSystem("/").setVisible(true);

    }//GEN-LAST:event_jButFileSystem1ActionPerformed

    private void jButProcessManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButProcessManagerActionPerformed
        // TODO add your handling code here:
        ProcessManager pm = new ProcessManager();
     pm.setVisible(true);
        
    }//GEN-LAST:event_jButProcessManagerActionPerformed

    private void jButMemoryManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButMemoryManagerActionPerformed
        // TODO add your handling code here:
        MemoryManager mm = new MemoryManager();
        mm.setVisible(true);
    }//GEN-LAST:event_jButMemoryManagerActionPerformed

    private void jButFileSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFileSystemActionPerformed
        // TODO add your handling code here:
        FileSystem fs = new FileSystem();
    fs.setVisible(true);
    }//GEN-LAST:event_jButFileSystemActionPerformed

    private void jButDevicesManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButDevicesManagerActionPerformed
        // TODO add your handling code here:
        IODevicesManager io = new IODevicesManager();
    io.setVisible(true);
    }//GEN-LAST:event_jButDevicesManagerActionPerformed

    private void jButShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButShutdownActionPerformed

int confirm = JOptionPane.showConfirmDialog(
        this,
        "Shutdown Zero Trace OS?",
        "Shutdown",
        JOptionPane.YES_NO_OPTION
    );
    if (confirm == JOptionPane.YES_OPTION) {
        dispose();
        new ShutdownScreen();
    }
    }//GEN-LAST:event_jButShutdownActionPerformed

    private void jButLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButLockActionPerformed

    dispose();
    new LoginScreen();
    
    
    
    
    }//GEN-LAST:event_jButLockActionPerformed

    private void jButtonGoogleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGoogleActionPerformed
        // TODO add your handling code here:
        try {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().browse(
                new java.net.URI("https://www.google.com"));
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    }//GEN-LAST:event_jButtonGoogleActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButtextEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtextEditorActionPerformed
        // TODO add your handling code here:
        TextEditorFrame editor = new TextEditorFrame();
            editor.setVisible(true);
    }//GEN-LAST:event_jButtextEditorActionPerformed

    private void jButFileManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFileManagerActionPerformed
        // TODO add your handling code here:
        jButFileManager.addActionListener(e -> {
    new FileManagerGUI().setVisible(true);
});
    }//GEN-LAST:event_jButFileManagerActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
//         try {
//        JFileChooser chooser = new JFileChooser();
//
//        int result = chooser.showOpenDialog(this);
//
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File file = chooser.getSelectedFile();
//
//            java.awt.Desktop.getDesktop().open(file);
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//    }

  try {
        // مسار الملف على Desktop
        File file = new File(System.getProperty("user.home") + "/Desktop/myfile.txt");

        // لو الملف مش موجود → نعمله
        if (!file.exists()) {
            file.createNewFile();
        }

        TextEditorFrame editor = new TextEditorFrame();
        editor.setVisible(true);

        editor.openFile(file);

    } catch (Exception e) {
        e.printStackTrace();
    }
  
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButStartContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButStartContainerActionPerformed
        // TODO add your handling code here:
 try {
    String containerName = "priceless_germain";

    ProcessBuilder builder = new ProcessBuilder(
        "cmd.exe", "/c", "docker start " + containerName
    );

    builder.redirectErrorStream(true);
    Process process = builder.start();

    int exitCode = process.waitFor(); // نستنى الأمر يخلص

    if (exitCode == 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "The container was successfully operated",
            "Success",
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    } else {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "An error occurreed while operating the container",
            "Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

} catch (Exception e) {
    e.printStackTrace();
}

    }//GEN-LAST:event_jButStartContainerActionPerformed

    private void jButTrash1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButTrash1ActionPerformed
        // TODO add your handling code here:
//        os_simulator.FileSystem fs = new os_simulator.FileSystem();
//    
//    // بنخلي الفريم تفتح على مجلد السلة اللي نقلنا فيه الملفات
//    fs.navigateTo("/root/.trash"); 
//    
//    fs.setVisible(true);
    new FileSystem("/root/.trash").setVisible(true);

    }//GEN-LAST:event_jButTrash1ActionPerformed

// Button hover effect Methods
public void addShakeEffect(JButton btn) {
    Border normal = btn.getBorder();
    Timer timer = new Timer(100, null);
    final int[] step = {0};
    timer.addActionListener(e -> {
        if (step[0] == 0) {
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        } 
        else if (step[0] == 1) {
            btn.setBorder(normal);
        } 
        else if (step[0] == 1) {
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        } 
        else {
            btn.setBorder(normal);
            timer.stop();
        }

        step[0]++;
    });

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            step[0] = 0;
            timer.start();
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            timer.stop();
            btn.setBorder(normal);
        }
    });
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainDesktop.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainDesktop.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainDesktop.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainDesktop.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainDesktop().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dateLabel;
    private javax.swing.JButton jButDevicesManager;
    private javax.swing.JButton jButFileManager;
    private javax.swing.JButton jButFileSystem;
    private javax.swing.JButton jButFileSystem1;
    private javax.swing.JButton jButHome;
    private javax.swing.JButton jButLock;
    private javax.swing.JButton jButMemoryManager;
    private javax.swing.JButton jButProcessManager;
    private javax.swing.JButton jButShutdown;
    private javax.swing.JButton jButStartContainer;
    private javax.swing.JButton jButTrash;
    private javax.swing.JButton jButTrash1;
    private javax.swing.JButton jButtextEditor;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButtonGoogle;
    private javax.swing.JButton jButtonTerminal;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jTxTime;
    private javax.swing.JLabel jTxTime1;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}
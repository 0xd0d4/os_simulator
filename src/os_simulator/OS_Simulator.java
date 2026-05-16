package os_simulator;

import javax.swing.SwingUtilities;
 
public class OS_Simulator {

    public static void main(String[] args) {        
        SwingUtilities.invokeLater(() -> {
        new LoginScreen(); 
        });
        
    }
}

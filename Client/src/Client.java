
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author thong
 */
public class Client {

    public static void main(String[] argv) {
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
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        System.out.println("Client Started!");
        Client client = new Client("127.0.0.1", 1235);

    }
    
    public DataManager dataManager;
    public ThreadManager threadManager;
    public Listener listener;
    public ServerPI serverPI;
    public GUI gui;

    private Client(String serverAddr, int port) {
        this.dataManager = new DataManager();
        this.threadManager = new ThreadManager();
        this.listener = new Listener(port, this);

        while (true) {
        try {
            serverAddr = JOptionPane.showInputDialog(null, "Enter server address: ", "Server for sharing", 1);
            if (serverAddr == null) System.exit(-1);
            this.serverPI = new ServerPI(serverAddr, 5321);
            break;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Can't connect to server " + serverAddr);
        }
        }
        
        new Thread(listener).start();

        
        this.gui = new GUI(this);
        this.gui.setVisible(true);
    }
}

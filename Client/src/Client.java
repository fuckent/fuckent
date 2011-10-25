
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author thong - tin
 */
public class Client {

    public static void main(String[] argv) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        System.out.println("Client Started!");
        
        Client client = new Client("127.0.0.1", 1236);

    }
    
    public DataManager dataManager;
    public ThreadManager threadManager;
    public Listener listener;
    public ServerPI serverPI;
    public GUI gui;
    public final int port;
    public final String title;

    private Client(String serverAddr, int port) {
        //SeedThread st = new SeedThread(this);
        //long t1 = System.currentTimeMillis();
        //System.out.println(st.getMD5Hash("/home/thong/Downloads/hihi.part1"));
        //long t2 = System.currentTimeMillis();
        //System.out.println(t2-t1);
        int rs = JOptionPane.showConfirmDialog(null, "Do you want to upload?", null, 0);
        if (rs  == 0) {
            this.port = 1235;
            this.title = "CUpload";
//            thi
        } else {
            this.port = 1236;
            this.title = "CDowload";
        }
        
        this.dataManager = new DataManager(this);
        this.threadManager = new ThreadManager();
        this.listener = new Listener(this.port, this);

        while (true) {
        try {
            serverAddr = JOptionPane.showInputDialog(null, "Enter server address: ", "Server for sharing", 1);
            if (serverAddr == null) System.exit(-1);
            this.serverPI = new ServerPI(serverAddr, 5321);
            break;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Can't connect to server " + serverAddr, "ERROR", 0);
        }
        }
        
        new Thread(listener).start();

        
        this.gui = new GUI(this);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                gui.setVisible(true);
            }
        });
    
    }
}

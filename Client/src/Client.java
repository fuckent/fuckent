
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

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
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        // com.sun.java.swing.plaf.windows.WindowsLookAndFeel
        //javax.swing.plaf.metal.MetalLookAndFeel
        //MetalLookAndFeel a = 
       //MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.OceanTheme());
        
     try {
              UIManager.setLookAndFeel(
                  new javax.swing.plaf.metal.MetalLookAndFeel()
              );
         // set UI manager properties here that affect Quaqua
         
         } catch (Exception e) {
             // take an appropriate action here
             
         }        //</editor-fold>        
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

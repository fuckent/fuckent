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
        this.serverPI = new ServerPI(serverAddr, 5321);
        this.listener = new Listener(port);

        this.gui = new GUI(this);
        this.gui.setVisible(true);
    }
}


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thong
 */
public class Listener implements Runnable {

    private ServerSocket listener;

    private Client client;

    Listener(int port, Client client) {
        this.client = client;
        // client.dataManager.

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            // TODO Print error message!
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Client Listening...");
        try {
        listener.setReuseAddress(true);
        } catch (IOException e) { }
        
        Socket conn;
        while (true) {
            try {
                conn = listener.accept();
                //conn.setReuseAddress(true);
                Thread t =new Thread(new UploadThread(conn, this.client));
                t.start();
                // client.threadManager.addThread(FileID, null);
               

            } catch (IOException e) {
                // TODO Print error and exit ! (or continue?)
                e.printStackTrace();
            }

        }

    }
}

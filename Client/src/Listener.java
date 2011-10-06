
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
        /* Listen connection from client */

        while (true) {
            System.out.println("Hello");
            try {
                System.out.println("Waiting a new connection from other client");
                Socket conn = listener.accept();

                /* create a new thread UploadThread to deal with this connection */
                Thread t = new Thread(new UploadThread(conn, this.client));
                t.start();

                // TODO Print LOG message HERE!

            } catch (IOException e) {
                // TODO Print error and exit ! (or continue?)
                e.printStackTrace();
            }

        }

    }
}

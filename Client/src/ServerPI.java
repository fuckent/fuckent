
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thong
 */
public class ServerPI {

    private Socket con;

    public String download(int fileID, String hash) {
        // TODO: CODE HERE
        // This function tell server that client want to download a file !
        // and return a list of clients' addr having that file!
        // randomly choose  a client to  to download

        return null;

    }

    public void share(int fileID, String hash) {
        // TODO: CODE HERE 
        // tell server we want share a file !!!
    }

    public void unshare(int fileID) {
        // TODO: CODE HRE
        // tell server we want unshare a file!!!
    }

    public int seed(String fileName, String hash) {
        // TODO: CODE 
        // tell server we want seed a file
        // and server return fileID

        return 0;
    }

    public ServerPI(String serverAddr, int port) {
        try {
            con = new Socket(serverAddr, port);


        } catch (UnknownHostException ex) {
            // TODO: CODE HERE!!
            Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            // TODO: CODE HERE!!!
            Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}

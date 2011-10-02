
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
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

    InputStreamReader in = null;
    BufferedReader reader;
    PrintWriter out;
    private Socket con;

    public synchronized String download(int fileID, String hash) {
        String[] lst;
        try {
            out.format("DOWNLOAD %d %s\n", fileID, hash).flush();

            String str = reader.readLine();
            if (str.matches("DOWNLOAD [^ ]+( [^ ]+)+")) {
                lst = str.split(" ");
                if (lst[1].equals("ERROR")) {
                    System.err.println("Error when DOWNLOAD REQ server");
                } else {
                    if (lst.length == 0) {
                        return null;
                    }
                    return lst[new Random().nextInt(lst.length)];
                }
            }

            return null;
        } catch (IOException ex) {
            Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;

    }

    public synchronized void share(int fileID, String hash) {
        // TODO: CODE HERE 
        // tell server we want share a file !!!
    }

    public synchronized void unshare(int fileID) {
        // TODO: CODE HRE
        // tell server we want unshare a file!!!
    }

    public synchronized int seed(String fileName, long fileSize, String hash) {
        String[] lst;
        
        try {
            out.format("SEED %s %d %s\n", fileName, fileSize, hash).flush();

            String str = reader.readLine();
            if (str.matches("SEED [^ ]+")) {
                lst = str.split(" ");
                return new Integer(lst[1]).intValue();
                }
        } catch (IOException ex) {
            Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return 0;
    }

    public ServerPI(String serverAddr, int port) throws IOException {

        con = new Socket(serverAddr, port);

        this.in = new InputStreamReader(con.getInputStream());
        this.reader = new BufferedReader(in);
        this.out = new PrintWriter(con.getOutputStream());

    }
}

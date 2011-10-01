
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class upload a file to another client.
 * 
 * Similar Server.ServerPI, it includes a client-client protocol interpreter
 * 
 * @author thong
 */
class UploadThread extends ClientThread {


    private Socket conn;
    private boolean exitRequest;
    private int rate;
    private int speed;
    private Client client;

    UploadThread(Socket conn, Client client) {
        this.conn = conn;
        this.client = client;
    }

    /**
     * Recv input from client (DOWNLOAD - RATE) and respond
     * 
     * We need manage upload rate in this class 
     *      Hint: Count Bytes had been upload and then Sleep in short-time to
     *          reach limit rate
     * 
     */
    public void Run() {
        // TODO: CODE HERE
        // read input from client and respond
        // Use System.currentTimeMilis to get current time
    }

    /**
     *  Notice: Client in other side limit DOWNLOAD rate by sent RATE XXX, whereas
     *          this function is called by GUI to limit UPLOAD rate.
     */
    @Override
    public void setRate(int speed) {
        this.rate = speed;
    }

    @Override
    public void closeThread() {
        /**
         * GUI call this function to exit sharing file.
         */
        this.exitRequest = true;
    }

    @Override
    public int getRate() {
        /**
         * GUI call this function to get upload speed and then show on fileTable (GUI)
         */
        return this.speed;
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getClientAddr() {
        return conn.getRemoteSocketAddress().toString();
    }
}

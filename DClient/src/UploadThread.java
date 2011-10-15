
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;

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
class UploadThread extends ClientThread  {

    private Socket conn;
    private int speed;
    private Client client;
    private InputStreamReader in;
    private BufferedReader reader;
    private PrintWriter out;
    private int rate;
    private boolean exitRequest;
    private ConcurrentLinkedQueue<String> msgQueue;
    private int fileID;
    private long totalCount;
    private int id;
    

    @Override
    public void recvMsg() {
        if (msgQueue.isEmpty()) {
            return;
        }
        String msg = msgQueue.poll();
        if (msg.compareTo("CLOSE @CODE: [fuckent]") == 0) {
            try {
                this.conn.close();
            } catch (IOException ex) {
                Logger.getLogger(UploadThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Closing thread");
            client.dataManager.updateCurrentSize(fileID, totalCount);
            //client.dataManager.updateStatus(fileID, "PAUSED");
            client.threadManager.removeThread(fileID);
            Thread.currentThread().stop();
           

        }
        /* More here! */
        // SET RATE LIMT
        //
        
    }
    
    
    @Override
    public void sendMsg(String str) {
        msgQueue.add(str);
    }
    
    UploadThread(Socket conn, Client client) {
        System.out.println("DOWN REQ!");
        this.conn = conn;
        this.client = client;
        msgQueue = new ConcurrentLinkedQueue<String> ();
    }

    /**
     * Recv input from client (DOWNLOAD - RATE) and respond
     * 
     * We need manage upload rate in this class 
     *      Hint: Count Bytes had been upload and then Sleep in short-time to
     *          reach limit rate
     * 
     */
    //@Override
    public void Run() {
        RandomAccessFile iS = null;
        int count;
        totalCount = 0;

        try {
            // TODO: CODE HERE
            // read input from client and respond
            // Use System.currentTimeMilis to get current time
            // conn.setSendBufferSize(4096);
            //conn.setReuseAddress(true);
            this.in = new InputStreamReader(conn.getInputStream());
            this.reader = new BufferedReader(in);
            this.out = new PrintWriter(conn.getOutputStream());

            String line = reader.readLine();
            System.out.println("REQ: " + line);
            if (line.matches("DOWNLOAD [0-9]+ [0-9a-f]+ [0-9]+")) {
                String[] lst = line.split(" ");
                totalCount = new Integer(lst[3]).longValue();
                ResultSet rs = client.dataManager.getFile(lst[1], lst[2]);
                if (rs == null) {
                    out.print("ERROR\n");
                    out.flush();
                } else {
                    try {
                        //
                        // Add this thread to thread manager
                        //
                        
                        this.fileID = new Integer(lst[1]).intValue();
                        System.out.println("Sending file");
                        String location = rs.getString("fileLocation");
                        long fileSize = rs.getLong("fileSize");
                        out.format("OK %d\n", fileSize).flush();
                        File f = new File(location);
                        iS = new RandomAccessFile(location, "r");
                        //System.out.println("totalCount: " + totalCount);
                        iS.seek(totalCount);

                        byte[] buf = new byte[4096];
                        int i = 0;
                        while (i < client.gui.model.getRowCount()){
                            if ((Integer) client.gui.model.getValueAt(i, 0) == fileID)
                                break;
                            i++;
                        }
                        
                        client.gui.model.setSwingWorker(i, this);
                        this.id = i;
                        // client.threadManager.addThread(i, this);
                        
                        while (totalCount < fileSize) {
                            this.recvMsg();
                            
                            count = iS.read(buf);
                            totalCount += count;
                            conn.getOutputStream().write(buf, 0, count);
                            conn.getOutputStream().flush();
                            publish(Long.valueOf(totalCount*100/ fileSize).intValue());
                        }
                        conn.getOutputStream().flush();
                        System.out.println("Finish upload file");
                        client.threadManager.removeThread(fileID);

                    } catch (SQLException ex) {
                        System.err.println("Exit thread");
                    }
                }
            }
        } catch (IOException ex) {
             System.err.println("Upload error.\nExit thread!");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (iS != null) {
                try {
                    iS.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     *  Notice: Client in other side limit DOWNLOAD rate by sent RATE XXX, whereas
     *          this function is called by GUI to limit UPLOAD rate.
     */

    public void setRate(int speed) {
        this.rate = speed;
    }

    @Override
    public void closeThread() {
        client.dataManager.updateCurrentSize(fileID, totalCount);
        client.dataManager.updateStatus(fileID, "SHARING");
        client.gui.model.setValueAt("SHARING", id, 6);
        // client.threadManager.removeThread(this.id);
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

    @Override
    public Long getcurSize() {
        return -1l;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        this.Run();
        return 0;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    @Override
    protected void process(java.util.List<Integer> c) {
        int i = 0;
        
        while (i < client.gui.model.getRowCount()) {
            int k = (Integer)client.gui.model.getValueAt(i, 0);
            if (k == this.fileID)
            {
                client.gui.model.setValueAt(c.get(c.size()-1), i, 3);
            }
            i++;
        }
    }
        
}

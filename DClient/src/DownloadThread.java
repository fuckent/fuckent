
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
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
class DownloadThread extends ClientThread implements Runnable {

    private final Client client;
    private final int fileID;
    private final String server;
    private Socket conn;
    private InputStreamReader in;
    private BufferedReader reader;
    private PrintWriter out;
    private final String fileHash;
    private final long pos;
    private File File;
    private RandomAccessFile oS;
    private InputStream inStream;
    int count;
    long totalCount;
    private long fileSize;
    private ConcurrentLinkedQueue<String> msgQueue;

    @Override
    public void sendMsg(String str) {
        msgQueue.add(str);
    }

    @Override
    public void recvMsg() {
        if (msgQueue.isEmpty()) {
            return;
        }
        String msg = msgQueue.poll();
        if (msg.compareTo("CLOSE @CODE: [fuckent]") == 0) {
            this.closeThread();

        }
        /* More here! */
        // SET RATE LIMT
        //

    }

    @Override
    public void run() {
        File = new File(fileHash);
        try {
            oS = new RandomAccessFile(File, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Thread download start!");
        String[] lst;

        try {
            this.conn = new Socket(server, 1235);

            this.in = new InputStreamReader(conn.getInputStream());
            //this.BIS = new BufferedInputStream(conn.getInputStream());

            this.reader = new BufferedReader(in);
            this.out = new PrintWriter(conn.getOutputStream());

            System.out.println("Connect to other client");
            out.format("DOWNLOAD %d %s %d\n", this.fileID, this.fileHash, this.pos).flush();

            String line = reader.readLine();
            System.out.println("res: " + line);
            if (line.matches("OK [0-9]+")) {
                lst = line.split(" ");
                oS.seek(pos);
                this.fileSize = new Integer(lst[1]).longValue();
                System.out.println("Download started!");
                byte[] buff = new byte[4096];
                int i = 0;
                while (totalCount < fileSize) {
                    this.recvMsg();
                    if (conn.getInputStream().available() > 0) {
                        count = conn.getInputStream().read(buff);
                        totalCount += count;

                        if (count < 0) {
                            System.err.println("DOWNLOAD ERROR!");
                            this.closeThread();
                        }
                        oS.write(buff, 0, count);
                    }
                }
                System.out.println("Finish download file");

            } else {
                System.err.println("DOWNLOAD ERROR!");
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UploadThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ioe) {
                }
            }
            if (oS != null) {
                try {
                    oS.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    public DownloadThread(String serverIP, int fileID, String fileHash, long pos, Client aThis) {
        this.server = serverIP;
        this.fileID = fileID;
        this.client = aThis;
        this.fileHash = fileHash;
        totalCount = 0;
        this.pos = pos;
        msgQueue = new ConcurrentLinkedQueue<String>();
        client.threadManager.addThread(this.fileID, this);
    }

    @Override
    public void setRate(int speed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeThread() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRate() {
        return -1;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getClientAddr() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "[unknown]";
    }
}


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thong
 */
class DownloadThread extends ClientThread implements Runnable {

    private Client client;
    private int fileID;
    private String server;
    private Socket conn;
    private InputStreamReader in;
    private BufferedReader reader;
    private PrintWriter out;
    private String fileHash;
    private long pos;
    private File File;
    private RandomAccessFile oS;
    private InputStream inStream;
    int count;
    long totalCount;
    private long fileSize;
    private ConcurrentLinkedQueue<String> msgQueue;
    private String fileName;
    private String clientAddr;

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
        String line = client.serverPI.download(fileID);

        if (line.matches("DOWNLOAD [^ ]+ [0-9]+ [0-9a-z]+( [^ ]+)+")) {

            String[] lst = line.split(" ");

            try {
                this.fileName = java.net.URLDecoder.decode(lst[1], "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
                fileName = "[unknown]";
            }
            this.fileSize = new Long(lst[2]).longValue();
            this.fileHash = lst[3];
            this.clientAddr = lst[4 + (new Random()).nextInt(lst.length - 4)];
            try {
                if (client.dataManager.getFile(String.valueOf(fileID), fileHash).next()) {
                    JOptionPane.showMessageDialog(null, "This file had already existed on your PC", "ERROR", 0);
                    return;
                }
            } catch (SQLException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "This file had already existed on your PC", "ERROR", 0);
                return;
            }

            client.dataManager.addFile(fileID, fileName, fileSize, 0, fileHash, "DOWNLOADING", "./");

        } else if (line.matches("DOWNLOAD ERROR 1")) {
            JOptionPane.showMessageDialog(null, "The file don't exists on server", "ERROR", 0);
            return;
        } else if (line.matches("DOWNLOAD ERROR 2")) {
            JOptionPane.showMessageDialog(null, "Nobody is sharing the file", "ERROR", 0);
            return;
        }


        File = new File(fileName);

        try {
            oS = new RandomAccessFile(File, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Thread download start!");
        String[] lst;
        line = null;

        try {
            this.conn = new Socket(server, 1235);

            this.in = new InputStreamReader(conn.getInputStream());
            //this.BIS = new BufferedInputStream(conn.getInputStream());

            this.reader = new BufferedReader(in);
            this.out = new PrintWriter(conn.getOutputStream());

            System.out.println("Connect to other client");
            out.format("DOWNLOAD %d %s %d\n", this.fileID, this.fileHash, this.pos).flush();

            line = reader.readLine();
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

    public DownloadThread(int fileID, int pos, Client aThis) {
        this.client = aThis;
        totalCount = 0;
        this.pos = pos;
        this.fileID = fileID;
        msgQueue = new ConcurrentLinkedQueue<String>();
        this.client.threadManager.addThread(this.fileID, this);
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
        return this.clientAddr;
    }
}

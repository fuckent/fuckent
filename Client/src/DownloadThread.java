
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
class DownloadThread extends ClientThread {

    private Client client;
    private int fileID;
    // private String server;
    private Socket conn;
    private InputStreamReader in;
    private BufferedReader reader;
    private PrintWriter out;
    private String fileHash;
    private long pos;
    private File f;
    private RandomAccessFile oS;
    // private InputStream inStream;
    int count;
    long totalCount;
    private long fileSize;
    private ConcurrentLinkedQueue<String> msgQueue;
    private String fileName;
    private String clientAddr;
    private long time;
    // private int limitrate;
    private long rate;
    // private Long curSize;
    private int id;
    private long t;
    private long c;
    private long size;
    private boolean closeReq;
    private DownloadThread thread;

    @Override
    public void sendMsg(String str) {
        msgQueue.add(str);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void recvMsg() {
        if (msgQueue.isEmpty()) {
            return;
        }
        String msg = msgQueue.poll();
        if (msg.compareTo("CLOSE @CODE: [fuckent]") == 0) {
            try {
                this.conn.close();
                this.oS.close();
                //this.f.
            } catch (IOException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            System.out.println("Closing thread");
            closeThread();
            Thread.currentThread().stop();

        }
        /* More here! */
        // SET RATE LIMT
        //
        if (msg.matches("LIMITRATE [-+]?[0-9]+")) {
            String[] lst = msg.split(" ");
            int tempRate = Integer.valueOf(lst[1]);

            out.format("DOWNLOADRATE %d\n", tempRate).flush();
        }

    }

    public void Run() {
        String line = client.serverPI.download(fileID);

        if (line.matches("DOWNLOAD [^ ]+ [0-9]+ [0-9a-z]+( [^ ]+)+")) {

            String[] lst = line.split(" ");

            try {
                this.fileName = java.net.URLDecoder.decode(lst[1], "ISO-8859-1");// + (this.client.port -
                // 1235);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(
                        Level.SEVERE, null, ex);
                fileName = "[unknown]";
            }
            this.fileSize = new Long(lst[2]).longValue();
            this.fileHash = lst[3];
            this.clientAddr = lst[4 + (new Random()).nextInt(lst.length - 4)];
            try {
                if (!client.dataManager.getFile(String.valueOf(fileID),
                        fileHash).next()) {
                    client.dataManager.addFile(fileID, fileName, fileSize, 0,
                            fileHash, "DOWNLOADING", "./" + fileName);
                    this.id = client.gui.model.getRowCount();
                    this.client.gui.model.addFile(new Files(fileID, this.fileName,
                            client.gui.formatSize(fileSize), null, 0,
                            clientAddr, fileHash, "DOWNLOADING"), this);

                } else {
                    if (client.dataManager.getStatus(fileID).compareTo(
                            "PAUSED") != 0) {
                        return;
                    }
                    
                    int i = 0;
                    while (i < client.gui.model.getRowCount()) {
                        if ((Integer) client.gui.model.getValueAt(i, 0) == fileID) {
                            break;
                        }
                        i++;
                    }
                    this.id = i;
                    client.gui.model.setValueAt("DOWNLOADING", this.id, 7);
                    client.dataManager.updateStatus(fileID, "DOWNLOADING");
                    client.gui.model.setSwingWorker(i, this);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(
                        Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null,
                        "This file had already existed on your PC", "ERROR", 0);
                return;
            }

        } else if (line.matches("DOWNLOAD ERROR 1")) {
            JOptionPane.showMessageDialog(null,
                    "This file doesn't exists on server", "Error", 0);
            return;
        } else if (line.matches("DOWNLOAD ERROR 2")) {
            JOptionPane.showMessageDialog(null, "Nobody is sharing this file",
                    "Error", 0);
            return;
        }

        f = new File(fileName);

        try {
            oS = new RandomAccessFile(f, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE,
                    null, ex);
            return;
        }

        System.out.println("Thread download start!");
        String[] lst;
        line = null;

        try {
            this.conn = new Socket(clientAddr, 1235);// (1236 + 1235) -
            // this.client.port);
            // conn.setSoTimeout(10000);
            conn.setKeepAlive(false);
            conn.setSoTimeout(5000);
            conn.setReuseAddress(false);

            this.in = new InputStreamReader(conn.getInputStream());
            // this.BIS = new BufferedInputStream(conn.getInputStream());

            this.reader = new BufferedReader(in);
            this.out = new PrintWriter(conn.getOutputStream());

            System.out.println("Connect to other client");
            out.format("DOWNLOAD %d %s %d\n", this.fileID, this.fileHash,
                    this.pos).flush();

            line = reader.readLine();
            System.out.println("res: " + line);
            if (line.matches("OK [0-9]+")) {
                lst = line.split(" ");
                oS.seek(pos);
                this.fileSize = Long.valueOf(lst[1]);
                oS.setLength(this.fileSize);

                System.out.println("Download started!");
                byte[] buff = new byte[4096];

                this.t = System.currentTimeMillis();
                this.c = this.totalCount;
                time = System.nanoTime();
                new Thread(new CaluRate()).start();
                new Thread(new SaveCurSize()).start();

                while (totalCount < fileSize) {
                    this.recvMsg();
                    count = conn.getInputStream().read(buff);

                    if (count < 0) {
                        throw new IOException();
                    }
                    oS.write(buff, 0, count);

                    totalCount += count;

                    publish(new ThreadInfo(Long.valueOf(
                            totalCount * 100 / fileSize).intValue(),
                            this.getRate()));
                }

                conn.close();
                oS.close();

                client.gui.model.setValueAt("CHECKING", this.id, 7);
                client.gui.model.setValueAt(null, this.id, 3);
                client.gui.model.setValueAt(null, this.id, 5);
                client.dataManager.updateCurrentSize(fileID, fileSize);
                // client.gui.model.setValueAt("SEEDED", this.id, 6);
                String path = "./" + fileName;
                String hashcheck = null;
                try {
                    hashcheck = getMD5Hash(path);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(DownloadThread.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                client.gui.model.setValueAt(100, this.id, 4);
                publish(new ThreadInfo(-1, -1));
                System.out.println("Finish download file");
                if (hashcheck.equals(client.dataManager.getfileHash(fileID))) {
                    client.dataManager.updateCurrentSize(fileID, totalCount);
                    client.gui.shareFile(this.id);
                } else {
                    System.out.println("File is not correct");
                    JOptionPane.showMessageDialog(null, "File is not correct",
                            "Error", 0);
                    client.dataManager.updateStatus(fileID, "PAUSED");
                    client.dataManager.updateCurrentSize(fileID, 0);
                    client.gui.model.setValueAt("PAUSED", this.id, 7);
                    client.gui.model.setValueAt("[unknown]", this.id, 5);
                }
            } else {

                this.closeThread();
                System.out.println("DOWNLOAD ERROR!");
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (IOException ex) {
            closeThread();
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

    public DownloadThread(int fileID, long pos, Client aThis) {
        this.client = aThis;
        this.pos = pos;
        totalCount = pos;
        this.fileID = fileID;
        msgQueue = new ConcurrentLinkedQueue<String>();
    }

    public void setRate() {
        long time1 = System.nanoTime();
        long dtime = time1 - time;
        // if (time != 0) {
        time = time1;
        rate = (int) (count * 1000 / (dtime / 1000));
        // }
        time = time1;
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeThread() {
        publish(new ThreadInfo(-1, -1));

        client.dataManager.updateCurrentSize(fileID, totalCount);
        client.dataManager.updateStatus(fileID, "PAUSED");
        client.gui.model.setValueAt("PAUSED", this.id, 7);
        client.gui.model.setValueAt(null, this.id, 5);
        client.gui.model.setValueAt(null, this.id, 3);
        // client.gui.fileTable.repaint();
        // client.gui.model.
        System.out.println("Closed thread");
        // client.threadManager.removeThread(this.id);
    }

    @Override
    public Long getcurSize() {
        return this.totalCount;
    }

    @Override
    public long getRate() {
        return rate;
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getClientAddr() {
        // throw new UnsupportedOperationException("Not supported yet.");
        return this.clientAddr;
    }

    @Override
    protected void process(java.util.List<ThreadInfo> c) {
        int i = 0;

        while (i < client.gui.model.getRowCount()) {
            int k = (Integer) client.gui.model.getValueAt(i, 0);
            if (k == this.fileID) {
                if (c.get(c.size() - 1).getP() < 0) {
                    // client.gui.model.setValueAt(0, i, 4);
                    client.gui.model.setValueAt(null, i, 3);
                } else {
                    client.gui.model.setValueAt(c.get(c.size() - 1).getP(), i,
                            4);
                    if (c.get(c.size() - 1).getRate() >= 0) {
                        client.gui.model.setValueAt(display(c.get(c.size() - 1).getRate()), i, 3);
                    } else {
                        client.gui.model.setValueAt(null, i, 3);
                    }

                }
            }
            i++;
        }
    }

    private String display(long rate) {
        if (rate < 1024) {
            return rate + " kB/s";
        } else {
            return (float) (rate * 100 / 1024) / 100.0 + " MB/s";
        }
    }

    @Override
    protected Integer doInBackground() throws Exception {
        this.thread = this;
        this.Run();
        return 0;
    }

    private class CaluRate implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    caluRate();
                    Thread.sleep(600);
                    if (thread.isCancelled() || thread.isDone()) {
                        System.out.println("CaluRate stopped!");
                        return;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(UploadThread.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class SaveCurSize implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                    if (thread.isCancelled() || thread.isDone()) {
                        System.out.println("SaveCurSize stopped!");
                        return;
                    }
                    if (client.dataManager.getcurSize(fileID) < totalCount) {
                        client.dataManager.updateCurrentSize(fileID, totalCount);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(UploadThread.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
    }

    private String getMD5Hash(String file) throws NoSuchAlgorithmException {
        {
            client.gui.model.setValueAt("CHECKING", this.id, 7);
            FileInputStream fis = null;
            try {
                File f = new File(file);
                this.size = f.length();
                System.out.println("File size: " + this.size);
                // f.
                MessageDigest md = MessageDigest.getInstance("MD5");
                fis = new FileInputStream(file);
                // this.size = fis.getChannel().size();
                byte[] dataBytes = new byte[1024];
                int nread = 0;
                long total = 0;
                while ((nread = fis.read(dataBytes)) != -1) {
                    total += nread;
                    this.recvMsg();
                    if (this.closeReq) {
                        return null;
                    }

                    publish(new ThreadInfo(Long.valueOf(total * 100 / this.size).intValue(), -1));

                    md.update(dataBytes, 0, nread);
                }

                byte[] mdbytes = md.digest();
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                return sb.toString();
            } catch (IOException ex) {
                Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE,
                        null, ex);

            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE,
                        null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(SeedThread.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }

            return null;
        }

    }

    public void caluRate() {
        long t1 = System.currentTimeMillis();
        long deltaT = t1 - this.t;
        long c1 = this.totalCount;
        long deltaC = c1 - this.c;

        if (deltaT > 0) {
            this.rate = (this.rate*90 + 10*(deltaC * 1000 / deltaT / 1024))/100;
        }

        t = t1;
        c = c1;
    }
}

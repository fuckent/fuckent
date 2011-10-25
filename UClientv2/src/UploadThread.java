
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.sql.ResultSet;
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
class UploadThread extends ClientThread {

    private Socket conn;
    //private int speed;
    private Client client;
    private InputStreamReader in;
    private BufferedReader reader;
    private PrintWriter out;
    private long rate;
    //private boolean exitRequest;
    private ConcurrentLinkedQueue<String> msgQueue;
    private int fileID;
    private long totalCount;
    private int id;
    private long time;
    private long dtime;
    private int limitrate;
    // private int uplimitrate;
    //private int downlimitrate;
    private int returnrate;
    private int countByte;
    private int dRate;
    private int uRate;
    private int count;
    private long t;
    private long c;

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
            //client.dataManager.updateCurrentSize(fileID, totalCount);
            closeThread();
            //client.dataManager.updateStatus(fileID, "PAUSED");
            //client.threadManager.removeThread(fileID);
            Thread.currentThread().stop();
        } else if (msg.matches("DOWNLOADRATE [-+]?[0-9]+")) {
            String[] lst = msg.split(" ");
            this.dRate = new Integer(lst[1]).intValue();
            System.out.println("dRate: " + dRate);
        } else if (msg.matches("LIMITRATE [-+]?[0-9]+")) {
            String[] lst = msg.split(" ");
            this.uRate = new Integer(lst[1]).intValue();
        }
    }

    @Override
    public void sendMsg(String str) {
        msgQueue.add(str);
    }

    UploadThread(Socket conn, Client client) {
        System.out.println("DOWN REQ!");
        this.conn = conn;
        this.client = client;
        msgQueue = new ConcurrentLinkedQueue<String>();
        dtime = 0;
        limitrate = -1;
        returnrate = 0;
        countByte = 0;
        uRate = -1;
        dRate = -1;
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
        //int count;
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
            ResultSet rs;
            if (line.matches("DOWNLOAD [0-9]+ [0-9a-f]+ [0-9]+")) {
                String[] lst = line.split(" ");
                totalCount = Long.valueOf(lst[3]);
                rs = client.dataManager.getFile(lst[1], lst[2]);
                if (rs == null) {
                    out.print("ERROR\n");
                    out.flush();
                } else {
                    this.fileID = new Integer(lst[1]).intValue();
                    System.out.println("Sending file");
                    String location = client.dataManager.getFileLocaltion(lst[1]);
                    long fileSize = client.dataManager.getfileSize(fileID);
                    out.format("OK %d\n", fileSize).flush();
                    File f = new File(location);
                    iS = new RandomAccessFile(location, "r");
                    //System.out.println("totalCount: " + totalCount);
                    iS.seek(totalCount);

                    byte[] buf = new byte[4096];
                    int i = 0;
                    while (i < client.gui.model.getRowCount()) {
                        if ((Integer) client.gui.model.getValueAt(i, 0) == fileID) {
                            break;
                        }
                        i++;
                    }

                    this.id = i;
                    client.gui.model.setSwingWorker(this.id, this);
                    // client.threadManager.addThread(i, this);
                    client.gui.model.setValueAt("UPLOADING", this.id, 6);
                    client.gui.model.setValueAt(conn.getInetAddress().getHostAddress(), this.id, 4);

                    this.t = System.currentTimeMillis();
                    this.c = this.totalCount;
                    time = System.nanoTime();

                    new Thread(new CaluRate()).start();

                    while (totalCount < fileSize) {
                        if (this.reader.ready()) {
                            line = reader.readLine();
                            System.out.println("Down REQ: " + line);
                            this.sendMsg(line);
                        }

                        this.recvMsg();

                        count = iS.read(buf);
                        totalCount += count;
                        conn.getOutputStream().write(buf, 0, count);
                        conn.getOutputStream().flush();
                        this.limitRate();
                        publish(new ThreadInfo(Long.valueOf(totalCount * 100 / fileSize).intValue(), this.getRate()));
                    }
                    conn.getOutputStream().flush();
                    // this.closeThread();
                    client.gui.model.setValueAt("SHARING", id, 6);
                    client.gui.model.setValueAt(null, this.id, 4);
                    client.dataManager.updateCurrentSize(fileID, fileSize);
                    System.out.println("Finish upload file");
                    // client.threadManager.removeThread(fileID);
                }

            }
        } catch (IOException ex) {
            this.closeThread();
            System.err.println("Upload error.\nExit thread!");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ioe) {
                }
            }
            if (iS != null) {
                try {
                    iS.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
    /**
     *  Notice: Client in other side limit DOWNLOAD rate by sent RATE XXX, whereas
     *          this function is called by GUI to limit UPLOAD rate.
     */
    long tC = 0;

    public void caluRate() {
        long t1 = System.currentTimeMillis();
        long deltaT = t1 - this.t;
        long c1 = this.totalCount;
        long deltaC = c1 - this.c;

        if (deltaT > 0) {
            // System.out.println(deltaT + " -- " + deltaC);
            this.rate = deltaC * 1000 / deltaT / 1024;
        }

        this.t = t1;
        this.c = c1;
    }

    private void sleepNano(long time) {
        long tTime = System.nanoTime();
        long t1 = time + tTime;
        try {
            Thread.sleep(time / 1000000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UploadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (t1 > System.nanoTime()) {
            // do nothing
            //System.out.print
        }
    }

    public void limitRate() {
        long t1 = System.nanoTime();
        dtime = t1 - time;
        int tempRate = uRate;
        if (uRate < 0) {
            tempRate = dRate;
        } else if (dRate < uRate && dRate > 0) {
            tempRate = dRate;
        }

        if (tempRate > 0) {
            long truetime = (long) (count * 1000.0 / (1024 * tempRate) * 1000000);
            if (truetime > dtime) {
                this.sleepNano(2 * (truetime - dtime));
            }
        }

        time = t1;
    }

    @Override
    public void closeThread() {
        client.dataManager.updateCurrentSize(fileID, totalCount);
        client.dataManager.updateStatus(fileID, "SHARING");
        client.gui.model.setValueAt("SHARING", id, 6);
        client.gui.model.setValueAt(null, this.id, 4);
        // client.threadManager.removeThread(this.id);
    }

    @Override
    public synchronized long getRate() {
        /**
         * GUI call this function to get upload speed and then show on fileTable (GUI)
         */
        return rate;
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
    protected void process(java.util.List<ThreadInfo> c) {

        client.gui.model.setValueAt(c.get(c.size() - 1).getP(), this.id, 3);
        client.gui.model.setValueAt(c.get(c.size() - 1).getRate(), this.id, 2);

    }

    private class CaluRate implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    caluRate();
                    Thread.sleep(600);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UploadThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

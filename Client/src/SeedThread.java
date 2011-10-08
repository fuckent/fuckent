import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
class SeedThread extends ClientThread implements Runnable {

    private Client client;
    private String fileName;
    private String path;
    private long fileSize;
    private String fileHash;

    private String getMD5Hash(String file) {
        {
            FileInputStream fis = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                fis = new FileInputStream(file);
                byte[] dataBytes = new byte[1024];
                int nread = 0;
                while ((nread = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, nread);
                }

                byte[] mdbytes = md.digest();
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                return sb.toString();
            } catch (IOException ex) {
                Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE, null, ex);

            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return null;
        }

    }

    /* This function going to be run when thread start and when this function 
     *  exit then thread also quit! 
     * TODO: CODE HERE
     * 
     */
    @Override
    public void run() {

        /**
         * - Connect with Server via client.serverPI
         * - add File to client data via client.dataManager
         * - Change file status to SEEDING via client.dataManager
         * - Call client.threadManager to remove this thread from list when 
         *      finish
         * 
         */
        int fileID = 0;
        if (client.dataManager.haveFile(fileName, fileSize, path))
        {
            //JOptionPane.showMessageDialog(null, this);
            //JOptionPane.showMessageDialog(this.client, this, "Error", "This file had been seeded!");
                //String str1 = JOptionPane.showInputDialog(null, "Enter file ID: ", "Server for downloading", 1);

            return ;
        }
        client.dataManager.addFile(-1, fileName, fileSize, fileSize, "[unknown]", "SEEDING", path);
        this.fileHash = getMD5Hash(path);

        try {
            fileID = client.serverPI.seed(URLEncoder.encode(fileName, "ISO-8859-1"), fileSize, fileHash);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (fileID == 0) {
            System.err.println("File " + path + "exists");
            return;
        }
        
        client.dataManager.addFile(fileID, fileName, fileSize, fileSize, fileHash, "SEEDED", path);

        // TODO:

    }

    public SeedThread(Client client, String path) {
        File f = new File(path);

        this.client = client;
        this.fileName = f.getName();
        this.fileSize = f.length();
        this.path = path;

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getClientAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
                        
    }
}

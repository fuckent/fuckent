/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
/**
 *
 * @author thong
 */
class DownloadThread extends ClientThread implements Runnable{

    private Client client;
    private int fileID;

    @Override
    public void run() {
        if (client.dataManager.haveFile(fileID))
        {
            //JOptionPane.showMessageDialog(null, this);
            //JOptionPane.showMessageDialog(null, this, "Error", "This file had been seeded!");
            JOptionPane.showMessageDialog(null, "The file already exists on your PC", "ERROR", 0);
                //String str1 = JOptionPane.showInputDialog(null, "Enter file ID: ", "Server for downloading", 1);
            
            return ;
        }


        String download = client.serverPI.download(fileID);
        System.out.println(download);
       // int ID;
        String name;
        long size;
        String hash;
        String IP;
        String path = null;
        if (download.matches("DOWNLOAD [^ ]+ [^ ]+ [^ ]+ [^ ]+")) {
            String[] lst = download.split(" ");
            name = lst[1];
            size = new Long(lst[2]).longValue();
            hash = lst[3];
            IP = lst[4];
            System.out.println("addfile into database");
            try {
                //return new Integer(lst[1]).intValue();
                client.dataManager.addFile(fileID, java.net.URLDecoder.decode(name, "ISO-8859-1"), size, size, hash, "DOWNLOADING...", path);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("add file complete");
            System.out.println(IP);
     //       JFileChooser jFC = new javax.swing.JFileChooser();
     //       jFC.showSaveDialog(jFC);
      //      jFC.
    //if (jFC.getSelectedFile() != null) {
      //  System.out.println(jFC.getSelectedFile().getAbsolutePath());
        //this.seedFile(jFC.getSelectedFile().getAbsolutePath());
    //}
            return;
        }
        if(download.matches("DOWNLOAD ERROR 1")){
            JOptionPane.showMessageDialog(null, "The file don't exists on server", "ERROR", 0);
            return;
        }
        if(download.matches("DOWNLOAD ERROR 2")){
            JOptionPane.showMessageDialog(null, "Nobody is sharing the file", "ERROR", 0);
            return;
        }
    }

    public DownloadThread(Client client, int fileID) {
        this.client = client;
        this.fileID = fileID;
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

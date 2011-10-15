/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GUI.java
 *
 * Created on Sep 20, 2011, 8:50:09 PM
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author thong
 */
public class GUI extends javax.swing.JFrame {

    private static final Color evenColor = new Color(230, 230, 230);
    Client client;
    public final GUI gui;
    public final FileTableModel model = new FileTableModel();

    // TODO: CODE FROM HERE!!!
    private void downloadFile(int fileID, long pos) {

        System.out.println("User wants to download file: " + fileID);
        System.out.println("Started download thread");

        Thread t = new Thread(new DownloadThread(fileID, pos, this.client));
        t.start();
        drawTable();


        /*    TODO: MORE HERE! */


        /* ------------------- */
        //System.out.
        //client.dataManager.
        //client.serverPI.download(fileID, "okie");
    }

    private int seedFile(String path) {

        new Thread(new SeedThread(this.client, path)).start();
        //System.out.println(t.getPriority());

        // t.setDaemon(true);
        // t.start();


        System.out.println("User wants to seed file: " + path);
        drawTable();
        return 0;
    }

    private void shareFile() {

        int row = fileTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        int ID = (Integer) model.getValueAt(row, 0);//);
        String Hash = (String) model.getValueAt(row, 5);
        System.out.println("User wants to share file: " + ID);
        Boolean share = client.serverPI.share(ID, Hash);
        if (share) {
            client.dataManager.updateStatus(ID, "SHARING...");
            JOptionPane.showMessageDialog(null, "Share file success", null, 1);
        } else {
            JOptionPane.showMessageDialog(null, "Share file false", "ERROR", 0);
        }

    }

    private void unShareFile() {
        //System.out.print("User wants to unshare file: ");
        int row = fileTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        int ID = (Integer) model.getValueAt(row, 0);
        System.out.print("User wants to unshare file: " + ID);
        String Hash = (String) model.getValueAt(row, 5);
        //System.out.println("User wants to unshare file: " + ID);

        Boolean check = client.serverPI.unshare(ID, Hash);
        if (check) {
            client.dataManager.updateStatus(ID, "UNSHARED");
            JOptionPane.showMessageDialog(null, "Unshare file success", null, 1);
        } else {
            System.out.println("ERROR TO UNSHARE");
            JOptionPane.showMessageDialog(null, "Unshare file false", "ERROR", 0);
        }
    }

    private void closeThread(int fileID) {
        System.out.println("User wants to close thread processing file: " + fileID);
        client.threadManager.getThread(fileID).sendMsg("CLOSE @CODE: [fuckent]");
        // client.threadManager.removeThread(fileID);
    }

    private void resumeThread(int fileID) {

        if (client.dataManager.getStatus(fileID).compareTo("PAUSED") != 0) {
            return;
        }

        long currentSize = client.dataManager.getcurSize(fileID);

        downloadFile(fileID, currentSize);

    }

    private void limitRate(int fileID, int rate) {
        System.out.print("User wants to limit file: " + fileID + " rate to " + rate + "kB");
    }

    private int getRate(int fileID) {
        System.out.print("User wants to get limit rate of file: " + fileID);
        return 0;
    }

    public void drawTable() {
        int fileID;
        String fileName;
        long fileSize;
        long curSize;
        String fileStatus;
        String clientAddr;
        String hash;
        int rate;
        ClientThread a;
        int slr = fileTable.getSelectedRow();
        int i;
        //System.out.println("Draw");
        try {
            FileTableModel model = (FileTableModel) fileTable.getModel();
            model.setRowCount(0);
            Vector<Integer> v = client.dataManager.getAllFile();
            for (i = 0; i < v.size(); i++) {
                fileID = v.get(i);
                fileName = client.dataManager.getfileName(fileID);
                fileSize = client.dataManager.getfileSize(fileID);
                hash = client.dataManager.getfileHash(fileID);

                if (client.threadManager.getThread(fileID) != null) {
                    curSize = client.threadManager.getThread(fileID).getcurSize();
                    //client.dataManager.updateCurrentSize(fileID, curSize);
                } else {
                    curSize = client.dataManager.getcurSize(fileID);
                }
                //client.dataManager.getcurSize(fileID);
                fileStatus = client.dataManager.getStatus(fileID);
                a = client.threadManager.getThread(fileID);
                if (a != null) {
                    clientAddr = a.getClientAddr();
                    rate = a.getRate();
                } else {
                    clientAddr = null;
                    rate = 0;
                }
                //   int rate = client.threadManager.getThread(fileID).getRate();
                 Integer done;
                if (fileStatus.matches("DOWNLOADING|UPLOADING"))
               done = Long.valueOf(curSize * 100 / fileSize).intValue();
                else done = 0;
                if (fileStatus.equals("SEEDING")) {
                    model.addRow(new Object[]{"*", fileName, String.valueOf(rate) + "kB", done, clientAddr, "[unknown]", "SEEDING"});
                } else {
                    model.addRow(new Object[]{fileID, fileName, String.valueOf(rate) + "kB", done, clientAddr, hash, fileStatus});
                }
            }
            // Get the ListSelectionModel of the JTable
            ListSelectionModel model1 = fileTable.getSelectionModel();

            // set the selected interval of rows. Using the "rowNumber"
            // variable for the beginning and end selects only that one row.
            model1.setSelectionInterval(slr, slr);
            // fileTable.
        } catch (Exception ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void updateCurSize() {
        int fileID;

        long curSize;

        int i;


        Vector<Integer> v = client.dataManager.getAllFile();
        for (i = 0; i < v.size(); i++) {
            fileID = v.get(i);
            if (client.threadManager.getThread(fileID) != null) {
                curSize = client.threadManager.getThread(fileID).getcurSize();
                client.dataManager.updateCurrentSize(fileID, curSize);
            }
        }
    }

    GUI(Client aThis) {



        initComponents();
        this.gui = this;
        client = aThis;
        javax.swing.Timer t = new javax.swing.Timer(1000, new ClockListener());
        //javax.swing.Timer t1 = new javax.swing.Timer(10000, new ClockListener1());
        new Thread(new UpdateCurSize()).start();
        //new Thread (new UpdateFileTable()).start();

        t.start();
        //t1.start();

        //drawTable();
        fileTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // Left mouse click
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Do something
                } // Right mouse click
                else if (SwingUtilities.isRightMouseButton(e)) {
                    // get the coordinates of the mouse click
                    Point p = e.getPoint();

                    // get the row index that contains that coordinate
                    int rowNumber = fileTable.rowAtPoint(p);

                    // Get the ListSelectionModel of the JTable
                    ListSelectionModel model = fileTable.getSelectionModel();

                    // set the selected interval of rows. Using the "rowNumber"
                    // variable for the beginning and end selects only that one row.
                    model.setSelectionInterval(rowNumber, rowNumber);
                    function.show(e.getComponent(), e.getX(), e.getY());


                }
            }
        });



    }

    private void deleteFile(int ID) {
        System.out.println("User want to delete file: " + ID);
        ClientThread t = client.threadManager.getThread(ID);
        if (t != null) t.sendMsg("CLOSE @CODE: [fuckent]");
        client.dataManager.removeFile(ID);
        drawTable();
    }

    private void closeAllThread() {
        client.threadManager.closeAllThreads();
        while (!client.threadManager.isEmpty()) {
            try {
                Thread.sleep(100);
                //Thread.yield();
            } catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.exit(0);
    }

    private class ClockListener implements ActionListener {

        public ClockListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            //System.out.println("Draw");
            drawTable();
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class UpdateFileTable implements Runnable {

        @Override
        public void run() {
            while (true) {
                drawTable();
                gui.repaint();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class UpdateCurSize implements Runnable {

        @Override
        public void run() {
            while (true) {
                updateCurSize();
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        function = new javax.swing.JPopupMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    //c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    // c.setForeground(getForeground());
                    c.setBackground((row%2==1)?evenColor:getBackground());
                }
                return c;
            }
        }
        ;
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        function.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        function.setFocusable(false);
        function.setRequestFocusEnabled(false);

        jMenuItem8.setText("Seed file");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        function.add(jMenuItem8);

        jMenuItem13.setText("Share file");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        function.add(jMenuItem13);

        jMenuItem14.setText("Unshare file");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        function.add(jMenuItem14);

        jMenuItem5.setText("Pause");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        function.add(jMenuItem5);

        jMenuItem10.setText("Download file");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        function.add(jMenuItem10);

        jMenuItem9.setText("Resume");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        function.add(jMenuItem9);

        jMenuItem6.setText("Delete");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        function.add(jMenuItem6);

        jMenuItem7.setText("Limit rate");
        function.add(jMenuItem7);

        jMenuItem11.setText("About");
        function.add(jMenuItem11);

        jMenuItem12.setText("Quit");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        function.add(jMenuItem12);

        function.getAccessibleContext().setAccessibleParent(fileTable);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Torrent - U");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        fileTable.setAutoscrolls(false);
        fileTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fileTable.setFillsViewportHeight(true);
        fileTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        fileTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fileTable.setShowHorizontalLines(false);
        fileTable.setShowVerticalLines(false);
        fileTable.getTableHeader().setReorderingAllowed(false);
        fileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileTableMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fileTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(fileTable);
        fileTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        TableColumn column =  fileTable.getColumnModel().getColumn(3);
        column.setCellRenderer(new ProgressRenderer());
        fileTable.setShowGrid(false);

        jMenu1.setText("File");

        jMenuItem1.setText("Seed file");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem4.setText("Download file");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem2.setText("Quit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Help");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem3.setText("About");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-668)/2, (screenSize.height-394)/2, 668, 394);
    }// </editor-fold>//GEN-END:initComponents

private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
// TODO add your handling code here:
    this.closeAllThread();
}//GEN-LAST:event_jMenuItem2ActionPerformed

private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
// TODO add your handling code here:
    this.closeAllThread();
}//GEN-LAST:event_jMenu2ActionPerformed

private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
// TODO add your handling code here:

    /*   GUI.About ab = new GUI.About();
    ab.setVisible(true);
    try {
    this.wait();
    } catch (InterruptedException ex) {
    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
    }*/
}//GEN-LAST:event_jMenuItem3ActionPerformed

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    // TODO add your handling code here:
    JFileChooser jFC = new javax.swing.JFileChooser();
    jFC.showDialog(jFC, null);
    if (jFC.getSelectedFile() != null) {
        System.out.println(jFC.getSelectedFile().getAbsolutePath());
        this.seedFile(jFC.getSelectedFile().getAbsolutePath());
    }

}//GEN-LAST:event_jMenuItem1ActionPerformed

private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
// TODO add your handling code here:

    String str = JOptionPane.showInputDialog(null, "Enter file ID: ", "Server for downloading", 1);
    
    if (str == null) 
            return ;

    this.downloadFile(new Integer(str).intValue(), 0);

}//GEN-LAST:event_jMenuItem4ActionPerformed

private void fileTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTableMouseReleased
// TODO add your handling code here:
}//GEN-LAST:event_fileTableMouseReleased

private void fileTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTableMouseClicked
}//GEN-LAST:event_fileTableMouseClicked

    private void fileTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTableMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_fileTableMousePressed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        // TODO add your handling code here:
        jMenuItem1ActionPerformed(evt);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        // TODO add your handling code here:
        this.closeAllThread();
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        // TODO add your handling code here:
        this.shareFile();
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        // TODO add your handling code here:
        this.unShareFile();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        int row = fileTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        int ID = (Integer) model.getValueAt(row, 0);
        this.closeThread(ID);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        // TODO add your handling code here:

        int row = fileTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        int ID = (Integer) model.getValueAt(row, 0);

        System.out.println("User wants to resume file: " + ID);
        this.resumeThread(ID);
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        // TODO add your handling code here:
        jMenuItem4ActionPerformed(evt);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        int row = fileTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        int ID = (Integer) model.getValueAt(row, 0);

        this.deleteFile(ID);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        System.out.println("Windows closing!");
        closeAllThread();

    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable fileTable;
    private javax.swing.JPopupMenu function;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

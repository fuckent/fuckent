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
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author thong
 */
public class GUI extends javax.swing.JFrame {

    private static final Color evenColor = new Color(245, 245, 245);
    private static final Color selectedColor = new Color(56, 114, 190);
    Client client;
    public final GUI gui;
    public final FileTableModel model = new FileTableModel();
    public final Executor executor = Executors.newCachedThreadPool();
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    private final TreeSet<Integer> deleteRowSet = new TreeSet<Integer>();

    // TODO: CODE FROM HERE!!!
    private void downloadFile(int fileID, long pos) {

        System.out.println("User wants to download file: " + fileID);
        System.out.println("Started download thread");
        DownloadThread t = new DownloadThread(fileID, pos, this.client);
        executor.execute(t);
        //t.start();
        //drawTable();


        /*    TODO: MORE HERE! */


        /* ------------------- */
        //System.out.
        //client.dataManager.
        //client.serverPI.download(fileID, "okie");
    }

    private int seedFile(String path) {
        System.out.println("User wants to seed file: " + path);

        SeedThread t = new SeedThread(this.client, path);
        this.executor.execute(t);
        //System.out.println(t.getPriority());

        // t.setDaemon(true);
        // t.start();


        // drawTable();
        return 0;
    }

    private void shareFile() {

        new Thread(new Runnable() {

            public void run() {

                int row = fileTable.getSelectedRow();
                if (row < 0) {
                    return;
                }
                //DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                row = fileTable.convertRowIndexToModel(row);
                int ID = (Integer) model.getValueAt(row, 0);//);
                String Hash = (String) model.getValueAt(row, 5);
                System.out.println("User wants to share file: " + ID);
                Boolean share = client.serverPI.share(ID, Hash);
                if (share) {
                    client.dataManager.updateStatus(ID, "SHARING");
                    model.setValueAt("SHARING", row, 6);
                    JOptionPane.showMessageDialog(null, "Share file successful", null, 1);
                } else {
                    JOptionPane.showMessageDialog(null, "Can't share this file", "Error", 0);
                }
            }
        }).start();

    }

    private void unShareFile() {
        new Thread(new Runnable() {

            public void run() {
                //System.out.print("User wants to unshare file: ");
                int row = fileTable.getSelectedRow();
                if (row < 0) {
                    return;
                }
                //DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
                row = fileTable.convertRowIndexToModel(row);
                int ID = (Integer) model.getValueAt(row, 0);

                System.out.print("User wants to unshare file: " + ID);
                String Hash = (String) model.getValueAt(row, 5);
                //System.out.println("User wants to unshare file: " + ID);

                Boolean check = client.serverPI.unshare(ID, Hash);
                if (check) {
                    client.dataManager.updateStatus(ID, "SEEDED");
                    model.setValueAt("SEEDED", row, 6);
                    JOptionPane.showMessageDialog(null, "Unshare successful!", null, 1);
                } else {
                    System.out.println("ERROR TO UNSHARE");
                    JOptionPane.showMessageDialog(null, "Can't unshare this file", "Error", 0);
                }
            }
        }).start();

    }

    private void shutdownThread(int ID) {
        //executor.
        ID = fileTable.convertRowIndexToModel(ID);
        int fileID = (Integer) model.getValueAt(ID, 0);
        System.out.println("User wants to close thread processing file: " + fileID);
        ClientThread t = (ClientThread) model.getSwingWorker(ID);
        if (t != null) {
            t.sendMsg("CLOSE @CODE: [fuckent]");
            while (!t.isDone()) {
                Thread.yield();
            }
        }
        // client.threadManager.removeThread(fileID);
    }

    private void closeThread(int ID) {
        //executor.
        ID = fileTable.convertRowIndexToModel(ID);
        int fileID = (Integer) model.getValueAt(ID, 0);
        System.out.println("User wants to close thread processing file: " + fileID);
        ClientThread t = (ClientThread) model.getSwingWorker(ID);
        if (t != null) {
            t.sendMsg("CLOSE @CODE: [fuckent]");
        }
        // client.threadManager.removeThread(fileID);
    }

    private void resumeThread(int ID) {
        ID = fileTable.convertRowIndexToModel(ID);
        int fileID = (Integer) model.getValueAt(ID, 0);
        System.out.println("User wants to resume file: " + fileID);

        // if (client.dataManager.getStatus(fileID).compareTo("PAUSED") != 0) {
        //    return;
        //}

        long currentSize = client.dataManager.getcurSize(fileID);

        downloadFile(fileID, currentSize);

    }

    private void limitRate(int row, int rate) {
        int ID = (Integer) model.getValueAt(row, 0);
        ClientThread t = (ClientThread) model.getSwingWorker(row);
        t.sendMsg(String.format("LIMITRATE %d", rate));

        System.out.print("User wants to limit file: " + ID + " rate to " + rate + "kB\n");
        // client.threadManager.getThread(fileID).sendMsg();
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
        int i;

        Vector<Integer> v = client.dataManager.getAllFile();
        for (i = 0; i < v.size(); i++) {
            fileID = v.get(i);
            fileName = client.dataManager.getfileName(fileID);
            fileSize = client.dataManager.getfileSize(fileID);
            hash = client.dataManager.getfileHash(fileID);

            // if (client.threadManager.getThread(fileID) != null) {
            //    curSize = client.threadManager.getThread(fileID).getcurSize();
            //client.dataManager.updateCurrentSize(fileID, curSize);
            //  } else {
            curSize = client.dataManager.getcurSize(fileID);
            //  }
            //client.dataManager.getcurSize(fileID);
            fileStatus = client.dataManager.getStatus(fileID);
            //  a = client.threadManager.getThread(fileID);
            //if (a != null) {
            //  clientAddr = a.getClientAddr();
            // rate = a.getRate();
            //} else {
            //  clientAddr = null;
//                    rate = 0;
            //              }
            //   int rate = client.threadManager.getThread(fileID).getRate();
            Integer speed = Long.valueOf(curSize * 100 / fileSize).intValue();
            if (fileStatus != null) {
                if (fileStatus.matches("DOWNLOADING")) {
                    client.dataManager.updateStatus(fileID, "PAUSED");
                    fileStatus = "PAUSED";
                } else if (fileStatus.matches("UPLOADING")) {
                    client.dataManager.updateStatus(fileID, "SHARING");
                    fileStatus = "SHARING";
                } else if (fileStatus.matches("SHARING")) {
                    client.serverPI.share(fileID, hash);
                }
            }
            Files f = new Files(Integer.valueOf(fileID), fileName, 0 + "kB", speed, null, hash, fileStatus);
            model.addFile(f, null);
        }
    }
    // Get the ListSelectionModel of the JTable

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

        new Thread(new UpdateCurSize()).start();

        drawTable();
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
                    fileTable.clearSelection();

                    // Get the ListSelectionModel of the JTable
                    ListSelectionModel model = fileTable.getSelectionModel();

                    // set the selected interval of rows. Using the "rowNumber"
                    // variable for the beginning and end selects only that one row.
                    model.setSelectionInterval(rowNumber, rowNumber);
                    popupSetup();
                    function.show(e.getComponent(), e.getX(), e.getY());


                }
            }
        });



    }

    private void popupSetup() {
        this.seedMenu.setEnabled(true);
        this.downloadMenu.setEnabled(true);
        this.shareMenu.setEnabled(true);
        this.unShareMenu.setEnabled(true);
        this.pauseMenu.setEnabled(true);
        this.resumeMenu.setEnabled(true);
        this.deleteMenu.setEnabled(true);
        this.limitRateMenu.setEnabled(false);
        int id = fileTable.getSelectedRow();
        //int 
        if (id < 0) {
            this.resumeMenu.setEnabled(false);
            this.pauseMenu.setEnabled(false);
            this.shareMenu.setEnabled(false);
            this.unShareMenu.setEnabled(false);
            this.deleteMenu.setEnabled(false);

            return;
        }
        id = fileTable.convertRowIndexToModel(id);
        String str = (String) model.getValueAt(id, 6);
        if (str.matches("DOWNLOADING|UPLOADING")) {
            this.seedMenu.setEnabled(false);
            this.shareMenu.setEnabled(false);
            this.unShareMenu.setEnabled(false);
            this.downloadMenu.setEnabled(false);
            this.limitRateMenu.setEnabled(true);
            this.resumeMenu.setEnabled(false);
        } else if (str.matches("SEEDING")) {
            this.seedMenu.setEnabled(false);
            this.downloadMenu.setEnabled(false);
            this.pauseMenu.setEnabled(false);
            this.resumeMenu.setEnabled(false);
            this.shareMenu.setEnabled(false);
            this.unShareMenu.setEnabled(false);
        } else if (str.matches("SEEDED")) {
            this.seedMenu.setEnabled(false);
            this.downloadMenu.setEnabled(false);
            this.pauseMenu.setEnabled(false);
            this.resumeMenu.setEnabled(false);
            //this.shareMenu.setEnabled(false);
            this.unShareMenu.setEnabled(false);
        } else if (str.matches("SHARING")) {
            this.seedMenu.setEnabled(false);
            this.downloadMenu.setEnabled(false);
            this.pauseMenu.setEnabled(false);
            this.resumeMenu.setEnabled(false);
            this.shareMenu.setEnabled(false);
            //this.unShareMenu.setEnabled(false);  
        } else if (str.matches("PAUSED")) {
            this.seedMenu.setEnabled(false);
            this.downloadMenu.setEnabled(false);
            this.pauseMenu.setEnabled(false);
            //this.resumeMenu.setEnabled(false);
            this.shareMenu.setEnabled(false);
            this.unShareMenu.setEnabled(false);
        }


    }

    private void deleteFile(int ID) {
        ID = fileTable.convertRowIndexToModel(ID);
        int fileID = (Integer) model.getValueAt(ID, 0);
        model.setValueAt(-1, ID, 0);
        System.out.println("User want to delete file: " + ID);
        final RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {

            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return !deleteRowSet.contains(entry.getIdentifier());
            }
        };
        
        ClientThread worker = (ClientThread) model.getSwingWorker(ID);
        
        worker.sendMsg("CLOSE @CODE: [fuckent]");

        deleteRowSet.add(ID);
        sorter.setRowFilter(filter);
        fileTable.repaint();
        /*        ClientThread t = (ClientThread) model.getSwingWorker(ID);
        if (t != null) {
        t.sendMsg("CLOSE @CODE: [fuckent]");
        } */
        client.dataManager.removeFile(fileID);
        //drawTable();
    }

    private void closeAllThread() {
        int i = 0;
        while (i < fileTable.getRowCount()) {
            this.shutdownThread(i);
            i++;
        }
        System.exit(0);
    }

    private void about() {
        System.out.println("User wants to see about dialog :-)");
        
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
                    Thread.sleep(5000);
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
        seedMenu = new javax.swing.JMenuItem();
        downloadMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        shareMenu = new javax.swing.JMenuItem();
        unShareMenu = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        pauseMenu = new javax.swing.JMenuItem();
        resumeMenu = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        deleteMenu = new javax.swing.JMenuItem();
        limitRateMenu = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();
        quitMenu = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    //c.setForeground(getSelectionForeground());
                    //c.setBackground(selectedColor);
                    // c.setForeground(new Color(255,255,255));
                }else{
                    //c.setForeground(getForeground());
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

        seedMenu.setText("Seed file");
        seedMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seedMenuActionPerformed(evt);
            }
        });
        function.add(seedMenu);

        downloadMenu.setText("Download file");
        downloadMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadMenuActionPerformed(evt);
            }
        });
        function.add(downloadMenu);
        function.add(jSeparator1);

        shareMenu.setText("Share file");
        shareMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shareMenuActionPerformed(evt);
            }
        });
        function.add(shareMenu);

        unShareMenu.setText("Unshare file");
        unShareMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unShareMenuActionPerformed(evt);
            }
        });
        function.add(unShareMenu);
        function.add(jSeparator2);

        pauseMenu.setText("Pause");
        pauseMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseMenuActionPerformed(evt);
            }
        });
        function.add(pauseMenu);

        resumeMenu.setText("Resume");
        resumeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resumeMenuActionPerformed(evt);
            }
        });
        function.add(resumeMenu);
        function.add(jSeparator3);

        deleteMenu.setText("Delete");
        deleteMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuActionPerformed(evt);
            }
        });
        function.add(deleteMenu);

        limitRateMenu.setText("Limit rate");
        limitRateMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limitRateMenuActionPerformed(evt);
            }
        });
        function.add(limitRateMenu);

        aboutMenu.setText("About");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        function.add(aboutMenu);

        quitMenu.setText("Quit");
        quitMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuActionPerformed(evt);
            }
        });
        function.add(quitMenu);

        function.getAccessibleContext().setAccessibleParent(fileTable);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Torrent - Uv2");
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
        fileTable.setRowSorter(sorter);
        fileTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fileTable.setShowHorizontalLines(false);
        fileTable.setShowVerticalLines(false);
        fileTable.getTableHeader().setReorderingAllowed(false);
        fileTable.setUpdateSelectionOnSort(false);
        fileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                fileTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileTableMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileTableMouseClicked(evt);
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-786)/2, (screenSize.height-294)/2, 786, 294);
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
    this.about();
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

    if (str == null) {
        return;
    }

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

    private void seedMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seedMenuActionPerformed
        // TODO add your handling code here:
        jMenuItem1ActionPerformed(evt);
    }//GEN-LAST:event_seedMenuActionPerformed

    private void quitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuActionPerformed
        // TODO add your handling code here:
        this.closeAllThread();
    }//GEN-LAST:event_quitMenuActionPerformed

    private void shareMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shareMenuActionPerformed
        // TODO add your handling code here:
        this.shareFile();
    }//GEN-LAST:event_shareMenuActionPerformed

    private void unShareMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unShareMenuActionPerformed
        // TODO add your handling code here:
        this.unShareFile();
    }//GEN-LAST:event_unShareMenuActionPerformed

    private void pauseMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseMenuActionPerformed
        int row = fileTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        this.closeThread(row);
    }//GEN-LAST:event_pauseMenuActionPerformed

    private void resumeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resumeMenuActionPerformed
        // TODO add your handling code here:

        int row = fileTable.getSelectedRow();
        if (row < 0) {
            return;
        }

        this.resumeThread(row);
    }//GEN-LAST:event_resumeMenuActionPerformed

    private void downloadMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadMenuActionPerformed
        // TODO add your handling code here:
        jMenuItem4ActionPerformed(evt);
    }//GEN-LAST:event_downloadMenuActionPerformed

    private void deleteMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuActionPerformed
        int row = fileTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        // DefaultTableModel model = (DefaultTableModel) fileTable.getModel();
        //  int ID = (Integer) model.getValueAt(row, 0);

        this.deleteFile(row);
    }//GEN-LAST:event_deleteMenuActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        System.out.println("Windows closing!");
        closeAllThread();

    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void limitRateMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limitRateMenuActionPerformed
        String limit = JOptionPane.showInputDialog(null, "Enter limit rate(kb): ", null, 1);
        int row = fileTable.getSelectedRow();
        row = fileTable.convertRowIndexToModel(row);

//          int ID = (Integer) model.getValueAt(row, 0);
        this.limitRate(row, new Integer(limit).intValue());
    }//GEN-LAST:event_limitRateMenuActionPerformed

private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuActionPerformed
    this.about();
}//GEN-LAST:event_aboutMenuActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JMenuItem deleteMenu;
    private javax.swing.JMenuItem downloadMenu;
    public javax.swing.JTable fileTable;
    private javax.swing.JPopupMenu function;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem limitRateMenu;
    private javax.swing.JMenuItem pauseMenu;
    private javax.swing.JMenuItem quitMenu;
    private javax.swing.JMenuItem resumeMenu;
    private javax.swing.JMenuItem seedMenu;
    private javax.swing.JMenuItem shareMenu;
    private javax.swing.JMenuItem unShareMenu;
    // End of variables declaration//GEN-END:variables
}

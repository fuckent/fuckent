// Copied
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//http://terai.xrea.jp/Swing/TableCellProgressBar.html
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class FileTableModel extends DefaultTableModel {

    private static final ColumnContext[] columnArray = {
        new ColumnContext("ID", Integer.class, false),
        new ColumnContext("Name", String.class, false),
        new ColumnContext("Rate", String.class, false),
        new ColumnContext("Progress", Integer.class, false),
        new ColumnContext("Client Addr", String.class, false),
        new ColumnContext("Hash", String.class, false),
        new ColumnContext("Status", String.class, false)
    };
    private final Map<Integer, SwingWorker> swmap = new HashMap<Integer, SwingWorker>();
    private final Map<Integer, Files> fmap = new HashMap<Integer, Files>();
    private int number = 0;

    public void addFile(Files t, SwingWorker worker) {
        Object[] obj = {t.getID(), t.getName(), t.getRate(), t.getProgress(),
            t.getClientAddr(), t.getHash(), t.getStatus()};
       // t.setid(number);
        super.addRow(obj);
        swmap.put(number, worker);
        // fmap.put(number,t);
        number++;
    }

    public synchronized void setSwingWorker(int number , SwingWorker worker) {
        swmap.put(number, worker);
      //  this.
    }    
    public synchronized SwingWorker getSwingWorker(int identifier) {
       // Integer key = (Integer) getValueAt(identifier, 0);
        return swmap.get(identifier);
      //  this.
    }

    public Files getFile(int identifier) {
        return fmap.get(identifier);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return columnArray[col].isEditable;
    }

    @Override
    public Class<?> getColumnClass(int modelIndex) {
        return columnArray[modelIndex].columnClass;
    }

    @Override
    public int getColumnCount() {
        return columnArray.length;
    }

    @Override
    public String getColumnName(int modelIndex) {
        return columnArray[modelIndex].columnName;
    }

    private static class ColumnContext {

        public final String columnName;
        public final Class columnClass;
        public final boolean isEditable;

        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}


class Files {

    private int id;
    private Integer ID;
    private String Name;
    private String Rate;
    private Integer Progress;
    private String ClientAddr;
    private String Hash;
    private String Status;

    public Files(Integer ID, String Name, String Rate, Integer Progress, String ClientAddr, String Hash, String Status) {
        this.ID = ID;
        this.Name = Name;
        this.Rate = Rate;
        this.Progress = Progress;
        this.ClientAddr = ClientAddr;
        this.Hash = Hash;
        this.Status = Status;

    }

    public void setName(String str) {
        Name = str;
    }

    public void setHash(String str) {
        Hash = str;
    }

    public void setStatus(String str) {
        Status = str;
    }

    public void setProgress(Integer str) {
        Progress = str;
    }

    public void setID(Integer str) {
        ID = str;
    }

    public void setRate(String str) {
        Rate = str;
    }

    public String getName() {
        return Name;// = str;
    }

    public String getHash() {
        return Hash;// = str;
    }

    public String getStatus() {
        return Status;// = str;
    }

    public Integer getProgress() {
        return Progress;// = str;
    }

    public Integer getID() {
        return ID;// = str;
    }

    public String getRate() {
        return Rate;// = str;
    }

    public String getClientAddr() {
        return ClientAddr;
    }

    public void setClientAddr(String str) {
        ClientAddr = str;
    }

    void setid(int number) {
        this.id = number;
    }
    public int getid() {
        return this.id;
    }
   
}

class ProgressRenderer extends DefaultTableCellRenderer {

    private final JProgressBar b = new JProgressBar(0, 100);

    public ProgressRenderer() {
        super();
        setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        Integer i = (Integer) value;
        String text = "Done";
        if (i < 0) {
            text = "Canceled";
        } else if (i < 100) {
            b.setValue(i);
            return b;
        }
        super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        return this;
    }
}

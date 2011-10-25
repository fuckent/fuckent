
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
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
public class DataManager {

    private Statement statement;
    private Connection connection;
    private int nCount = -1;
    private ResultSet rs;
    
    public synchronized ResultSet getFile(String fileID, String fileHash) {
        try {
            rs = statement.executeQuery(String.format("select * from fileManager where fileID = %s and fileHash = '%s'",fileID, fileHash));
            return rs;
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public synchronized String getFileLocaltion(String fileID) {
        try {
            rs = statement.executeQuery(String.format("select fileLocation from fileManager where fileID = %s",fileID));
            if (rs.next()) return rs.getString("fileLocation"); 
            else return null;
                    
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }    
   public synchronized long getcurSize(int fileID){
        try{
           ResultSet rs= statement.executeQuery(String.format("select curSize from FileManager where fileID=%d",fileID));
           if(rs.next()){
               return rs.getLong("curSize");
           }
           else  return -1;
        }
      catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       return -1;
    }     
   
   public synchronized long getfileSize(int fileID){
        try{
           ResultSet rs= statement.executeQuery(String.format("select fileSize from FileManager where fileID=%d",fileID));
           if(rs.next()){
               return rs.getLong("fileSize");
           }
           else  return -1;
        }
      catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       return -1;
    }        

    public synchronized Vector<Integer> getAllFile() {
        String fileStatus;
        long curSize;
        String hash;
        long fileSize;
        String fileName;
        int ID;

        Vector<Integer> v = new Vector<Integer>();
        try {
            rs = statement.executeQuery("select fileID from FileManager");
            while (rs.next()) {
                ID = rs.getInt("fileID");
                v.add(ID);
            }

            //return srs;
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return v;

    }

    public synchronized Boolean haveFile(String fileName, long fileSize, String fileLocation) {
        try {
            return statement.executeQuery(String.format("select * from FileManager where fileName = '%s' and fileSize = %d and fileLocation = '%s'", fileName, fileSize, fileLocation)).next();
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public synchronized void addFile(int fileID, String fileName, long fileSize, long curSize, String hash, String fileStatus, String fileLocation) {
        try {
            if (fileID < 0) {
                nCount--;
                statement.executeUpdate(String.format("insert into FileManager values (null, '%s', %d, %d, '%s', '%s', '%s')", fileName, fileSize, curSize, hash, fileStatus, fileLocation));

            } else {
                statement.executeUpdate(String.format("delete from FileManager where fileName = '%s' and fileSize = %d and curSize = %d and fileHash = '[unknown]' and status = 'SEEDING' and fileLocation = '%s'", fileName, fileSize, curSize, fileLocation));
                statement.executeUpdate(String.format("insert into FileManager values (%d, '%s', %d, %d, '%s', '%s', '%s')", fileID, fileName, fileSize, curSize, hash, fileStatus, fileLocation));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void updateStatus(int fileID, String status) {
        try {
            statement.executeUpdate(String.format("update FileManager set status = '%s' where fileID = %d", status, fileID));
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized String getStatus(int fileID) {
        try {
            ResultSet rs = statement.executeQuery(String.format("select status from FileManager where fileID = %d", fileID));
            if (rs.next()) {
                return rs.getString("status");
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    
    public synchronized String getfileName(int fileID) {
        try {
            ResultSet rs = statement.executeQuery(String.format("select fileName from FileManager where fileID=%d", fileID));
            if (rs.next()) {
                return rs.getString("fileName");
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

   
    
    public synchronized String getfileHash(int fileID) {
        try {
            ResultSet rs = statement.executeQuery(String.format("select fileHash from FileManager where fileID=%d", fileID));
            if (rs.next()) {
                return rs.getString("fileHash");
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public synchronized void updateCurrentSize(int fileID, long fileSize) {
        try {
            statement.executeUpdate(String.format("update FileManager set curSize = %s where fileID = %d", fileSize, fileID));
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void removeFile(int fileID) {
        try {
            statement.executeUpdate("delete from FileManager where fileID = " + fileID);
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Get All files from database
     * 
     * @return 
     */
    public synchronized ResultSet getFileList() {
        try {
            return statement.executeQuery("select * from FileManager");
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public DataManager(Client c) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.out.println("Can't found sqlite library");
            System.exit(0);
        }
        // testing
        connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:client" + (c.port - 1235) + ".db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            /* Create a new database structure */
            statement.executeUpdate("create table if not exists FileManager (fileID INTEGER PRIMARY KEY, fileName VARCHAR, fileSize INTEGER, curSize INTERGER, fileHash VARCHAR, status VARCHAR, fileLocation VARCHAR)");

        } catch (SQLException e) {
            // TODO: CODE HERE!!

            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // connection close failed.
            System.err.println(e);
        }

        //do finalization here
        super.finalize(); //not necessary if extending Object.
    }
}

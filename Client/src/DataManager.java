
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void addFile(int fileID, String fileName, int fileSize, int curSize, String hash, String fileStatus) {
        try {
                statement.executeUpdate("insert into FileManager values (" + fileID + ", '" + fileName + "', " +  fileSize + ", 0   , '" + hash  + "' ,   null   )");
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeFile(int fileID) {
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
    public ResultSet getFileList() {
        try {
            return statement.executeQuery("select * from FileManager");
        } catch (SQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }


    public DataManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.out.println("Can't found sqlite library");
            System.exit(0);
        }

        connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:client.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            /* Create a new database structure */
            statement.executeUpdate("create table if not exists FileManager (fileID INTEGER PRIMARY KEY, fileName VARCHAR, fileSize INTEGER, curSize INTERGER, fileHash VARCHAR, status VARCHAR)");

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

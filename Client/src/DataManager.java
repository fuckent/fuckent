
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void addFile(int fileID, String fileName, int fileSize, int curSize, String fileStatus) {
        // TODO: SQL Statement HERE!!!
    }

    public void removeFile(int fileID) {
        // TODO: SQL Statement HERE!!!
    }

    /**
     * Get All files from database
     * 
     * @return 
     */
    public ResultSet listFile() {
        // TODO: SQL !!!
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

//
//  Use synchronized keyword to syn methods when excute SQL Statements
//
public class ServerDataManager {

    private Statement statement;
    private Connection connection;

    public synchronized int addFile(String FileName, long FileSize, String Hash) {
        try {
            // TODO: SQL Statement add this file to database
            // Similar ClientManager ...
            String str = String.format("insert into FileManager values (null, '%s', %d, '%s')", FileName, FileSize, Hash);
            // System.out.println(str);
            statement.executeUpdate(str);
            str = String.format("select fileID from FileManager where fileName = '%s' and fileHash = '%s'", FileName, Hash);
            ResultSet rs = statement.executeQuery(str);

            while (rs.next()) {
                return rs.getInt("fileID");
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    public synchronized Boolean haveFile(int FileID) {
        // TODO: CODE HERE!!!  
        // check if database have a file by FileID
        //

        return null;

    }

    public ServerDataManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.out.println("Can't found sqlite library");
            System.exit(0);
        }

        connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:server.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            /* Create a new database structure */
            statement.executeUpdate("create table if not exists FileManager (fileID INTEGER PRIMARY KEY, fileName VARCHAR, fileSize BIGINT, fileHash VARCHAR)");
            // statement.executeUpdate("insert into FileManager values(NULL, 531, '123')");
            //			statement.e
            //			connection.close();
            //			statement.executeUpdate("insert into person values(2, 'yui')");

        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            // TODO: CODE HERE!!

            System.err.println(e.getMessage());
        }



    }

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

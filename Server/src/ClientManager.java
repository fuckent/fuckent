
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manage client information by SQLite in a virtual file in memory (fast!)
 * It creates new database tables when a object created
 *  
 * @author xxx
 *
 */
public class ClientManager {

    private Statement statement;
    private Connection connection;

    /**
     * This func get list of user have file which have file id = fileID
     * 
     * @param fileID
     * @return a Vector<String> 
     */
    public synchronized Vector<String> getListClient(int fileID) {
    	//
        try {
            ResultSet rs = statement.executeQuery("select clientAddr, clientPort from clientManager where fileID = " + fileID);
            Vector<String> v = new Vector<String>();
            while (rs.next()) {
                // read the result set
                String clientAddr = rs.getString("clientAddr");
                v.addElement(clientAddr);

                /* Don't use this variable */
                int clientPort = rs.getInt("clientPort");
            }

            return v;
        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            // TODO: CODE HERE!!

            System.err.println(e.getMessage());
        }

        return null;

    }
    public synchronized Boolean haveSharedFile(String clientAddr, int port, int fileID) {
        try {
            ResultSet rs = statement.executeQuery("select clientAddr, clientPort from clientManager where fileID = " + fileID +" clientAddr = " +clientAddr + " clientPort = " + port);
            return rs.next();


        } catch (SQLException ex) {
            Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public synchronized void addClientFile(String clientAddr, int port, int fileID) {
        try {
            statement.executeUpdate("insert into clientManager values ('" + clientAddr + "', " + port + ", " + fileID + ")");
        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            // TODO: CODE HERE!!

            System.err.println(e.getMessage());
        }

    }

    public synchronized void removeClientFile(String clientAddr, int port, int fileID) {
        try {
            statement.executeUpdate("delete from clientManager where clientAdrr = '" + clientAddr + "' and clientPort = " + port + " and fileID = " + fileID);
        } catch (SQLException e) {
            // if the error message is "out of memory", 
            // it probably means no database file is found
            // TODO: CODE HERE!!

            System.err.println(e.getMessage());
        }
    }

    public synchronized void removeClient(String clientAddr, int port) {
        try {
            statement.executeUpdate("delete from clientManager where clientAdrr = '" + clientAddr + "' and clientPort = " + port);
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

    public ClientManager() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.out.println("Can't found sqlite library");
            System.exit(0);
        }

        connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:cdata.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            /* Create a new database structure */
            statement.executeUpdate("drop table if exists clientManager");
            statement.executeUpdate("create table clientManager (clientAddr string, clientPort int, fileID int)");
            //statement.executeUpdate("insert into clientManager values('127.0.0.1', 531, 123)");
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
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
public class ServerPI {

	InputStreamReader in = null;
	BufferedReader reader;
	PrintWriter out;
	private Socket con;

	public synchronized String download(int fileID) {
		try {
			System.out.println("Download REQ file: " + fileID);
			out.format("DOWNLOAD %d\n", fileID).flush();

			String str = reader.readLine();
			System.out.println("line: " + str);

			return str;
		} catch (IOException ex) {
			Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		return null;
	}

	public synchronized Boolean share(int fileID, String hash) {
		try {
			// TODO: CODE HERE
			// tell server we want share a file !!!
			out.format("SHARE %d %s\n", fileID, hash).flush();
			String str = reader.readLine();
			if (str.matches("OK")) {
				return true;
			}
			if (str.matches("ERROR")) {
				return false;
			}
		} catch (IOException ex) {
			Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return false;
	}

	public synchronized Boolean unshare(int fileID, String hash) {
		try {
			// TODO: CODE HRE
			// tell server we want unshare a file!!!
			out.format("UNSHARE %d %s\n", fileID, hash).flush();
			String str = reader.readLine();
			System.out.println("line: " + str);
			if (str.matches("OK")) {
				return true;
			}
			if (str.matches("ERROR")) {
				return false;
			}
		} catch (IOException ex) {
			Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return false;
	}

	public synchronized int seed(String fileName, long fileSize, String hash) {
		String[] lst;

		try {
			out.format("SEED %s %d %s\n", fileName, fileSize, hash).flush();

			String str = reader.readLine();
			if (str.matches("SEED [^ ]+")) {
				lst = str.split(" ");
				return new Integer(lst[1]).intValue();
			}
		} catch (IOException ex) {
			Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return 0;
	}

	public ServerPI(String serverAddr, int port) throws IOException {

		con = new Socket(serverAddr, port);

		this.in = new InputStreamReader(con.getInputStream());
		this.reader = new BufferedReader(in);
		this.out = new PrintWriter(con.getOutputStream());

	}
}

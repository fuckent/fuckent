import java.net.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPI implements Runnable {

	private Socket con;
	/* Use variables below to interactive with the Database */
	private ClientManager cm;
	private ServerDataManager sdm;
	private final String addr;
	private final Integer port;

	public ServerPI(Socket con, ClientManager cm, ServerDataManager sdm) {
		this.cm = cm;
		this.sdm = sdm;
		this.con = con;
		this.addr = con.getInetAddress().toString().substring(1);
		this.port = (Integer) (con.getPort());
	}

	@Override
	public void run() {
		// TODO CODE HERE ! Deal with Client connection
		System.out.println("Connect from a client");
		try {
			InputStreamReader in = new InputStreamReader(con.getInputStream());
			BufferedReader reader = new BufferedReader(in);

			while (true) {
				/* Read input from client */
				try {
					if (!reader.ready()) {
						Thread.yield();
					}

					String line = reader.readLine();
					if (line == null) {
						break;
					}
					System.out.println("Request from client: " + line);
					/*
					 * TODO Here We matches client's input with strings in
					 * protocol
					 */
					if (line.matches("SEED [^ ]+ [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						seed(lst[1], lst[2], lst[3]);
					} else if (line.matches("DOWNLOAD [^ ]+")) {
						String[] lst = line.split(" ");
						download(lst[1]);

					} else if (line.matches("SHARE [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						share(new Integer(lst[1]).intValue(), lst[2]);
					} else if (line.matches("UNSHARE [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						unshare(lst[1], lst[2]);
					} else {
						// TODO: Deal with UNKNOWN REQ
						System.out.println("UNKN REQ");
					}

				} catch (IOException e) {
					// TODO Handler the error and exit
					break;
				}
			}
			/* Close connection */
			System.out.println("Connection closed!");
			cm.removeClient(addr, port);
			con.close();

		} catch (IOException e) {
			// TODO Print Error and Exit (remove this client in ClientManager)
			e.printStackTrace();
		}

	}

	private void seed(String FileName, String Size, String Hash) {

		try {

			System.out.println("SEED REQ");
			Long size = new Long(Size);

			int fileID = sdm.addFile(
					java.net.URLDecoder.decode(FileName, "ISO-8859-1"),
					size.longValue(), Hash);
			try {
				new PrintWriter(con.getOutputStream()).format("SEED %d\n",
						fileID).flush();
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE, null,
					ex);
		}

	}

	String join(Collection<?> s, String delimiter) {
		StringBuilder builder = new StringBuilder();
		Iterator iter = s.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}

	private void download(String FileID) {

		System.out.println("DOWN REQ");

		if (!sdm.haveFile(new Integer(FileID).intValue(), null)) {
			try {
				new PrintWriter(con.getOutputStream()).format(
						"DOWNLOAD ERROR 1\n").flush();
				// System.out.println("error1");
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		} else {
			ResultSet inforOfFile = sdm.getInfoOfFile(new Integer(FileID)
					.intValue());

			Vector<String> listClient = cm.getListClient(new Integer(FileID)
					.intValue());
			String listAddr = this.join(listClient, " ");
			if (listClient.isEmpty()) {
				try {
					new PrintWriter(con.getOutputStream()).format(
							"DOWNLOAD ERROR 2\n").flush();
				} catch (IOException ex) {
					Logger.getLogger(ServerPI.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			} else {
				try {
					String name = inforOfFile.getString("fileName");
					String size = inforOfFile.getString("fileSize");
					String hash = inforOfFile.getString("fileHash");

					try {
						new PrintWriter(con.getOutputStream()).format(
								"DOWNLOAD %s %s %s %s\n",
								java.net.URLEncoder.encode(name, "ISO-8859-1"),
								size, hash, listAddr).flush();

					} catch (IOException ex) {
						Logger.getLogger(ServerPI.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				} catch (SQLException ex) {
					Logger.getLogger(ServerPI.class.getName()).log(
							Level.SEVERE, null, ex);
				}

			}
		}

	}

	private void share(int FileID, String Hash) {
		// TODO process SHARE request
		System.out.println("SHAR REQ");
		Boolean check = sdm.haveFile(FileID, Hash);
		// SocketAddress Addr;
		if (!check) {
			try {
				new PrintWriter(con.getOutputStream()).format("ERROR\n")
						.flush();
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		} else {
			try {

				new PrintWriter(con.getOutputStream()).format("OK\n").flush();
				if (!cm.haveSharedFile(addr, port, FileID)) {
					cm.addClientFile(addr, port, FileID);
				}
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
	}

	private void unshare(String FileID, String Hash) {
		// TODO process UNSHARE request
		System.out.println("UNSR REQ");
		// String addr = con.getInetAddress().toString();

		// int port = (Integer) (con.getPort());
		Boolean check = cm.haveSharedFile(addr, port,
				new Integer(FileID).intValue());
		if (!check) {
			try {
				new PrintWriter(con.getOutputStream()).format("ERROR\n")
						.flush();
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		} else {
			try {
				System.out.println("OK");
				new PrintWriter(con.getOutputStream()).format("OK\n").flush();
			} catch (IOException ex) {
				Logger.getLogger(ServerPI.class.getName()).log(Level.SEVERE,
						null, ex);
			}
			cm.removeClientFile(addr, port, new Integer(FileID).intValue());
		}
	}
}

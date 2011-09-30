import java.net.*;
import java.io.*;

public class ServerPI implements Runnable {

	private Socket con;
	
	/* Use  variables below to interactive with the Database */
	private ClientManager cm;
	private ServerDataManager sdm;

	public ServerPI(Socket con, ClientManager cm, ServerDataManager sdm) {
		this.cm = cm;
		this.sdm = sdm;
		this.con = con;
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
					if (!reader.ready()) Thread.yield();

					String line = reader.readLine();
					if (line == null) break;
					System.out.println("Request from client: " + line);
					/* TODO Here We matches client's input with strings in protocol */
					if (line.matches("SEED [^ ]+ [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						seed(lst[1], lst[2], lst[3]);
					} else
					if (line.matches("DOWNLOAD [^ ]+")) {
						String[] lst = line.split(" ");
						download(lst[1]);

					} else

					if (line.matches("SHARE [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						share(lst[1], lst[2]);
					} else

					if (line.matches("UNSHARE [^ ]+ [^ ]+")) {
						String[] lst = line.split(" ");
						unshare(lst[1], lst[2]);
					} else
					{
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
			con.close();

		} catch (IOException e) {
			// TODO Print Error and Exit (remove this client in ClientManager)
			e.printStackTrace();
		}

	}

	private void seed(String FileName, String Size, String Hash) {
		// TODO process SEED request
		System.out.println("SEED REQ");

	}

	private void download(String FileID) {
		// TODO process DOWNLOAD request
		System.out.println("DOWN REQ");

	}

	private void share(String FileID, String Hash) {
		// TODO process SHARE request
		System.out.println("SHAR REQ");

	}

	private void unshare(String FileID, String Hash) {
		// TODO process UNSHARE request
		System.out.println("UNSR REQ");

	}
}

import java.io.*;
import java.net.*;

/* Test netbeans git */

public class Server implements Runnable {

	private ClientManager clientManager;
	private ServerDataManager serverDataManager;
	private ServerSocket listener;

	/**
	 * Init Client Manager, ServerDataManager
	 * 
	 * @param port
	 *            Listen Port
	 */
	Server(int port) {
		try {
			listener = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Print error message!
			e.printStackTrace();
		}

		// serverPI = new ServerPI(listener);
		clientManager = new ClientManager();
		serverDataManager = new ServerDataManager();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Create a new object from class Server: param listenPort
		Thread t = new Thread(new Server(5321));
		t.run();
		//server.run();
	}

	public void run() {
		// TODO Code HERE!

		System.out.println("Running Server...");
		/* Listen connection from client */

		while (true) {
			try {
				System.out.println("Waiting a new connection");
				Socket conn = listener.accept();

				/* create a new thread ServerPI to deal with this connection */
				Thread t = new Thread(new ServerPI(conn, clientManager, serverDataManager));
				t.start();

				// TODO Print LOG message HERE!

			} catch (IOException e) {
				// TODO Print error and exit ! (or continue?)
				e.printStackTrace();
			}

		}

	}

}

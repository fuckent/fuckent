import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author thong
 */
public class Listener implements Runnable {

	private ServerSocket listener;

	private Client client;

	Listener(int port, Client client) {
		Boolean Ok = false;
		this.client = client;
		// client.dataManager.
		do {
			try {
				listener = new ServerSocket(client.port);
				listener.setReuseAddress(true);
				Ok = true;
			} catch (IOException e) {
				System.err.println("Can't listen at port: " + client.port
						+ "\n[Download Only] Try with port: " + ++client.port);
				client.title = "[Download only] Client";

			}
		} while (!Ok);
	}

	@Override
	public void run() {
		System.out.println("Client Listening...");

		Socket conn;
		UploadThread t;
		while (true) {
			try {
				conn = listener.accept();
				t = new UploadThread(conn, this.client);
				t.execute();

			} catch (IOException e) {
				// TODO Print error and exit ! (or continue?)
				e.printStackTrace();
			}

		}

	}
}

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;
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
class SeedThread extends ClientThread {

	private Client client;
	private String fileName;
	private String path;
	private long fileSize;
	private String fileHash;
	private ConcurrentLinkedQueue<String> msgQueue;
	private int id;
	private long size;
	private int key;
	private boolean closeReq = false;

	@Override
	public void recvMsg() {
		if (msgQueue.isEmpty()) {
			return;
		}

		String msg = msgQueue.poll();
		if (msg.compareTo("CLOSE @CODE: [fuckent]") == 0) {

			// client.dataManager.updateCurrentSize(fileID, totalCount);
			// client.dataManager.updateStatus(fileID, "PAUSED");
			// client.threadManager.removeThread(this.id);
			Thread.currentThread().stop();

		}
	}

	@Override
	public void sendMsg(String str) {
		msgQueue.add(str);
	}

	private String getMD5Hash(String file) {
		{
			FileInputStream fis = null;
			try {
				File f = new File(file);
				this.size = f.length();
				System.out.println("File size: " + this.size);
				client.gui.model.setValueAt(client.gui.formatSize(this.size),
						this.id, 2);
				// f.
				MessageDigest md = MessageDigest.getInstance("MD5");
				fis = new FileInputStream(file);
				// this.size = fis.getChannel().size();
				byte[] dataBytes = new byte[1024];
				int nread = 0;
				long total = 0;
				while ((nread = fis.read(dataBytes)) != -1) {
					total += nread;
					this.recvMsg();
					if (this.closeReq)
						return null;

					publish(new ThreadInfo(Long
							.valueOf(total * 100 / this.size).intValue(), 0));

					md.update(dataBytes, 0, nread);
				}

				byte[] mdbytes = md.digest();
				StringBuilder sb = new StringBuilder("");
				for (int i = 0; i < mdbytes.length; i++) {
					sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
							.substring(1));
				}

				return sb.toString();
			} catch (IOException ex) {
				Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE,
						null, ex);

			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE,
						null, ex);
			} finally {
				try {
					fis.close();
				} catch (IOException ex) {
					Logger.getLogger(SeedThread.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}

			return null;
		}

	}

	/*
	 * This function going to be run when thread start and when this function
	 * exit then thread also quit! TODO: CODE HERE
	 */
	// @Override
	public void Run() {

		/**
		 * - Connect with Server via client.serverPI - add File to client data
		 * via client.dataManager - Change file status to SEEDING via
		 * client.dataManager - Call client.threadManager to remove this thread
		 * from list when finish
		 * 
		 */
		int fileID = 0;
		if (client.dataManager.haveFile(fileName, fileSize, path)) {

			return;
		}

		//
		// add this thread to thread manager
		//
		/* Can't do this :( */

		Files ff = new Files(new Integer(-1), fileName, null, null,
				new Integer(0), null, null, "SEEDING");

		key = client.gui.model.getRowCount();
		client.gui.model.addFile(ff, this);
		this.id = key;

		this.fileHash = getMD5Hash(path);
		if (fileHash == null)
			return;
		try {
			fileID = client.serverPI.seed(
					URLEncoder.encode(fileName, "ISO-8859-1"), fileSize,
					fileHash);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(SeedThread.class.getName()).log(Level.SEVERE,
					null, ex);
		}

		if (fileID == 0) {
			System.err.println("File " + path + "exists");
			return;
		}

		client.dataManager.addFile(fileID, fileName, fileSize, fileSize,
				fileHash, "SEEDED", path);
		client.gui.model.setValueAt("SEEDED", id, 7);
		client.gui.model.setValueAt(fileID, id, 0);
		client.gui.model.setValueAt(fileHash, id, 6);
		client.gui.shareFile(id);
	}

	public SeedThread(Client client, String path) {
		File f = new File(path);

		this.client = client;
		this.fileName = f.getName();
		this.fileSize = f.length();
		this.path = path;
		msgQueue = new ConcurrentLinkedQueue<String>();
		// .//msgQ

	}

	@Override
	protected void done() {
		super.done();

		closeThread();

	}

	@Override
	protected void process(java.util.List<ThreadInfo> c) {

		client.gui.model.setValueAt(c.get(c.size() - 1).getP(), key, 4);
		// client.gui.model.setValueAt(c.get(c.size()-1).getRate(), key, 3);

	}

	public void setRate(int speed) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void closeThread() {
		System.out.println("Closing thread - Seeding");
		this.closeReq = true;

	}

	@Override
	public long getRate() {
		return 0;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getClientAddr() {
		return null;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Long getcurSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected Integer doInBackground() throws Exception {
		this.Run();
		return 0;
	}
}

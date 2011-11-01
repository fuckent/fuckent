import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * 
 * @author thong
 */
abstract class ClientThread extends SwingWorker<Integer, ThreadInfo> {

	// abstract public void setRate(int speed);
	abstract public void closeThread();

	abstract public long getRate();

	abstract public String getClientAddr();

	abstract public void recvMsg();

	abstract public void sendMsg(String str);

	abstract public Long getcurSize();
}

class ThreadInfo {

	int p;
	long rate;

	public ThreadInfo(int a, long b) {
		this.p = a;
		this.rate = b;
	}

	public int getP() {
		return p;
	}

	public long getRate() {
		return rate;
	}
}
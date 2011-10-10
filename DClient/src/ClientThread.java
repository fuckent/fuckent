/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author thong
 */
abstract class ClientThread {

    abstract public void setRate(int speed);

    abstract public void closeThread();

    abstract public int getRate();

    abstract public String getClientAddr();
    
    abstract public void recvMsg();
    
    abstract public void sendMsg(String str);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author thong
 */
abstract class ClientThread extends Thread{
    abstract public void setRate(int speed);
    abstract public void closeThread();
    abstract public int getRate();
}

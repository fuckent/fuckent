
import java.util.*;
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
public class ThreadManager {

    /* Use java.util.Dictionary Class here */
    Hashtable<Integer, ClientThread> dic;

    public synchronized Boolean isEmpty() {
        return dic.isEmpty();
    }

    public synchronized void closeAllThreads() {
        Enumeration<ClientThread> es = dic.elements();
        while (es.hasMoreElements()) {
            es.nextElement().sendMsg("CLOSE @CODE: [fuckent]");

        }
    }

    public synchronized void addThread(int FileID, ClientThread t) {
        System.out.println("Add thread handle file: " + FileID);
        dic.put(new Integer(FileID), t);
    }

    public synchronized ClientThread getThread(int FileID) {
        // System.out.println("get " + FileID);

        return dic.get(new Integer(FileID));
    }

    public synchronized void removeThread(int fileID) {
        System.out.println("Remove thread handle file: " + fileID);
        dic.remove(new Integer(fileID));
    }

    public ThreadManager() {
        this.dic = new Hashtable<Integer, ClientThread>();
        dic.clear();
    }
}

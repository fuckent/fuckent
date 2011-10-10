
import java.util.*;

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
    
    public synchronized void addThread(int FileID, ClientThread t) {
      //  System.out.println("Add " + FileID);
        dic.put(new Integer(FileID), t);
    }
    
    public synchronized ClientThread getThread(int FileID) {
       // System.out.println("get " + FileID);
        
        return dic.get(new Integer(FileID));
    }
    
    public synchronized void removeThread(int fileID) {
        dic.remove(new Integer(fileID));
    }

    public ThreadManager() {
        this.dic = new Hashtable<Integer, ClientThread>();
    }
    
}

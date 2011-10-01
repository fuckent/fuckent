
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
    
    public void addThread(int FileID, ClientThread t) {
        dic.put(new Integer(FileID), t);
    }
    
    public ClientThread getThread(int FileID) {
        return dic.get(new Integer(FileID));
    }
    
    public void removeThread(int fileID) {
        dic.remove(new Integer(fileID));
    }

    public ThreadManager() {
        this.dic = new Hashtable<Integer, ClientThread>();
    }
    
}

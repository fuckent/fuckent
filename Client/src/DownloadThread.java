/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author thong
 */
class DownloadThread extends ClientThread{
    
    public void Run() {
        
    }

    public DownloadThread() {
    }

    @Override
    public void setRate(int speed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeThread() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

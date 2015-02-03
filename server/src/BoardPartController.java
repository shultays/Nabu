import com.nabu.bloodworks.models.BoardPartModel;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.nabu.bloodworks.Constants.*;

/**
 * Created by Engin on 2/1/14.
 */
public class BoardPartController{
    int x, y;
    boolean inited;
    BoardPartController neighbors[][] = new BoardPartController[3][3];

    BoardPartModel model;


    public BoardPartController(int x, int y){
        this.x = x;
        this.y = y;
        model = new BoardPartModel(x, y);
    }

    void addNeighbor(int x, int y, BoardPartController n){
        neighbors[x+1][y+1] = n;
    }

    void setCell(int x, int y, byte b){
        model.setCell(x, y, b);
    }

    /* */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void getReadLock(){
        lock.readLock().lock();
    }
    public void releaseReadLock(){
        lock.readLock().unlock();
    }
    public void getWriteLock(){
        lock.writeLock().lock();
    }
    public void releaseWriteLock(){
        lock.writeLock().unlock();
    }

    public void init() {
        neighbors = new BoardPartController[3][3];
        model = new BoardPartModel(x, y);
        model.init();
        model.tick = 1;
        model.randomize();
        inited = true;
    }

    public int getTick() {
        return model.tick;
    }
}



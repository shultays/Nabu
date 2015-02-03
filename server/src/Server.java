import com.nabu.bloodworks.models.BoardPartModel;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import static com.nabu.bloodworks.Constants.*;

//Engin Mercan
//250702022
//CSE471 Term Project

public class Server extends Thread{

    //public Vector<ClientThread> clients = new Vector<ClientThread>();
    LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<Runnable>();

    BoardPartController parts[][] = new BoardPartController[MAX_PART][MAX_PART];

	public static void main(String args[]) throws IOException{
        new Server();
	}

    public Server(){
        ServerSocket server = null;
        try {
            server = new ServerSocket(42043);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for(int i=0; i<MAX_PART; i++){
            for(int j=0; j<MAX_PART; j++){
                parts[i][j] = new BoardPartController(i, j);
            }
        }

        for(int i=0; i<MAX_PART; i++){
            for(int j=0; j<MAX_PART; j++){
                for(int a=-1; a<=1; a++){
                    for(int b=-1; b<=1; b++){
                        parts[i][j].addNeighbor(a, b, getPartController(i+a, j+b));
                    }
                }
                parts[i][j].init();
            }
        }
/*
        int mid = MAX_PART/2;
        Random r = new Random();
        for(int i=mid-2; i<=mid+2; i++){
            for(int j=mid-2; j<=mid+2; j++){
                parts[i][j].init();
            }
        }*/

        start();

        System.out.println("ready to rock");

        while(true){
            Socket newClient = null;
            try {
                newClient = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("client connected");
            ClientThread ct = new ClientThread(newClient, Server.this);
        }
    }

    public void run(){
        while(true){
            Runnable runnable = null;
            try {
                runnable = queue.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            if(runnable != null){
                runnable.run();
            }
        }
    }

    public BoardPartController getPartControllerFromPlayerPos(float x, float y){
        int i = (int) (x/(PART_SIZE));
        int j = (int) (y/(PART_SIZE));
        return getPartController(i, j);
    }

    public BoardPartController getPartController(int i, int j){
        if(i<0) i+=MAX_PART;
        else if(i>=MAX_PART) i-=MAX_PART;
        if(j<0) j+=MAX_PART;
        else if(j>=MAX_PART) j-=MAX_PART;
        return parts[i][j];
    }

    public void post(Runnable runnable){
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
        }
    }

}
import com.nabu.bloodworks.models.BoardPartModel;
import com.nabu.bloodworks.models.Model;
import com.nabu.bloodworks.models.PlayerPosModel;

import java.io.*;
import java.net.*;

import static com.nabu.bloodworks.BloodworksConstans.*;
import static com.nabu.bloodworks.Constants.*;

//Engin Mercan
//250702022
//CSE471 Term Project


public class ClientThread implements Runnable{
    Server server;
    Socket socket = null;
    int id;
    public static int nextId = 0;
    DataInputStream in;
    DataOutputStream out;

    BoardPartController registeredBoards[] = new BoardPartController[4];
    BoardPartController currentBoard;


    PlayerPosModel pos = new PlayerPosModel();

    boolean active;
    private boolean running = true;

    public ClientThread(Socket sock, Server server){
        this.server = server;
    	socket = sock;
    	try {
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
    	} catch (IOException e) {
		}
        id = nextId;
        nextId++;

        new Thread(this).start();
    }

    @Override
    public void run() {
        pos.x = MAX_PART*PART_SIZE/2.0f;
        pos.y = MAX_PART*PART_SIZE/2.0f;

        while (running){
            int commandID = ILLEGAL_COMMAND;

            try {
                commandID = in.readInt();
            } catch (IOException e) {
                e.printStackTrace();
                die();
                return;
            }

            System.out.println("command " + commandID);
            switch (commandID){
                case ILLEGAL_COMMAND:
                    die();
                    break;
                case PLAYER_MOVE_COMMAND:{
                    if(!readModel(pos)) return;
                    playerMoved();
                }
                break;
                case REQUEST_PART_COMMAND:{
                    try {
                        int x = in.readInt();
                        int y = in.readInt();
                        int tick = in.readInt();
                        BoardPartController p = server.getPartController(x, y);
                        if(p.getTick() > tick){
                            p.getReadLock();
                            out.writeInt(REPLY_PART_COMMAND);
                            out.writeInt(x);
                            out.writeInt(y);
                            p.model.write(out);
                            p.releaseReadLock();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
            }
        }
    }

    private void playerMoved() {
        currentBoard = server.getPartControllerFromPlayerPos(pos.x, pos.y);
        int showX, showY;

        float onBoardX = pos.x-currentBoard.x*PART_SIZE;
        float onBoardY = pos.y-currentBoard.x*PART_SIZE;

        if(onBoardX < PART_SIZE/2.0f){
            showX = -1;
        }else{
            showX = +1;
        }
        if(onBoardY < PART_SIZE/2.0f){
            showY = -1;
        }else{
            showY = 1;
        }

        registeredBoards[0] = currentBoard;
        registeredBoards[1] = server.getPartController(currentBoard.x+showX, currentBoard.y+showY);
        registeredBoards[2] = server.getPartController(currentBoard.x, currentBoard.y+showY);
        registeredBoards[3] = server.getPartController(currentBoard.x+showX, currentBoard.y);

        for(int i=0; i<4; i++){
            System.out.println("registered " + registeredBoards[i].x + " " + registeredBoards[i].y);
        }
    }

    private boolean readModel(Model m){
        try {
            m.read(in);
        } catch (IOException e) {
            die();
            return false;
        }
        return true;
    }
    private void die() {
        running = false;
        try {
            in.close();
        } catch (IOException e) {
        }
        try {
            out.close();
        } catch (IOException e) {
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }
}

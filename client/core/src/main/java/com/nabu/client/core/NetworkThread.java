package com.nabu.client.core;

import com.badlogic.gdx.Gdx;
import com.nabu.bloodworks.models.BoardPartModel;
import com.nabu.bloodworks.models.PlayerPosModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import static com.nabu.bloodworks.BloodworksConstans.*;
/**
 * Created by Engin on 2/1/14.
 */
public class NetworkThread extends Thread {
    Socket sock;
    DataInputStream in;
    DataOutputStream out;
    boolean running = true;

    Nabu nabu;
    public NetworkThread(Nabu nabu){
        this.nabu = nabu;

        try {
            sock = new Socket("127.0.0.1", 42043);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void run(){
        while(running){
            int commandID = ILLEGAL_COMMAND;

            try {
                commandID = in.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }


            switch (commandID){
                case ILLEGAL_COMMAND:
                    running = false;
                    die();
                    break;
                case REPLY_PART_COMMAND:{
                    try {
                        int x = in.readInt();
                        int y = in.readInt();
                        final BoardPartModel p = new BoardPartModel(x, y);
                        p.read(in);
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                nabu.updatePart(p);
                            }
                        });
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }


        }
    }


    private void die() {
        running = false;
    }

    public DataInputStream in(){
        return in;
    }
    public DataOutputStream out(){
        return out;
    }
}

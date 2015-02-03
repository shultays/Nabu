package com.nabu.bloodworks.models;


import com.nabu.bloodworks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import static com.nabu.bloodworks.Constants.*;

/**
 * Created by Engin on 1/31/14.
 */
public class BoardPartModel implements Model{
    public int tick;
    public int x, y;
    public byte[] data;

    public BoardPartModel(int i, int j) {
        this.x = i;
        this.y = j;
        tick = 0;
    }

    public void init(){
        data = new byte[DATA_SIZE];
    }

    public void deinit(){
        data = null;
    }

    public void setCell(int i, int j, byte b) {
        data[i+PART_SIZE*j] = b;
    }

    public byte getCell(int i, int j) {
        return data[i+PART_SIZE*j];
    }

    public void read(DataInputStream in) throws IOException {
        tick = in.readInt();
        x = in.readInt();
        y = in.readInt();
        if(in.readBoolean()){
            if(data == null) init();
            Tools.readBytesFromSocket(in, data);
        }
    }
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(tick);
        out.writeInt(x);
        out.writeInt(y);
        if(data == null){
            out.writeBoolean(false);
        }else{
            out.writeBoolean(true);
            out.write(data, 0, DATA_SIZE);
        }
    }

    public void randomize() {
        Random r = new Random();
        r.setSeed(x+y*MAX_PART);
        for(int i=0; i<PART_SIZE; i++){
            for(int j=0; j<PART_SIZE; j++){
                if(r.nextInt(4)==0){
                    setCell(i, j, (byte) (r.nextInt(26)+1));
                }
            }
        }
    }
}

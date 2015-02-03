package com.nabu.bloodworks.models;

import com.nabu.bloodworks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.nabu.bloodworks.Constants.DATA_SIZE;

/**
 * Created by Engin on 2/1/14.
 */
public class PlayerPosModel implements Model{
    public float x;
    public float y;

    public void read(DataInputStream in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
    }
    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }
}

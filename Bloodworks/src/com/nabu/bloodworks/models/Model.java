package com.nabu.bloodworks.models;

import com.nabu.bloodworks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.nabu.bloodworks.Constants.DATA_SIZE;

/**
 * Created by Engin on 2/1/14.
 */
public interface Model {
    public void read(DataInputStream in) throws IOException;
    public void write(DataOutputStream out) throws IOException;
}

package com.nabu.bloodworks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static com.nabu.bloodworks.Constants.*;

/**
 * Created by Engin on 2/1/14.
 */
public class Tools {

    public static void readBytesFromSocket(DataInputStream in, byte b[]) throws IOException {
        int left = b.length;
        int i = 0;
        while(left>0){
            int r = in.read(b, i, left);
            if(r>0){
                left-=r;
                i+=r;
            }else{
                return;
            }
        }
    }

    public static int getPartIndex(float x){
        return ((int)(x/(PART_SIZE)));
    }

}

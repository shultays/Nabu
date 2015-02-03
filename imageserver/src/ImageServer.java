import com.nabu.bloodworks.models.BoardPartModel;
import com.sun.swing.internal.plaf.synth.resources.synth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import static com.nabu.bloodworks.Constants.*;
import java.awt.image.BufferedImage;
/**
 * Created by Engin on 2/2/14.
 */
public class ImageServer {
    Socket sock;
    DataInputStream in;
    DataOutputStream out;

    public static BoardPartModel parts[][] = new BoardPartModel[MAX_PART][MAX_PART];


    public static class Level1Reader extends Thread{
        int x, y, w, h;
        public Level1Reader(int x, int y, int w, int h){
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
        @Override
        public void run() {

            BufferedImage bmp = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bmp.createGraphics();


            g.setBackground(new Color(0, 0, 0, 0));

            int size = IMAGE_SIZE/PART_SIZE;


            for(int a=x; a<x+w; a++){
                for(int b=y; b<y+h; b++){
                    BoardPartModel p = parts[a][b];


                    for(int i=0; i<PART_SIZE; i++){
                        for(int j=0; j<PART_SIZE; j++){
                            byte c = p.getCell(i, j);
                            int startX = IMAGE_SIZE*i/PART_SIZE;
                            int startY = IMAGE_SIZE-IMAGE_SIZE*(j+1)/PART_SIZE;
                            if(c>0){
                                g.drawImage(images[c-1], startX, startY, size, size, null);
                            }else{
                                g.clearRect(startX, startY, size, size);
                            }
                        }
                    }
                    try {
                        ImageIO.write(bmp, "png", new File("mips/level_1/p_"+a+"_"+b+".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    public static class LevelNReader extends Thread{
        int x, y, w, h;
        int level;
        public LevelNReader(int x, int y, int w, int h, int level){
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.level = level;
        }
        @Override
        public void run() {

            BufferedImage dbmp = new BufferedImage(IMAGE_SIZE*2, IMAGE_SIZE*2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D dg = dbmp.createGraphics();
            dg.setBackground(new Color(0, 0, 0, 0));

            try {
            for(int a=x; a<x+w; a++){
                for(int b=y; b<y+h; b++){
                    dg.clearRect(0, 0, IMAGE_SIZE * 2, IMAGE_SIZE * 2);
                    for(int i=0; i<2; i++){
                        for(int j=0; j<2; j++){
                            int n = a*2+i;
                            int m = b*2+j;

                            BufferedImage img = null;
                                img = ImageIO.read(new File("./mips/level_" + (level / 2) + "/p_" + n + "_" + m + ".png"));

                            dg.drawImage(img, i*IMAGE_SIZE, IMAGE_SIZE*2-(j+1)*IMAGE_SIZE, null);

                        }
                    }
                    ImageIO.write(scaleHalfIntRGB(dbmp), "png", new File("mips/level_"+level+"/p_"+a+"_"+b+".png"));

                }
            }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

            public static  Image images[] = new Image[26];
    public ImageServer() throws InterruptedException, IOException {



        System.out.println("hi");

        /*sock = new Socket("95.85.34.10", 42043);
        in = new DataInputStream(sock.getInputStream());
        out = new DataOutputStream(sock.getOutputStream());
        while(true){



            Thread.sleep(1000*10);
        }*/

    }

    public static BufferedImage scaleHalfIntRGB(BufferedImage src)
    {
        if (src.getWidth() % 2 != 0)
            throw new IllegalStateException();
        if (src.getHeight() % 2 != 0)
            throw new IllegalStateException();

        int w = src.getWidth() / 2;
        int h = src.getHeight() / 2;

        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] full = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        int[] half = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();

        if (w * h * 4 != full.length)
            throw new IllegalStateException();
        if (w * h != half.length)
            throw new IllegalStateException();

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int rgb1 = full[((y << 1) + 0) * (w << 1) + ((x << 1) + 0)];
                int rgb2 = full[((y << 1) + 1) * (w << 1) + ((x << 1) + 0)];
                int rgb3 = full[((y << 1) + 0) * (w << 1) + ((x << 1) + 1)];
                int rgb4 = full[((y << 1) + 1) * (w << 1) + ((x << 1) + 1)];

                int a = ((((rgb1 & 0xFF000000)>>>24) + ((rgb2 & 0xFF000000)>>>24) + ((rgb3 & 0xFF000000)>>>24) + ((rgb4 & 0xFF000000)>>>24)) >> ( 2));
                int r = (((rgb1 & 0x00FF0000) + (rgb2 & 0x00FF0000) + (rgb3 & 0x00FF0000) + (rgb4 & 0x00FF0000)) >> (16 + 2));
                int g = (((rgb1 & 0x0000FF00) + (rgb2 & 0x0000FF00) + (rgb3 & 0x0000FF00) + (rgb4 & 0x0000FF00)) >> (8 + 2));
                int b = (((rgb1 & 0x000000FF) + (rgb2 & 0x000000FF) + (rgb3 & 0x000000FF) + (rgb4 & 0x000000FF)) >> (0 + 2));

                half[y * w + x] = (a<<24) | (r << 16) | (g << 8) | (b << 0);
            }
        }
        return dst;
    }

    public static void main(String args[]){
/*
        for(int i=0; i<26; i++){
            try {
                images[i] = ImageIO.read(new File("./assets/"+((char)('A'+i))+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i=0; i<MAX_PART; i++){
            for(int j=0; j<MAX_PART; j++){
                parts[i][j] = new BoardPartModel(i, j);
            }
        }

        for(int i=0; i<MAX_PART; i++){
            for(int j=0; j<MAX_PART; j++){
                parts[i][j].init();
                parts[i][j].randomize();
            }
        }

        int t = 16;
        Level1Reader l[][] = new Level1Reader[t][t];
        for(int i=0; i<t; i++){
            for(int j=0; j<t; j++){
                l[i][j] = new Level1Reader(i*MAX_PART/t, j*MAX_PART/t, MAX_PART/t , MAX_PART/t);
                l[i][j].start();
            }
        }

        for(int i=0; i<t; i++){
            for(int j=0; j<t; j++){
                try {
                    l[i][j].join();
                    l[i][j] = null;
                } catch (InterruptedException e) {

                }
            }
        }
        l = null;
*/

        for(int level = 64; level<=256; level *=2){
            int t = 1;
            int S = MAX_PART/level;
            LevelNReader l2[][] = new LevelNReader[t][t];
            for(int i=0; i<t; i++){
                for(int j=0; j<t; j++){
                    l2[i][j] = new LevelNReader(i*S/t, j*S/t, S/t , S/t, level);
                    l2[i][j].start();
                }
            }

            for(int i=0; i<t; i++){
                for(int j=0; j<t; j++){
                    try {
                        l2[i][j].join();
                    } catch (InterruptedException e) {

                    }
                }
            }
            System.out.println("ok"+level);
        }
        /*
        try {
            new ImageServer();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }
}

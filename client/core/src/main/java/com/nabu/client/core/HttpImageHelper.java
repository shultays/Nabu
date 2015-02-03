package com.nabu.client.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Engin on 2/3/14.
 */
public class HttpImageHelper {


    public interface HttpImageHelperInterface{
        public void imageLoaded(Texture texture);
    }

    public static void loadImage(final String url, final HttpImageHelperInterface callback){
        new Thread(new Runnable() {
            private int download (byte[] out, String url) {
                InputStream in = null;
                try {
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection)new URL(url).openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setUseCaches(true);
                    conn.connect();
                    in = conn.getInputStream();
                    int readBytes = 0;
                    while (true) {
                        int length = in.read(out, readBytes, out.length - readBytes);
                        if (length == -1) break;
                        readBytes += length;
                    }
                    return readBytes;
                } catch (Exception ex) {
                    return 0;
                } finally {
                    StreamUtils.closeQuietly(in);
                }
            }

            @Override
            public void run () {
                byte[] bytes = new byte[200 * 1024]; // assuming the content is not bigger than 200kb.
                int numBytes = download(bytes, url);
                if (numBytes != 0) {
                    Pixmap pixmap = new Pixmap(bytes, 0, numBytes);
                    final int originalWidth = pixmap.getWidth();
                    final int originalHeight = pixmap.getHeight();

                    int width = MathUtils.nextPowerOfTwo(pixmap.getWidth());
                    int height = MathUtils.nextPowerOfTwo(pixmap.getHeight());
                    final Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
                    potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                    pixmap.dispose();
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run () {
                            callback.imageLoaded(new Texture(potPixmap));
                        }
                    });
                }
            }
        }).start();
    }
}

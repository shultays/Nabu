package com.nabu.client.core;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.TimeUtils;
import com.nabu.bloodworks.Tools;
import com.nabu.bloodworks.models.BoardPartModel;
import com.nabu.bloodworks.models.PlayerPosModel;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static com.nabu.bloodworks.BloodworksConstans.*;
import static com.nabu.bloodworks.Constants.*;

public class Nabu implements ApplicationListener, InputProcessor{
    public class MipIndex{
        int x;
        int y;
        int mip;
    }
	SpriteBatch batch;
	long elapsed;
    NetworkThread server;
    PlayerPosModel pos = new PlayerPosModel();

    BoardPartModel parts[][] = new BoardPartModel[MAX_PART][MAX_PART];
    Queue<BoardPartModel> partBuffer = new ArrayDeque<BoardPartModel>(101);

    int xIndex = -1;
    int yIndex = -1;
    int mipXIndex[] = {-1, -1, -1, -1, -1, -1, -1};
    int mipYIndex[] = {-1, -1, -1, -1, -1, -1, -1};
    int mipPaths[] = {4, 8, 16, 32, 64, 128, 256};
    public static TextureAtlas atlas;
    TextureRegion letters[] = new TextureRegion[26];
    Camera cam;
    int width, height;

    long current;
    float zoom = 1;

    Texture[][][] mips = new Texture[7][][];
    boolean[][][] mipsLoading = new boolean[7][][];
    Queue<MipIndex> mipsBuffer = new ArrayDeque<MipIndex>(256);



    @Override
	public void create () {
        for(int i=0; i<7; i++){
            int len = 1<<((6-i));
            mips[i] = new Texture[len][len];
            mipsLoading[i] = new boolean[len][len];
        }

		batch = new SpriteBatch();
        pos.x = MAX_PART*PART_SIZE/2;
        pos.y = MAX_PART*PART_SIZE/2;

        atlas  = new TextureAtlas(Gdx.files.internal("letters.atlas"));

        for(int i=0; i<26; i++){
            letters[i] = atlas.findRegion((char)('A'+i)+"");
        }
        for(int i=0; i<MAX_PART; i++){
            for(int j=0; j<MAX_PART; j++){
                parts[i][j] = new BoardPartModel(i, j);
                parts[i][j].tick = 0;
            }
        }


        server = new NetworkThread(this);
        server.start();
        pos.x = PART_SIZE*MAX_PART/2.0f;
        pos.y = PART_SIZE*MAX_PART/2.0f;
        move(1, 1);
        Gdx.input.setInputProcessor(this);

        current = TimeUtils.millis();
	}

    @Override
    public void resize(int w, int h) {
        this.width = w;
        this.height = h;
        cam = new OrthographicCamera(w, h);
    }

    @Override
	public void render () {
        tick();
		elapsed += Gdx.graphics.getDeltaTime()*1000;

        if(zoom < 0.00104612216/2){
            Gdx.gl.glClearColor(200/255.0f, 200/255.0f, 200/255.0f, 1);

            Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            return;
        }

        Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);


        batch.begin();


        int mipsLevel = 0;

        if(zoom > 0.12844433333){
            mipsLevel = -1;
        }else if(zoom > 0.03284545666){
            mipsLevel = 0;
        }else if(zoom > 0.01622350333){
            mipsLevel = 1;
        }else if(zoom > 0.00813451333){
            mipsLevel = 2;
        }else if(zoom > 0.00407867166){
            mipsLevel = 3;
        }else if(zoom > 0.0020532595){
            mipsLevel = 4;
        }else if(zoom > 0.00104612216){
            mipsLevel = 5;
        }else{
            mipsLevel = 6;
        }

        mipsLevel = -1;

        if(mipsLevel == -1){
            int z = 0;
            for(int i=-z; i<=z; i++){
                for(int j=-z; j<=z; j++){
                    int a = xIndex+i;
                    int b = yIndex+j;
                    System.out.println(a+ " " + b);
                    BoardPartModel p = getPartController(a, b);
                    float x = (a*PART_SIZE-pos.x)*PART_DIM*zoom+width/2-PART_DIM*PART_SIZE*zoom/2;
                    float y = (b*PART_SIZE-pos.y)*PART_DIM*zoom+height/2-PART_DIM*PART_SIZE*zoom/2;
                    if (p.data != null){
                        for(int n=0; n<PART_SIZE; n++){
                            for(int m=0; m<PART_SIZE; m++){
                                byte cell = p.getCell(n, m);
                                if(cell != 0){
                                    batch.draw(letters[cell-1], x+n*PART_DIM*zoom, y+m*PART_DIM*zoom, PART_DIM*zoom, PART_DIM*zoom);
                                }
                            }
                        }
                    }
                }
            }
        }else{
            for(int i=-2; i<=2; i++){
                for(int j=-2; j<=2; j++){
                    int ax = mipXIndex[mipsLevel]+i;
                    int ay = mipYIndex[mipsLevel]+j;
                    int len = 1<<((6-mipsLevel));
                    int moveX = 0;
                    int moveY = 0;
                    while(ax<0){
                        ax += len;
                        moveX -= len;

                    }
                    while(ax >= len){
                        ax -= len;
                        moveX += len;
                    }

                    while(ay<0){
                        ay += len;
                        moveY -= len;
                    }
                    while(ay >= len){
                        ay -= len;
                        moveY += len;
                    }

                    float x = ((ax+moveX)*PART_SIZE*mipPaths[mipsLevel]-pos.x)*IMAGE_SIZE*zoom/4+width/2-IMAGE_SIZE*zoom*PART_SIZE*mipPaths[mipsLevel]/8;
                    float y = ((ay+moveY)*PART_SIZE*mipPaths[mipsLevel]-pos.y)*IMAGE_SIZE*zoom/4+height/2-IMAGE_SIZE*zoom*PART_SIZE*mipPaths[mipsLevel]/8;

                    if(mips[mipsLevel][ax][ay] != null){

                        batch.draw(mips[mipsLevel][ax][ay], x, y, IMAGE_SIZE*zoom*PART_SIZE*mipPaths[mipsLevel]/4, IMAGE_SIZE*zoom*PART_SIZE*mipPaths[mipsLevel]/4, 0, 0, 1, 1);
                    }
                }
            }
        }
		batch.end();
	}

    private void tick() {

        float delta = (TimeUtils.millis()-current)/1000.0f;
        current = TimeUtils.millis();
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            zoom *= Math.pow(0.999, delta*1000);
            System.out.println(delta + " " + zoom + " " + PART_DIM*zoom);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            zoom /= Math.pow(0.999, delta*1000);
            System.out.println(delta + " " + zoom + " " + PART_DIM*zoom);
        }
    }

    @Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}

    public void moveBy(float x, float y){
        move(pos.x+x, pos.y+y);
    }
    public void move(float x, float y){

        pos.x = x;
        pos.y = y;
        int oldXIndex = xIndex;
        int oldYIndex = yIndex;
        try {
            server.out().writeInt(PLAYER_MOVE_COMMAND);
            pos.write(server.out());
        } catch (IOException e) {
            e.printStackTrace();
        }
        xIndex = Tools.getPartIndex(x);
        yIndex = Tools.getPartIndex(y);

        try {
            server.out().writeInt(REQUEST_PART_COMMAND);
            BoardPartModel p = getPartController(xIndex , yIndex);
            server.out().writeInt(p.x);
            server.out().writeInt(p.y);
            server.out().writeInt(p.tick);
        }catch (IOException e) {
            e.printStackTrace();
        }

        if(oldXIndex != xIndex || oldYIndex != yIndex){
            for(int i=-2; i<=2; i++){
                for(int j=-2; j<=2; j++){
                    if(i==0 && j == 0) continue;
                    try {
                        server.out().writeInt(REQUEST_PART_COMMAND);
                        BoardPartModel p = getPartController(xIndex + i, yIndex + j);
                        server.out().writeInt(p.x);
                        server.out().writeInt(p.y);
                        server.out().writeInt(p.tick);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            server.out().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int n=6; n>=0; n--){
            int newMipXIndex = xIndex/mipPaths[n];
            int newMipYIndex = yIndex/mipPaths[n];
            int len = 1<<((6-n));

            if(newMipXIndex != mipXIndex[n] || newMipYIndex != mipYIndex[n]){
                mipXIndex[n] = newMipXIndex;
                mipYIndex[n] = newMipYIndex;

                for(int i=-2; i<=2; i++){
                    for(int j=-2; j<=2; j++){
                        int ax = newMipXIndex+i;
                        int ay = newMipYIndex+j;

                        while(ax<0) ax += len;
                        while(ax >= len) ax -= len;

                        while(ay<0) ay += len;
                        while(ay >= len) ay -= len;

                        if(mips[n][ax][ay] == null && !mipsLoading[n][ax][ay]){
                            final int finalN = n;
                            final int finalAx = ax;
                            final int finalAy = ay;
                            mipsLoading[n][ax][ay] = true;
                            HttpImageHelper.loadImage("http://95.85.34.10/level_"+(mipPaths[n])+"/p_"+ay+"_"+ax+".png", new HttpImageHelper.HttpImageHelperInterface() {
                                @Override
                                public void imageLoaded(Texture texture) {
                                    mips[finalN][finalAx][finalAy] = texture;
                                    mipsLoading[finalN][finalAx][finalAy] = false;
                                    while(mipsBuffer.size() >= 256){
                                        System.out.println("hmm");
                                        MipIndex m = mipsBuffer.poll();
                                        mips[m.mip][m.x][m.y].dispose();
                                        mips[m.mip][m.x][m.y] = null;
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }


    public BoardPartModel getPartControllerFromPlayerPos(float x, float y){
        int i = (int) (x/(PART_SIZE));
        int j = (int) (y/(PART_SIZE));
        return getPartController(i, j);
    }

    public BoardPartModel getPartController(int i, int j){
        if(i<0) i+=MAX_PART;
        else if(i>=MAX_PART) i-=MAX_PART;
        if(j<0) j+=MAX_PART;
        else if(j>=MAX_PART) j-=MAX_PART;
        return parts[i][j];
    }

    public void updatePart(BoardPartModel p) {
        while(partBuffer.size() >= 100){
            BoardPartModel remove = partBuffer.poll();
            remove.deinit();
            remove.tick = 0;
        }
        partBuffer.add(p);
        parts[p.x][p.y] = p;
    }

    boolean pressed = false;
    int oldX, oldY;
    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {
        if(pressed) return false;
        oldX = x;
        oldY = y;
        pressed = true;
        return true;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        pressed = false;
        return true;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        if(pressed){
            moveBy(-(x-oldX)/(PART_DIM*zoom), (y-oldY)/(PART_DIM*zoom));
            oldX = x;
            oldY = y;

            return true;
        }
        return false;
    }






    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }
    @Override
    public boolean mouseMoved(int i, int i2) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }
}

package com.nabu.client.java;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.nabu.client.core.Nabu;


public class NabuDesktop {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 480;
		config.useGL20 = true;
		new LwjglApplication(new Nabu(), config);
	}
}

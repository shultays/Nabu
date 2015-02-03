package com.nabu.client.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.nabu.client.core.*;

public class TestHtml extends GwtApplication {
	@Override
	public ApplicationListener getApplicationListener () {
		return new Nabu();
	}
	
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(480, 320);
	}
}

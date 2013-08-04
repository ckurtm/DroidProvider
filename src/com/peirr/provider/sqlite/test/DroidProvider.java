package com.peirr.provider.sqlite.test;

import android.app.Application;
import android.content.Context;

public class DroidProvider extends Application {
	
	private static Context context;
	
	public void onCreate() {
		super.onCreate();
		DroidProvider.context = this;
	};
	
	public static Context getContext() {
		return context;
	}

}

package com.peirr.provider.sqlite.test;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.peirr.provider.models.ParentData;
import com.peirr.provider.models.Pojo;
import com.peirr.provider.models.Pojo2;
import com.peirr.provider.sqlite.BaseDataStore;

public class MyDataStore extends BaseDataStore {

	public MyDataStore(Context context) {
		super(context);
	}

	@Override
	public Map<String,Class<?>> getObjects() {
		Map<String,Class<?>> objects = new HashMap<String,Class<?>>();
		objects.put(Pojo.TABLE,Pojo.class);
		objects.put(Pojo2.TABLE,Pojo2.class);
		objects.put(ParentData.TABLE,ParentData.class);
		return objects;
	}
}

package com.peirr.provider.sqlite.test;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.peirr.provider.models.Pojo;
import com.peirr.provider.models.Pojo2;
import com.peirr.provider.sqlite.BaseDataStore;

public class MyDataStore extends BaseDataStore {

	public MyDataStore(Context context) {
		super(context);
	}

	@Override
	public Map<String, Object> getObjects() {
		Map<String,Object> objects = new HashMap<String,Object>();
		objects.put(Pojo.TABLE, new Pojo());
		objects.put(Pojo2.TABLE, new Pojo2());
		return objects;
	}
}

package com.peirr.provider.sqlite.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.peirr.provider.models.ParentData;
import com.peirr.provider.models.Pojo;
import com.peirr.provider.models.Pojo2;
import com.peirr.provider.sqlite.BaseDataStore;
import com.peirr.provider.sqlite.annotations.ObjectTable;

public class MyDataStore extends BaseDataStore {

	public MyDataStore(Context context) {
		super(context);
	}

	@Override
	public List<Class<? extends ObjectTable>> getDefinedClasses() {
		List<Class<? extends ObjectTable>> classes = new ArrayList<Class<? extends ObjectTable>>();
		classes.add(Pojo.class);
		classes.add(Pojo2.class);
		classes.add(ParentData.class);
		return classes;
	}
}

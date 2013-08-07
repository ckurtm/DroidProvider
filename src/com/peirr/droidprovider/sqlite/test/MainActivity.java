/*
 * Copyright (c) 2012 Peirr Mobility.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   ckurtm at gmail dot com
 *   https://github.com/ckurtm/PeirrContentProvider
 */
package com.peirr.droidprovider.sqlite.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.peirr.droidprovider.models.ChildData;
import com.peirr.droidprovider.models.ParentData;
import com.peirr.droidprovider.models.Pojo;
import com.peirr.droidprovider.sqlite.annotations.ProviderUtil;
import com.peirr.provider.R;

public class MainActivity extends Activity {
	String tag = MainActivity.class.getSimpleName();
	TextView msg1,msg2;
	List<ContentValues> contentValues = new ArrayList<ContentValues>();
	//    BaseSQLite base;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		msg1 = (TextView)findViewById(R.id.msg1);
		msg2 = (TextView)findViewById(R.id.msg2);
		//        base = new BaseSQLite(this);
		for(int i=0;i<30000;i++){
			ContentValues values = new ContentValues();
			values.put(Pojo.Mapper.pid,0);
			values.put(Pojo.Mapper.name,"xxx");
			contentValues.add(values);
		}

		findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					ChildData c1 = new ChildData();
					ProviderUtil.createDummyInstance(c1);
					ParentData pd = new ParentData("f1",3.0f,c1);
					Uri uri = getContentResolver().insert(ParentData.CONTENT_URI,ProviderUtil.getContentValues(pd));
					Log.d(tag,"[]+ " + uri);
				} catch (Exception e) {
					Log.d(tag,"error: " ,e);
				}

			}
		});


		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Cursor c = getContentResolver().query(ParentData.CONTENT_URI,null,null,null,null);

				List<ParentData> list;
				try {
					list = ProviderUtil.getRows(c,ParentData.class);
					for(ParentData parent:list){
						Log.d(tag,"got: " + parent);
					}
					msg2.setText("" + list.size());
				} catch (InstantiationException e) {
					Log.e(tag,"",e);
				} catch (IllegalAccessException e) {
					Log.e(tag,"",e);
				} catch (NoSuchFieldException e) {
					Log.e(tag,"",e);
				}

			}
		});
	}


}

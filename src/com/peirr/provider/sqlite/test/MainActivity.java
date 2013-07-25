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
package com.peirr.provider.sqlite.test;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.peirr.provider.R;
import com.peirr.provider.models.Pojo;
import com.peirr.provider.models.Pojo2;
import com.peirr.provider.sqlite.annotations.ObjectProcessor;

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
                    	ObjectProcessor op = new ObjectProcessor(null);
                        Pojo p2 = new Pojo();
                        Log.d(tag,"was: " + p2);
                        op.createTable(p2.getClass().getName(),p2.TABLE);
						Log.d(tag,"now: " + p2);
					} catch (Exception e) {
						Log.d(tag,"error: " ,e);
					}
                    
            }
        });


        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            
                Pojo2 p = new Pojo2();
                p.name ="opjo2";
                p.other = new Date();
//                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Pojo.CONTENT_URI);
//                builder.withValues(values);
//                ContentProviderOperation operation = builder.build();

                long start = System.currentTimeMillis();
                Uri uri = null;
				try {
					uri = getContentResolver().insert(Pojo2.CONTENT_URI,ObjectProcessor.getContentValues(p));
				} catch (Exception e) {
					Log.e(tag,"Error ",e);
				}
//                getContentResolver().bulkInsert(Pojo.CONTENT_URI,contentValues.toArray(new ContentValues[contentValues.size()]));
//                base.addBatchToTable("co.qchan.db",contentValues);
                long time = System.currentTimeMillis() - start;
                Log.d(tag,"time: " + time);
//                int rows = getContentResolver().delete(Pojo2.CONTENT_URI,Pojo.ObjectMapper._ID + ">?",new String[]{"5"});
//                int rows = getContentResolver().update(Pojo2.CONTENT_URI,values,Pojo2.ObjectMapper._ID + ">?",new String[]{"100"});
//               Cursor c = getContentResolver().query(Pojo2.CONTENT_URI,null,Pojo.ObjectMapper._ID + ">?",new String[]{"100"},null);
                msg2.setText("" + uri);
            }
        });
    }


}

/*
 *
 *  * Copyright (c) 2015 Kurt Mbanje.
 *  *
 *  *   Licensed under the Apache License, Version 2.0 (the "License");
 *  *   you may not use this file except in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *   Unless required by applicable law or agreed to in writing, software
 *  *   distributed under the License is distributed on an "AS IS" BASIS,
 *  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *   See the License for the specific language governing permissions and
 *  *   limitations under the License.
 *  *
 *  *   ckurtm at gmail dot com
 *  *   https://github.com/ckurtm/DroidProvider
 *
 */

package test.test.providersample;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.peirr.droidprovider.sqlite.annotations.ProviderUtil;
import com.peirr.droidprovider.test.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    String TAG = MainActivity.class.getSimpleName();

    private MyPojoAdapter adapter;
    private List<MyPojo> items = new ArrayList<>();
    private Button button;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        list = (ListView) findViewById(R.id.items);
        adapter = new MyPojoAdapter(this,R.layout.list_item,items);
        list.setAdapter(adapter);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        addNewRow();

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    private void addNewRow() {
        try {
            MyPojo pojo = ProviderUtil.createDummyInstance(MyPojo.class);
            getBaseContext().getContentResolver().insert(MyPojo.CONTENT_URI,ProviderUtil.getContentValues(pojo));
        } catch (Exception e) {
            Log.e(TAG, "error",e);
        }
        getLoaderManager().restartLoader(0,null,this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,MyPojo.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG,"onLoadFinished()");
        try {
            List<MyPojo> rows = ProviderUtil.getRows(cursor,MyPojo.class);
            Log.d(TAG,"found: " + rows.size());
            items.clear();
            items.addAll(rows);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

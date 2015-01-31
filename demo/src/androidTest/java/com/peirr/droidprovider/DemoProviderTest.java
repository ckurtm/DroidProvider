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

package com.peirr.droidprovider;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.peirr.droidprovider.sqlite.annotations.ProviderUtil;
import com.peirr.provider.DemoDataProvider;
import com.peirr.provider.DemoDataStore;
import com.peirr.provider.ProviderContract;

import test.test.providersample.MyPojo;


/**
 *
 * Created by kurt on 31 01 2015 .
 *
 */
public class DemoProviderTest extends ProviderTestCase2<DemoDataProvider> {
    private ContentResolver resolver;

    public DemoProviderTest() {
        super(DemoDataProvider.class, ProviderContract.CONTENT_AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getContext().getContentResolver();
    }

    public void testInsertDelete() throws Exception {
        //INSERT
        MyPojo item = ProviderUtil.createDummyInstance(MyPojo.class);
        Uri uri = resolver.insert(MyPojo.CONTENT_URI, ProviderUtil.getContentValues(item, false));
        assertNotNull(uri);
        //DELETE
        long id = ContentUris.parseId(uri);
        item._id = id;
        int result = resolver.delete(MyPojo.CONTENT_URI, MyPojo.Mapper._ID + "=?", new String[]{String.valueOf(id)});
        assertEquals(1, result);
    }


    public void testInsertUpdateQueryDelete() throws Exception {
        //INSERT
        MyPojo item = ProviderUtil.createDummyInstance(MyPojo.class);
        Uri uri = resolver.insert(MyPojo.CONTENT_URI, ProviderUtil.getContentValues(item, false));
        assertNotNull(uri);
        long id = ContentUris.parseId(uri);
//        item._id = id;

        //UPDATE
        item.myint = 30;
        ContentValues values = ProviderUtil.getContentValues(item, true);
        int updates = resolver.update(MyPojo.CONTENT_URI, values, MyPojo.Mapper._ID + "=?", new String[]{String.valueOf(id)});
        assertEquals(1, updates);

        //QUERY
        Cursor cursor = resolver.query(MyPojo.CONTENT_URI, null, MyPojo.Mapper._ID + "=?", new String[]{String.valueOf(id)}, null);
        cursor.moveToNext();
        MyPojo upItem = ProviderUtil.getRow(cursor, MyPojo.class);
        assertNotNull(upItem);
        assertEquals(30, upItem.myint);

        //DELETE
        item._id = id;
        int result = resolver.delete(MyPojo.CONTENT_URI, MyPojo.Mapper._ID + "=?", new String[]{String.valueOf(id)});
        assertEquals(1, result);

    }


    public void testCanGetProvider() throws Exception {
        ContentProviderClient client = resolver.acquireContentProviderClient(ProviderContract.CONTENT_AUTHORITY);
        assertNotNull(client);
        ContentProvider contentProvider = client.getLocalContentProvider();
        assertNotNull(contentProvider);
        DemoDataProvider provider = (DemoDataProvider) contentProvider;
        assertNotNull(provider);
        DemoDataStore dataStore = (DemoDataStore) provider.getMyDB();
        assertNotNull(dataStore);
    }
}

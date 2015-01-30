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

package com.peirr.droidprovider.sqlite;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.peirr.droidprovider.sqlite.annotations.ObjectRow;
import com.peirr.droidprovider.sqlite.annotations.ProviderUtil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * This is the database util class for handling reads & writes to app sqlite db
 *
 * @author kurt
 * @playing [Say My Name (Cyril Hahn Remix)]
 */
public abstract class BaseDataStore extends SQLiteSecureHelper {
    String tag = BaseDataStore.class.getSimpleName();
    private List<ProviderObjectValue> objectValues = new ArrayList<ProviderObjectValue>();
    private Map<String, Class<?>> definedObjs;
    private Context context;

    public BaseDataStore(Context context) {
        super(context);
        this.context = context;
        try {
            if (objectValues.size() == 0) {
                createObjectValues();
            }
        } catch (Exception e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public abstract List<Class<? extends ObjectRow>> getDefinedClasses();

    public List<ProviderObjectValue> getObjectValues() {
        return objectValues;
    }

    public Map<String, Class<?>> getDefinedObjects() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Map<String, Class<?>> objects = new HashMap<String, Class<?>>();
        List<Class<? extends ObjectRow>> definedClasses = getDefinedClasses();
        for (Class<?> clazz : definedClasses) {
            String[] data = ProviderUtil.getMetaDaTa(clazz);
            objects.put(data[0], clazz);
        }
        return objects;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        try {
            List<ProviderObjectValue> objectVals = getObjectValues();
            if (definedObjs == null) {
                definedObjs = getDefinedObjects();
            }
            for (ProviderObjectValue value : objectVals) {
                proc.createTable(definedObjs.get(value.TABLE), value.TABLE);
            }
        } catch (Exception e) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    /**
     * This creates the {@link ProviderObjectValue}s that are used by the {@link BaseProvider} to
     * map the objects to the corresponding tables.
     *
     * @throws java.security.NoSuchAlgorithmException
     * @throws NoSuchFieldException
     * @throws java.security.InvalidKeyException
     * @throws IllegalAccessException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws javax.crypto.BadPaddingException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.io.UnsupportedEncodingException
     * @throws ClassNotFoundException
     * @throws javax.crypto.IllegalBlockSizeException
     */
    public void createObjectValues() throws NoSuchAlgorithmException, NoSuchFieldException, InvalidKeyException, IllegalAccessException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, ClassNotFoundException, IllegalBlockSizeException {
        objectValues.clear();
        int i = 0;
        if (definedObjs == null) {
            definedObjs = getDefinedObjects();
        }
        for (String key : definedObjs.keySet()) {
            ProviderObjectValue pv = ProviderUtil.getProviderValues(definedObjs.get(key));
//			Log.d(tag,"[]+ " + pv);
            pv.ONE = (i + 2) - 1;
            pv.MANY = (i + 2);
            objectValues.add(pv);
            i++;
        }
    }

    public Context getContext() {
        return context;
    }

    /**
     * Add a new row into the provided table
     *
     * @param values - the data to insert
     * @param uri    - the tables content uri
     * @return insertion id
     */
    public long addRowToTable(Uri uri, ContentValues values) {
        Uri resUri = ctx.getApplicationContext().getContentResolver().insert(uri, values);
        return ContentUris.parseId(resUri);
    }


    public int deleteRow(Uri uri, long id) {
        Uri nuri = ContentUris.withAppendedId(uri, id);
        return ctx.getApplicationContext().getContentResolver().delete(nuri, null, null);
    }

    public int deleteRow(Uri uri, String key, String value, ContentValues values) {
        return ctx.getApplicationContext().getContentResolver().delete(uri, key + "=?", new String[]{value});
    }

    public boolean exists(Uri uri, HashMap<String, String> keyValues) {
        boolean exists = false;
        StringBuilder query = new StringBuilder();
        for (String key : keyValues.keySet()) {
            query.append(String.format("%s = '%s' AND ", key, keyValues.get(key)));
        }
        query.delete(query.lastIndexOf("AND"), query.length());
        Cursor c = ctx.getContentResolver().query(uri, null, query.toString(), null, null);

        if (c != null) {
            if (c.getCount() > 0) {
                exists = true;
            }
            c.close();
        }

        return exists;
    }

    public boolean exists(Uri uri, String key, String value) {
        boolean exists = false;
        Cursor c = ctx.getApplicationContext().getContentResolver().query(uri, null, key + "=?", new String[]{value}, null);
        if (c != null) {
            if (c.getCount() > 0) {
                exists = true;
            }
            c.close();
        }
        return exists;
    }


    public int updateRow(Uri uri, long id, ContentValues values) {
        Uri nuri = ContentUris.withAppendedId(uri, id);
        //		Log.d(tag,"update: " + nuri);
        return ctx.getApplicationContext().getContentResolver().update(nuri, values, null, null);
    }

    public int updateRow(Uri uri, String key, String value, ContentValues values) {
        return ctx.getApplicationContext().getContentResolver().update(uri, values, key + "=?", new String[]{value});
    }

    /**
     * Add a new <b>rows</b> into the provided table
     *
     * @param values - the data to insert
     * @param uri    - Content URI of the table
     * @return true if insertion was succesful else false
     */
    public boolean addRowsToTable(Uri uri, List<ContentValues> values) {
        //TODO use http://developer.android.com/reference/android/provider/ContactsContract.RawContacts.html getContentResolver().applyBatch(..)
        Log.d(tag, "addRowsToTable() [uri:" + uri + "] [values:" + values.size() + "] ...");
        int rows = ctx.getApplicationContext().getContentResolver().bulkInsert(uri, values.toArray(new ContentValues[values.size()]));
        if (rows == values.size()) {
            Log.d(tag, "SUCCEDED bulk insert ...");
            return true;
        }
        Log.d(tag, "FAILED bulk insert ...");
        return false;
    }

    //TODO optimise this batch insert method.

    /**
     * This is not yet optimised to notify the underlying content provider that data has changed at a broader level,
     * USE AT YOUR OWN RISK , as notification of change is made @ an atomic level, i.e. for each row added a notification
     * is sent back to all loaders that are listening for changes.
     *
     * @param authority
     * @param values
     * @return
     */
    public boolean addBatchToTable(Uri uri, String authority, List<ContentValues> values) {
        boolean success = false;
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
        for (ContentValues cv : values) {
            builder.withValues(cv);
            operations.add(builder.build());
        }
        try {
            ContentProviderResult[] results = ctx.getApplicationContext().getContentResolver().applyBatch(authority, operations);
            if (results.length == values.size()) {
                success = true;
            }
        } catch (RemoteException e) {
            Log.e(tag, e.getMessage(), e);
        } catch (OperationApplicationException e) {
            Log.e(tag, e.getMessage(), e);
        }
        return success;
    }


}

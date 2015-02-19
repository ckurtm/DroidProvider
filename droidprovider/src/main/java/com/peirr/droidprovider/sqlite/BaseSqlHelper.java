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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
 * This is the database util class for handling reads and writes to app sqlite db
 *
 * @author kurt
 *         PLAYING [Say My Name (Cyril Hahn Remix)]
 */
public abstract class BaseSqlHelper extends SQLiteSecureHelper {
    String tag = BaseSqlHelper.class.getSimpleName();
    private List<ProviderObjectValue> objectValues = new ArrayList<>();
    private Map<String, Class<?>> definedObjs;
    private Context context;

    /**
     * init
     *
     * @param context The context
     * @param dbFile  the db file name
     */
    public BaseSqlHelper(Context context, String dbFile) {
        super(context, dbFile);
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

    /**
     * get all object values (internal use)
     *
     * @return list of the provider object values
     */
    public List<ProviderObjectValue> getObjectValues() {
        return objectValues;
    }

    /**
     * gets the list of all defined objects within the project
     *
     * @return the list
     * @throws IllegalArgumentException if something goes wrong
     * @throws NoSuchFieldException     if something goes wrong
     * @throws IllegalAccessException   if something goes wrong
     */
    public Map<String, Class<?>> getDefinedObjects() throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Map<String, Class<?>> objects = new HashMap<>();
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
     * @throws NoSuchAlgorithmException           if something goes wrong
     * @throws NoSuchFieldException               if something goes wrong
     * @throws InvalidKeyException                if something goes wrong
     * @throws IllegalAccessException             if something goes wrong
     * @throws NoSuchPaddingException             if something goes wrong
     * @throws BadPaddingException                if something goes wrong
     * @throws InvalidAlgorithmParameterException if something goes wrong
     * @throws IllegalAccessException             if something goes wrong
     * @throws NoSuchPaddingException             if something goes wrong
     * @throws BadPaddingException                if something goes wrong
     * @throws InvalidAlgorithmParameterException if something goes wrong
     * @throws UnsupportedEncodingException       if something goes wrong
     * @throws ClassNotFoundException             if something goes wrong
     * @throws IllegalBlockSizeException          if something goes wrong
     */
    public void createObjectValues() throws NoSuchAlgorithmException, NoSuchFieldException, InvalidKeyException,
            IllegalAccessException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException, ClassNotFoundException, IllegalBlockSizeException {
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

    /**
     * gets current context
     *
     * @return current context
     */
    public Context getContext() {
        return context;
    }

    /**
     * checks if value exists in DB
     *
     * @param uri   table Content URI
     * @param key   column name
     * @param value column value
     * @return true if it exists
     */
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


}

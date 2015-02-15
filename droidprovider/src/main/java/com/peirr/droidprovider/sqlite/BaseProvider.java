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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.List;


/**
 * This is a dynamic content Provider that maps all objects that were instantiated in the {@link BaseDataStore} class as tables
 * for this app.
 *
 * @author kurt
 */
public abstract class BaseProvider extends ContentProvider {

    String TAG = BaseProvider.class.getSimpleName();

    public static final int PROVIDE_TABLE = 0x029;
    public static final int PROVIDE_URI = 0x030;
    public static final int PROVIDE_KEY = 0x035;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private BaseDataStore sqLite;

    public static Uri getContentUri(String contentString) {
        String string = contentString.replace("#AUTHORITY#", DroidProviderContract.CONTENT_AUTHORITY);
        return Uri.parse(string);
    }


    @Override
    public boolean onCreate() {
        sqLite = getMyDB();
        String authority = DroidProviderContract.CONTENT_AUTHORITY;
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            sURIMatcher.addURI(authority, pv.TABLE, pv.MANY);
            sURIMatcher.addURI(authority, pv.TABLE + "/#", pv.ONE);
        }
        return true;
    }

    public abstract BaseDataStore getMyDB();

    /**
     * gets the name of the current db
     *
     * @return the name of the current database
     */
    public String getCurrentDB() {
        return BaseDataStore.DATABASE;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        int rowsAffected = 0;
        boolean found = false;
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if (uriType == pv.MANY || uriType == pv.ONE) {
                found = true;
                rowsAffected = pv.getDeletedRows(uri, uriType, sqlDB, selection, selectionArgs);
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
//        Log.d(TAG, "notify delete(" + rowsAffected + ")");
        notifyChange(uri);
        return rowsAffected;
    }


    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if (uriType == pv.MANY) {
                return pv.TYPE;
            }
            if (uriType == pv.ONE) {
                return pv.ITEM_TYPE;
            }
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
//    	Log.d(tag,"insert: " + uri);
        int uriType = sURIMatcher.match(uri);
        boolean found = false;
        String table = "";
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if (uriType == pv.MANY) {
                table = pv.TABLE;
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Invalid URI for insertion");
        }
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        long newID = sqlDB.insert(table, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
//            Log.d(TAG, "notify insert()");
            notifyChange(uri);
            return newUri;
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        int rowsAffected = 0;
        boolean found = false;
        String table = "";
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if (uriType == pv.MANY) {
                found = true;
                table = pv.TABLE;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown or Invalid URI for bulkInsert " + uri);
        }
        for (ContentValues cv : values) {
            long newID = sqlDB.insert(table, null, cv);
            if (newID > 0) {
                rowsAffected += 1;
            }
        }
        notifyChange(uri);
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        boolean found = false;
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if ((uriType == pv.ONE) || (uriType == pv.MANY)) {
                queryBuilder.setTables(pv.TABLE);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(sqLite.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        registerUri(cursor, uri);
        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        int rowsAffected = 0;
        boolean found = false;
        List<ProviderObjectValue> valueList = sqLite.getObjectValues();
        for (ProviderObjectValue pv : valueList) {
            if (uriType == pv.MANY || uriType == pv.ONE) {
                found = true;
                rowsAffected = pv.getUpdatedRows(uri, values, uriType, sqlDB, selection, selectionArgs);
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        notifyChange(uri);
        return rowsAffected;
    }

    private void notifyChange(Uri uri) {
//        Log.d(TAG, "notifyChange() [uri: " + uri + "]");
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private void registerUri(Cursor cursor, Uri uri) {
//        Log.d(TAG, "registerUri() [uri: " + uri + "]");
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
    }
}

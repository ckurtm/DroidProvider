/*
 * Copyright 2012 Peirr Mobility.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ckurtm at gmail dot com
 */
package com.peirr.provider.sqlite;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * This is a dynamic content Provider that maps all objects that were instatiated in the {@link BaseSQLite} class as tables
 * for this app.
 * @author kurt
 * 
 */
public class BaseProvider extends ContentProvider {

    static String tag = BaseProvider.class.getSimpleName();
    public static final int PROVIDE_BASE = 0x029;
    public static final int PROVIDE_URI = 0x030;
    public static final int PROVIDE_ITEM_TYPE = 0x031;
    public static final int PROVIDE_TYPE = 0x032;
    public static final int PROVIDE_ONE = 0x033;
    public static final int PROVIDE_MANY = 0x034;
    public static final int PROVIDE_KEY = 0x035;

    private BaseSQLite sqLite;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static String AUTHORITY = "za.co.standardbank.ost.db2";
    @Override
    public boolean onCreate() {
        sqLite = new BaseSQLite(getContext());
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            Log.d(tag,"pv: " + pv);
            sURIMatcher.addURI(AUTHORITY,pv.BASE,pv.MANY);
            sURIMatcher.addURI(AUTHORITY,pv.BASE + "/#",pv.ONE);
        }
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        int rowsAffected = 0;
        boolean found = false;
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if(uriType == pv.MANY || uriType == pv.ONE){
                found = true;
                rowsAffected = pv.getDeletedRows(uri,uriType,sqlDB,selection,selectionArgs);
                break;
            }
        }
        if(!found){
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if(uriType == pv.MANY){
                return pv.TYPE;
            }
            if(uriType == pv.ONE){
                return pv.ITEM_TYPE;
            }
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        boolean found = false;
        String table = "";
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if(uriType == pv.MANY){
                table = pv.BASE;
                found = true;
                break;
            }
        }
        if (!found){
            throw new IllegalArgumentException("Invalid URI for insertion");
        }
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        long newID = sqlDB.insert(table, null, values);
        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
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
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if(uriType == pv.MANY){
                found = true;
                table = pv.BASE;
                break;
            }
        }
        if(!found){
            throw new IllegalArgumentException("Unknown or Invalid URI for bulkInsert " + uri);
        }
        for(int i=0;i<values.length;i++){
            ContentValues cv = values[i];
            long newID = sqlDB.insert(table, null, cv);
            if (newID > 0) {
                rowsAffected +=1;
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        boolean found = false;

        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if((uriType == pv.ONE) || (uriType == pv.MANY)){
                queryBuilder.setTables(pv.BASE);
                found = true;
                break;
            }
            if(uriType == pv.ONE){
                queryBuilder.appendWhere(pv.KEY + "=" + uri.getLastPathSegment());
            }
        }

        if(!found){
            throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(sqLite.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = sqLite.getWritableDatabase();
        int rowsAffected = 0;
        boolean found = false;
        for(ProviderObjectValue pv:sqLite.getObjectValues()){
            if(uriType == pv.MANY || uriType == pv.ONE){
                found = true;
                rowsAffected = pv.getUpdatedRows(uri,values,uriType,sqlDB,selection,selectionArgs);
                break;
            }
        }
        if(!found){
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }




    public static final Uri getContentUri(String contentString){
        String string = contentString.replace("#AUTHORITY#", AUTHORITY);
        Uri uri = Uri.parse(string);
        return uri;
    }
}

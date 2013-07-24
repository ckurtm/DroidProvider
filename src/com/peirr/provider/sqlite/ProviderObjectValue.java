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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
/**
 *   
 * @author kurt 
 * PeirrContentProvider
 */
public class ProviderObjectValue {
    public int MANY;
    public int ONE;
    public String TYPE;
    public String ITEM_TYPE;
    public String KEY;
    public String BASE;
    public Uri URI;

    public int getDeletedRows(Uri uri,int type,SQLiteDatabase db,String selection,String[] args){
        int rowsAffected = 0;
        if(type == MANY){
            rowsAffected = db.delete(BASE,selection,args);
        }else if(type == ONE){
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = db.delete(BASE,KEY + "=" + id, null);
            } else {
                rowsAffected = db.delete(BASE,selection + " and " + KEY + "=" + id,args);
            }
        }
        return rowsAffected;
    }

    public int getUpdatedRows(Uri uri,ContentValues values,int type,SQLiteDatabase db,String selection,String[] args){
        int rowsAffected = 0;
        if(type == ONE){
            String id = uri.getLastPathSegment();
            StringBuilder modSelection = new StringBuilder(KEY + "=" + id);
            if (!TextUtils.isEmpty(selection)) {
                modSelection.append(" AND " + selection);
            }
            rowsAffected = db.update(BASE,values, modSelection.toString(), null);
        }else if(type == MANY){
            rowsAffected =db.update(BASE,values, selection, args);
        }
        return rowsAffected;
    }

    @Override
    public String toString() {
        return "ProviderObjectValue{" +
                "MANY=" + MANY +
                ", ONE=" + ONE +
                ", TYPE='" + TYPE + '\'' +
                ", ITEM_TYPE='" + ITEM_TYPE + '\'' +
                ", KEY='" + KEY + '\'' +
                ", BASE='" + BASE + '\'' +
                ", URI=" + URI +
                '}';
    }
}

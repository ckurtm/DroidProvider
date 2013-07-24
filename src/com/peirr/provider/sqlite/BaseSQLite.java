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

package com.peirr.provider.sqlite;

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

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.peirr.provider.models.Pojo;
import com.peirr.provider.models.Pojo2;
import com.peirr.provider.sqlite.annotations.ObjectProcessor;

/**
 * This is the database util class for handling reads & writes to app sqlite db
 * @playing [Say My Name (Cyril Hahn Remix)]
 * @author kurt
 *
 */
public class BaseSQLite extends SQLiteSecureHelper {



	private static Map<String,Object> objects = new HashMap<String,Object>();
	private List<ProviderObjectValue> objectValues = new ArrayList<ProviderObjectValue>();
	/**
	 * This is where you add your objects that are "DB aware"
	 */
	static {
		objects.put(Pojo.TABLE, new Pojo());
		objects.put(Pojo2.TABLE, new Pojo2());
	}

	String tag = BaseSQLite.class.getSimpleName();
	public List<ProviderObjectValue> getObjectValues() {
		return objectValues;
	}

	public BaseSQLite(Context context) {
		super(context);
		try {
			if(objectValues.size() == 0){
				createObjectValues();
			}
		} catch (ClassNotFoundException e) {
			Log.e(tag,e.getMessage(),e);
		} catch (NoSuchFieldException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (IllegalAccessException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (BadPaddingException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			Log.e(tag, e.getMessage(), e);
		} catch (InvalidKeyException e) {
			Log.e(tag, e.getMessage(), e);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		try {
			for(ProviderObjectValue value:getObjectValues()){
				proc.createTable(objects.get(value.TABLE).getClass().getName(),value.TABLE);
			}
		} catch (ClassNotFoundException e) {
			Log.e(tag, e.getMessage(), e);
		}
	}

	/**
	 * This creates the {@link ProviderObjectValue}s that are used by the {@link BaseProvider} to
	 * map the objects to the corresponding tables.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchFieldException
	 * @throws InvalidKeyException
	 * @throws IllegalAccessException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException
	 * @throws IllegalBlockSizeException
	 */
	public void createObjectValues() throws NoSuchAlgorithmException, NoSuchFieldException, InvalidKeyException, IllegalAccessException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, ClassNotFoundException, IllegalBlockSizeException {
		Log.d(tag,"createObjectValues()");
		objectValues.clear();
		int i = 0;
		for(String key: objects.keySet()){
			ProviderObjectValue pv = ObjectProcessor.getProviderValues(objects.get(key));
			Log.d(tag,"[]+ " + pv);
			pv.ONE = (i+2) -1;
			pv.MANY = (i+2);
			objectValues.add(pv);
			i++;
		}
	}

	/**
	 * Add a new row into the provided table
	 * @param values - the data to insert
	 * @param uri    - the tables content uri
	 * @return insertion id
	 */
	public long addRowToTable(Uri uri,ContentValues values){
		Uri resUri = ctx.getApplicationContext().getContentResolver().insert(uri,values);
		return ContentUris.parseId(resUri);
	}


	public int deleteRow(Uri uri,long id){
		Uri nuri = ContentUris.withAppendedId(uri,id);
		return ctx.getApplicationContext().getContentResolver().delete(nuri, null, null);
	}

	public int deleteRow(Uri uri,String key,String value,ContentValues values){
		return ctx.getApplicationContext().getContentResolver().delete(uri,key+"=?",new String[]{value});
	}

	public int updateRow(Uri uri,long id,ContentValues values){
		Uri nuri = ContentUris.withAppendedId(uri,id);
		//		Log.d(tag,"update: " + nuri);
		return ctx.getApplicationContext().getContentResolver().update(nuri, values,null,null);
	}

	public int updateRow(Uri uri,String key,String value,ContentValues values){
		return ctx.getApplicationContext().getContentResolver().update(uri, values,key+"=?",new String[]{value});
	}

	/**
	 * Add a new <b>rows</b> into the provided table
	 * @param values - the data to insert
	 * @param uri    - Content URI of the table
	 * @return true if insertion was succesful else false
	 */
	public boolean addRowsToTable(Uri uri,List<ContentValues> values){
		//TODO use http://developer.android.com/reference/android/provider/ContactsContract.RawContacts.html getContentResolver().applyBatch(..)
		Log.d(tag,"addRowsToTable() [uri:"+uri+"] [values:"+values.size()+"] ...");
		int rows = ctx.getApplicationContext().getContentResolver().bulkInsert(uri,values.toArray(new ContentValues[values.size()]));
		if(rows == values.size()){
			Log.d(tag,"SUCCEDED bulk insert ...");
			return true;
		}
		Log.d(tag,"FAILED bulk insert ...");
		return false;
	}

	//TODO optimise this batch insert method.
	/**
	 * This is not yet optimised to notify the underlying content provider that data has changed at a broader level,
	 * USE AT YOUR OWN RISK , as notification of change is made @ an atomic level, i.e. for each row added a notification
	 * is sent back to all loaders that are listening for changes.
	 * @param authority
	 * @param values
	 * @return
	 */
	public boolean addBatchToTable(Uri uri,String authority,List<ContentValues> values)  {
		boolean success = false;
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
		for(ContentValues cv:values){
			builder.withValues(cv);
			operations.add(builder.build());
		}
		try {
			ContentProviderResult[] results = ctx.getApplicationContext().getContentResolver().applyBatch(authority,operations);
			if(results.length == values.size()){
				success = true;
			}
		} catch (RemoteException e) {
			Log.e(tag,e.getMessage() ,e);
		} catch (OperationApplicationException e) {
			Log.e(tag,e.getMessage() ,e);
		}
		return success;
	}





}

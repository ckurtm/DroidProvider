package com.peirr.provider.test;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import com.peirr.provider.sqlite.Config;
import com.peirr.provider.sqlite.ProviderObjectValue;
import com.peirr.provider.sqlite.annotations.ObjectProcessor;
import com.peirr.provider.sqlite.test.MyDataStore;
import com.peirr.provider.sqlite.test.MyProvider;

public class MyProviderTestCase extends ProviderTestCase2<MyProvider> {
	String tag = MyProviderTestCase.class.getSimpleName();
	// Contains a reference to the mocked content resolver for the provider under test.
	private ContentResolver resolver;
	// Contains an SQLite database, used as test data
	private SQLiteDatabase mDb;
	private MyDataStore ds;
	//the test objects that were defined as table aware
	Map<String, Object> objectMap = new HashMap<String, Object>();

	public MyProviderTestCase(Class<MyProvider> providerClass,String providerAuthority) {
		super(providerClass, providerAuthority);
	}

	public MyProviderTestCase() {
		super(MyProvider.class,Config.AUTHORITY);
	}

	/*
	 * Sets up the test environment before each test method. note that I use the real database for now.
	 * too lazy to read about mockcontent resolver yada yada. let me know if you have a better way of doing this.
	 * https://plus.google.com/101764998634567436621 
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Log.d(tag,"setUp");
		resolver = getContext().getContentResolver();
		ds = new MyDataStore(getContext());
		mDb = ds.getReadableDatabase();
		objectMap = ds.getObjects();
		//delete all matching tables before we start testing
		for(ProviderObjectValue value:ds.getObjectValues()){
			Log.d(tag,"[]+ " + objectMap.get(value.TABLE).getClass());
			mDb.delete(value.TABLE,null,null);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * this tests to see if we can do inserts on all the different objects :)
	 * @throws Exception
	 */
	public void testInsertAndQuery() throws Exception {
		for(ProviderObjectValue value:ds.getObjectValues()){
			Object obj = objectMap.get(value.TABLE);
			//create a dummy instance with values we can track for this object
			ObjectProcessor.createDummyInstance(obj);
			Log.d(tag,"testInsertAndQuery(): " + obj.getClass());
			resolver.insert(value.URI,ObjectProcessor.getContentValues(obj));
			// table should only contain the record just inserted.
			Cursor cursor = resolver.query(value.URI,null,null,null,null);
			// Asserts that there should be only 1 record.
			assertEquals(1, cursor.getCount());		
			assertTrue(cursor.moveToFirst());
			// Tests each column in the returned cursor against the data that was inserted, comparing
			//			assertEquals(TEST_DATA[0].getPid(),cursor.getLong(cursor.getColumnIndex(Pojo.Mapper.pid)));
			//			assertEquals(TEST_DATA[0].getName(),cursor.getString(cursor.getColumnIndex(Pojo.Mapper.name)));
		}
	}


	public void testInsertAndDelete() throws Exception {
		for(ProviderObjectValue value:ds.getObjectValues()){
			Object obj = objectMap.get(value.TABLE);
			//create a dummy instance with values we can track for this object
			ObjectProcessor.createDummyInstance(obj);
			Log.d(tag,"testInsertAndDelete(): " + obj.getClass());
			//insert the data into db
			Uri uri = resolver.insert(value.URI,ObjectProcessor.getContentValues(obj));
			assertNotNull(uri);
			long id = ContentUris.parseId(uri);
			int deleted = resolver.delete(value.URI,"_id=?",new String[]{String.valueOf(id)});
			//we should be able to delete what we insert :)
			assertEquals(1,deleted);	
		}
	}


	public void testInsertAndQueryAndUpdate() throws Exception {
		for(ProviderObjectValue value:ds.getObjectValues()){
			Object obj = objectMap.get(value.TABLE);
			//create a dummy instance with values we can track for this object
			ObjectProcessor.createDummyInstance(obj);
			Log.d(tag,"testInsertAndQueryAndUpdate(): " + obj.getClass());
			Uri uri = resolver.insert(value.URI,ObjectProcessor.getContentValues(obj));
			assertNotNull(uri);
			long id = ContentUris.parseId(uri);
			ContentValues values = ObjectProcessor.getContentValues(obj);
			int updated = resolver.update(value.URI,values,"_id=?",new String[]{String.valueOf(id)});
			assertEquals(1,updated);
			Cursor c = resolver.query(value.URI,null,"_id=?",new String[]{String.valueOf(id)},null);
			assertNotNull(c);
			assertEquals(1,c.getCount());
		}
	}



}

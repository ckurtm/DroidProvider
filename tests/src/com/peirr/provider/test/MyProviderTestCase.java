package com.peirr.provider.test;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.peirr.provider.models.Pojo;
import com.peirr.provider.sqlite.Config;
import com.peirr.provider.sqlite.annotations.ObjectProcessor;
import com.peirr.provider.sqlite.test.MyDataStore;
import com.peirr.provider.sqlite.test.MyProvider;

public class MyProviderTestCase extends ProviderTestCase2<MyProvider> {

	// A URI that the provider does not offer, for testing error handling.
	private static final Uri INVALID_URI =  Uri.withAppendedPath(Pojo.CONTENT_URI, "invalid");
	// Contains a reference to the mocked content resolver for the provider under test.
	private ContentResolver resolver;
	// Contains an SQLite database, used as test data
	private SQLiteDatabase mDb;

	Pojo[] TEST_DATA = new Pojo[]{
			new Pojo(1,"one"),
			new Pojo(2,"two"),
	};

	public MyProviderTestCase(Class<MyProvider> providerClass,String providerAuthority) {
		super(providerClass, providerAuthority);
	}

	public MyProviderTestCase() {
		super(MyProvider.class,Config.AUTHORITY);
	}


	/*
	 * Sets up the test environment before each test method. Creates a mock content resolver,
	 * gets the provider under test, and creates a new database for the provider.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		resolver = getContext().getContentResolver();
		mDb = new MyDataStore(getContext()).getReadableDatabase();
		mDb.delete(Pojo.TABLE,null,null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testInsertAndQuery() throws Exception {
		resolver.insert(Pojo.CONTENT_URI,ObjectProcessor.getContentValues(TEST_DATA[0]));
		// table should only contain the record just inserted.
		Cursor cursor = resolver.query(Pojo.CONTENT_URI,null,null,null,null);
		// Asserts that there should be only 1 record.
		assertEquals(1, cursor.getCount());		
		assertTrue(cursor.moveToFirst());
		// Tests each column in the returned cursor against the data that was inserted, comparing
		// the field in the NoteInfo object to the data at the column index in the cursor.
		assertEquals(TEST_DATA[0].getPid(),cursor.getLong(cursor.getColumnIndex(Pojo.Mapper.pid)));
		assertEquals(TEST_DATA[0].getName(),cursor.getString(cursor.getColumnIndex(Pojo.Mapper.name)));
	}


	public void testInsertAndDelete() throws Exception {
		resolver.insert(Pojo.CONTENT_URI,ObjectProcessor.getContentValues(TEST_DATA[0]));
		int deleted = resolver.delete(Pojo.CONTENT_URI,Pojo.Mapper.pid + "=?",new String[]{TEST_DATA[0].getPid()+""});
		assertEquals(1,deleted);		
	}
	
	
	public void testInsertAndQueryAndUpdate() throws Exception {
		Uri uri = resolver.insert(Pojo.CONTENT_URI,ObjectProcessor.getContentValues(TEST_DATA[0]));
		assertNotNull(uri);
		long old = TEST_DATA[0].getPid();
		ContentValues values = ObjectProcessor.getContentValues(TEST_DATA[0]);
		values.put(Pojo.Mapper.pid,33);
		int updated = resolver.update(Pojo.CONTENT_URI,values,Pojo.Mapper.pid + "=?",new String[]{old+""});
        assertEquals(1,updated);
        Cursor c = resolver.query(Pojo.CONTENT_URI,null,Pojo.Mapper.pid + "=?",new String[]{33+""},null);
        assertNotNull(c);
        assertEquals(1,c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(33,c.getLong(c.getColumnIndex(Pojo.Mapper.pid)));
		assertEquals(TEST_DATA[0].getName(),c.getString(c.getColumnIndex(Pojo.Mapper.name)));
        
	}



}

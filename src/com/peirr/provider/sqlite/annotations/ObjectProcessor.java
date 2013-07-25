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
package com.peirr.provider.sqlite.annotations;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.ProviderObjectValue;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This processes the Annotations on the Provider enabled classes
 * @author kurt 
 * 
 */
public class ObjectProcessor {
	protected SQLiteDatabase db;
	protected SecretKey k;
	static String tag = ObjectProcessor.class.getSimpleName();
	
	
	public static final String TEST_STRING = "SSSS";
	public static final int TEST_INTEGER = 1000;
	public static final long TEST_LONG = 1111l;
	public static final boolean TEST_BOOLEAN = true;
	public static final float TEST_FLOAT = 0.22f;
	public static final float TEST_DOUBLE = 0.55555f;
	public static final Date TEST_DATE = new Date(123456);

	public ObjectProcessor(SecretKey k) {
		this.k = k;
	}

	public ObjectProcessor(SQLiteDatabase db, SecretKey k) {
		this.db = db;
		this.k = k;
	}

	/**
	 * Create a table via reflection of a class anotated using the {@link Column} and {@link Index} anotations.
	 * @param className
	 * @throws ClassNotFoundException
	 */
	@SuppressLint("DefaultLocale")
	public void createTable(String className,String table) throws ClassNotFoundException {
		Log.d(tag,"createTableFromClass [class:"+className+"][table:"+table+"]");
		List<Field> fields = new ArrayList<Field>();
		StringBuilder queryBuilder = new StringBuilder();
		Class<?> clazz = Class.forName(className);
		Field[] privateFields = clazz.getDeclaredFields();
		Field[] publicFields = clazz.getFields();
		if(privateFields != null){
			for(Field pf:privateFields){
				fields.add(pf);
			}
		}
		if(publicFields != null){
			for(Field pf:publicFields){
				if(!fields.contains(pf)){
					fields.add(pf);
				}
			}
		}
		//		String name = clazz.getSimpleName();
		queryBuilder.append("CREATE TABLE " + table + " (");
		//		LOG.d(tag,"fields: " + fields);
		boolean firstField = true;
		boolean added = false;
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Column.class);
			if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if (!firstField) {
					queryBuilder.append(", ");
				}
				if (annotation instanceof Column) {
					Column col = (Column)annotation;
					queryBuilder.append(col.n() + " ");
					added = true;
				}

				if (String.class.isAssignableFrom(field.getType())) {
					queryBuilder.append("TEXT");
				}

				if (field.getType() == Integer.TYPE) {
					queryBuilder.append("INTEGER");
				}else if (field.getType() == Long.TYPE) {
					queryBuilder.append("INTEGER");
				}else if (field.getType() == Boolean.TYPE) {
					queryBuilder.append("INTEGER");
				}else if (field.getType() == Float.TYPE) {
					queryBuilder.append("REAL");
				}else if (field.getType() == Double.TYPE) {
					queryBuilder.append("REAL");
				}else if(Date.class.isAssignableFrom(field.getType())){
					queryBuilder.append("INTEGER");
				}
			}

			Annotation annotationAtr = field.getAnnotation(Index.class);
			if (annotationAtr != null) {
				if (annotationAtr instanceof Index) {
					Index attr = (Index) annotationAtr;
					if (attr.primaryKey())
						queryBuilder.append(" PRIMARY KEY");
				}
			}
			if(added){
				firstField = false;
			}
		}
		queryBuilder.append(");");
		String query = queryBuilder.toString();
		Log.d(tag, "TABLE CREATION: " + query);
		db.execSQL(query);
	}


	/**
	 * Creates a {@link android.content.ContentValues} instance for the given Class that uses the {@link Column} annotation to define fields.
	 * @param obj
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ContentValues getContentValues(Object obj) throws Exception{
		//        LOG.d(tag,"getContentValues: " + obj);
		ContentValues cv =  new ContentValues();
		List<Field> fields = new ArrayList<Field>();
		Class<?> clazz = Class.forName(obj.getClass().getName());
		Class<?> cls = obj.getClass();

		Field[] privateFields = clazz.getDeclaredFields();
		Field[] publicFields = clazz.getFields();
		if(privateFields != null){
			for(Field pf:privateFields){
				fields.add(pf);
			}
		}
		if(publicFields != null){
			for(Field pf:publicFields){
				if(!fields.contains(pf)){
					fields.add(pf);
				}
			}
		}

		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Column.class);
			if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if ((annotation instanceof Column) && !((Column)annotation).n().equals("_id")) {
					Column col = (Column)annotation;
					//                    LOG.d(tag,"getField: " + field.getName());
					Field isf = cls.getDeclaredField(field.getName());
					if(isf.getModifiers() == Modifier.PRIVATE){
						isf.setAccessible(true);
					}
					if (String.class.isAssignableFrom(field.getType())) {
						String val = (String) isf.get(obj);
						cv.put(col.n(),val);
					}else if(field.getType() == Integer.TYPE){
						int val = isf.getInt(obj);
						cv.put(col.n(),val);
					}else if (field.getType() == Long.TYPE) {
						long val  = isf.getLong(obj);
						cv.put(col.n(),val);
					}else if (field.getType() == Boolean.TYPE) {
						boolean val = isf.getBoolean(obj);
						cv.put(col.n(),val?1:0);
					}else if (field.getType() == Float.TYPE) {
						float val = isf.getFloat(obj);
						cv.put(col.n(),val);
					}else if (field.getType() == Double.TYPE) {
						double val = isf.getDouble(obj);
						cv.put(col.n(),val);
					}else if(Date.class.isAssignableFrom(field.getType())){
						Date date = (Date)isf.get(obj);
						cv.put(col.n(),date.getTime());
					}
					if(isf.getModifiers() == Modifier.PRIVATE){
						isf.setAccessible(false);
					}

				}

			}
		}
				Log.d(tag,"CONTENT CREATION: " + cv);
		return cv;
	}


	public  void decryptObject(Object obj){
		List<Field> fields = new ArrayList<Field>();

		Class<?> clazz;
		try {
			clazz = Class.forName(obj.getClass().getName());
			Field[] privateFields = clazz.getDeclaredFields();
			Field[] publicFields = clazz.getFields();
			if(privateFields != null){
				for(Field pf:privateFields){
					fields.add(pf);
				}
			}
			if(publicFields != null){
				for(Field pf:publicFields){
					if(!fields.contains(pf)){
						fields.add(pf);
					}
				}
			}
			Class<?> cls = obj.getClass();
			for (Field field : fields) {
				Annotation annotation = field.getAnnotation(Column.class);
				if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
					if ((annotation instanceof Column) && !((Column)annotation).n().equals("_id")) {
						@SuppressWarnings("unused")
						Column col = (Column)annotation;
						Field isf = cls.getDeclaredField(field.getName());
						if (String.class.isAssignableFrom(field.getType())) {
							String val = (String) isf.get(obj);
							//                            if(col.e()){
							//                                val = cipher.decrypt(k, val);
							//                            }
							isf.set(obj, val);
						}
					}

				}
			}
		} catch (ClassNotFoundException e) {
			//			Log.e(tag,"ERROR: " + e.getMessage() ,e);
		} catch (NoSuchFieldException e) {
			//			Log.e(tag,"ERROR: " + e.getMessage() ,e);
		} catch (IllegalArgumentException e) {
			//			Log.e(tag,"ERROR: " + e.getMessage() ,e);
		} catch (IllegalAccessException e) {
			//			Log.e(tag,"ERROR: " + e.getMessage() ,e);
		} catch (Exception e) {
			//			Log.e(tag,"ERROR: " + e.getMessage() ,e);
		}
	}
	
	
	
	/**
	 * you pass it and instance of any object & this class creates dummy data for the annotated fields marked with {@linkplain Column} annotation
	 * @param obj
	 * @throws Exception
	 */
	public static void createDummyInstance(Object obj) throws Exception{
		//        LOG.d(tag,"getContentValues: " + obj);
		List<Field> fields = new ArrayList<Field>();
		Class<?> clazz = Class.forName(obj.getClass().getName());
		Class<?> cls = obj.getClass();

		Field[] privateFields = clazz.getDeclaredFields();
		Field[] publicFields = clazz.getFields();
		if(privateFields != null){
			for(Field pf:privateFields){
				fields.add(pf);
			}
		}
		if(publicFields != null){
			for(Field pf:publicFields){
				if(!fields.contains(pf)){
					fields.add(pf);
				}
			}
		}

		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Column.class);
			if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if ((annotation instanceof Column) && !((Column)annotation).n().equals("_id")) {
					Field isf = cls.getDeclaredField(field.getName());
					if(isf.getModifiers() == Modifier.PRIVATE){
						isf.setAccessible(true);
					}
					if (String.class.isAssignableFrom(field.getType())) {
						isf.set(obj,TEST_STRING);
					}else if(field.getType() == Integer.TYPE){
						isf.set(obj,TEST_INTEGER);
					}else if (field.getType() == Long.TYPE) {
						isf.set(obj,TEST_LONG);
					}else if (field.getType() == Boolean.TYPE) {
						isf.set(obj,TEST_BOOLEAN);
					}else if (field.getType() == Float.TYPE) {
						isf.set(obj,TEST_FLOAT);
					}else if (field.getType() == Double.TYPE) {
						isf.set(obj,TEST_DOUBLE);
					}else if(Date.class.isAssignableFrom(field.getType())){
						isf.set(obj,TEST_DATE);
					}
					if(isf.getModifiers() == Modifier.PRIVATE){
						isf.setAccessible(false);
					}

				}

			}
		}
	}





	public static ProviderObjectValue getProviderValues(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		//        LOG.d(tag,"getContentValues: " + obj);
		ProviderObjectValue pv =  new ProviderObjectValue();
		Field[] fields = null;
		Class<?> clazz = Class.forName(obj.getClass().getName());
		fields = clazz.getFields();
		Class<?> cls = obj.getClass();

		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Provide.class);
			if (Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if ((annotation instanceof Provide)) {
					Provide col = (Provide)annotation;
					Field isf = cls.getDeclaredField(field.getName());
					switch (col.value()){
						case BaseProvider.PROVIDE_TABLE:
							pv.TABLE = (String) isf.get(obj);
							break;
						case BaseProvider.PROVIDE_KEY:
							pv.KEY = (String) isf.get(obj);
							break;
						case BaseProvider.PROVIDE_URI:
							pv.URI = (Uri) isf.get(obj);
							break;
					}

				}

			}
		}
		pv.ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE  + "/" + pv.TABLE;
		pv.TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + pv.TABLE;
		//		LOG.d(tag,"CONTENT CREATION: " + cv);
		return pv;
	}


}

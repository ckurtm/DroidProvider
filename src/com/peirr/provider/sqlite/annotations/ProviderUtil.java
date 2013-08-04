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

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.ProviderObjectValue;

/**
 * This processes the Annotations on the Provider enabled classes
 * @author kurt 
 * 
 */
public class ProviderUtil {
	protected SQLiteDatabase db;
	protected SecretKey k;
	static String tag = ProviderUtil.class.getSimpleName();
	public static final String TEST_STRING = "SSSS";
	public static final int TEST_INTEGER = 1000;
	public static final long TEST_LONG = 1111l;
	public static final boolean TEST_BOOLEAN = true;
	public static final float TEST_FLOAT = 0.22f;
	public static final float TEST_DOUBLE = 0.55555f;
	public static final Date TEST_DATE = new Date(123456);

	public ProviderUtil(SecretKey k) {
		this.k = k;
	}

	public ProviderUtil(SQLiteDatabase db, SecretKey k) {
		this.db = db;
		this.k = k;
	}

	/**
	 * Create a table via reflection of a class anotated using the {@link Column} and {@link Index} anotations.
	 * @param className
	 * @throws ClassNotFoundException
	 */
	@SuppressLint("DefaultLocale")
	public void createTable(Class<?> clazz,String table) throws ClassNotFoundException {
		Log.d(tag,"createTableFromClass [class:"+clazz.getName()+"][table:"+table+"]");
		List<Field> fields = new ArrayList<Field>();
		StringBuilder queryBuilder = new StringBuilder();
		Field[] privateFields = clazz.getDeclaredFields();
		Field[] publicFields = clazz.getFields();
		List<Class<?>> mergeFields = new ArrayList<Class<?>>(); //list of all merge fields

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
				}else if(BigDecimal.class.isAssignableFrom(field.getType())){
					queryBuilder.append("REAL");
				}
			}

			if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(ColumnMerge.class) != null)) {
				Annotation mannotation = field.getAnnotation(ColumnMerge.class);
				ColumnMerge cm = (ColumnMerge) mannotation;
				mergeFields.add(cm.c());
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

		if(mergeFields.size() > 0){
			int prefix = 0;
			for(Class<?> c:mergeFields){
				prefix++;
				queryBuilder.append(" , " +getFields(c.getName(),prefix));
			}
		}

		queryBuilder.append(");");
		String query = queryBuilder.toString();
		Log.d(tag, "TABLE CREATION: " + query);
		db.execSQL(query);
	}

	/**
	 * gets the database fields from this object, but not the primary key fields if they do exists
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static String getFields(String className,Integer prefix) throws ClassNotFoundException {
		//		Log.d(tag,"getFields [class:"+className+"]");
		String columns = null;
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
		//		queryBuilder.append("CREATE TABLE " + table + " (");
		boolean firstField = true;
		boolean added = false;
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Column.class);
			if (!Modifier.isStatic(field.getModifiers()) && (annotation != null) && (annotation instanceof Column) && !((Column)annotation).n().equals("_id")) {
				if (!firstField) {
					queryBuilder.append(", ");
				}
				if (annotation instanceof Column) {
					Column col = (Column)annotation;
//					queryBuilder.append(col.n() + "" + (prefix!=null?prefix:"") + " ");
					queryBuilder.append(clazz.getSimpleName() + col.n() + " ");
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

			if(added){
				firstField = false;
			}
		}
		//		queryBuilder.append(");");
		columns = queryBuilder.toString();
		//		Log.d(tag,columns);
		return columns;
	}

	/**
	 * Creates a {@link android.content.ContentValues} instance for the given Class that uses the {@link Column} annotation to define fields.
	 * @param obj
	 * @param includePrimary - if true, then include the primary key as part of the content values
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static ContentValues getContentValues(Object obj,boolean... includePrimary) throws Exception{
		Log.d(tag,"getContentValues: " + obj);
		ContentValues cv =  new ContentValues();
		List<Field> fields = new ArrayList<Field>();
		List<FieldValue> mergeFields = new ArrayList<FieldValue>();//list of all merged fields
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
					else if(BigDecimal.class.isAssignableFrom(field.getType())){
						BigDecimal bigDecimal = (BigDecimal)isf.get(obj);
						cv.put(col.n(),bigDecimal.toEngineeringString());
					}
					if(isf.getModifiers() == Modifier.PRIVATE){
						isf.setAccessible(false);
					}

				}
			}

			if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(ColumnMerge.class) != null)) {
				Object o = field.get(obj);
				ColumnMerge cmerge = field.getAnnotation(ColumnMerge.class);
				if(o != null){
					mergeFields.add(new FieldValue(o,cmerge.c(),null));
				}
			}
		}

		if(mergeFields.size() > 0){
//			int prefix = 0;
			for(FieldValue o:mergeFields){
//				prefix++;
				if(o != null){
					ContentValues values = getContentValues(o.object,false);
					for(Map.Entry<String, Object> kv:values.valueSet()){
						cv.put(o.clazz.getSimpleName() + kv.getKey(),String.valueOf(kv.getValue()));
				     }
				}
			}
		}	
		//		Log.d(tag,"CONTENT VALUES: " + cv);
		return cv;
	}





	public  void decryptObject(Object obj){
		List<Field> fields = new ArrayList<Field>();
		Class<?> clazz;


		try {
			clazz = Class.forName(obj.getClass().getName());
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
			//			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
		} catch (NoSuchFieldException e) {
			//			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
		} catch (IllegalArgumentException e) {
			//			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
		} catch (IllegalAccessException e) {
			//			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
		} catch (Exception e) {
			//			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
		}

	}


	/**
	 * This gets the details about the class, i.e. the table name, the primary key
	 * @param clazz
	 * @return a string array [0] = table name, [1] = key
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String[] getMetaDaTa(Class<?> clazz) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Log.d(tag,"getMetadata() " + clazz.getName());
		String[] metadata = new String[2];
		List<Field> fields = new ArrayList<Field>();
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
			Annotation annotation = field.getAnnotation(Provide.class);
			if (Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if ((annotation instanceof Provide)) {
					Provide col = (Provide)annotation;
					Field isf = clazz.getDeclaredField(field.getName());
					switch(col.value()){
						case BaseProvider.PROVIDE_TABLE:
							metadata[0] = (String) isf.get(clazz);
							Log.d(tag," .. table: " + metadata[0]);
							break;
						case BaseProvider.PROVIDE_KEY:
							metadata[1] = (String) isf.get(clazz);
							Log.d(tag," .. key  : " + metadata[1]);
							break;
						case BaseProvider.PROVIDE_URI:
							break;
					}
				}

			}
		}
		return metadata;
	}


	public static ProviderObjectValue getProviderValues(Class<?> clazz) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		//        LOG.d(tag,"getContentValues: " + obj);
		ProviderObjectValue pv =  new ProviderObjectValue();
		List<Field> fields = new ArrayList<Field>();
//		Class<?> clazz = obj.getClass();
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
			Annotation annotation = field.getAnnotation(Provide.class);
			if (Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
				if ((annotation instanceof Provide)) {
					Provide col = (Provide)annotation;
					Field isf = clazz.getDeclaredField(field.getName());
					switch (col.value()){
						case BaseProvider.PROVIDE_TABLE:
							pv.TABLE = (String) isf.get(clazz);
							break;
						case BaseProvider.PROVIDE_KEY:
							pv.KEY = (String) isf.get(clazz);
							break;
						case BaseProvider.PROVIDE_URI:
							pv.URI = (Uri) isf.get(clazz);
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

	
	/**
	 * This gets a row from the table for the given class T using the given {@link Cursor}
	 * @param cursor
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static <T extends ObjectTable> T getRow(Cursor cursor,Class<T> clazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException{
		T object = clazz.newInstance();
		List<Field> fields = new ArrayList<Field>();
		Field[] privateFields = clazz.getDeclaredFields();
		Field[] publicFields = clazz.getFields();
		List<FieldValue> mergeFields = new ArrayList<FieldValue>(); //list of all merge fields

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
				if ((annotation instanceof Column)) {
					Column col = (Column)annotation;
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(true);
					}
					if (String.class.isAssignableFrom(field.getType())) {
						field.set(object,cursor.getString(cursor.getColumnIndex(col.n())));
					}else if(field.getType() == Integer.TYPE){
						field.set(object,cursor.getInt(cursor.getColumnIndex(col.n())));
					}else if (field.getType() == Long.TYPE) {
						field.set(object,cursor.getLong(cursor.getColumnIndex(col.n())));
					}else if (field.getType() == Boolean.TYPE) {
						field.set(object,cursor.getInt(cursor.getColumnIndex(col.n()))>0?true:false);
					}else if (field.getType() == Float.TYPE) {
						field.set(object,cursor.getFloat(cursor.getColumnIndex(col.n())));
					}else if (field.getType() == Double.TYPE) {
						field.set(object,cursor.getDouble(cursor.getColumnIndex(col.n())));
					}else if(Date.class.isAssignableFrom(field.getType())){
						field.set(object,new Date(cursor.getLong(cursor.getColumnIndex(col.n()))));
					}
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(false);
					}
				}
			}
			//found a merge column, need to instatate the object before we can inject data into it.
			if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(ColumnMerge.class) != null)) {
				ColumnMerge cmerge = field.getAnnotation(ColumnMerge.class);
				mergeFields.add(new FieldValue(null,cmerge.c(), field));
			}
		}
		
		
		for(FieldValue fv:mergeFields){
			Object childObj = getChildPersistValue(cursor,fv.clazz);
			fv.field.set(object, childObj);
		}
		return object;
	}
	
	
	/**
	 * Get a List<T> from the DB using {@link ContentProvider} for class T
	 * @param cursor
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static <T extends ObjectTable> List<T> getRows(Cursor cursor,Class<T> clazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException{
	   List<T> list = new ArrayList<T>();
	   while(cursor.moveToNext()){
		   T obj = getRow(cursor, clazz);
		   list.add(obj);
	   }
	   return list;
	}
	
	/**
	 * Same as {@link ProviderUtil::getPersistValue} but sets the field values expect for the _id field.
	 * This is used to instantiate fields annotated with {@link ColumnMerge} 
	 * @param cursor
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static <T> T getChildPersistValue(Cursor cursor,Class<T> clazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException{
		T object = clazz.newInstance();
		List<Field> fields = new ArrayList<Field>();
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
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(true);
					}
					if (String.class.isAssignableFrom(field.getType())) {
						field.set(object,cursor.getString(cursor.getColumnIndex(clazz.getSimpleName()+col.n())));
					}else if(field.getType() == Integer.TYPE){
						field.set(object,cursor.getInt(cursor.getColumnIndex(clazz.getSimpleName()+col.n())));
					}else if (field.getType() == Long.TYPE) {
						field.set(object,cursor.getLong(cursor.getColumnIndex(clazz.getSimpleName()+col.n())));
					}else if (field.getType() == Boolean.TYPE) {
						field.set(object,cursor.getInt(cursor.getColumnIndex(clazz.getSimpleName()+col.n()))>0?true:false);
					}else if (field.getType() == Float.TYPE) {
						field.set(object,cursor.getFloat(cursor.getColumnIndex(clazz.getSimpleName()+col.n())));
					}else if (field.getType() == Double.TYPE) {
						field.set(object,cursor.getDouble(cursor.getColumnIndex(clazz.getSimpleName()+col.n())));
					}else if(Date.class.isAssignableFrom(field.getType())){
						field.set(object,new Date(cursor.getLong(cursor.getColumnIndex(clazz.getSimpleName()+col.n()))));
					}
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(false);
					}
				}
			}
		}
		
		return object;
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
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(true);
					}
					if (String.class.isAssignableFrom(field.getType())) {
						field.set(obj,TEST_STRING);
					}else if(field.getType() == Integer.TYPE){
						field.set(obj,TEST_INTEGER);
					}else if (field.getType() == Long.TYPE) {
						field.set(obj,TEST_LONG);
					}else if (field.getType() == Boolean.TYPE) {
						field.set(obj,TEST_BOOLEAN);
					}else if (field.getType() == Float.TYPE) {
						field.set(obj,TEST_FLOAT);
					}else if (field.getType() == Double.TYPE) {
						field.set(obj,TEST_DOUBLE);
					}else if(Date.class.isAssignableFrom(field.getType())){
						field.set(obj,TEST_DATE);
					}
					if(field.getModifiers() == Modifier.PRIVATE){
						field.setAccessible(false);
					}

				}

			}
		}
	}
	
	private static class FieldValue{
		public Object object;
		public Class<?> clazz;
		public Field field;
		
		public FieldValue(Object object, Class<?> clazz,Field field) {
			this.object = object;
			this.clazz = clazz;
			this.field = field;
		}
	}

}

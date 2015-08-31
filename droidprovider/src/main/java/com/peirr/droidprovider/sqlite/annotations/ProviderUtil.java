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

package com.peirr.droidprovider.sqlite.annotations;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.peirr.droidprovider.sqlite.BaseProvider;
import com.peirr.droidprovider.sqlite.ProviderObjectValue;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

//import android.util.Log;

/**
 * This processes the Annotations on the Provider enabled classes
 *
 * @author kurt
 */
public class ProviderUtil {
    public static final int TEST_INTEGER = 1000;
    public static final long TEST_LONG = 1111l;
    public static final boolean TEST_BOOLEAN = true;
    public static final float TEST_FLOAT = 0.22f;
    public static final float TEST_DOUBLE = 0.55555f;
    public static final Date TEST_DATE = new Date(123456);
    static String TAG = ProviderUtil.class.getSimpleName();
    protected SQLiteDatabase db;
    protected SecretKey k;
    private static final String CHARACTER_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ";
    private static Random random = new Random();

    /**
     * @param k the secret key to be used for db field encryption
     */
    public ProviderUtil(SecretKey k) {
        this.k = k;
    }


    /**
     * @param db reference to the Sqlite database
     * @param k  the secret key to be used for db field encryption
     */
    public ProviderUtil(SQLiteDatabase db, SecretKey k) {
        this.db = db;
        this.k = k;
    }

    /**
     * gets the database fields from this object, but not the primary key fields if they do exists
     *
     * @param className the classname of the class you want fields for
     * @param prefix    prefix to be assigned at end of class name if any
     * @return the fields
     * @throws ClassNotFoundException
     */
    private static String getFields(String className, Integer prefix) throws ClassNotFoundException {
        //		Log.d(tag,"getFields [class:"+className+"]");
        String columns;
        ArrayList<Field> fields = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        //		queryBuilder.append("CREATE TABLE " + table + " (");
        boolean firstField = true;
        boolean added = false;
        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null) && !((DroidColumn) annotation).name().equals("_id")) {
                if (!firstField) {
                    queryBuilder.append(", ");
                }
                DroidColumn col = (DroidColumn) annotation;
                //					queryBuilder.append(col.name() + "" + (prefix!=null?prefix:"") + " ");
                queryBuilder.append(clazz.getSimpleName()).append(col.name()).append(prefix != null ? ("_" + prefix) : "").append(" ");
                added = true;

                if (String.class.isAssignableFrom(field.getType())) {
                    queryBuilder.append("TEXT");
                }

                Class<?> type = field.getType();
                if (type == Integer.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Long.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Boolean.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Float.TYPE) {
                    queryBuilder.append("REAL");
                } else if (type == Double.TYPE) {
                    queryBuilder.append("REAL");
                } else if (Date.class.isAssignableFrom(type)) {
                    queryBuilder.append("INTEGER");
                }
            }

            if (added) {
                firstField = false;
            }
        }
        //		queryBuilder.append(");");
        columns = queryBuilder.toString();
        //		Log.d(tag,columns);
        return columns;
    }

    /**
     * Creates a {@link android.content.ContentValues} instance for the given Class that uses the {@link DroidColumn} annotation to define fields.
     *
     * @param obj            - object instance that you would like to get content values from. This should be a class extending from the ObjectRow class
     * @param includePrimary - if true, then include the primary key as part of the content values
     * @return the actual content values
     */
    public static ContentValues getContentValues(Object obj, boolean... includePrimary) {
        //		Log.d(tag,"getContentValues: " + obj);
        ContentValues cv = new ContentValues();
        ArrayList<Field> fields = new ArrayList<>();
        List<FieldValue> mergeFields = new ArrayList<FieldValue>();//list of all merged fields

        try {
            Class<?> clazz = Class.forName(obj.getClass().getName());

            Field[] privateFields = clazz.getDeclaredFields();
            Field[] publicFields = clazz.getFields();
            if (privateFields != null) {
                Collections.addAll(fields, privateFields);
            }
            if (publicFields != null) {
                for (int i=0,size=publicFields.length;i<size;i++) {
                    Field pf = publicFields[i];
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }

            for (int i=0,size=fields.size();i<size;i++) {
                Field field = fields.get(i);
                Annotation annotation = field.getAnnotation(DroidColumn.class);
                if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                    if (!((DroidColumn) annotation).name().equals("_id")) {
                        DroidColumn col = (DroidColumn) annotation;
                        //                    LOG.d(tag,"getField: " + field.getName());
                        if (field.getModifiers() == Modifier.PRIVATE) {
                            field.setAccessible(true);
                        }

                        Class<?> type = field.getType();
                        final String colName = col.name();
                        if (String.class.isAssignableFrom(type)) {
                            String val = (String) field.get(obj);
                            cv.put(colName, val);
                        } else if (type == Integer.TYPE) {
                            int val = field.getInt(obj);
                            cv.put(colName, val);
                        } else if (type == Long.TYPE) {
                            long val = field.getLong(obj);
                            cv.put(colName, val);
                        } else if (type == Boolean.TYPE) {
                            boolean val = field.getBoolean(obj);
                            cv.put(colName, val ? 1 : 0);
                        } else if (type == Float.TYPE) {
                            float val = field.getFloat(obj);
                            cv.put(colName, val);
                        } else if (type == Double.TYPE) {
                            double val = field.getDouble(obj);
                            cv.put(colName, val);
                        } else if (Date.class.isAssignableFrom(type)) {
                            Date date = (Date) field.get(obj);
                            if (date != null)
                                cv.put(colName, date.getTime());
                        } else if (BigDecimal.class.isAssignableFrom(type)) {
                            BigDecimal bigDecimal = (BigDecimal) field.get(obj);
                            cv.put(colName, bigDecimal.toEngineeringString());
                        }
                        if (field.getModifiers() == Modifier.PRIVATE) {
                            field.setAccessible(false);
                        }

                    }
                }

                if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(DroidColumnMerge.class) != null)) {
                    Object o = field.get(obj);
                    DroidColumnMerge cmerge = field.getAnnotation(DroidColumnMerge.class);
                    if (o != null) {
                        mergeFields.add(new FieldValue(o, cmerge.c(), null, cmerge.seq()));
                    }
                }
            }

            if (mergeFields.size() > 0) {
                for (FieldValue o : mergeFields) {
                    if (o != null) {
                        ContentValues values = getContentValues(o.object, false);
                        for (Map.Entry<String, Object> kv : values.valueSet()) {
                            cv.put(o.clazz.getSimpleName() + kv.getKey() + "_" + o.seq, String.valueOf(kv.getValue()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to get content values", e);
        }
        return cv;
    }

    /**
     * This gets the details about the class, i.e. the table name, the primary key
     *
     * @param clazz the class that you want to get metadata from
     * @return a string array [0] = table name, [1] = key
     * @throws NoSuchFieldException     if something goes wrong
     * @throws IllegalArgumentException if something goes wrong
     * @throws IllegalAccessException   if something goes wrong
     */
    public static String[] getMetaDaTa(Class<?> clazz) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        //		Log.d(tag,"getMetadata() " + clazz.getName());
        String[] metadata = new String[2];
        ArrayList<Field> fields = new ArrayList<>();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidProvider.class);
            if (Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                DroidProvider col = (DroidProvider) annotation;
                Field isf = clazz.getDeclaredField(field.getName());
                switch (col.value()) {
                    case BaseProvider.PROVIDE_TABLE:
                        metadata[0] = (String) isf.get(clazz);
                        //							Log.d(tag," .. table: " + metadata[0]);
                        break;
                    case BaseProvider.PROVIDE_KEY:
                        metadata[1] = (String) isf.get(clazz);
                        //							Log.d(tag," .. key  : " + metadata[1]);
                        break;
                    case BaseProvider.PROVIDE_URI:
                        break;
                }
            }
        }
        return metadata;
    }


    /**
     * gets the content provider values from the supplied class
     *
     * @param clazz class you want to get provider values from
     * @return the provider object value
     * @throws ClassNotFoundException             possible exception
     * @throws NoSuchFieldException               possible exception
     * @throws IllegalAccessException             possible exception
     * @throws NoSuchPaddingException             possible exception
     * @throws InvalidAlgorithmParameterException possible exception
     * @throws UnsupportedEncodingException       possible exception
     * @throws IllegalBlockSizeException          possible exception
     * @throws BadPaddingException                possible exception
     * @throws NoSuchAlgorithmException           possible exception
     * @throws InvalidKeyException                possible exception
     */
    public static ProviderObjectValue getProviderValues(Class<?> clazz) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        //        LOG.d(tag,"getContentValues: " + obj);
        ProviderObjectValue pv = new ProviderObjectValue();
        ArrayList<Field> fields = new ArrayList<>();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidProvider.class);
            if (Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                DroidProvider col = (DroidProvider) annotation;
                Field isf = clazz.getDeclaredField(field.getName());
                switch (col.value()) {
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
        pv.ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + pv.TABLE;
        pv.TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + pv.TABLE;
        //		LOG.d(tag,"CONTENT CREATION: " + cv);
        return pv;
    }

    /**
     * This gets a row from the table for the given class T using the given {@link android.database.Cursor}
     *
     * @param clazz  the class type
     * @param cursor the query cursor
     * @param <T>    the class type
     * @return the object instance of the supplied clazz with data bound from the supplied cursor
     */
    public static <T extends ObjectRow> T getRow(Cursor cursor, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            ArrayList<Field> fields = new ArrayList<>();
            Field[] privateFields = clazz.getDeclaredFields();
            Field[] publicFields = clazz.getFields();
            List<FieldValue> mergeFields = new ArrayList<FieldValue>(); //list of all merge fields

            if (privateFields != null) {
                Collections.addAll(fields, privateFields);
            }
            if (publicFields != null) {
                for (int i=0,size=publicFields.length;i<size;i++) {
                    Field pf = publicFields[i];
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }

            for (int i=0,size=fields.size();i<size;i++) {
                Field field = fields.get(i);
                Annotation annotation = field.getAnnotation(DroidColumn.class);
                if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                    DroidColumn col = (DroidColumn) annotation;
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    Class<?> type = field.getType();
                    final String colName = col.name();
                    if (String.class.isAssignableFrom(type)) {
                        //                        LOG.d(tag, "[column: " + col.name() + "]");
                        field.set(object, cursor.getString(cursor.getColumnIndex(colName)));
                    } else if (type == Integer.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(colName)));
                    } else if (type == Long.TYPE) {
                        field.set(object, cursor.getLong(cursor.getColumnIndex(colName)));
                    } else if (type == Boolean.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(colName)) > 0);
                    } else if (type == Float.TYPE) {
                        field.set(object, cursor.getFloat(cursor.getColumnIndex(colName)));
                    } else if (type == Double.TYPE) {
                        field.set(object, cursor.getDouble(cursor.getColumnIndex(colName)));
                    } else if (Date.class.isAssignableFrom(type)) {
                        field.set(object, new Date(cursor.getLong(cursor.getColumnIndex(colName))));
                    }
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(false);
                    }
                }
                //found a merge column, need to instatate the object before we can inject data into it.
                if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(DroidColumnMerge.class) != null)) {
                    DroidColumnMerge cmerge = field.getAnnotation(DroidColumnMerge.class);
                    mergeFields.add(new FieldValue(null, cmerge.c(), field, cmerge.seq()));
                }
            }

            for (FieldValue fv : mergeFields) {
                Object childObj = getChildColumn(cursor, fv.clazz, fv.seq);
                fv.field.set(object, childObj);
            }
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException()", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException()", e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "NoSuchFieldException()", e);
        }
        return object;
    }


    /**
     * gets a single row from supplied cursor
     *
     * @param clazz      the class type
     * @param cursor     the query cursor
     * @param moveTonext if true then the cursor will be moved to next row before binding starts
     * @param <T>        the class type
     * @return the object instace bound to the class type supplied
     */
    public static <T extends ObjectRow> T getRow(Cursor cursor, Class<T> clazz, boolean moveTonext) {
        if (moveTonext)
            cursor.moveToNext();
        return getRow(cursor, clazz);
    }

    /**
     * Get a List from the DB using qa{@link android.content.ContentProvider} for class T
     *
     * @param clazz  the class type
     * @param cursor the query cursor
     * @param <T>    the class type
     * @return a list of object instances bound to the class type supplied
     */
    private static <T extends ObjectRow> List<T> getRows(Cursor cursor, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            try {
                T obj = getRow(cursor, clazz);
                list.add(obj);
            } catch (Exception e) {
                Log.e(TAG, "failed to get rows:", e);
            }
        }
        return list;
    }


    public static <T extends ObjectRow> List<T> getRows(Cursor cursor, Class<T> clazz,boolean closeCursor){
        List<T> rows = getRows(cursor,clazz);
        if(closeCursor){
            cursor.close();
        }
        return rows;
    }

    /**
     * Same as {@link ProviderUtil ::getPersistValue} but sets the field values expect for the _id field.
     * This is used to instantiate fields annotated with {@link DroidColumnMerge}
     *
     * @param clazz  the class type
     * @param cursor the query cursor
     * @param <T>    the class type
     * @param prefix prefix no to be used on appending to class name
     * @return a list of object instances bound to the class type supplied
     * @throws InstantiationException when something goes wrong
     * @throws IllegalAccessException when something goes wrong
     * @throws NoSuchFieldException   when something goes wrong
     */
    public static <T> T getChildColumn(Cursor cursor, Class<T> clazz, int prefix) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        T object = clazz.newInstance();
        ArrayList<Field> fields = new ArrayList<>();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }


        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if (!((DroidColumn) annotation).name().equals("_id")) {
                    DroidColumn col = (DroidColumn) annotation;
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    Class<?> type = field.getType();
                    final String colName = col.name();
                    final String className = clazz.getSimpleName();
                    final String colIndexStr = new StringBuilder().append(className).append(colName).append("_").append(prefix).toString();
                    if (String.class.isAssignableFrom(type)) {
                        field.set(object, cursor.getString(cursor.getColumnIndex(colIndexStr)));
                    } else if (type == Integer.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(colIndexStr)));
                    } else if (type == Long.TYPE) {
                        field.set(object, cursor.getLong(cursor.getColumnIndex(colIndexStr)));
                    } else if (type == Boolean.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(colIndexStr)) > 0);
                    } else if (type == Float.TYPE) {
                        field.set(object, cursor.getFloat(cursor.getColumnIndex(colIndexStr)));
                    } else if (type == Double.TYPE) {
                        field.set(object, cursor.getDouble(cursor.getColumnIndex(colIndexStr)));
                    } else if (Date.class.isAssignableFrom(type)) {
                        field.set(object, new Date(cursor.getLong(cursor.getColumnIndex(colIndexStr))));
                    }
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(false);
                    }
                }
            }
        }

        return object;
    }

    /**
     * you pass it an instance of any object and this class creates dummy data for the annotated fields marked with {@linkplain DroidColumn} annotation
     *
     * @param clazz the class
     * @param <T>   the class type
     * @return a populated instance of supplied class type
     * @throws java.lang.Exception if something goes wrong
     */
    public static <T> T createDummyInstance(Class<T> clazz) throws Exception {
        ArrayList<Field> fields = new ArrayList<>();
        T obj = (T) clazz.newInstance();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }

        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if (!((DroidColumn) annotation).name().equals("_id")) {
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    Class<?> type = field.getType();
                    if (String.class.isAssignableFrom(type)) {
                        field.set(obj, randomString());
                    } else if (type == Integer.TYPE) {
                        field.set(obj, TEST_INTEGER);
                    } else if (type == Long.TYPE) {
                        field.set(obj, TEST_LONG);
                    } else if (type == Boolean.TYPE) {
                        field.set(obj, TEST_BOOLEAN);
                    } else if (type == Float.TYPE) {
                        field.set(obj, TEST_FLOAT);
                    } else if (type == Double.TYPE) {
                        field.set(obj, TEST_DOUBLE);
                    } else if (Date.class.isAssignableFrom(type)) {
                        field.set(obj, TEST_DATE);
                    }
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(false);
                    }

                }

            }
        }
        return obj;
    }


    /**
     * generates a random string
     *
     * @return the generated string
     */
    private static String randomString() {
        int len = 8;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARACTER_SET.charAt(random.nextInt(CHARACTER_SET.length())));
        }
        return sb.toString();
    }

    /**
     * Create a table via reflection of a class anotated using the {@link DroidColumn} annotation.
     *
     * @param clazz the class that you want to create a table of
     * @param table the table name
     * @throws java.lang.ClassNotFoundException if something goes wrong
     */
    @SuppressLint("DefaultLocale")
    public void createTable(Class<?> clazz, String table) throws ClassNotFoundException {
//        Log.d(tag, "createTableFromClass [class:" + clazz.getName() + "][table:" + table + "]");
        ArrayList<Field> fields = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        List<FieldValue> mergeFields = new ArrayList<FieldValue>(); //list of all merge fields

        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (int i=0,size=publicFields.length;i<size;i++) {
                Field pf = publicFields[i];
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        queryBuilder.append("CREATE TABLE ").append(table).append(" (");
        //		LOG.d(tag,"fields: " + fields);
        boolean firstField = true;
        boolean added = false;
        for (int i=0,size=fields.size();i<size;i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if (!firstField) {
                    queryBuilder.append(", ");
                }

                DroidColumn col = (DroidColumn) annotation;
                queryBuilder.append(col.name()).append(" ");
                added = true;


                if (String.class.isAssignableFrom(field.getType())) {
                    queryBuilder.append("TEXT");
                }
                Class<?> type = field.getType();
                if (type == Integer.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Long.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Boolean.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (type == Float.TYPE) {
                    queryBuilder.append("REAL");
                } else if (type == Double.TYPE) {
                    queryBuilder.append("REAL");
                } else if (Date.class.isAssignableFrom(type)) {
                    queryBuilder.append("INTEGER");
                } else if (BigDecimal.class.isAssignableFrom(type)) {
                    queryBuilder.append("REAL");
                }
            }

            if (!Modifier.isStatic(field.getModifiers()) && (field.getAnnotation(DroidColumnMerge.class) != null)) {
                Annotation mannotation = field.getAnnotation(DroidColumnMerge.class);
                DroidColumnMerge cm = (DroidColumnMerge) mannotation;
                mergeFields.add(new FieldValue(null, cm.c(), null, cm.seq()));
            }

            DroidColumn col = (DroidColumn) annotation;
            if (col != null && col.primary()) {
                queryBuilder.append(" PRIMARY KEY NOT NULL");
            } else if (col != null && col.unique()) {
                queryBuilder.append(" NOT NULL UNIQUE");
            }


            if (added) {
                firstField = false;
            }
        }

        if (mergeFields.size() > 0) {
            for (FieldValue fv : mergeFields) {
                queryBuilder.append(" , ").append(getFields(fv.clazz.getName(), fv.seq));
            }
        }

        queryBuilder.append(");");
        String query = queryBuilder.toString();
//        Log.d(tag, "TABLE CREATION: " + query);
        db.execSQL(query);
    }

    /**
     * decrypts data from the supplied object from the db
     *
     * @param obj class instance to decrypt
     */
    public void decryptObject(Object obj) {
        ArrayList<Field> fields = new ArrayList<>();
        Class<?> clazz;


        try {
            clazz = Class.forName(obj.getClass().getName());
            Class<?> cls = obj.getClass();
            Field[] privateFields = clazz.getDeclaredFields();
            Field[] publicFields = clazz.getFields();
            if (privateFields != null) {
                Collections.addAll(fields, privateFields);
            }
            if (publicFields != null) {
                for (int i=0,size=publicFields.length;i<size;i++) {
                    Field pf = publicFields[i];
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }
            for (int i=0,size=fields.size();i<size;i++) {
                Field field = fields.get(i);
                Annotation annotation = field.getAnnotation(DroidColumn.class);
                if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                    if (!((DroidColumn) annotation).name().equals("_id")) {
                        Field isf = cls.getDeclaredField(field.getName());
                        if (String.class.isAssignableFrom(field.getType())) {
                            String val = (String) isf.get(obj);
                            isf.set(obj, val);
                        }
                    }

                }
            }
        } catch (Exception e) {
            //			Log.e(tag,"decryptObject ERROR: " + e.getMessage() ,e);
        }

    }

    private static class FieldValue {
        public Object object;
        public Class<?> clazz;
        public Field field;
        public int seq;

        public FieldValue(Object object, Class<?> clazz, Field field, int seq) {
            this.object = object;
            this.clazz = clazz;
            this.field = field;
            this.seq = seq;
        }
    }

}

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
        List<Field> fields = new ArrayList<Field>();
        StringBuilder queryBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        //		queryBuilder.append("CREATE TABLE " + table + " (");
        boolean firstField = true;
        boolean added = false;
        for (Field field : fields) {
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

                if (field.getType() == Integer.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Long.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Boolean.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Float.TYPE) {
                    queryBuilder.append("REAL");
                } else if (field.getType() == Double.TYPE) {
                    queryBuilder.append("REAL");
                } else if (Date.class.isAssignableFrom(field.getType())) {
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
     * @throws java.lang.Exception if something goes wrong
     */
    public static ContentValues getContentValues(Object obj, boolean... includePrimary) {
        //		Log.d(tag,"getContentValues: " + obj);
        ContentValues cv = new ContentValues();
        List<Field> fields = new ArrayList<Field>();
        List<FieldValue> mergeFields = new ArrayList<FieldValue>();//list of all merged fields

        try {
            Class<?> clazz = Class.forName(obj.getClass().getName());

            Field[] privateFields = clazz.getDeclaredFields();
            Field[] publicFields = clazz.getFields();
            if (privateFields != null) {
                Collections.addAll(fields, privateFields);
            }
            if (publicFields != null) {
                for (Field pf : publicFields) {
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }

            for (Field field : fields) {
                Annotation annotation = field.getAnnotation(DroidColumn.class);
                if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                    if (!((DroidColumn) annotation).name().equals("_id")) {
                        DroidColumn col = (DroidColumn) annotation;
                        //                    LOG.d(tag,"getField: " + field.getName());
                        Field isf = field;
                        if (isf.getModifiers() == Modifier.PRIVATE) {
                            isf.setAccessible(true);
                        }
                        if (String.class.isAssignableFrom(field.getType())) {
                            String val = (String) isf.get(obj);
                            cv.put(col.name(), val);
                        } else if (field.getType() == Integer.TYPE) {
                            int val = isf.getInt(obj);
                            cv.put(col.name(), val);
                        } else if (field.getType() == Long.TYPE) {
                            long val = isf.getLong(obj);
                            cv.put(col.name(), val);
                        } else if (field.getType() == Boolean.TYPE) {
                            boolean val = isf.getBoolean(obj);
                            cv.put(col.name(), val ? 1 : 0);
                        } else if (field.getType() == Float.TYPE) {
                            float val = isf.getFloat(obj);
                            cv.put(col.name(), val);
                        } else if (field.getType() == Double.TYPE) {
                            double val = isf.getDouble(obj);
                            cv.put(col.name(), val);
                        } else if (Date.class.isAssignableFrom(field.getType())) {
                            Date date = (Date) isf.get(obj);
                            if (date != null)
                                cv.put(col.name(), date.getTime());
                        } else if (BigDecimal.class.isAssignableFrom(field.getType())) {
                            BigDecimal bigDecimal = (BigDecimal) isf.get(obj);
                            cv.put(col.name(), bigDecimal.toEngineeringString());
                        }
                        if (isf.getModifiers() == Modifier.PRIVATE) {
                            isf.setAccessible(false);
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
                //			int prefix = 0;
                for (FieldValue o : mergeFields) {
                    //				prefix++;
                    if (o != null) {
                        ContentValues values = getContentValues(o.object, false);
                        for (Map.Entry<String, Object> kv : values.valueSet()) {
                            cv.put(o.clazz.getSimpleName() + kv.getKey() + "_" + o.seq, String.valueOf(kv.getValue()));
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException e) {
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
        List<Field> fields = new ArrayList<Field>();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        for (Field field : fields) {
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
        List<Field> fields = new ArrayList<Field>();
        //		Class<?> clazz = obj.getClass();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        for (Field field : fields) {
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
     * @throws java.lang.Exception if something goes wrong
     */
    public static <T extends ObjectRow> T getRow(Cursor cursor, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            List<Field> fields = new ArrayList<Field>();
            Field[] privateFields = clazz.getDeclaredFields();
            Field[] publicFields = clazz.getFields();
            List<FieldValue> mergeFields = new ArrayList<FieldValue>(); //list of all merge fields

            if (privateFields != null) {
                Collections.addAll(fields, privateFields);
            }
            if (publicFields != null) {
                for (Field pf : publicFields) {
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }

            for (Field field : fields) {
                Annotation annotation = field.getAnnotation(DroidColumn.class);
                if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                    DroidColumn col = (DroidColumn) annotation;
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    if (String.class.isAssignableFrom(field.getType())) {
                        //                        LOG.d(tag, "[column: " + col.name() + "]");
                        field.set(object, cursor.getString(cursor.getColumnIndex(col.name())));
                    } else if (field.getType() == Integer.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(col.name())));
                    } else if (field.getType() == Long.TYPE) {
                        field.set(object, cursor.getLong(cursor.getColumnIndex(col.name())));
                    } else if (field.getType() == Boolean.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(col.name())) > 0);
                    } else if (field.getType() == Float.TYPE) {
                        field.set(object, cursor.getFloat(cursor.getColumnIndex(col.name())));
                    } else if (field.getType() == Double.TYPE) {
                        field.set(object, cursor.getDouble(cursor.getColumnIndex(col.name())));
                    } else if (Date.class.isAssignableFrom(field.getType())) {
                        field.set(object, new Date(cursor.getLong(cursor.getColumnIndex(col.name()))));
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
     * @throws Exception when something goess wrong
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
     * @throws Exception when something goes wrong
     */
    public static <T extends ObjectRow> List<T> getRows(Cursor cursor, Class<T> clazz) {
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
        List<Field> fields = new ArrayList<Field>();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }

        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if (!((DroidColumn) annotation).name().equals("_id")) {
                    DroidColumn col = (DroidColumn) annotation;
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    if (String.class.isAssignableFrom(field.getType())) {
                        field.set(object, cursor.getString(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)));
                    } else if (field.getType() == Integer.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)));
                    } else if (field.getType() == Long.TYPE) {
                        field.set(object, cursor.getLong(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)));
                    } else if (field.getType() == Boolean.TYPE) {
                        field.set(object, cursor.getInt(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)) > 0);
                    } else if (field.getType() == Float.TYPE) {
                        field.set(object, cursor.getFloat(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)));
                    } else if (field.getType() == Double.TYPE) {
                        field.set(object, cursor.getDouble(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix)));
                    } else if (Date.class.isAssignableFrom(field.getType())) {
                        field.set(object, new Date(cursor.getLong(cursor.getColumnIndex(clazz.getSimpleName() + col.name() + "_" + prefix))));
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
        List<Field> fields = new ArrayList<Field>();
        Object obj = (T) clazz.newInstance();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }

        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(DroidColumn.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if (!((DroidColumn) annotation).name().equals("_id")) {
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(true);
                    }
                    if (String.class.isAssignableFrom(field.getType())) {
                        field.set(obj, randomString(8));
                    } else if (field.getType() == Integer.TYPE) {
                        field.set(obj, TEST_INTEGER);
                    } else if (field.getType() == Long.TYPE) {
                        field.set(obj, TEST_LONG);
                    } else if (field.getType() == Boolean.TYPE) {
                        field.set(obj, TEST_BOOLEAN);
                    } else if (field.getType() == Float.TYPE) {
                        field.set(obj, TEST_FLOAT);
                    } else if (field.getType() == Double.TYPE) {
                        field.set(obj, TEST_DOUBLE);
                    } else if (Date.class.isAssignableFrom(field.getType())) {
                        field.set(obj, TEST_DATE);
                    }
                    if (field.getModifiers() == Modifier.PRIVATE) {
                        field.setAccessible(false);
                    }

                }

            }
        }
        return (T) obj;
    }


    /**
     * generates a random string
     *
     * @param len length of the string
     * @return the generated string
     */
    private static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(CHARACTER_SET.charAt(random.nextInt(CHARACTER_SET.length())));
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
        List<Field> fields = new ArrayList<Field>();
        StringBuilder queryBuilder = new StringBuilder();
        Field[] privateFields = clazz.getDeclaredFields();
        Field[] publicFields = clazz.getFields();
        List<FieldValue> mergeFields = new ArrayList<FieldValue>(); //list of all merge fields

        if (privateFields != null) {
            Collections.addAll(fields, privateFields);
        }
        if (publicFields != null) {
            for (Field pf : publicFields) {
                if (!fields.contains(pf)) {
                    fields.add(pf);
                }
            }
        }
        queryBuilder.append("CREATE TABLE ").append(table).append(" (");
        //		LOG.d(tag,"fields: " + fields);
        boolean firstField = true;
        boolean added = false;
        for (Field field : fields) {
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

                if (field.getType() == Integer.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Long.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Boolean.TYPE) {
                    queryBuilder.append("INTEGER");
                } else if (field.getType() == Float.TYPE) {
                    queryBuilder.append("REAL");
                } else if (field.getType() == Double.TYPE) {
                    queryBuilder.append("REAL");
                } else if (Date.class.isAssignableFrom(field.getType())) {
                    queryBuilder.append("INTEGER");
                } else if (BigDecimal.class.isAssignableFrom(field.getType())) {
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
        List<Field> fields = new ArrayList<Field>();
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
                for (Field pf : publicFields) {
                    if (!fields.contains(pf)) {
                        fields.add(pf);
                    }
                }
            }
            for (Field field : fields) {
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

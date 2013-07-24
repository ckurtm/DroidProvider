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
package com.peirr.provider.sqlite.annotations;

import android.annotation.SuppressLint;
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

/**
 * This processes the Annotations on the Provider enabled classes
 * @author kurt 
 * 
 */
public class ObjectProcessor {
    protected SQLiteDatabase db;
    protected SecretKey k;
    String tag = ObjectProcessor.class.getSimpleName();

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
        Field[] fields = null;
        StringBuilder queryBuilder = new StringBuilder();
        Class<?> clazz = Class.forName(className);
        fields = clazz.getFields();
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
    public  ContentValues getContentValues(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
//        LOG.d(tag,"getContentValues: " + obj);
        ContentValues cv =  new ContentValues();
        Field[] fields = null;
        Class<?> clazz = Class.forName(obj.getClass().getName());
        fields = clazz.getFields();
        Class<?> cls = obj.getClass();
//
//        for (Field field : fields) {
//            Annotation annotation = field.getAnnotation(Column.class);
//            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
//                if ((annotation instanceof Column) && !field.getName().equals("_id")) {
//                    LOG.d(tag," - " + field.getName());
//                }
//            }
//        }


        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(Column.class);
            if (!Modifier.isStatic(field.getModifiers()) && (annotation != null)) {
                if ((annotation instanceof Column) && !((Column)annotation).n().equals("_id")) {
                    Column col = (Column)annotation;
//                    LOG.d(tag,"getField: " + field.getName());
                    Field isf = cls.getDeclaredField(field.getName());
                    if (String.class.isAssignableFrom(field.getType())) {
                        String val = (String) isf.get(obj);
//                        if(col.e()){
//                            val = cipher.encrypt(k,val);
//                        }
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
                    }

                }

            }
        }
//		LOG.d(tag,"CONTENT CREATION: " + cv);
        return cv;
    }


    public  void decryptObject(Object obj){
        Field[] fields = null;
        Class<?> clazz;
        try {
            clazz = Class.forName(obj.getClass().getName());
            fields = clazz.getFields();
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
                        case BaseProvider.PROVIDE_BASE:
                            pv.BASE = (String) isf.get(obj);
                            break;
                        case BaseProvider.PROVIDE_KEY:
                            pv.KEY = (String) isf.get(obj);
                            break;
                        case BaseProvider.PROVIDE_MANY:
                             pv.MANY = isf.getInt(obj);
                            break;
                        case BaseProvider.PROVIDE_ONE:
                            pv.ONE = isf.getInt(obj);
                            break;
                        case BaseProvider.PROVIDE_TYPE:
                            pv.TYPE = (String) isf.get(obj);
                            break;
                        case BaseProvider.PROVIDE_ITEM_TYPE:
                            pv.ITEM_TYPE = (String) isf.get(obj);
                            break;
                        case BaseProvider.PROVIDE_URI:
                            pv.URI = (Uri) isf.get(obj);
                            break;
                    }
                }

            }
        }
//		LOG.d(tag,"CONTENT CREATION: " + cv);
        return pv;
    }


}

/*
 * Copyright (c) 2012 Kurt Mbanje
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
 *   https://github.com/ckurtm/DroidProvider
 */

package mbanje.kurt.todo.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.peirr.droidprovider.sqlite.annotations.ProviderUtil;

import mbanje.kurt.todo.TodoItem;

/**
 * Created by kurt on 2014/07/19.
 * Use this class to perfom CRUD operations on the Todo database table
 */
public class TodoHelper {

    static String TAG = TodoHelper.class.getSimpleName();

    public static Uri createTodo(ContentResolver resolver, TodoItem item) {
        try {
            Uri uri = resolver.insert(TodoItem.CONTENT_URI, ProviderUtil.getContentValues(item, false));
            if (uri != null) {
                Log.d(TAG, "added new task");
            }
            return uri;
        } catch (Exception e) {
            Log.e(TAG, "error adding item...", e);
        }
        return null;
    }

    public static int deleteTodo(ContentResolver resolver, TodoItem item) {
        try {
            int result = resolver.delete(TodoItem.CONTENT_URI, TodoItem.Mapper._ID + "=?", new String[]{String.valueOf(item._id)});
            if (result > 0) {
                Log.d(TAG, "deleted task");
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "error deleting item: ", e);
        }
        return -1;
    }

    public static TodoItem getTodo(ContentResolver resolver, long id) {
        try {
            Cursor cursor = resolver.query(TodoItem.CONTENT_URI, null, TodoItem.Mapper._ID + "=?", new String[]{String.valueOf(id)}, null);
            cursor.moveToFirst();
            return ProviderUtil.getRow(cursor, TodoItem.class);
        } catch (Exception e) {
            Log.e(TAG, "error getting item: ", e);
        }
        return null;
    }

    public static int updateTodo(ContentResolver resolver, TodoItem item) {
        try {
            return resolver.update(TodoItem.CONTENT_URI, ProviderUtil.getContentValues(item, true), TodoItem.Mapper._ID + "=?", new String[]{String.valueOf(item._id)});
        } catch (Exception e) {
            Log.e(TAG, "error updating item...", e);
        }
        return -1;
    }

    public static void deleteAll(ContentResolver resolver) {
        resolver.delete(TodoItem.CONTENT_URI, null, null);
    }

}

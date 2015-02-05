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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.peirr.droidprovider.sqlite.BaseDataStore;
import com.peirr.droidprovider.sqlite.annotations.ObjectRow;

import java.util.ArrayList;
import java.util.List;

import mbanje.kurt.todo.TodoItem;

/**
 * Created by kurt on 2014/07/18.
 */
public class TodoDataStore extends BaseDataStore {

    public TodoDataStore(Context context) {
        super(context, "base");
    }

//    @Override
//    public List<Class<? extends ObjectTable>> getDefinedClasses() {
//        List<Class<? extends ObjectTable>> tables = new ArrayList<Class<? extends ObjectTable>>();
//        tables.add(TodoItem.class);
//        return tables;
//    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public List<Class<? extends ObjectRow>> getDefinedClasses() {
        List<Class<? extends ObjectRow>> tables = new ArrayList<Class<? extends ObjectRow>>();
        tables.add(TodoItem.class);
        return tables;
    }
}

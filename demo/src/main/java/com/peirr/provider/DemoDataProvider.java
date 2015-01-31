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

package com.peirr.provider;


import android.content.Context;
import android.content.SharedPreferences;

import com.peirr.droidprovider.sqlite.BaseDataStore;
import com.peirr.droidprovider.sqlite.BaseProvider;

/**
 *
 * Created by kurt on 31 01 2015 .
 *
 */
public class DemoDataProvider extends BaseProvider {

    @Override
    public BaseDataStore getMyDB() {
        return new DemoDataStore(getContext(), getCurrentDB());
    }

    public String getCurrentDB() {
        String dbname;
        try {
            SharedPreferences settings = getContext().getSharedPreferences(BaseDataStore.DATABASE_KEY, Context.MODE_PRIVATE);
            dbname = settings.getString(BaseDataStore.DATABASE_KEY, BaseDataStore.DATABASE);
        } catch (Exception e) {
            dbname = BaseDataStore.DATABASE;
        }
        return dbname;
    }
}

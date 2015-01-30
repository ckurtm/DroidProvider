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

package com.test.providersample;

import com.peirr.droidprovider.sqlite.BaseProvider;
import com.peirr.droidprovider.sqlite.annotations.DroidColumn;
import com.peirr.droidprovider.sqlite.annotations.DroidProvider;
import com.peirr.droidprovider.sqlite.annotations.ObjectMapper;
import com.peirr.droidprovider.sqlite.annotations.ObjectRow;

import java.util.Date;

/**
 * Created by kurt on 2015/01/30.
 */
public class MyPojo extends ObjectRow {

    @DroidProvider(BaseProvider.PROVIDE_TABLE)
    public static final String TABLE = "mypojo";
    @DroidProvider(BaseProvider.PROVIDE_URI)
    public static final android.net.Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    @DroidProvider(BaseProvider.PROVIDE_KEY)
    public static final String KEY = Mapper._ID;

    @DroidColumn(name = Mapper.mystrcol)
    public String mystring;

    @DroidColumn(name = Mapper.myintcol)
    public int myint;

    @DroidColumn(name = Mapper.mydatecol)
    public Date mydate;


    public static final class Mapper extends ObjectMapper {
        public static final String myintcol = "myint";
        public static final String mystrcol = "mstr";
        public static final String mydatecol = "mydte";
    }

}

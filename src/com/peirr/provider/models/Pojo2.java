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

package com.peirr.provider.models;


import java.util.Date;

import android.net.Uri;

import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.annotations.Column;
import com.peirr.provider.sqlite.annotations.ObjectMapper;
import com.peirr.provider.sqlite.annotations.ObjectTable;
import com.peirr.provider.sqlite.annotations.Provide;


/**
 * 
   Just a pojo
 * @author kurt 
 * PeirrContentProvider
 */
public class Pojo2 extends ObjectTable {

    @Column(n = Pojo2.Mapper.pid,e=false) 
    public long pid;
    
    @Column(n = Pojo2.Mapper.name,e =false) 
    public String name;
    
    @Column(n = Pojo2.Mapper.date,e =false) 
    public Date other;

    @Provide(BaseProvider.PROVIDE_TABLE) 
    public static final String TABLE = "pojo2";
    
    @Provide(BaseProvider.PROVIDE_URI) 
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    
    @Provide(BaseProvider.PROVIDE_KEY) 
    public static final String KEY = "pid";

    public final class Mapper extends ObjectMapper {
        public static final String pid = "pid";
        public static final String name = "name";
        public static final String date = "date";
    }


}
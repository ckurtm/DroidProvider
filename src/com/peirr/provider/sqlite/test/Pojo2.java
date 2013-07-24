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

package com.peirr.provider.sqlite.test;


import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.annotations.Column;
import com.peirr.provider.sqlite.annotations.ObjectMapper;
import com.peirr.provider.sqlite.annotations.ObjectTable;
import com.peirr.provider.sqlite.annotations.Provide;

import android.content.ContentResolver;
import android.net.Uri;


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
    
    @Column(n = Pojo2.Mapper.other,e =false) 
    public String other;

    @Provide(BaseProvider.PROVIDE_BASE) 
    public static final String BASE = "pojo2";
    
    @Provide(BaseProvider.PROVIDE_URI) 
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + BASE);
    
    @Provide(BaseProvider.PROVIDE_ITEM_TYPE) 
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE  + "/" + BASE;
    
    @Provide(BaseProvider.PROVIDE_TYPE) 
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE;
    
//    @Provide(BaseProvider.PROVIDE_ONE) 
    public static int ONE;
    
//    @Provide(BaseProvider.PROVIDE_MANY) 
    public static int MANY;
    
    @Provide(BaseProvider.PROVIDE_KEY) 
    public static final String KEY = "pid";

    public final class Mapper extends ObjectMapper {
        public static final String pid = "pid";
        public static final String name = "name";
        public static final String other = "other";
    }


}
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


import android.net.Uri;

import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.annotations.Column;
import com.peirr.provider.sqlite.annotations.ObjectMapper;
import com.peirr.provider.sqlite.annotations.ObjectTable;
import com.peirr.provider.sqlite.annotations.Provide;


/**
 * just a dummy pojo
   
 * @author kurt
 * PeirrContentProvider
 */
public class Pojo extends ObjectTable {

    @Column(n = Pojo.Mapper.pid,e=false)
    private long pid;
    
    @Column(n = Pojo.Mapper.name,e =false)
    private String name;
    
    @Provide(BaseProvider.PROVIDE_TABLE)
    public static final String TABLE = "pojo";
    
    @Provide(BaseProvider.PROVIDE_URI)
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    
    @Provide(BaseProvider.PROVIDE_KEY)
    public static final String KEY = "pid";
    
    public long getPid() {
		return pid;
	}


    public Pojo(){}

	public Pojo(long pid, String name) {
		this.pid = pid;
		this.name = name;
	}




	public void setPid(long pid) {
		this.pid = pid;
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public static final class Mapper extends ObjectMapper {
        public static final String pid = "pid";
        public static final String name = "name";
    }


}
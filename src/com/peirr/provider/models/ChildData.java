package com.peirr.provider.models;

import android.net.Uri;

import com.peirr.provider.sqlite.BaseProvider;
import com.peirr.provider.sqlite.annotations.Column;
import com.peirr.provider.sqlite.annotations.ObjectMapper;
import com.peirr.provider.sqlite.annotations.ObjectTable;
import com.peirr.provider.sqlite.annotations.Provide;

public class ChildData extends ObjectTable {
	
	@Column(n = Mapper.CHILDFIELD1,e=false)
	public String childField1;
	
	@Column(n = Mapper.CHILDFIELD2,e=false)
	public float childField2;
	
	@Column(n = Mapper.CHILDFIELD3,e=false)
	public int childField3;
	
	@Provide(BaseProvider.PROVIDE_TABLE)
    public static final String TABLE = "childData";
    
    @Provide(BaseProvider.PROVIDE_URI)
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    
    @Provide(BaseProvider.PROVIDE_KEY)
    public static final String KEY = "_id";
    
    
    public static final class Mapper extends ObjectMapper {
        public static final String CHILDFIELD1 = "ci";
        public static final String CHILDFIELD2 = "cii";
        public static final String CHILDFIELD3 = "ciii";

    }


	@Override
	public String toString() {
		return "ChildData [childField1=" + childField1 + ", childField2="
				+ childField2 + ", childField3=" + childField3 + "]";
	}

    

}

package com.peirr.droidprovider.models;

import android.net.Uri;

import com.peirr.droidprovider.sqlite.BaseProvider;
import com.peirr.droidprovider.sqlite.annotations.Column;
import com.peirr.droidprovider.sqlite.annotations.ColumnMerge;
import com.peirr.droidprovider.sqlite.annotations.ObjectMapper;
import com.peirr.droidprovider.sqlite.annotations.ObjectTable;
import com.peirr.droidprovider.sqlite.annotations.Provide;

public class ParentData extends ObjectTable {
	
	public ParentData() {}
	
	public ParentData(String parentField1, float parentField2,ChildData details1) {
		this.parentField1 = parentField1;
		this.parentField2 = parentField2;
		this.details1 = details1;
	}

	@Column(n = Mapper.PARENTFIELD1,e=false)
	public String parentField1;
	
	@Column(n = Mapper.PARENTFIELD2,e=false)
	public float parentField2;
	
	@ColumnMerge(c=ChildData.class)
	public ChildData details1;
	
//	@ColumnMerge(c=ChildData.class)
//	public ChildData details2;
	
	@Provide(BaseProvider.PROVIDE_TABLE)
    public static final String TABLE = "parentData";
    
    @Provide(BaseProvider.PROVIDE_URI)
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    
    @Provide(BaseProvider.PROVIDE_KEY)
    public static final String KEY = "_id";
    
    
    public static final class Mapper extends ObjectMapper {
        public static final String PARENTFIELD1 = "pi";
        public static final String PARENTFIELD2 = "pii";

    }


	@Override
	public String toString() {
		return "ParentData [parentField1=" + parentField1 + ", parentField2="
				+ parentField2 + ", details1=" + details1 + ", _id=" + _id
				+ "]";
	}

}

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
	
	public ParentData(String p1, float p2,ChildData c1,ChildData c2) {
		this.parent1 = p1;
		this.parent2 = p2;
		this.parent3 = c1;
		this.parent4 = c2;
	}

	@Column(n = Mapper.FIELD1,e=false)
	public String parent1;
	
	@Column(n = Mapper.FIELD2,e=false)
	public float parent2;
	
	@ColumnMerge(c=ChildData.class)
	public ChildData parent3;
	
	@ColumnMerge(c=ChildData.class)
	public ChildData parent4;
	
	@Provide(BaseProvider.PROVIDE_TABLE)
    public static final String TABLE = "parentData";
    
    @Provide(BaseProvider.PROVIDE_URI)
    public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);
    
    @Provide(BaseProvider.PROVIDE_KEY)
    public static final String KEY = "_id";
    
    
    public static final class Mapper extends ObjectMapper {
        public static final String FIELD1 = "parent1";
        public static final String FIELD2 = "parent2";

    }


	@Override
	public String toString() {
		return "ParentData [parent1=" + parent1 + ", parent2=" + parent2
				+ ", parent3=" + parent3 + ", parent4=" + parent4 + "]";
	}




}

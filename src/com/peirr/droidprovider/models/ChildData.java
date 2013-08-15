package com.peirr.droidprovider.models;

import android.net.Uri;

import com.peirr.droidprovider.sqlite.BaseProvider;
import com.peirr.droidprovider.sqlite.annotations.Column;
import com.peirr.droidprovider.sqlite.annotations.ObjectMapper;
import com.peirr.droidprovider.sqlite.annotations.ObjectTable;
import com.peirr.droidprovider.sqlite.annotations.Provide;

public class ChildData extends ObjectTable {

	@Column(n = Mapper.FIELD1,e=false)
	public String child1;

	@Column(n = Mapper.FIELD2,e=false)
	public float  child2;

	@Provide(BaseProvider.PROVIDE_TABLE)
	public static final String TABLE = "childData";

	@Provide(BaseProvider.PROVIDE_URI)
	public static final Uri CONTENT_URI = BaseProvider.getContentUri("content://#AUTHORITY#/" + TABLE);

	@Provide(BaseProvider.PROVIDE_KEY)
	public static final String KEY = "_id";


	public static final class Mapper extends ObjectMapper {
		public static final String FIELD1 = "childA";
		public static final String FIELD2 = "childB";
	}

	@Override
	public String toString() {
		return "ChildData [child1=" + child1 + ", child2=" + child2 + "]";
	}

}

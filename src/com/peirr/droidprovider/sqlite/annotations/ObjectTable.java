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

package com.peirr.droidprovider.sqlite.annotations;



/**
 * Base columns required for each provider enabled object->table mapping
 * @author kurt 
 */
public class ObjectTable {
	@Column(n = "_id", e =false)
	@Index(primaryKey = true)
	public long _id;
	public static int ONE;
	public static int MANY;
	public static String CONTENT_ITEM_TYPE;
	public static String CONTENT_TYPE;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ObjectTable objectTable = (ObjectTable) o;

		if (_id != objectTable._id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (_id ^ (_id >>> 32));
		return result;
	}

}

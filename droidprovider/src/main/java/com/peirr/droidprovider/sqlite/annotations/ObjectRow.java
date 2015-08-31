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

package com.peirr.droidprovider.sqlite.annotations;


/**
 * Base columns required for each provider enabled object to table mapping
 *
 * @author kurt
 */
public class ObjectRow {
    public static int ONE;
    public static int MANY;
    public static String CONTENT_ITEM_TYPE;
    public static String CONTENT_TYPE;
    @DroidColumn(name = "_id", encrypt = false, primary = true)
    public long _id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectRow objectRow = (ObjectRow) o;

        return _id == objectRow._id;

    }

    @Override
    public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }

}

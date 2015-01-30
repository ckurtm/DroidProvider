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

package com.peirr.droidprovider.sqlite;

import java.lang.reflect.Field;

/**
 * Created by kurt on 2014/09/03.
 */
public class DroidProviderContract {
    private static final String[] values = initProvider();
    public static final String CONTENT_AUTHORITY = values[0];
    public static final int CONTENT_VERSION = Integer.valueOf(values[1]);

    private static String[] initProvider() {
        String[] values = new String[2];
        String authority = "com.peirr.droidprovider"; //The default AUTHORITY if non was found
        try {
            ClassLoader loader = DroidProviderContract.class.getClassLoader();
            Class<?> clz = loader.loadClass("com.peirr.provider.ProviderContract");
            Field declaredField = clz.getDeclaredField("CONTENT_AUTHORITY");
            values[0] = declaredField.get(null).toString();
            declaredField = clz.getDeclaredField("CONTENT_VERSION");
            values[1] = declaredField.get(null).toString();
        } catch (Exception e) {
            System.out.println("failed initialization of provider contract " + e.getMessage());
            values[0] = authority;
            values[1] = "1";
        }
        return values;
    }
}

/*
 * Copyright (c) 2012 Kurt Mbanje
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
 *   https://github.com/ckurtm/DroidProvider
 */

package mbanje.kurt.todo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.SystemClock;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

/**
 * Created by kurt on 2014/07/19.
 */
public class TestProviderTest extends ProviderTestCase2<TestProvider> {
    String TAG = TestProviderTest.class.getSimpleName();
    private ContentResolver resolver;

    public TestProviderTest() {
        super(TestProvider.class,Todo.PROVIDER_CLASS);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getContext().getContentResolver();
    }

    public void testNewPerfomance() throws Exception {
        resolver.delete(TodoTable.CONTENT_URI,null,null);
        int sample = 10;
        long[] values = new long[sample];
        for (int index = 0; index < sample ; index++) {
            long start = SystemClock.uptimeMillis();
            runInsertionTest(1000);
            values[index] = SystemClock.uptimeMillis() - start;
            Log.d(TAG,"...["+index+"] " + values[index] + " ...");
        }
        long total = 0;
        for (int i = 0; i < sample; i++) {
            total += values[i];
        }
        Log.d(TAG,"[sample:"+sample+"] [average:"+ ((float)total/(float)sample) + "]");
    }



    private void runInsertionTest(int count) throws Exception {
        TodoItem item = new TodoItem("label","thisis a description",false);
        for (int i = 0; i < count; i++) {
            ContentValues values = TodoTable.getContentValues(item);
            resolver.insert(TodoTable.CONTENT_URI,values);
        }
    }
}

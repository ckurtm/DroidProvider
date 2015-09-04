package mbanje.kurt.todo;

import android.app.Application;


/**
 * Created by kurt on 2015/07/01.
 */
public class Todo extends Application {

    public static final String PROVIDER_CLASS = "TestProvider";
    @Override public void onCreate() {
        super.onCreate();
    }
}

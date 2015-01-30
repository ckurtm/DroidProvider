package test.test.providersample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.peirr.droidprovider.test.R;

import java.util.List;


/**
 * Created by kurt on 2015/01/30.
 */
public class MyPojoAdapter extends ArrayAdapter<MyPojo>{

    private Context c;
    private int id;
    private List<MyPojo> items;

    public MyPojoAdapter(android.content.Context context, int textViewResourceId, List<MyPojo> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public MyPojo getMyPojo(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

        final MyPojo o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.col1);
            TextView t2 = (TextView) v.findViewById(R.id.col2);
            TextView t3 = (TextView) v.findViewById(R.id.col3);
            if (t1 != null)
                t1.setText(String.valueOf(o._id));
            if (t2 != null)
                t2.setText(String.valueOf(o.mystring));
            if (t3 != null)
                t3.setText(o.mydate.toString());

        }
        return v;
    }
}

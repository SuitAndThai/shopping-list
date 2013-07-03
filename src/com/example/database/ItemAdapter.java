package com.example.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.model.Item;
import com.example.shopping_list.R;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/30/13
 * Time: 1:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class ItemAdapter extends SimpleCursorAdapter {
    private Context context;

    private int layout;

    public ItemAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context = context;
        this.layout = layout;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.item_info_text_view);
        String text = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME));
        textView.setText(text);


        int nameCol = cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME);
        String name = cursor.getString(nameCol);

        // set the name of the entry
        TextView name_text = (TextView) view.findViewById(R.id.item_info_text_view);
        if (name_text != null) {
            name_text.setText(name);
        }

        shadeView(view, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int nameCol = c.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME);

        String name = c.getString(nameCol);

        // Next set the name of the entry.
        TextView itemInfoText = (TextView) v.findViewById(R.id.item_info_text_view);
        if (itemInfoText != null) {
            itemInfoText.setText(name);
        }

        shadeView(v, c);

        return v;
    }

    private void shadeView(View view, Cursor cursor) {
        int statusCol = cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS);
        int status = cursor.getInt(statusCol);
        if (status == Item.BOUGHT) {
            view.setAlpha((float) 0.3);
        } else {
            view.setAlpha((float) 1);
        }
    }
}

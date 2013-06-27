package com.example.shopping_list;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: sizhao
 * Date: 6/25/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyListFragments extends ListFragment{
    public static String lf = "MyListFragment";
    public MyListFragments(){}
    String[] items = new String[] {
            "item 1",
            "item 2",
            "item 3",
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(lf, "entered");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}

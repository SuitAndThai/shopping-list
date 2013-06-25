package com.example.shopping_list;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: sizhao
 * Date: 6/25/13
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScanAddActivity extends ListFragment{
    String[] lists = new String[] {
            "List 1",
            "List 2",
            "List 3",
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, lists);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}

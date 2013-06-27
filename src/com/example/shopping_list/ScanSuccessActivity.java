package com.example.shopping_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/25/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScanSuccessActivity extends Activity {
    public static String st = "scan-tag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_success);

        Log.d(st, "In scan success activity");

        TextView item_name = (TextView) findViewById(R.id.item_name);
        ImageView item_image = (ImageView) findViewById(R.id.item_image);
        final Button add_to_list_button = (Button) findViewById(R.id.add_to_list_button);
        final Button buy_online_button = (Button) findViewById(R.id.buy_online_button);

        Intent intent = getIntent();
        String content = intent.getStringExtra("CONTENT");
//        String format = intent.getStringExtra("FORMAT");
        SignedRequestsHelper helper = new SignedRequestsHelper(content);
        String urlString = helper.sign();

        Log.d(st, "url = " + urlString);

        try {
            URL url = new URL(urlString);
            String data = (new ConnectionTask()).execute(url).get();
            Log.d(st, "data: " + data);

            Toast toast = Toast.makeText(this, "Data: " + data, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        add_to_list_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        buy_online_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }
}
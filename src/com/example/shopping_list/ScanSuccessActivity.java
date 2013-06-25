package com.example.shopping_list;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/25/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScanSuccessActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_recv);

        final Button transfer_button = (Button) findViewById(R.id.transfer_button);
        final Button receive_button = (Button) findViewById(R.id.receive_button);

        transfer_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        receive_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }
}

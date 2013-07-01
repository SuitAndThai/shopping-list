package com.example.shopping_list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.api.ConnectionTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/25/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScanSuccessActivity extends Activity {
    public static final String KEY = "AIzaSyDpxWLapNipYoZ9SiLTaXCBEL7c_9_DkRI";
    private ArrayList<String> itemsToAdd;

    public static String st = "scan-tag";
    TextView item_name;
    String link;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_success);

        Log.d(st, "In scan success activity");

        item_name = (TextView) findViewById(R.id.item_name);
        ImageView item_image = (ImageView) findViewById(R.id.item_image);
        final Button add_to_list_button = (Button) findViewById(R.id.add_to_list_button);
        final Button buy_online_button = (Button) findViewById(R.id.buy_online_button);

        Intent intent = getIntent();
        String content = intent.getStringExtra("CONTENT");
        itemsToAdd = new ArrayList<String>();

        try {
            getItemInfo(content);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        add_to_list_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChooseLists.class);
                i.putExtra(MainActivity.ITEM_INTENT, itemsToAdd);
                startActivity(i);
            }
        });
    }

    public void buyOnline(View view) {
        Log.d("buyOnline", link);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        startActivityForResult(i, 1);
    }

    private void getItemInfo(String content) throws MalformedURLException, ExecutionException, InterruptedException, JSONException {
        String message = "https://www.googleapis.com/shopping/search/v1/public/products?key=" + KEY + "&country=US&q=" + content;

        URL url = new URL(message);
        String data = (new ConnectionTask()).execute(url).get();
        Log.d("search result: ", data);

        JSONObject jObject = new JSONObject(data);
        Log.d("search result in JSONObject: ", jObject.toString());

        JSONArray item = jObject.getJSONArray("items");
        Log.d("itemArray: ", item.toString());

        JSONObject itemObject = item.getJSONObject(0);
        JSONObject productObject = itemObject.getJSONObject("product");
        link = productObject.getString("link");
        JSONArray image = productObject.getJSONArray("images");
        String title = productObject.getString("title");
        JSONObject imageLinkObject = image.getJSONObject(0);
        String imageLink = imageLinkObject.getString("link");

        item_name.setText(title);
        item_name.setTextSize(20);


        Log.d("getItemInfo", link);
        Log.d("title: ", title);
        Log.d("imageLink: ", imageLink);

        new DownloadImage((ImageView) findViewById(R.id.item_image))
                .execute(imageLink);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView layoutimage;

        public DownloadImage(ImageView Image) {
            this.layoutimage = Image;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(String... Image_URL) {

            String Downloadimage = Image_URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(Downloadimage)
                        .openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                // Error Log
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set image into image.xml layout
            layoutimage.setImageBitmap(result);
        }
    }
}
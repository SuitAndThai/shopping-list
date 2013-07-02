package com.example.shopping_list;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.api.ConnectionTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class RecipesActivity extends Activity {

    protected EditText recipeInput;
    protected ListView ingredientsList;
    protected Button addRecipeButton;
    protected ArrayList<String> ingredients;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipes);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recipeInput = (EditText) findViewById(R.id.recipe_name_edit_text);
        ingredientsList = (ListView) findViewById(R.id.ingredients_list_view);
        addRecipeButton = (Button) findViewById(R.id.add_ingredients_button);
        ingredients = new ArrayList<String>();

        // set up soft keyboard for input
        recipeInput.setSingleLine();
        recipeInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        recipeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    String recipe = v.getText().toString();
                    if (!recipe.equals("")) {
                        // form the url
                        String baseUrl = "http://api.yummly.com/v1/api/recipes?_app_id=0e79ba35&_app_key=bf480af86567c713fe101a375763c5e4&q=";
                        String urlString = (baseUrl + recipe).replace(" ", "+");
                        URL url = null;

                        try {
                            url = new URL(urlString);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        String data = null;
                        try {
                            data = (new ConnectionTask()).execute(url).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(data);
                            JSONArray recipeArray = jObject.getJSONArray("matches");
                            String ingredientsString = " ";
                            for (int i = 0; i < recipeArray.length(); i++) {
                                try {
                                    JSONObject oneObject = recipeArray.getJSONObject(i);
                                    ingredientsString = oneObject.getString("ingredients");
                                } catch (JSONException e) {
                                }
                            }

                            String[] ingredientsArray = ingredientsString.substring(1, ingredientsString.length() - 1).replace("\"", "").split(",");
                            ingredients.clear();
                            for (String s : ingredientsArray) {
                                ingredients.add(s);
                            }

                            StringBuilder s = new StringBuilder();
                            for (String i : ingredientsArray) {
                                s.append(i + " ");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            hideSoftKeyboard();
                        }
                        addRecipeButton.setVisibility(View.VISIBLE);
                    } else {
                        ingredients.clear();
                        hideSoftKeyboard();
                        addRecipeButton.setVisibility(View.INVISIBLE);
                    }

                    final StableArrayAdapter adapter = new StableArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ingredients);
                    ingredientsList.setAdapter(adapter);

                    return true;
                }
                return false;
            }
        });

        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChooseLists.class);
                i.putExtra(MainActivity.ITEM_INTENT, ingredients);
                startActivityForResult(i, MainActivity.ADD_REQUEST);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }


    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.ADD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}
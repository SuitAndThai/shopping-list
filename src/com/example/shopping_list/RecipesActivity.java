package com.example.shopping_list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

    private EditText recipeInput;
    private ListView ingredientsList;
    private Button addRecipeButton;

    private StableArrayAdapter mAdapter;
    private ArrayList<String> ingredients;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipes);

        recipeInput = (EditText) findViewById(R.id.recipe_name_edit_text);
        ingredientsList = (ListView) findViewById(R.id.ingredients_list_view);
        addRecipeButton = (Button) findViewById(R.id.add_ingredients_button);
        ingredients = new ArrayList<String>();
//        ingredientsList.setTextFilterEnabled(true);


        recipeInput.setSingleLine();
        recipeInput.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        recipeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    // get text
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
                        Toast.makeText(getApplicationContext(), "Recipe info = " + data, Toast.LENGTH_SHORT);

                        JSONObject jObject = null;
                        try {
                            jObject = new JSONObject(data);
                            String totalMatchCount = jObject.getString("totalMatchCount");
                            JSONArray recipeArray = jObject.getJSONArray("matches");
                            Context context = getApplicationContext();

                            Toast.makeText(context, "Total Matches = " + totalMatchCount, Toast.LENGTH_SHORT).show();

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

//                            final StableArrayAdapter adapter = new StableArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ingredients);
//                            ingredientsList.setAdapter(adapter);

                            StringBuilder s = new StringBuilder();
                            for (String i : ingredientsArray) {
                                s.append(i + " ");
                            }
                            Toast.makeText(getApplicationContext(), "Ingredients = " + s.toString(), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } finally {
                            hideSoftKeyboard();
                        }

                        Log.d("recipe info as JObject: ", jObject.toString());
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
//                ingredients = new ArrayList<String>();

            }
        });
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

    private void displayIngredientsList(String[] ingredientsArray) {

    }


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
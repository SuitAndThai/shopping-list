package com.example.database;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */

import com.example.model.Item;
import com.example.model.ShoppingList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.*;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "comments.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO: only open DB once
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        for (String statement : DBConstants.DDL) {
            database.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.ShoppingListsCols.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.ItemsCols.TABLE_NAME);
        onCreate(db);
    }

    public SQLiteDatabase open() throws SQLException {
        return this.getWritableDatabase();
    }

    //Item methods
    public void addItem(Item newItem) {
        SQLiteDatabase db = open();
        ContentValues values = new ContentValues();

        //get size of the list
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, new String[]{DBConstants.ItemsCols._ID, DBConstants.ItemsCols.NAME, DBConstants.ItemsCols.STATUS, DBConstants.ItemsCols.ITEM_ORDER, DBConstants.ItemsCols._SHOPPING_LIST_ID},
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(newItem.listId)}
                , null, null, null);
        int count;

        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            count = cursor.getCount();
        } finally {
            cursor.close();
        }

        values.put(DBConstants.ItemsCols._SHOPPING_LIST_ID, newItem.listId);
        values.put(DBConstants.ItemsCols.NAME, newItem.name);
        values.put(DBConstants.ItemsCols.ITEM_ORDER, count);
        values.put(DBConstants.ItemsCols.STATUS, Item.UNBOUGHT);

        db.insert(DBConstants.ItemsCols.TABLE_NAME, null, values);

        db.close();
    }

    public int toggleItem(Item newItem) {
        Item item = new Item(newItem);

        SQLiteDatabase db = open();
        ContentValues values = new ContentValues();
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, new String[]{DBConstants.ItemsCols._ID, DBConstants.ItemsCols.NAME, DBConstants.ItemsCols.STATUS, DBConstants.ItemsCols.ITEM_ORDER, DBConstants.ItemsCols._SHOPPING_LIST_ID},
                DBConstants.ItemsCols._ID + "=?", new String[]{String.valueOf(item.id)}
                , null, null, null);
        cursor.moveToNext();

        item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._ID));
        item.name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME));
        item.listId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._SHOPPING_LIST_ID));
        item.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.ITEM_ORDER));
        int old_status = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS));
        item.status = Math.abs(old_status - 1);

        db.close();

        return updateItem(item);
    }

    public int updateItem(Item item) {
        SQLiteDatabase db = open();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ItemsCols._SHOPPING_LIST_ID, item.listId);
        values.put(DBConstants.ItemsCols.NAME, item.name);
        values.put(DBConstants.ItemsCols.ITEM_ORDER, item.order);
        values.put(DBConstants.ItemsCols.STATUS, item.status);

        // updating row
        int result = db.update(DBConstants.ItemsCols.TABLE_NAME, values, DBConstants.ItemsCols._ID + " = ?",
                new String[]{String.valueOf(item.id)});
        db.close();
        return result;
    }


    private void deleteItem(Item item) {
        SQLiteDatabase db = open();
        db.delete(DBConstants.ItemsCols.TABLE_NAME, DBConstants.ItemsCols._ID + " = ?",
                new String[]{String.valueOf(item.id)});
        db.close();
    }

    /*
    Returns the cursor moved to the first row or null if no entries exist
     */
    public Cursor fetchAllItems(ShoppingList list) {
        SQLiteDatabase db = open();
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(list.id)}
                , null, null, DBConstants.ItemsCols.ITEM_ORDER);
        return cursor;
    }

    public boolean doesItemExist(String name) {
        SQLiteDatabase db = open();
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols.NAME + "=?", new String[]{String.valueOf(name)}
                , null, null, DBConstants.ItemsCols.ITEM_ORDER);

        return cursor.getCount() > 0;
    }

    /*
    Returns the cursor moved to the first row or null if no entries exist
     */
    public Cursor fetchList(ShoppingList list) {
        SQLiteDatabase db = open();
        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, new String[]{DBConstants.ShoppingListsCols._ID, DBConstants.ShoppingListsCols.LIST_ORDER},
                DBConstants.ShoppingListsCols.TITLE + "=?", new String[]{String.valueOf(list.title)}
                , null, null, null);
        return cursor;
    }

    //List methods

    /*
     * takes in a ShoppingList object and inserts it into the database and reorders the lists.
     * returns the list_order of the list that was just inserted
     */
    public int addList(ShoppingList newList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, ShoppingList.UNFAVORITE);
        values.put(DBConstants.ShoppingListsCols.TITLE, newList.title);
        values.put(DBConstants.ShoppingListsCols.LIST_ORDER, 0);

        db.insert(DBConstants.ShoppingListsCols.TABLE_NAME, null, values);

        db.close();
        orderLists();

        // TODO: ensure that duplicate lists cannot be added by searching for other list names prior to insertion
        // don't judge me
        db = this.getWritableDatabase();
        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, new String[]{DBConstants.ShoppingListsCols._ID, DBConstants.ShoppingListsCols.TITLE, DBConstants.ShoppingListsCols.LIST_ORDER},
                DBConstants.ShoppingListsCols.TITLE + "=?", new String[]{String.valueOf(newList.title)}
                , null, null, null);
        cursor.moveToFirst();
        int list_order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.LIST_ORDER));
        return list_order;
    }

    public void deleteList(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBConstants.ShoppingListsCols.TABLE_NAME, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
        db.close();
    }

    public void orderLists() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursorFavorites = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.IS_FAVORITE + "=?", new String[]{String.valueOf(ShoppingList.FAVORITE)}
                , null, null, null);
        Cursor cursorUnfavorites = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.IS_FAVORITE + "=?", new String[]{String.valueOf(ShoppingList.UNFAVORITE)}
                , null, null, null);

        SortedMap<String, ShoppingList> favoriteLists = new TreeMap<String, ShoppingList>();
        SortedMap<String, ShoppingList> unfavoriteLists = new TreeMap<String, ShoppingList>();

        if ((cursorFavorites == null) || (cursorUnfavorites == null)) {
            throw new RuntimeException();
        }
        try {

            if (cursorFavorites.getCount() > 0) {
                cursorFavorites.moveToFirst();
                do {
                    ShoppingList list = new ShoppingList();
                    list.id = cursorFavorites.getInt(cursorFavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols._ID));
                    list.title = cursorFavorites.getString(cursorFavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.TITLE));
                    list.favorite = cursorFavorites.getInt(cursorFavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.IS_FAVORITE));
                    list.order = cursorFavorites.getInt(cursorFavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.LIST_ORDER));
                    favoriteLists.put(list.title, list);
                } while (cursorFavorites.moveToNext());
            }

            if (cursorUnfavorites.getCount() > 0) {
                cursorUnfavorites.moveToFirst();
                do {
                    ShoppingList list = new ShoppingList();
                    list.id = cursorUnfavorites.getInt(cursorUnfavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols._ID));
                    list.title = cursorUnfavorites.getString(cursorUnfavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.TITLE));
                    list.favorite = cursorUnfavorites.getInt(cursorUnfavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.IS_FAVORITE));
                    list.order = cursorUnfavorites.getInt(cursorUnfavorites.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.LIST_ORDER));
                    unfavoriteLists.put(list.title, list);
                } while (cursorUnfavorites.moveToNext());
            }

        } finally {
            cursorFavorites.close();
            cursorUnfavorites.close();
        }

        db.close();

        int i = 0;
        SortedSet<String> unfavoriteKeys = new TreeSet<String>(unfavoriteLists.keySet());
        SortedSet<String> favoriteKeys = new TreeSet<String>(favoriteLists.keySet());
        for (String title : favoriteKeys) {
            ShoppingList list = favoriteLists.get(title);
            list.order = i;
            i++;
            updateList(list);
        }
        for (String title : unfavoriteKeys) {
            ShoppingList list = unfavoriteLists.get(title);
            list.order = i;
            i++;
            updateList(list);
        }
    }

    public void orderListItems(ShoppingList list) {
        Cursor cursor = fetchAllItems(list);

        SortedMap<String, Item> unboughtItems = new TreeMap<String, Item>();
        SortedMap<String, Item> boughtItems = new TreeMap<String, Item>();

        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            cursor.moveToFirst();

            do {
                Item item = new Item();
                item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._ID));
                item.name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME));
                item.status = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS));
                item.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.ITEM_ORDER));
                item.listId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._SHOPPING_LIST_ID));

                if (item.status == Item.BOUGHT) {
                    boughtItems.put(item.name, item);
                } else {
                    unboughtItems.put(item.name, item);
                }
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }

        int i = 0;
        SortedSet<String> unboughtKeys = new TreeSet<String>(unboughtItems.keySet());
        SortedSet<String> boughtKeys = new TreeSet<String>(boughtItems.keySet());

        for (String name : unboughtKeys) {
            Item item = new Item(unboughtItems.get(name));
            item.order = i;
            i++;
            updateItem(item);
        }
        for (String name : boughtKeys) {
            Item item = new Item(boughtItems.get(name));
            item.order = i;
            i++;
            updateItem(item);
        }
    }

    public int toggleFavorite(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, Math.abs(list.favorite - 1));
        values.put(DBConstants.ShoppingListsCols.TITLE, list.title);

        // updating row
        int result = db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});

        db.close();
        return result;
    }

    public int renameList(ShoppingList list, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, list.favorite);
        values.put(DBConstants.ShoppingListsCols.TITLE, newName);

        // updating row
        int result = db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
        db.close();
        return result;
    }


    public ShoppingList getList(int order) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.LIST_ORDER + "=?", new String[]{String.valueOf(order)}
                , null, null, null);
        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            if (cursor.moveToFirst()) {
                ShoppingList list = new ShoppingList();
                list.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols._ID));
                list.title = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.TITLE));
                list.favorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.IS_FAVORITE));
                list.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.LIST_ORDER));
                db.close();
                return list;
            }
            db.close();
            return null;
        } finally {
            cursor.close();
        }

    }

    public boolean listExists(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean exists = false;

        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.TITLE + "=?", new String[]{title}
                , null, null, null);
        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            exists = cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
        db.close();

        return exists;
    }

    public boolean listExists(int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean exists = false;

        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.LIST_ORDER + "=?", new String[]{String.valueOf(order)}
                , null, null, null);
        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            exists = cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
        db.close();

        return exists;
    }

    public int updateList(ShoppingList list) {
        SQLiteDatabase db = open();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols.TITLE, list.title);
        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, list.favorite);
        values.put(DBConstants.ShoppingListsCols.LIST_ORDER, list.order);

        // updating row
        int result = db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
        db.close();
        return result;
    }

}
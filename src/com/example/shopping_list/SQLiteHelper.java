package com.example.shopping_list;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */

import Model.Item;
import Model.ShoppingList;
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
    private static final int DATABASE_VERSION = 4;

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
        int listId = newItem.listId;
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(listId)}
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
        item.status = Math.abs(item.status - 1);
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
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, new String[]{DBConstants.ItemsCols._ID, DBConstants.ItemsCols.NAME},
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(list.id)}
                , null, null, null);
        return cursor;
    }

    //List methods
    public void addList(ShoppingList newList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, ShoppingList.UNFAVORITE);
        values.put(DBConstants.ShoppingListsCols.TITLE, newList.title);
        values.put(DBConstants.ShoppingListsCols.LIST_ORDER, 0);

        db.insert(DBConstants.ShoppingListsCols.TABLE_NAME, null, values);

        db.close();
        orderLists();
    }

    public void deleteList(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBConstants.ShoppingListsCols.TABLE_NAME, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
        db.close();
    }

    public void orderLists() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(DBConstants.ShoppingListsCols.TABLE_NAME, null,
                DBConstants.ShoppingListsCols.IS_FAVORITE + "=?", new String[]{String.valueOf(ShoppingList.IS_FAVORITE)}
                , null, null, null);

        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            ArrayList<ShoppingList> shoppingLists = new ArrayList<ShoppingList>();
            while (cursor.moveToNext()) {
                ShoppingList list = new ShoppingList();
                list.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols._ID));
                list.title = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.TITLE));
                list.favorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.IS_FAVORITE));
                list.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ShoppingListsCols.LIST_ORDER));
                shoppingLists.add(list);
            }

            SortedMap<String, ShoppingList> favoriteLists = new TreeMap<String, ShoppingList>();
            SortedMap<String, ShoppingList> notFavoriteLists = new TreeMap<String, ShoppingList>();
            for (ShoppingList list : shoppingLists) {
                if (list.favorite == ShoppingList.IS_FAVORITE) {
                    favoriteLists.put(list.title, list);
                } else {
                    notFavoriteLists.put(list.title, list);
                }
            }

            int i = 0;
            SortedSet<String> favoriteKeys = new TreeSet<String>(favoriteLists.keySet());
            for (String title : favoriteKeys) {
                favoriteLists.get(title).order = i;
                i++;
                updateList(favoriteLists.get(title));
            }

            SortedSet<String> notFavoriteKeys = new TreeSet<String>(notFavoriteLists.keySet());
            for (String title : notFavoriteKeys) {
                notFavoriteLists.get(title).order = i;
                i++;
                updateList(notFavoriteLists.get(title));
            }
        } finally {
            cursor.close();
        }

        db.close();
    }

    public void orderListItems(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(list.id)}
                , null, null, null);

        if (cursor == null) {
            throw new RuntimeException();
        }
        try {
            ArrayList<Item> itemList = new ArrayList<Item>();
            while (cursor.moveToNext()) {
                Item item = new Item();
                item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._ID));
                item.name = cursor.getString(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.NAME));
                item.status = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.STATUS));
                item.order = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols.ITEM_ORDER));
                item.listId = cursor.getInt(cursor.getColumnIndexOrThrow(DBConstants.ItemsCols._SHOPPING_LIST_ID));
                itemList.add(item);
            }

            SortedMap<String, Item> unboughtItems = new TreeMap<String, Item>();
            SortedMap<String, Item> boughtItems = new TreeMap<String, Item>();
            for (Item item : itemList) {
                if (item.status == 1) {
                    boughtItems.put(item.name, item);
                } else {
                    unboughtItems.put(item.name, item);
                }
            }

            int i = 0;
            SortedSet<String> unboughtKeys = new TreeSet<String>(unboughtItems.keySet());
            for (String name : unboughtKeys) {
                unboughtItems.get(name).order = i;
                i++;
                updateItem(unboughtItems.get(name));
            }

            SortedSet<String> boughtKeys = new TreeSet<String>(boughtItems.keySet());
            for (String name : boughtKeys) {
                boughtItems.get(name).order = i;
                i++;
                updateItem(boughtItems.get(name));
            }
        } finally {
            cursor.close();
        }

        db.close();
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
package com.example.shopping_list;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.*;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_COMMENTS = "comments";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COMMENT = "comment";

    private static final String DATABASE_NAME = "commments.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_COMMENT
            + " text not null);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

        values.put(DBConstants.ItemsCols._ID, newItem.id);
        values.put(DBConstants.ItemsCols._SHOPPING_LIST_ID, newItem.listId);
        values.put(DBConstants.ItemsCols.NAME, newItem.name);
        values.put(DBConstants.ItemsCols.ORDER, newItem.order);
        values.put(DBConstants.ItemsCols.STATUS, newItem.status);

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

        values.put(DBConstants.ItemsCols._ID, item.id);
        values.put(DBConstants.ItemsCols._SHOPPING_LIST_ID, item.listId);
        values.put(DBConstants.ItemsCols.NAME, item.name);
        values.put(DBConstants.ItemsCols.ORDER, item.order);
        values.put(DBConstants.ItemsCols.STATUS, item.status);

        // updating row
        return db.update(DBConstants.ItemsCols.TABLE_NAME, values, DBConstants.ItemsCols._ID + " = ?",
                new String[]{String.valueOf(item.id)});
    }


    private void deleteItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBConstants.ItemsCols.TABLE_NAME, DBConstants.ItemsCols._ID + " = ?",
                new String[]{String.valueOf(item.id)});
        db.close();
    }

    public Cursor fetchAllItems(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(list.id)}
                , null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    //List methods
    public void addList(ShoppingList newList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols._ID, newList.id);
        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, newList.favorite);
        values.put(DBConstants.ShoppingListsCols.TITLE, newList.title);

        db.insert(DBConstants.ShoppingListsCols.TABLE_NAME, null, values);
        db.close();
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

        if (cursor != null)
            cursor.moveToFirst();

        ArrayList<ShoppingList> shoppingLists = new ArrayList<ShoppingList>();
        while (cursor.moveToNext()) {
            ShoppingList list = new ShoppingList();
            list.id = cursor.getInt(0);
            list.title = cursor.getString(1);
            list.favorite = cursor.getInt(2);
            list.order = cursor.getInt(3);
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

    }

    public void orderListItems(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(DBConstants.ItemsCols.TABLE_NAME, null,
                DBConstants.ItemsCols._SHOPPING_LIST_ID + "=?", new String[]{String.valueOf(list.id)}
                , null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        ArrayList<Item> itemList = new ArrayList<Item>();
        while (cursor.moveToNext()) {
            Item item = new Item();
            item.id = cursor.getInt(0);
            item.name = cursor.getString(1);
            item.status = cursor.getInt(2);
            item.order = cursor.getInt(3);
            item.listId = cursor.getInt(4);
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
    }

    public int toggleFavorite(ShoppingList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols._ID, list.id);
        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, Math.abs(list.favorite - 1));
        values.put(DBConstants.ShoppingListsCols.TITLE, list.title);

        // updating row
        return db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
    }

    public int renameList(ShoppingList list, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols._ID, list.id);
        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, list.favorite);
        values.put(DBConstants.ShoppingListsCols.TITLE, newName);

        // updating row
        return db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
    }


    public ShoppingList getList(int order){
        // TODO: get the list object
        return null;
    }

    public int updateList(ShoppingList list) {
        SQLiteDatabase db = open();
        ContentValues values = new ContentValues();

        values.put(DBConstants.ShoppingListsCols._ID, list.id);
        values.put(DBConstants.ShoppingListsCols.TITLE, list.title);
        values.put(DBConstants.ShoppingListsCols.IS_FAVORITE, list.favorite);
        values.put(DBConstants.ShoppingListsCols.ORDER, list.order);

        // updating row
        return db.update(DBConstants.ShoppingListsCols.TABLE_NAME, values, DBConstants.ShoppingListsCols._ID + " = ?",
                new String[]{String.valueOf(list.id)});
    }

}
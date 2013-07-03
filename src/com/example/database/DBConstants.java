package com.example.database;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBConstants {

    public static class ShoppingListsCols {
        private ShoppingListsCols() {}
        public static final String TABLE_NAME ="ShoppingLists";

        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String IS_FAVORITE = "is_favorite";
        public static final String LIST_ORDER = "list_order";

    }

    public static class ItemsCols {
        private ItemsCols() {}
        public static final String TABLE_NAME ="Items";

        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String STATUS = "status";
        public static final String ITEM_ORDER = "item_order";
        public static final String _SHOPPING_LIST_ID = "_shopping_list_id";

    }

    public static String[] DDL = {
            "CREATE TABLE Items(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, status INTEGER, item_order INTEGER, _shopping_list_id INTEGER NOT NULL);",
            "CREATE TABLE ShoppingLists(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, is_favorite INTEGER, list_order INTEGER);",
            "CREATE TRIGGER IF NOT EXISTS ShoppingLists_deleted AFTER DELETE ON ShoppingLists BEGIN DELETE FROM Items WHERE _shopping_list_id = old._id; END;",
    };
}


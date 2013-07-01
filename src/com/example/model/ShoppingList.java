package com.example.model;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShoppingList {
    public int id;
    public String title;
    public int favorite;
    public int order;

    public static int FAVORITE = 1;
    public static int UNFAVORITE = 0;

    public ShoppingList() {

    }

    public ShoppingList(ShoppingList list) {
        this.id = list.id;
        this.title = list.title;
        this.favorite = list.favorite;
        this.order = list.order;
    }

    public ShoppingList(String title) {

        this.title = title;
        favorite = 0;

    }
}

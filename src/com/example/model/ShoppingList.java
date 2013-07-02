package com.example.model;

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
}

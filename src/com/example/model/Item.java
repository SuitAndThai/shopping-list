package com.example.model;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/27/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class Item {
    public static int BOUGHT = 1;
    public static int UNBOUGHT = 0;

    public int id;
    public String name;
    public int status;
    public int order;
    public int listId;

    public Item(){
        status = 0;
    }

    public Item(Item item) {
        this.id = item.id;
        this.name = item.name;
        this.status = item.status;
        this.order = item.order;
        this.listId = item.listId;
    }
}

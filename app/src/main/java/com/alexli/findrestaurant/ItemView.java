package com.alexli.findrestaurant;

/**
 * Created by alexandrli on 22/09/17.
 */

public class ItemView
{
    public String name;
    public String cuisines;
    public String costForTwo;
    public String rating;
    public String address;

    public ItemView(String name, String cuisines, String costForTwo, String rating, String address) {
        this.name = name;
        this.cuisines = cuisines;
        this.costForTwo = costForTwo;
        this.rating = rating;
        this.address = address;
    }
}

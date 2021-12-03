package org.sinou.android.pydia.browse;

import java.util.ArrayList;
import java.util.Random;

public class Item {

    private final static Random generator = new Random(System.currentTimeMillis());

    String title;
    String description;
    long timeStamp;
    long size;
    String localUri;

    Item(String title) {
        this(title, "", 0, 0, "");
    }

    Item(String title, String description, int timeStamp, int size, String localUri) {
        this.title = title;
        this.description = description;
        this.timeStamp = timeStamp;
        this.size = size;
        this.localUri = localUri;
    }

    public static Item createChild(Item parent, int id) {
        Item curr = new Item(parent.title + "-" + id);
        curr.description = "Description for " + curr.title;
        curr.timeStamp = System.currentTimeMillis()/1000;
        curr.size = generator.nextInt()/1000;
        return curr;
    }

    public static Item[] getItems(Item parent) {
        ArrayList<Item> mItems = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mItems.add(createChild(parent, i));
        }
        return mItems.toArray(new Item[50]);
    }
}

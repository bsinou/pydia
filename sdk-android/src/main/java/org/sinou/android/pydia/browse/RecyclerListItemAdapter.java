package org.sinou.android.pydia.browse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import org.sinou.android.pydia.R;

/**
 * Manage view layout of a single level of the tree when we are in list mode.
 */
public class RecyclerListItemAdapter extends RecyclerView.Adapter<ItemRowViewHolder> {

    private static final String TAG = "RecyclerListAdap";
    private final Item parentItem;

    private NavController navController;


    private Item[] items;

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param items Item[] containing the data to populate views to be used by RecyclerView.
     */
    public RecyclerListItemAdapter(@NonNull NavController navController, @NonNull Item parentItem, @NonNull Item[] items) {
        this.navController = navController;
        this.parentItem = parentItem;
        this.items = items;
    }

    /**
     * Or only getting the parent and computing (or getting) children at a later point ?
     *
     * @param navController
     * @param parentItem
     */
    public RecyclerListItemAdapter(@NonNull NavController navController, Item parentItem) {
        this.navController = navController;
        this.parentItem = parentItem;
        items = new Item[]{};
    }

//    public void setAllData(Item[] dataSet) {
//        items = dataSet;
//    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public ItemRowViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_item_row, viewGroup, false);
        return new ItemRowViewHolder(navController, v);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ItemRowViewHolder viewHolder, final int position) {

        Item item = items[position];
        viewHolder.setData(item);
    }

    /**
     * @return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return items.length;
    }
}

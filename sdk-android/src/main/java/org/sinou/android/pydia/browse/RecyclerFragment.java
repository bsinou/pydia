package org.sinou.android.pydia.browse;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.sinou.android.pydia.R;

/**
 * Experiment with latest {@link RecyclerView}
 */
public class RecyclerFragment extends Fragment {

    private static final String TAG = "RecyclerFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";

    private NavController navController;

    private View scrim;
    private View bottomView;
    private BottomSheetBehavior behavior;

    // TODO make this dependant of screen width
    private static final int SPAN_COUNT = 4;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected View rootView;
    protected RecyclerView mView;
    protected RecyclerListItemAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    // Holds the list of items that is currently displayed and has been loaded from the local cache
    private Item[] items;

    // Also hold a reference to the parent State to ease update?
    private Item parentItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO cleanly manage full item retrieval
        String title = RecyclerFragmentArgs.fromBundle(getArguments()).getItemTitle();
        updateParentState(new Item(title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        rootView.setTag(TAG);

        mView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        // LinearLayoutManager is used here, tguide/topics/ui/declaring-layout.htmlhis will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        navController = NavHostFragment.findNavController(this);

        ActionBar bar = getActivity().getActionBar();
        if (bar != null) {
            bar.setTitle(parentItem.title);
        }

        // Set CustomAdapter as the adapter for RecyclerView.
        mAdapter = new RecyclerListItemAdapter(navController, parentItem, items);
        mView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrim = rootView.findViewById(R.id.bottom_sheet_scrim);
        scrim.setClickable(true);
        scrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomSheet();
            }
        });


        bottomView = rootView.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mView.setLayoutManager(mLayoutManager);
        mView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * @param state
     * @return true if the parent has changed
     */
    private boolean updateParentState(@NonNull Item state) {

        if (state.equals(parentItem)) {
            return false;
        }

        parentItem = state;
        items = Item.getItems(state);
        // TODO Also notify background activities

        return true;
    }

    protected void showMenuFor(Item item) {
        Log.d(TAG, "Showing menu for " + item.title);

        // Update UI for current item
        ((TextView) bottomView.findViewById(R.id.menu_title)).setText(item.title);
        ((TextView) bottomView.findViewById(R.id.menu_item_1)).setText(item.description);


        bottomView.findViewById(R.id.menu_item_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askNewTitle(bottomView.getContext());
            }
        });

        // Dummy important flag based on the name
        boolean checked = item.title.endsWith("2") || item.title.endsWith("7");
        SwitchMaterial importantToggle = ((SwitchMaterial) bottomView.findViewById(R.id.important_toggle));
        importantToggle.setChecked(checked);
        importantToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO do something important

                // Tell the fragment we are done with the action
                hideBottomSheet();
            }
        });

        // Finally show the scrim and expand the menu
        scrim.setVisibility(View.VISIBLE);         // TODO add a fade in
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        scrim.setVisibility(View.GONE);
    }

    private void askNewTitle(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please enter new item title");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // | InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                Snackbar.make(rootView, "New title: " + title + ". Note: read-only demo nothing is persisted.",
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                hideBottomSheet();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}

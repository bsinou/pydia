package org.sinou.android.pydia;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class BrowseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_left_layout);

        // Replace the default (legacy) action bar by the more recent toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open,
                R.string.nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.browse_host_fragment);
        navController = navHostFragment.getNavController();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        System.out.println("###### Options Item selected: " + item.getItemId());
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void tmpMsg(int id) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Snackbar.make(drawer, "Item menu " + id + " clicked.",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        tmpMsg(id);

        boolean success = false;
        switch (id) {
            default:
                // Enable direct reference to a fragment ID from the XML menu definition.
                success = NavigationUI.onNavDestinationSelected(item, navController)
                        || super.onOptionsItemSelected(item);

//            case R.id.nav_to_dummy_tree:
//                Intent i = new Intent(DrawerActivity.this, DummyTreeFragment.class);
//                startActivity(i);
//                break;
        }
        //
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//        } else if (id == R.id.nav_slideshow) {
//        } else if (id == R.id.nav_manage) {
//        } else if (id == R.id.nav_share) {
//        } else if (id == R.id.nav_send) {
//        }
        if (success) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }
}




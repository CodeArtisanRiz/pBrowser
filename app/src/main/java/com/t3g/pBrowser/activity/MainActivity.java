package com.t3g.pBrowser.activity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.t3g.pBrowser.R;
import com.t3g.pBrowser.adapter.CategoryAdapter;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = findViewById(R.id.viewPager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(mContext, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.op_main_menu, menu);

//        Get the SearchView and set searchable config
        SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        Do not iconify th widget; expand it by default
        searchView.setIconifiedByDefault(false);
//        SearchView hint
        searchView.setQueryHint("Search or enter url");
//        Submit btn
        searchView.setSubmitButtonEnabled(true);

//        searchView.getMaxWidth()

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
//                Toast.makeText(MainActivity.this,"url captured",Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
//        Disabling options not applicable on start page
        menu.findItem(R.id.op_share).setEnabled(false);
        menu.findItem(R.id.op_page).setEnabled(false);
        menu.findItem(R.id.op_findInPage).setEnabled(false);
        menu.findItem(R.id.op_addBookmark).setEnabled(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//         Handle item selection
        switch (item.getItemId()) {

            case R.id.op_share:

                return true;

            case R.id.op_newTab:

                return true;

            case R.id.op_addBookmark:

                return true;

            case R.id.op_incognitoMode:
                Intent op_5 = new Intent(getApplicationContext(), IncognitoWebActivity.class);
                startActivity(op_5);
                return true;

            case R.id.op_bookmarks:
                Intent op_1 = new Intent(getApplicationContext(), DownloadActivity.class);
                startActivity(op_1);
                return true;

            case R.id.op_downloads:

                return true;

            case R.id.op_history:
                Intent op_2 = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(op_2);
                return true;

//            case R.id.op_findInPage:
//
//                return true;

//            case R.id.op_reqDesktopSite:
//
//                return true;
//
//            case R.id.op_print:
//
//                return true;
//
//            case R.id.op_pageSource:
//
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //    On back button pressed fn
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK)
            return super.onKeyDown(keyCode, event);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //yes
                        finishAffinity();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //no
                        break;


                }

            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        return super.onKeyDown(keyCode, event);
    }
}

package com.t3g.pBrowser.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.t3g.pBrowser.Model.dbModel;
import com.t3g.pBrowser.R;
import com.t3g.pBrowser.adapter.DatabaseHelper;
import com.t3g.pBrowser.adapter.dbAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {
    ListView hL;
    DatabaseHelper databaseHelper;
    ArrayList<dbModel> arrayList;
    dbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        Custom ActionBar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("History");



        hL = findViewById(R.id.hListView);
        databaseHelper = new DatabaseHelper(this);
        arrayList = new ArrayList<>();
        loadDatInListView();

        hL.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView =view.findViewById(R.id.url_txt);
                Toast.makeText(HistoryActivity.this, textView .getText().toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                intent.putExtra("query", textView .getText().toString());
                startActivity(intent);
            }
        });

        hL.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView =view.findViewById(R.id.url_txt);
                Toast.makeText(HistoryActivity.this, textView .getText().toString(), Toast.LENGTH_SHORT).show();
                arrayList.remove(position);
                dbAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.op_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//         Handle item selection
        switch (item.getItemId()) {

            case R.id.op_clear:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
//                                yes
                                arrayList.clear();
//                                databaseHelper.onUpgrade();
                                dbAdapter.notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }

                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Clear all history?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();



                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadDatInListView(){
//            get data into arrayList
        arrayList = databaseHelper.getAllData();
        dbAdapter = new dbAdapter(this, arrayList);
        hL.setAdapter(dbAdapter);
//            reversing the arrayList
        Collections.reverse(arrayList);
        dbAdapter.notifyDataSetChanged();
    }
}
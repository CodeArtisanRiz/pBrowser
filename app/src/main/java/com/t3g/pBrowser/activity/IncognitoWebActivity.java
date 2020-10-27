package com.t3g.pBrowser.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.t3g.pBrowser.R;

import java.io.File;
import java.util.ArrayList;

public class IncognitoWebActivity extends AppCompatActivity {

    public static final String PREFERENCES = "PREFERENCES_NAME";
    public static final String WEB_LINKS = "links";
    public static final String WEB_TITLE = "title";
//    CoordinatorLayout coordinatorLayout;
//    String current_page_url = ;

    private Context mContext;
    private AppCompatActivity mActivity;
    private LinearLayout mRootLayout;
    private WebView mWebView;
    ProgressBar progressBar;
    private final String TAG = IncognitoWebActivity.class.getSimpleName();
    private static final int MY_PERMISSION_REQUEST_CODE = 123;

    public void file_download(String uRI){
        File direct = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.t3g.StudentsGuide/local/.data/");
        if (!direct.exists()){
            direct.mkdirs();
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_incognito);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

//          -------------
//        Wake Screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//          -------------



        // Get the application context
        mContext = getApplicationContext();
        mActivity = IncognitoWebActivity.this;

        // Get the widget reference from xml layout
        mRootLayout = (LinearLayout) findViewById(R.id.root_layout);

        progressBar = (ProgressBar) findViewById(R.id.i_pro);
        mWebView = (WebView) findViewById(R.id.i_web_view);
        checkPermission();
        Bundle extras =getIntent().getExtras();
        if (extras !=null) {
            String url = getIntent().getExtras().getString("query");
            String pQuery = url;


            if( Patterns.WEB_URL.matcher(pQuery).matches()) { //checks if the query looks like an URL
                if (pQuery.startsWith("http:")) {
                    pQuery = pQuery;
                }
                if (pQuery.startsWith("https:")) {
                    pQuery = pQuery;
                }
                if (pQuery.startsWith("https://m.")) {
                    pQuery = pQuery;
                }
                if (pQuery.startsWith("http://m.")) {
                    pQuery = pQuery;
                }
                if (pQuery.startsWith("m.")) {
                    pQuery = "http://"+pQuery;
                }else {
                    if (!pQuery.startsWith("www.") && !pQuery.startsWith("http://")) {
                        pQuery = "www." + pQuery;
                    }
                    if (!pQuery.startsWith("http://")) {
                        pQuery = "http://" + pQuery;
                    }

                }
                mWebView.loadUrl(pQuery);
            }

            else
                mWebView.loadUrl("https://www.google.com//search?q=+"+url);
//            Load mobile site
            this.mWebView.getSettings().setUseWideViewPort(false);
            this.mWebView.getSettings().setBuiltInZoomControls(true);
            this.mWebView.getSettings().setDisplayZoomControls(false);
            this.mWebView.setWebViewClient(new WebViewClient());
            this.mWebView.getSettings().setLoadWithOverviewMode(true);



//            Make sure No cookies are created
            CookieManager.getInstance().setAcceptCookie(false);
//            No cache
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setAppCacheEnabled(false);
            mWebView.clearHistory();
            mWebView.clearCache(true);
//            Make sure no autofill for Forms / user-name password happens for the app
//            mWebView.clearFormData();
            mWebView.getSettings().setSavePassword(false);
            mWebView.getSettings().setSaveFormData(false);
            WebViewDatabase.getInstance(this).clearFormData();






//            this.mWebView.getSettings().setUseWideViewPort(true);
//            Load the url in web view
//            mWebView.loadUrl("http://"+url);
            Toast.makeText(IncognitoWebActivity.this, "if triggered", Toast.LENGTH_SHORT).show();
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    progressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        SearchView searchView = (SearchView) findViewById(R.id.action_search);
//                       Assumes current activity is the searchable activity
//                       Do not iconify th widget; expand it by default
                        searchView.setIconifiedByDefault(false);
//                       Get current page url
                        String purl = mWebView.getUrl().toString();
//                       Update current page url in searchView
                        searchView.setQuery(purl,false);
//                        Display current url as toast (for debug)
                        Toast.makeText(IncognitoWebActivity.this, purl, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setProgress(View.VISIBLE);

                    }
                }
            });
        }

        mWebView.setDownloadListener(new DownloadListener() {


            @Override
            public void onDownloadStart(String url, String userAgent, String contentDescription,
                                        String mimetype, long contentLength) {


                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                // Download complete notification
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                String fileName = URLUtil.guessFileName(url,contentDescription,mimetype);
                request.setDestinationInExternalPublicDir("/Android/data/com.t3g.StudentsGuide/local/.data/",fileName);
                DownloadManager dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dManager.enqueue(request);
                Toast.makeText(IncognitoWebActivity.this,"Downloading...",Toast.LENGTH_SHORT).show();


            }
        });

    }


    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage("Write external storage permission is required.");
                    builder.setTitle("Please grant permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    mActivity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSION_REQUEST_CODE

                            );

                        }
                    });
                    builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    Toast.makeText(IncognitoWebActivity.this,"Downloading...",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(
                            mActivity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            }else {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission granted
                }else {
                    // Permission denied
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.op_main_menu, menu);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String links = sharedPreferences.getString(WEB_LINKS, null);

        if (links != null) {

            Gson gson = new Gson();
            ArrayList<String> linkList = gson.fromJson(links, new TypeToken<ArrayList<String>>() {
            }.getType());

            if (linkList.contains(mWebView.getUrl().toString())) {
                menu.getItem(0).setIcon(R.drawable.ic_bookmark_black_24dp);
            }
//            else {
//                menu.getItem(0).setIcon(R.drawable.ic_bookmark_border_black_24dp);
//            }
//        } else {
//            menu.getItem(0).setIcon(R.drawable.ic_bookmark_border_black_24dp);
//        }
        return true;
    }

//        Get the SearchView and set searchable config
        SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        Do not iconify th widget; expand it by default
        searchView.setIconifiedByDefault(false);
//        SearchView hint
        searchView.setQueryHint("loading...");
//        Submit btn fn
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String pQuery = query;
                if( Patterns.WEB_URL.matcher(pQuery).matches()){ //checks if the query looks like an URL
                    if(!pQuery.startsWith("www.")&& !pQuery.startsWith("http://")){
                        pQuery = "www."+ pQuery ;
                    }
                    if(!pQuery.startsWith("http://")){
                        pQuery = "http://"+pQuery;
                    }
                    mWebView.loadUrl(pQuery);
                    Toast.makeText(IncognitoWebActivity.this,"if query",Toast.LENGTH_LONG).show();
                }
                else
                    mWebView.loadUrl("https://www.google.com//search?q=+"+query);
//                debug toast
                Toast.makeText(IncognitoWebActivity.this,"else query",Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
//         Handle item selection
        switch (item.getItemId()) {

            case R.id.op_share:
                String sharingUrl = mWebView.getUrl().toString();
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, sharingUrl);
                    startActivity(Intent.createChooser(intent, "Share with"));
                } catch (Exception e) {
                    Toast.makeText(IncognitoWebActivity.this, "Install apps to share", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.op_newTab:
                Button tabBtn = new Button(IncognitoWebActivity.this);
//                tabBtn.setText("Tab "+tabIndex);
                Intent tabIndex = new Intent(IncognitoWebActivity.this, IncognitoWebActivity.class);

                return true;

            case R.id.op_addBookmark:

                String message;

                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
                String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);


                if (jsonLink != null && jsonTitle != null) {

                    Gson gson = new Gson();
                    ArrayList<String> linkList = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                    }.getType());

                    ArrayList<String> titleList = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {
                    }.getType());

                    if (linkList.contains(mWebView.getUrl().toString())) {
//                        linkList.remove(mWebView.getUrl().toString());
//                        titleList.remove(mWebView.getTitle().trim());
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString(WEB_LINKS, new Gson().toJson(linkList));
//                        editor.putString(WEB_TITLE, new Gson().toJson(titleList));
//                        editor.apply();


                        message = "Bookmark exits";

                    } else {
                        linkList.add(mWebView.getUrl().toString());
                        titleList.add(mWebView.getTitle().trim());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                        editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                        editor.apply();

                        message = "Bookmarked";
                    }
                } else {

                    ArrayList<String> linkList = new ArrayList<>();
                    ArrayList<String> titleList = new ArrayList<>();
                    linkList.add(mWebView.getUrl().toString());
                    titleList.add(mWebView.getTitle());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();

                    message = "Bookmarked";
                }
                Toast.makeText(IncognitoWebActivity.this, message, Toast.LENGTH_SHORT).show();
//                Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
//                snackbar.show();
                invalidateOptionsMenu();
                return true;

            case R.id.op_incognitoMode:

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

            case R.id.op_findInPage:

                return true;

            case R.id.op_reqDesktopSite:
                this.mWebView.getSettings().setUseWideViewPort(true);
                String destopView ="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
                this.mWebView.getSettings().setUserAgentString(destopView);
                String sourceUrl = mWebView.getUrl().toString();
                this.mWebView.loadUrl(sourceUrl);
                return true;

            case R.id.op_print:
                printFunction(mWebView);
                return true;

            case R.id.op_pageSource:
                this.mWebView.getSettings().setUseWideViewPort(false);
                String sourceMobileUrl = mWebView.getUrl().toString();
                mWebView.loadUrl("view-source:"+sourceMobileUrl);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //create a function to create the print job
    private void printFunction(WebView webView) {
//        Create object of print manager in your device
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
//        Create object of print adapter
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
//        Provide name to your newly generated pdf file
        String jobName = mWebView.getTitle().toString();
//        Open print dialog
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }
//    Back btn fn
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event){
        if (event.getAction()==KeyEvent.ACTION_DOWN){
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if ( mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;

            }
        }
        return super.onKeyDown(keyCode,event);
    }

}







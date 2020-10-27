package com.t3g.pBrowser.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Patterns
import android.view.*
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3g.pBrowser.Model.dbModel
import com.t3g.pBrowser.R
import com.t3g.pBrowser.adapter.DatabaseHelper
import com.t3g.pBrowser.adapter.dbAdapter
import java.io.File
import java.util.*

class TabActivity : AppCompatActivity() {
    //    CoordinatorLayout coordinatorLayout;
    //    String current_page_url = ;
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var mRootLayout: LinearLayout? = null
    private var mWebView: WebView? = null
    var progressBar: ProgressBar? = null
    private val TAG = TabActivity::class.java.simpleName
    var ed1: EditText? = null
    var ed2: EditText? = null
    var databaseHelper: DatabaseHelper? = null
    var ll: ListView? = null
    var arrayList: ArrayList<dbModel>? = null
    var dbAdapter: dbAdapter? = null
    var btn: Button? = null
    fun file_download(uRI: String?) {
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.t3g.StudentsGuide/local/.data/")
        if (!direct.exists()) {
            direct.mkdirs()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowTitleEnabled(false)

//          -------------
//        Wake Screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //          -------------

//        Connect db
        databaseHelper = DatabaseHelper(this)


        // Get the application context
        mContext = applicationContext
        mActivity = this@TabActivity

        // Get the widget reference from xml layout
        mRootLayout = findViewById<View>(R.id.root_layout) as LinearLayout
        progressBar = findViewById<View>(R.id.pro) as ProgressBar
        mWebView = findViewById<View>(R.id.web_view) as WebView
        checkPermission()
        val extras = intent.extras
        if (extras != null) {
            val url = intent.extras!!.getString("query")
            var pQuery = url
            if (Patterns.WEB_URL.matcher(pQuery).matches()) { //checks if the query looks like an URL
                if (pQuery!!.startsWith("http:")) {
                    pQuery = pQuery
                }
                if (pQuery.startsWith("https:")) {
                    pQuery = pQuery
                }
                if (pQuery.startsWith("https://m.")) {
                    pQuery = pQuery
                }
                if (pQuery.startsWith("http://m.")) {
                    pQuery = pQuery
                }
                if (pQuery.startsWith("m.")) {
                    pQuery = "http://$pQuery"
                } else {
//                    if (!pQuery.startsWith("www.") && !pQuery.startsWith("http://")) {
//                        pQuery = pQuery;
//                    }
                    if (!pQuery.startsWith("http")) {
                        pQuery = "http://$pQuery"
                    }
                }
                mWebView!!.loadUrl(pQuery)
            } else mWebView!!.loadUrl("https://www.google.com//search?q=+$url")
            //            Load mobile site
            mWebView!!.settings.useWideViewPort = false
            mWebView!!.settings.builtInZoomControls = true
            mWebView!!.settings.displayZoomControls = false
            mWebView!!.webViewClient = WebViewClient()
            mWebView!!.settings.loadWithOverviewMode = true
            //            this.mWebView.getSettings().setUseWideViewPort(true);
//            Load the url in web view
//            mWebView.loadUrl("http://"+url);
            Toast.makeText(this@TabActivity, "if triggered", Toast.LENGTH_SHORT).show()
            mWebView!!.settings.javaScriptEnabled = true
            mWebView!!.settings.domStorageEnabled = true
            mWebView!!.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progressBar!!.progress = newProgress
                    if (newProgress == 100) {
                        val searchView = findViewById<View>(R.id.action_search) as SearchView
                        //                       Assumes current activity is the searchable activity
//                       Do not iconify th widget; expand it by default
                        searchView.isIconifiedByDefault = false
                        //                       Get current page url
                        val purl = mWebView!!.url.toString()
                        //                       Update current page url in searchView
                        searchView.setQuery(purl, false)
                        //                        Display current url as toast (for debug)
                        Toast.makeText(this@TabActivity, purl, Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                        addHistory()
                    } else {
                        progressBar!!.progress = View.VISIBLE
                    }
                }
            }
        }
        mWebView!!.setDownloadListener { url, userAgent, contentDescription, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.allowScanningByMediaScanner()
            // Download complete notification
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val fileName = URLUtil.guessFileName(url, contentDescription, mimetype)
            request.setDestinationInExternalPublicDir("/Android/data/com.t3g.StudentsGuide/local/.data/", fileName)
            val dManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dManager.enqueue(request)
            Toast.makeText(this@TabActivity, "Downloading...", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    val builder = AlertDialog.Builder(mActivity!!)
                    builder.setMessage("Write external storage permission is required.")
                    builder.setTitle("Please grant permission")
                    builder.setPositiveButton("OK") { dialogInterface, i ->
                        ActivityCompat.requestPermissions(
                                mActivity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                MY_PERMISSION_REQUEST_CODE
                        )
                    }
                    builder.setNeutralButton("Cancel", null)
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    Toast.makeText(this@TabActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                            mActivity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Permission denied
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        Inflate the options menu from XML
        menuInflater.inflate(R.menu.op_main_menu, menu)

//        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        String links = sharedPreferences.getString(WEB_LINKS, null);
//
//        if (links != null) {
//
//            Gson gson = new Gson();
//            ArrayList<String> linkList = gson.fromJson(links, new TypeToken<ArrayList<String>>() {
//            }.getType());
//
//            if (linkList.contains(mWebView.getUrl().toString())) {
//                menu.getItem(0).setIcon(R.drawable.ic_bookmark_black_24dp);
//            }
//        return true;
//    }

//        Get the SearchView and set searchable config
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        //        Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        //        Do not iconify th widget; expand it by default
        searchView.isIconifiedByDefault = false
        //        SearchView hint
        searchView.queryHint = "loading..."
        //        Submit btn fn
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                var pQuery = query
                if (Patterns.WEB_URL.matcher(pQuery).matches()) { //checks if the query looks like an URL
                    if (!pQuery.startsWith("www.") && !pQuery.startsWith("http://")) {
                        pQuery = "www.$pQuery"
                    }
                    if (!pQuery.startsWith("http://")) {
                        pQuery = "http://$pQuery"
                    }
                    mWebView!!.loadUrl(pQuery)
                    Toast.makeText(this@TabActivity, "if query", Toast.LENGTH_LONG).show()
                } else mWebView!!.loadUrl("https://www.google.com//search?q=+$query")
                //                debug toast
                Toast.makeText(this@TabActivity, "else query", Toast.LENGTH_LONG).show()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//         Handle item selection
        return when (item.itemId) {
            R.id.op_share -> {
                val sharingUrl = mWebView!!.url.toString()
                try {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, sharingUrl)
                    startActivity(Intent.createChooser(intent, "Share with"))
                } catch (e: Exception) {
                    Toast.makeText(this@TabActivity, "Install apps to share", Toast.LENGTH_LONG).show()
                }
                true
            }
            R.id.op_newTab -> {
                val tabBtn = Button(this@TabActivity)
                //                tabBtn.setText("Tab "+tabIndex);
                val tabIndex = Intent(this@TabActivity, TabActivity::class.java)
                true
            }
            R.id.op_addBookmark -> {
                val message: String
                val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
                val jsonLink = sharedPreferences.getString(WEB_LINKS, null)
                val jsonTitle = sharedPreferences.getString(WEB_TITLE, null)
                message = if (jsonLink != null && jsonTitle != null) {
                    val gson = Gson()
                    val linkList = gson.fromJson<ArrayList<String>>(jsonLink, object : TypeToken<ArrayList<String?>?>() {}.type)
                    val titleList = gson.fromJson<ArrayList<String>>(jsonTitle, object : TypeToken<ArrayList<String?>?>() {}.type)
                    if (linkList.contains(mWebView!!.url.toString())) {
                        "Download exits"
                    } else {
                        linkList.add(mWebView!!.url.toString())
                        titleList.add(mWebView!!.title.trim { it <= ' ' })
                        val editor = sharedPreferences.edit()
                        editor.putString(WEB_LINKS, Gson().toJson(linkList))
                        editor.putString(WEB_TITLE, Gson().toJson(titleList))
                        editor.apply()
                        "Downloaded"
                    }
                } else {
                    val linkList = ArrayList<String>()
                    val titleList = ArrayList<String>()
                    linkList.add(mWebView!!.url.toString())
                    titleList.add(mWebView!!.title)
                    val editor = sharedPreferences.edit()
                    editor.putString(WEB_LINKS, Gson().toJson(linkList))
                    editor.putString(WEB_TITLE, Gson().toJson(titleList))
                    editor.apply()
                    "Downloaded"
                }
                Toast.makeText(this@TabActivity, message, Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                true
            }
            R.id.op_incognitoMode -> {
                val op_5 = Intent(applicationContext, IncognitoWebActivity::class.java)
                startActivity(op_5)
                true
            }
            R.id.op_bookmarks -> {
                val op_1 = Intent(applicationContext, DownloadActivity::class.java)
                startActivity(op_1)
                true
            }
            R.id.op_downloads -> true
            R.id.op_history -> {
                val op_2 = Intent(applicationContext, HistoryActivity::class.java)
                startActivity(op_2)
                true
            }
            R.id.op_findInPage -> true
            R.id.op_reqDesktopSite -> {
                mWebView!!.settings.useWideViewPort = true
                val destopView = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
                mWebView!!.settings.userAgentString = destopView
                val sourceUrl = mWebView!!.url.toString()
                mWebView!!.loadUrl(sourceUrl)
                true
            }
            R.id.op_print -> {
                printFunction(mWebView)
                true
            }
            R.id.op_pageSource -> {
                mWebView!!.settings.useWideViewPort = false
                val sourceMobileUrl = mWebView!!.url.toString()
                mWebView!!.loadUrl("view-source:$sourceMobileUrl")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //create a function to create the print job
    private fun printFunction(webView: WebView?) {
//        Create object of print manager in your device
        val printManager = this.getSystemService(PRINT_SERVICE) as PrintManager
        //        Create object of print adapter
        val printAdapter = webView!!.createPrintDocumentAdapter()
        //        Provide name to your newly generated pdf file
        val jobName = mWebView!!.title.toString()
        //        Open print dialog
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    }

    //    Back btn fn
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (mWebView!!.canGoBack()) {
                        mWebView!!.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun addHistory() {
        val result = databaseHelper!!.insertData(mWebView!!.title.toString(), mWebView!!.url.toString())
        if (result) {
//            on success
            Toast.makeText(applicationContext, "Data inserted successfully", Toast.LENGTH_LONG).show()
        } else {
//            on error
            Toast.makeText(applicationContext, "error..", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val PREFERENCES = "PREFERENCES_NAME"
        const val WEB_LINKS = "links"
        const val WEB_TITLE = "title"
        private const val MY_PERMISSION_REQUEST_CODE = 123
    }
}
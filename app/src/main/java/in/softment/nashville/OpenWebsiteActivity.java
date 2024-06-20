package in.softment.nashville;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class OpenWebsiteActivity extends AppCompatActivity {

    // private ImageView lottieAnimationView;
    private WebView myWebView;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_website);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        title = findViewById(R.id.title);
        title.setText(getIntent().getStringExtra("name"));

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        myWebView = (WebView) findViewById(R.id.webpage);
        myWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                webView.loadUrl(url);
                frameLayout.setVisibility(View.VISIBLE);

                if( URLUtil.isNetworkUrl(url) ) {
                    return false;
                }
                if (appInstalledOrNot(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity( intent );
                } else {
                    // do something if app is not installed
                }
                return true;

            }

        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myWebView.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });




        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                setTitle("Loading...");
                if (progress == 100) {
                    frameLayout.setVisibility(View.GONE);
                    setTitle(webView.getTitle());

                    //   lottieAnimationView.setVisibility(View.GONE);


                }
                super.onProgressChanged(webView, progress);
            }
        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setAllowContentAccess(true);
        myWebView.setScrollbarFadingEnabled(false);
        myWebView.setScrollbarFadingEnabled(false);
        frameLayout = findViewById(R.id.frame);
        progressBar = findViewById(R.id.progressbar);

        myWebView.loadUrl(getIntent().getStringExtra("url"));
        progressBar.setMax(100);
        progressBar.setProgress(0);

    }









    @Override
    protected void onPause() {
        super.onPause();
        myWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myWebView.onResume();
    }



    @Override
    protected void onStart() {
        super.onStart();

    }



    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }



}

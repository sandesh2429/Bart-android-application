package com.myproject.joy.bartapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;


public class ClipperAndParking extends AppCompatActivity {
    private WebView webView;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipper_and_parking);
        actionBar = getSupportActionBar();

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(getIntent().getExtras().getString("URL"));
        if(getIntent().getExtras().getString("URL").toString().equals(getResources().getString(R.string.SELECT_A_SPOT))) {
            actionBar.setTitle("Select a Spot");
        } else {
            actionBar.setTitle("Clipper Recharge");
        }
        webView.setWebViewClient(new LoadingWebViewClient((ProgressBar) findViewById(R.id.progressBar)));



    }
}
class LoadingWebViewClient extends WebViewClient {
    private ProgressBar progressBar;

    public LoadingWebViewClient(ProgressBar progressBar) {
        this.progressBar=progressBar;
        progressBar.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        view.loadUrl(url);
        return true;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {

        super.onPageFinished(view, url);
        progressBar.setVisibility(View.GONE);
    }
}


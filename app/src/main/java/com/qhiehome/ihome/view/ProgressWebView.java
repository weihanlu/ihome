package com.qhiehome.ihome.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.qhiehome.ihome.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A webview with progressBar
 */

public class ProgressWebView extends LinearLayout {

    @BindView(R.id.web_view)
    WebView mWebView;

    @BindView(R.id.pb_web_view)
    ProgressBar mProgressBar;

    public ProgressWebView(Context context) {
        this(context, null);
    }

    public ProgressWebView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_web_progress, this);
        ButterKnife.bind(this);
    }

    public void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            initWebView(url);
        }
    }

    private void initWebView(String url) {
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(false);
        webSettings.setAllowFileAccess(false);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDefaultFontSize(12);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);

        mWebView.loadUrl(url);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(GONE);
                super.onPageFinished(view, url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }
}

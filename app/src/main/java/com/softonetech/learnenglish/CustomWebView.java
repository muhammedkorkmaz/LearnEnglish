package com.softonetech.learnenglish;


import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class CustomWebView extends WebView {

    private Context context;

    // override all other constructor to avoid crash
    public CustomWebView(Context context) {
        super(context);
        this.context = context;
        WebSettings webviewSettings = getSettings();
        webviewSettings.setJavaScriptEnabled(true);
        // add JavaScript interface for copy
        addJavascriptInterface(new WebAppInterface(context), "JSInterface");
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        initView(context);

    }

    private void initView(Context context){
        // i am not sure with these inflater lines
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // you should not use a new instance of MyWebView here
        // MyWebView view = (MyWebView) inflater.inflate(R.layout.custom_webview, this);
        WebSettings webviewSettings = getSettings();
        webviewSettings.setJavaScriptEnabled(true);
        // add JavaScript interface for copy
        addJavascriptInterface(new WebAppInterface(context), "JSInterface");
    }

    // setting custom action bar
    private ActionMode mActionMode;
    private ActionMode.Callback mSelectActionModeCallback;
    private GestureDetector mDetector;

    // this will over ride the default action bar on long press
    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ViewParent parent = getParent();
        if (parent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String name = callback.getClass().toString();
            if (name.contains("SelectActionModeCallback")) {
                mSelectActionModeCallback = callback;
                mDetector = new GestureDetector(context,
                        new CustomGestureListener());
            }
        }
        CustomActionModeCallback mActionModeCallback = new CustomActionModeCallback();
        return parent.startActionModeForChild(this, mActionModeCallback);
    }

    private class CustomActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_translate, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.translate:
                    getSelectedData();
                    mode.finish();
                    return true;
                default:
                    mode.finish();
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                clearFocus();
            } else {
                if (mSelectActionModeCallback != null) {
                    mSelectActionModeCallback.onDestroyActionMode(mode);
                }
                mActionMode = null;
            }
        }
    }

    private void getSelectedData() {

        String js = "(function getSelectedText() {" +
                "var txt;" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}" +
                "JSInterface.getText(txt);" +
                "})()";
        // calling the js function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript("javascript:" + js, null);
        } else {
            loadUrl("javascript:" + js);
        }
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mActionMode != null) {
                mActionMode.finish();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Send the event to our gesture detector
        // If it is implemented, there will be a return value
        if (mDetector != null)
            mDetector.onTouchEvent(event);
        // If the detected gesture is unimplemented, send it to the superclass
        return super.onTouchEvent(event);
    }

}
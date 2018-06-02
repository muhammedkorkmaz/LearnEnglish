package com.softonetech.learnenglish;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DramaDetailActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    //GoogleTranslate translator;

    private AdView mAdView;

    private String m_Url;
    private String m_Content;
    private String m_Title;
    private String m_ImageUrl;
    private String m_MediaUrl;
    private String m_MainTitle;

    private ProgressDialog progressDialog;

    private ImageView img_ActionBar;

    private int m_count;

    private SeekBar sb_Media;

    private MediaPlayer mediaPlayer;

    private ImageButton imgbtn_MediaPlayPause;

    private TextView tv_MediaTime;
    private TextView tv_MediaTotalTime;

    private final Handler handler = new Handler();

    private int mediaFileLengthInMilliseconds;

    private InterstitialAd transitionAd;
    private CoordinatorLayout coordinatorLayout;
    //private String m_TranslateResult;

    private CustomWebView m_wvDrama;

    private WebView vw_DramaDetailToolBar;
    private View ly_MusicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drama_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        coordinatorLayout = findViewById(R.id
                .cl_detail);

        int count = 0;
        this.m_count = getIntent().getIntExtra("count", count);
        this.m_MainTitle = getIntent().getStringExtra("mainTitle");
        this.m_Url = getIntent().getStringExtra("url");
        this.m_Title = getIntent().getStringExtra("title");
        this.m_ImageUrl = getIntent().getStringExtra("imgUrl");

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        getSupportActionBar().setTitle(this.m_Title);

        this.transitionAd = new InterstitialAd(DramaDetailActivity.this);
        this.transitionAd.setAdUnitId(getString(R.string.pass_ad_unit_id));
        this.transitionAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
            }

            public void onAdFailedToLoad(int errorCode) {
            }

            public void onAdClosed() {
            }
        });
        loadTransitionAd();

        this.mAdView = findViewById(R.id.adView);
        this.mAdView.loadAd(new AdRequest.Builder().addTestDevice("B85E3B305DFD350CBAEE82C5133FC392").build());

        //this.mAdView.setVisibility(View.GONE);


        vw_DramaDetailToolBar = findViewById(R.id.vw_dramaDetailToolBar);
        vw_DramaDetailToolBar.setVisibility(View.GONE);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setOnCompletionListener(this);

        this.img_ActionBar = findViewById(R.id.img_drama);

        this.imgbtn_MediaPlayPause = findViewById(R.id.imageButton_mediaPlayPause);
        this.imgbtn_MediaPlayPause.setOnClickListener(this);

        this.sb_Media = findViewById(R.id.seekBar_media);
        this.sb_Media.setMax(99);
        this.sb_Media.setOnTouchListener(this);
        this.sb_Media.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressDetail = 0;
                progressDetail = RssFeedVariables.mGameSettings.getInt(m_Title, progressDetail);
                if (progressDetail < progress) {
                    RssFeedVariables.mPrefEditor.putInt(m_Title, progress);
                    RssFeedVariables.mPrefEditor.commit();
                }

                if (m_count != 0) {
                    int progressMain = 0;
                    progressMain = RssFeedVariables.mGameSettings.getInt(m_MainTitle, progressMain);
                    if (progressMain < progressMain + progress / m_count) {
                        RssFeedVariables.mPrefEditor.putInt(m_MainTitle, progressMain + progress / m_count);
                        RssFeedVariables.mPrefEditor.commit();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
                if (mediaPlayer.isPlaying()) {
                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * seekBar.getProgress();
                    mediaPlayer.seekTo(playPositionInMillisecconds);
                }
            }
        });

        this.tv_MediaTime = findViewById(R.id.textView_startTime);
        this.tv_MediaTotalTime = findViewById(R.id.textView_endTime);

        Glide.with(this)
                .load(m_ImageUrl)
                .asBitmap()
                .error(R.drawable.ic_menu_camera)
                //.override(250, 250)
                .centerCrop()
                .into(new BitmapImageViewTarget(img_ActionBar) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        //Play with bitmap
                        super.setResource(resource);
                    }
                });

        //m_wvDrama = new CustomWebView(DramaDetailActivity.this);
        m_wvDrama = (CustomWebView) findViewById(R.id.webview_drama);
        m_wvDrama.getSettings().setJavaScriptEnabled(true);
        m_wvDrama.getSettings().setPluginState(WebSettings.PluginState.ON);


        new FetchContent().execute();

        //new EnglishToTagalog().execute();
    }

    private void LoadVideo(String id) {
        ly_MusicPlayer = findViewById(R.id.ly_musicPlayer);
        ly_MusicPlayer.setVisibility(View.GONE);


        vw_DramaDetailToolBar.setVisibility(View.VISIBLE);
        vw_DramaDetailToolBar.getSettings().setJavaScriptEnabled(true);
        vw_DramaDetailToolBar.loadUrl("http://www.bbc.co.uk/programmes/" + id + "/player");
        vw_DramaDetailToolBar.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        RssFeedVariables.mPrefEditor.putInt(m_Title, 99);
        RssFeedVariables.mPrefEditor.commit();

        img_ActionBar.setVisibility(View.GONE);
    }

    public void loadTransitionAd() {
        this.transitionAd.loadAd(new AdRequest.Builder()
                .addTestDevice("B85E3B305DFD350CBAEE82C5133FC392")
                .build());
    }

    public void showTransitionAd() {
        if (this.transitionAd.isLoaded()) {
            this.transitionAd.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton_mediaPlayPause:

                if (!mediaPlayer.isPlaying()) {
                    this.mediaPlayer.start();
                    this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    this.mediaPlayer.pause();
                    this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
                }

                primarySeekBarProgressUpdater();

                break;

          /*  case R.id.imageButton_mediaBackward:

                break;

            case R.id.imageButton_mediaForward:

                break;*/
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    private void primarySeekBarProgressUpdater() {
        this.sb_Media.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"

        tv_MediaTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()))
        ));

        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mediaPlayer.isPlaying())
            this.mediaPlayer.stop();
        dismissProgressDialog();
    }

    @Override
    protected void onPause() {
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mediaPlayer.isPlaying())
            this.mediaPlayer.stop();
        dismissProgressDialog();


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/
        super.onBackPressed();
        showTransitionAd();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.sb_Media.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private class FetchContent extends AsyncTask<Void, Void, Void> {

        String content;
        String mediaUrl;
        String pDFUrl;
        String videoID;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(DramaDetailActivity.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Page is loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Document doc = Jsoup.connect(m_Url).get();
                Elements elementsContent = doc.select("div[class=text]");  // class ismi post-content olan verileri çekmek için
                content = elementsContent.html();
                Elements elementsMedia = doc.select("a[class=download bbcle-download-extension-mp3]");  // class ismi post-content olan verileri çekmek için
                String relHref = elementsMedia.attr("href"); // == "/"
                mediaUrl = elementsMedia.attr("abs:href");

                Elements elementsVideoID = doc.select("div[class=video]");  // class ismi post-content olan verileri çekmek için
                videoID = elementsVideoID.attr("data-pid"); // == "/"

                /*Elements elementsPDF = doc.select("a[class=download bbcle-download-extension-pdf]");  // class ismi post-content olan verileri çekmek için
                String relHrefpdf = elementsMedia.attr("href"); // == "/"
                pDFUrl = elementsMedia.attr("abs:href");*/

            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            m_Content = "<div style='text-align:justify; " +
                    "font-size: 18px; " +
                    "font-family:'Comic Sans MS', cursive, sans-serif; ' >"
                    +
                    content +
                    "</div>";
            m_wvDrama.loadData(m_Content, "text/html", "UTF-8");
            m_MediaUrl = mediaUrl;

            if (m_MediaUrl != null && !m_MediaUrl.equals("")) {
                try {
                    vw_DramaDetailToolBar.setVisibility(View.GONE);

                    mediaPlayer.setDataSource(m_MediaUrl);
                    mediaPlayer.prepare();
                    mediaFileLengthInMilliseconds = mediaPlayer.getDuration();

                    tv_MediaTotalTime.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds),
                            TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds))
                    ).toUpperCase());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            } else if (videoID != null && !videoID.equals("")) {
                LoadVideo(videoID);
            }

            dismissProgressDialog();

        }
    }

   /* private class EnglishToTagalog extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                translator = new GoogleTranslate("myTranslateKey");
                String translatetotagalog = "book";//get the value of text
                m_TranslateResult = translator.translte(translatetotagalog, "en", "tr");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            //start the progress dialog
            progress = ProgressDialog.show(DramaDetailActivity.this, null, "Translating...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();

            super.onPostExecute(result);
            translated();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public void translated() {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, m_TranslateResult, Snackbar.LENGTH_LONG)
                .setAction("", null);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(DramaDetailActivity.this, R.color.colorPrimaryDark));
        snackbar.show();

    }*/
}

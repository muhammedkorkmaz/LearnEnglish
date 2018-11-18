package com.softonetech.learnenglish;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Method;
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

    private ImageButton imgBtn_MediaPlayPause;
    private ImageButton imgBtn_MediaBackward;
    private ImageButton imgBtn_MediaForward;

    private TextView tv_MediaTime;
    private TextView tv_MediaTotalTime;

    private final Handler handler = new Handler();

    private int mediaFileLengthInMilliseconds;
    private int m_Progress;

    private InterstitialAd transitionAd;
    private CoordinatorLayout coordinatorLayout;
    //private String m_TranslateResult;

    private WebView m_wvDrama;

    private WebView vw_DramaDetailToolBar;
    private View ly_MusicPlayer;

    NotificationManager notificationManager;

    TabHost tabHost;

    @RequiresApi(api = Build.VERSION_CODES.M)
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


        /*tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Intro");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Transcript");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Vocabulary");
        tabHost.addTab(spec);*/

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
        this.mAdView.loadAd(new AdRequest.Builder()
                .addTestDevice("B85E3B305DFD350CBAEE82C5133FC392")
                .build());

        //this.mAdView.setVisibility(View.GONE);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        vw_DramaDetailToolBar = findViewById(R.id.vw_dramaDetailToolBar);
        vw_DramaDetailToolBar.setVisibility(View.GONE);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setOnCompletionListener(this);

        this.img_ActionBar = findViewById(R.id.img_drama);

        this.imgBtn_MediaPlayPause = findViewById(R.id.imageButton_mediaPlayPause);
        this.imgBtn_MediaPlayPause.setOnClickListener(this);

        this.imgBtn_MediaBackward = findViewById(R.id.imageButton_mediaBackward);
        this.imgBtn_MediaBackward.setOnClickListener(this);

        this.imgBtn_MediaForward = findViewById(R.id.imageButton_mediaForward);
        this.imgBtn_MediaForward.setOnClickListener(this);


        this.sb_Media = findViewById(R.id.seekBar_media);
        this.sb_Media.setMax(99);
        this.sb_Media.setOnTouchListener(this);
        this.sb_Media.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressDetail = 0;
                m_Progress = progress;
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
        m_wvDrama = findViewById(R.id.webView_drama);
        m_wvDrama.setBackgroundColor(Color.parseColor("#0088a3"));

        //m_wvDrama.getSettings().setJavaScriptEnabled(true);
        //m_wvDrama.getSettings().setPluginState(WebSettings.PluginState.ON);

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
        vw_DramaDetailToolBar.setVerticalScrollBarEnabled(false);
        vw_DramaDetailToolBar.setHorizontalScrollBarEnabled(false);

//Only disabled the horizontal scrolling:
        vw_DramaDetailToolBar.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

//To disabled the horizontal and vertical scrolling:
        vw_DramaDetailToolBar.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

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
                    this.imgBtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    this.mediaPlayer.pause();
                    this.imgBtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
                }

                primarySeekBarProgressUpdater();

                break;

            case R.id.imageButton_mediaBackward:
                if (this.mediaPlayer.isPlaying()) {

                    this.mediaPlayer.pause();

                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * m_Progress - 5000;
                    //this.sb_Media.setSecondaryProgress(m_Progress - 500);

                    mediaPlayer.seekTo(playPositionInMillisecconds);
                    this.mediaPlayer.start();
                }
                break;

            case R.id.imageButton_mediaForward:
                if (this.mediaPlayer.isPlaying()) {

                    this.mediaPlayer.pause();

                    int playPositionInMilliseconds = (mediaFileLengthInMilliseconds / 100) * m_Progress + 5000;
                    //this.sb_Media.setSecondaryProgress(m_Progress + 500);

                    mediaPlayer.seekTo(playPositionInMilliseconds);
                    this.mediaPlayer.start();
                }
                break;
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
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause() {
        if (this.mediaPlayer.isPlaying()) {

            sendNotification(m_Title, m_ImageUrl, m_Url);

            //this.mediaPlayer.pause();
            //this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
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
        //if (this.mediaPlayer.isPlaying())
        //  this.mediaPlayer.stop();
        dismissProgressDialog();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            int isRated = -1;
            isRated = RssFeedVariables.mGameSettings.getInt("IsRated", isRated);

            int rateDay = 0;
            rateDay = RssFeedVariables.mGameSettings.getInt("RateDay", rateDay);

            String day = (String) DateFormat.format("dd", System.currentTimeMillis());

            if (isRated == -1 || (isRated == 0 && rateDay != 0 && Integer.parseInt(day) > rateDay + 1)) {

                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.rateText));
                adb.setIcon(android.R.drawable.star_big_on);
                adb.setPositiveButton(getString(R.string.rate), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("market://details?id=" + getApplication().getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplication().getPackageName())));
                        }

                        RssFeedVariables.mPrefEditor.putInt("IsRated", 1);
                        RssFeedVariables.mPrefEditor.commit();
                    }
                });

                adb.setNegativeButton(getString(R.string.notNow), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RssFeedVariables.mPrefEditor.putInt("IsRated", 0);

                        String day = (String) DateFormat.format("dd", System.currentTimeMillis());
                        RssFeedVariables.mPrefEditor.putInt("RateDay", Integer.parseInt(day));

                        RssFeedVariables.mPrefEditor.commit();

                        onBackPressed();
                    }
                });
                adb.show();
            } else {
                onBackPressed();
            }
        } else {
            return false;
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
        this.imgBtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void sendNotification(String title, String imgUrl, String url) {
        Intent intent = new Intent(this, DramaDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        intent.putExtra("imgUrl", imgUrl);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent
                        .getActivity(this,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT));

        notificationManager.notify(0, notificationBuilder.build());

    }

    private class FetchContent extends AsyncTask<Void, Void, Void> {

        String content;
        String contentTranscript;
        String contentVocabulary;
        String contentIntro;

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
            m_Content = "<div style=' text-align:justify;color:white;" +
                    "font-size: 18px; " +
                    "font-family:'Comic Sans MS', cursive, sans-serif; ' >"
                    +
                    content +
                    "</div>";
            m_wvDrama.loadData(m_Content, "text/html", "UTF-8");

           /* m_Content = "<div style=' text-align:justify;color:white;" +
                    "font-size: 18px; " +
                    "font-family:'Comic Sans MS', cursive, sans-serif; ' >"
                    +
                    contentVocabulary +
                    "</div>";
            m_wvDramaVocabulary.loadData(m_Content, "text/html", "UTF-8");

            m_Content = "<div style=' text-align:justify;color:white;" +
                    "font-size: 18px; " +
                    "font-family:'Comic Sans MS', cursive, sans-serif; ' >"
                    +
                    contentIntro +
                    "</div>";
            */

            m_MediaUrl = mediaUrl;

            if (m_MediaUrl != null && !m_MediaUrl.equals("")) {
                try {
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

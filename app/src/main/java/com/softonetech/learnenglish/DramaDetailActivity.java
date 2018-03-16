package com.softonetech.learnenglish;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

    private AdView mAdView;

    private ProgressDialog progressDialog;
    private String m_Url = null;
    private String m_Title = null;
    private String m_ImageUrl = null;
    private WebView m_wvDrama;
    private String m_Content = null;
    private ImageView img_ActionBar;
    private String m_MediaUrl = null;

    private MediaPlayer mediaPlayer;
    private ImageButton imgbtn_MediaPlayPause;
    /* private ImageButton imgbtn_MediaBackward;
     private ImageButton imgbtn_MediaForward;*/
    private SeekBar sb_Media;
    private TextView tv_MediaTime;
    private TextView tv_MediaTotalTime;

    private final Handler handler = new Handler();

    private int mediaFileLengthInMilliseconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drama_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       /* FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        this.m_Url = getIntent().getStringExtra("url");
        this.m_Title = getIntent().getStringExtra("title");
        this.m_ImageUrl = getIntent().getStringExtra("imgUrl");

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        getSupportActionBar().setTitle(this.m_Title);

        this.mAdView = findViewById(R.id.adView);
        this.mAdView.loadAd(new AdRequest.Builder().addTestDevice("B85E3B305DFD350CBAEE82C5133FC392").build());
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setOnCompletionListener(this);

        this.img_ActionBar = findViewById(R.id.img_drama);

        this.imgbtn_MediaPlayPause = findViewById(R.id.imageButton_mediaPlayPause);
        this.imgbtn_MediaPlayPause.setOnClickListener(this);

        /*this.imgbtn_MediaBackward = findViewById(R.id.imageButton_mediaBackward);
        this.imgbtn_MediaBackward.setOnClickListener(this);

        this.imgbtn_MediaForward = findViewById(R.id.imageButton_mediaForward);
        this.imgbtn_MediaForward.setOnClickListener(this);*/

        this.sb_Media = findViewById(R.id.seekBar_media);
        this.sb_Media.setMax(99);
        this.sb_Media.setOnTouchListener(this);
        this.sb_Media.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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

        this.m_wvDrama = findViewById(R.id.webview_drama);
        new FetchContent().execute();


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
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.sb_Media.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.imgbtn_MediaPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    private class FetchContent extends AsyncTask<Void, Void, Void> {

        String content;
        String mediaUrl;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(DramaDetailActivity.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Page is loading...");
            progressDialog.setIndeterminate(false);
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
            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            m_Content = "<div style='text-align:justify; " +
                    "font-size: 18px; " +
                    "font-family:'Comic Sans MS', cursive, sans-serif; ' >" +
                    content +
                    "</div>";
            m_wvDrama.loadData(m_Content, "text/html", "UTF-8");
            m_MediaUrl = mediaUrl;

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
            }

            progressDialog.dismiss();

        }
    }
}

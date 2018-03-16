package com.softonetech.learnenglish;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FeedFragment.OnFragmentInteractionListener {

    Fragment fragment = null;
    Bundle bundle = new Bundle();
    FragmentTransaction transaction;

    String mTitle;
    String mUrl;
    private boolean doubleBackToExitPressedOnce = false;
    CoordinatorLayout mCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        mCL = findViewById(R.id.coordinatorLayout_main);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Random generator = new Random();
        int i = generator.nextInt(7);

        if (i == 0)
            openWordsInTheNews();
        if (i == 1)
            openNewsReport();
        if (i == 2)
            openTheEnglishWeSpeak();
        if (i == 3)
            openLingoHack();
        if (i == 4)
            openDramas();
        if (i == 5)
            openSixMinuteEnglish();
        if (i == 6)
            openEnglishAtUniversity();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;

            Snackbar.make(mCL, "Please click BACK again to exit", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_drama) {
            openDramas();
        } else if (id == R.id.nav_6_minute_english) {
            openSixMinuteEnglish();
        } else if (id == R.id.nav_lingohack) {
            openLingoHack();
        } else if (id == R.id.nav_the_english_we_speak) {
            openTheEnglishWeSpeak();
        } else if (id == R.id.nav_news_report) {
            openNewsReport();
        } else if (id == R.id.nav_english_at_university) {
            openEnglishAtUniversity();
        } else if (id == R.id.nav_witn) {
            openWordsInTheNews();
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction("android.intent.action.SEND");
            sendIntent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=com.softonetech.learnenglish");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openDramas() {
        mTitle = "Drama";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/drama/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

        /*Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/drama/rss");
        intent.putExtra("title", "Drama");
        startActivity(intent);*/
    }

    private void openSixMinuteEnglish() {
        mTitle = "6 Minute English";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/6-minute-english/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

        /*Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/6-minute-english/rss");
        intent.putExtra("title", "6 Minute English");
        startActivity(intent);*/
    }

    private void openLingoHack() {
        mTitle = "LingoHack";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/lingohack/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

        /*Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/lingohack/rss");
        intent.putExtra("title", "LingoHack");
        startActivity(intent);*/
    }

    private void openTheEnglishWeSpeak() {
        mTitle = "The English We Speak";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/the-english-we-speak/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

        /*Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/the-english-we-speak/rss");
        intent.putExtra("title", "The English We Speak");
        startActivity(intent);*/
    }

    private void openNewsReport() {
        mTitle = "News Report";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/news-report/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

        /*Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/news-report/rss");
        intent.putExtra("title", "News Report");
        startActivity(intent);*/
    }

    private void openEnglishAtUniversity() {
        mTitle = "English At University";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/english-at-university/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

       /* Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/english-at-university/rss");
        intent.putExtra("title", "English At University");
        startActivity(intent);*/
    }

    private void openWordsInTheNews() {
        mTitle = "Words in the News";
        mUrl = "http://feeds.bbci.co.uk/learningenglish/english/features/witn/rss";

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, FeedFragment.newInstance(mUrl, mTitle));
        transaction.commit();

     /*   Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra("url", "http://feeds.bbci.co.uk/learningenglish/english/features/witn/rss");
        intent.putExtra("title", "Words in the News");
        startActivity(intent);*/
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

package com.softonetech.learnenglish;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class FeedDetailActivity extends AppCompatActivity {

    private AdView mAdView;

    private Adapter mAdapter;
    private Context mContext;
    private List<RssFeedModel> mFeedModelList;
    private String m_Title = null;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    RelativeLayout mRelativeLayout;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        this.mContext = getApplicationContext();
        this.mRelativeLayout = findViewById(R.id.rl);
        this.mRecyclerView = findViewById(R.id.recycler_view);
        this.mRecyclerView.setHasFixedSize(true);

        this.mLayoutManager = new StaggeredGridLayoutManager(1, 1);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);

        this.mAdView = findViewById(R.id.adViewFeedDetail);
        this.mAdView.loadAd(new AdRequest.Builder().addTestDevice("B85E3B305DFD350CBAEE82C5133FC392").build());

        //mFeedModelList = (List<RssFeedModel>) getIntent().getSerializableExtra("dataset");
        mFeedModelList = RssFeedVariables.mRssFeedModelsGlobal;

        this.m_Title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(this.m_Title);

        FeedDetailActivity.this.mRecyclerView.setAdapter(
                new FeedDetailAdapter(this.mContext, this.mFeedModelList, m_Title));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

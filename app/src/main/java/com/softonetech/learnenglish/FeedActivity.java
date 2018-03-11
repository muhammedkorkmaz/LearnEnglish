package com.softonetech.learnenglish;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private AdView mAdView;

    private String m_RSSUrl = null;
    private String m_ActionTitle = null;

    private ProgressDialog progressDialog;
    private Adapter mAdapter;
    private Context mContext;
    private String mFeedDescription;
    private String mFeedLink;
    private String mFeedTitle;
    private String mFeedImgUrl;
    private List<RssFeedModel> mFeedModelList;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    LinearLayout mRelativeLayout;

    public static final String nameSpace = null;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        this.m_ActionTitle = getIntent().getStringExtra("title");
        this.m_RSSUrl = getIntent().getStringExtra("url");

        mAdView = findViewById(R.id.adViewFeed);
        mAdView.loadAd(new AdRequest.Builder().addTestDevice("B85E3B305DFD350CBAEE82C5133FC392").build());

        getSupportActionBar().setTitle(this.m_ActionTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mContext = getApplicationContext();
        this.mRelativeLayout = findViewById(R.id.rl);
        this.mRecyclerView = findViewById(R.id.recycler_view);
        this.mRecyclerView.setHasFixedSize(true);

        //this.mLayoutManager = new StaggeredGridLayoutManager(2, 1);
        this.mLayoutManager = new GridLayoutManager(this, 2);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(1), true));

        new FetchFeedTask().execute(new Void[]{(Void) null});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String imgUrl = null;
        boolean isItem = false;

        List<RssFeedModel> items = new ArrayList();
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            xmlPullParser.setInput(inputStream, null);
            xmlPullParser.nextTag();
            while (xmlPullParser.next() != 1) {
                int eventType = xmlPullParser.getEventType();
                String name = xmlPullParser.getName();
                if (name != null) {
                    if (eventType == 3) {
                        if (name.equalsIgnoreCase("item")) {
                            isItem = false;
                        }
                    } else if (eventType == 2 && name.equalsIgnoreCase("item")) {
                        isItem = true;
                    } else {
                        Log.d("MyXmlParser", "Parsing name ==> " + name);
                        String result = BuildConfig.FLAVOR;
                        if (xmlPullParser.next() == 4) {
                            result = xmlPullParser.getText();
                            xmlPullParser.nextTag();
                        }
                        if (name.equalsIgnoreCase("title")) {
                            title = result;
                        } else if (name.equalsIgnoreCase("link")) {
                            link = result;
                        } else if (name.equalsIgnoreCase("description")) {
                            description = result;
                        } else if (name.equalsIgnoreCase("media:thumbnail")) {

                            String img = xmlPullParser.getAttributeValue(null, "url");

                            img = img.replace("144", "624");
                            imgUrl = img;
                        }
                        if (!(title == null || link == null || description == null || imgUrl == null)) {
                            if (isItem) {
                                items.add(new RssFeedModel(title, link, description, imgUrl));
                            } else {
                                this.mFeedTitle = title;
                                this.mFeedLink = link;
                                this.mFeedDescription = description;
                                this.mFeedImgUrl = imgUrl;
                            }
                            title = null;
                            link = null;
                            description = null;
                            imgUrl = null;
                            isItem = false;
                        }
                    }
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        private String urlLink;

        private FetchFeedTask() {
        }

        protected void onPreExecute() {
            this.urlLink = m_RSSUrl;
            progressDialog = new ProgressDialog(FeedActivity.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Content is loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        protected Boolean doInBackground(Void... voids) {
            try {
                if (!(this.urlLink.startsWith("http://") || this.urlLink.startsWith("https://"))) {
                    this.urlLink = "http://" + this.urlLink;
                }
                FeedActivity.this.mFeedModelList = FeedActivity.this.parseFeed(new URL(this.urlLink).openConnection().getInputStream());

                return Boolean.valueOf(true);
            } catch (IOException e) {
                return Boolean.valueOf(false);
            } catch (XmlPullParserException e2) {
                return Boolean.valueOf(false);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        protected void onPostExecute(Boolean success) {
            if (success.booleanValue()) {
                FeedActivity.this.mRecyclerView.setAdapter(
                        new FeedAdapter(FeedActivity.this.mContext,
                                FeedActivity.this.mFeedModelList,
                                this.urlLink.contains("drama") ? "Drama" : "Other"));
            } else {
                Toast.makeText(FeedActivity.this, "Enter a valid Rss feed url", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}


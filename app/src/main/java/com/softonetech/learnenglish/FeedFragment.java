package com.softonetech.learnenglish;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mUrl;
    public static String mTitle;

    private OnFragmentInteractionListener mListener;

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

    private ShimmerFrameLayout mShimmerViewContainer;
    SharedPreference sharedPreference;
    List<RssFeedModel> favorites;

    //private AdView adView;
    //LinearLayout adContainer;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_PARAM1);
            mTitle = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.m_ActionTitle = mTitle;
        this.m_RSSUrl = mUrl;

        mAdView = getActivity().findViewById(R.id.adViewFeed);
        mAdView.loadAd(new AdRequest.Builder().addTestDevice("B85E3B305DFD350CBAEE82C5133FC392").build());
        //mAdView.setVisibility(View.GONE);

        //adContainer = view.findViewById(R.id.banner_container);

        getActivity().setTitle(this.m_ActionTitle);

        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);

        this.mContext = getActivity().getApplicationContext();
        this.mRelativeLayout = view.findViewById(R.id.rl);
        this.mRecyclerView = view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setHasFixedSize(true);

        //this.mLayoutManager = new StaggeredGridLayoutManager(2, 1);
        this.mLayoutManager = new GridLayoutManager(mContext, 1);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(1), true));

        sharedPreference = new SharedPreference();
        favorites = sharedPreference.getFavorites(mContext);

        new FetchFeedTask().execute(new Void[]{(Void) null});
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
            /*progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Content is loading...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        protected Boolean doInBackground(Void... voids) {
            try {
                if (m_ActionTitle.equals("Favorites")) {
                    mFeedModelList = favorites;
                } else {
                    if (!(this.urlLink.startsWith("http://") || this.urlLink.startsWith("https://"))) {
                        this.urlLink = "http://" + this.urlLink;
                    }
                    mFeedModelList = parseFeed(new URL(this.urlLink).openConnection().getInputStream());
                }
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
                mRecyclerView.setAdapter(
                        new FeedAdapter(mContext,
                                mFeedModelList,
                                this.urlLink.contains("drama") ? "Drama" : "Other"));
            } else {
                Toast.makeText(mContext, "Enter a valid Rss feed url", Toast.LENGTH_SHORT).show();
            }
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);

            //adView = new AdView(mContext, "651032438571643_654451448229742", AdSize.BANNER_HEIGHT_50);
            // Find the Ad Container

            // Add the ad view to your activity layout
            //adContainer.addView(adView);
            // Request an ad
            //adView.loadAd();
            //progressDialog.dismiss();
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

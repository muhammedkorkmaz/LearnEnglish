package com.softonetech.learnenglish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.autofill.Dataset;
import android.support.annotation.RequiresApi;
import android.support.design.internal.ParcelableSparseArray;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import me.tankery.lib.circularseekbar.CircularSeekBar;


public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<RssFeedModel> mRssFeedModels;
    private List<RssFeedModel> mRssFeedModelsGlobal;
    private String m_Title;
    List<RssFeedModel> RssFeedModel;

    SharedPreference sharedPreference;

    private static final int TYPE_POST = 0;
    private static final int TYPE_AD = 1;

    ///////////////////////
    private final String TAG = FeedAdapter.class.getSimpleName();
    private NativeAd nativeAd;
    //////////////////////

    Map<String, String> nativeAdLoaded = new HashMap<String, String>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFeedTitle;
        TextView textViewFeedDescription;
        ImageView imageViewFeed;
        RelativeLayout ly;
        CircularSeekBar sb_Time;
        ImageView img_Progress;
        RelativeLayout rl_Progress;
        TextView tv_progress;
        ImageView img_Favorite;

        public ViewHolder(View v) {
            super(v);
            this.textViewFeedTitle = v.findViewById(R.id.textView_feedTitle);
            this.textViewFeedDescription = v.findViewById(R.id.textView_feedDescription);
            this.imageViewFeed = v.findViewById(R.id.imageView_feed);
            this.ly = v.findViewById(R.id.layout_feed);
            this.sb_Time = v.findViewById(R.id.sb_Time);
            this.sb_Time.setMax(100);
            this.img_Progress = v.findViewById(R.id.imageView_progress);
            this.rl_Progress = v.findViewById(R.id.rl_progress);
            this.tv_progress = v.findViewById(R.id.tv_progress);
            this.img_Favorite = v.findViewById(R.id.img_favorite);
        }
    }

    private static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        AdIconView adIconView;
        TextView tvAdTitle;
        TextView tvAdBody;
        Button btnCTA;
        View container;
        TextView sponsorLabel;
        LinearLayout adChoicesContainer;
        MediaView mediaView;

        NativeAdViewHolder(View itemView) {
            super(itemView);
            this.container = itemView;
            adIconView = (AdIconView) itemView.findViewById(R.id.adIconView);
            tvAdTitle = (TextView) itemView.findViewById(R.id.tvAdTitle);
            tvAdBody = (TextView) itemView.findViewById(R.id.tvAdBody);
            btnCTA = (Button) itemView.findViewById(R.id.btnCTA);
            adChoicesContainer = (LinearLayout) itemView.findViewById(R.id.adChoicesContainer);
            mediaView = (MediaView) itemView.findViewById(R.id.mediaView);
            sponsorLabel = (TextView) itemView.findViewById(R.id.sponsored_label);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FeedAdapter(Context context, List<RssFeedModel> dataSet, String title) {

        sharedPreference = new SharedPreference();

        this.m_Title = title;
        this.mRssFeedModelsGlobal = new ArrayList<RssFeedModel>();
        List<RssFeedModel> mRssFeedModels = new ArrayList<RssFeedModel>();

        for (RssFeedModel o : dataSet) {
            mRssFeedModelsGlobal.add(new RssFeedModel(
                    o.getTitle().trim(), o.getLink().trim(), o.getDescription().trim(), o.getImgUrl().trim()
            ));
        }

        if (!FeedFragment.mTitle.equals("Pronunciation")) {
            for (RssFeedModel feed : dataSet) {
                String rs = feed.title;
                int firstIndex = rs.indexOf("/");

                if (firstIndex != -1) {
                    rs = rs.substring(firstIndex + 1, rs.length()).trim();

                    if (rs.contains(",")) {
                        rs = rs.substring(0, rs.indexOf(",")).trim();
                    }
                    if (rs.contains(":")) {
                        rs = rs.substring(0, rs.indexOf(":")).trim();
                    }
                    if (rs.contains("-")) {
                        rs = rs.substring(0, rs.indexOf("-")).trim();
                    }
                }
                feed.title = rs;
            }
        }


        if (FeedFragment.mTitle.equals("Drama")) {
            SortedSet<RssFeedModel> set = new TreeSet<RssFeedModel>(new Comparator<RssFeedModel>() {
                public int compare(RssFeedModel o1, RssFeedModel o2) {
                    return o2.getTitle().trim().equals(o1.getTitle().trim()) ? 0 : -1;
                }
            });
            set.addAll(dataSet);

            List<RssFeedModel> list = new ArrayList<>(set);

            for (int i = 0; i < list.size(); i++) {

                if (i == 3) {
                    mRssFeedModels.add(new RssFeedModel("AD", "AD", "AD", "AD"));
                }

                mRssFeedModels.add(new RssFeedModel(
                        list.get(i).getTitle().trim()
                        , list.get(i).getLink().trim()
                        , list.get(i).getDescription().trim()
                        , dataSet.get(i).getImgUrl().trim()
                ));
            }

            this.mRssFeedModels = list;
        } else {

            for (int i = 0; i < dataSet.size(); i++) {

                if (i == 3) {
                    mRssFeedModels.add(new RssFeedModel("AD", "AD", "AD", "AD"));
                }

                mRssFeedModels.add(new RssFeedModel(
                        dataSet.get(i).getTitle().trim(), dataSet.get(i).getLink().trim(), dataSet.get(i).getDescription().trim(), dataSet.get(i).getImgUrl().trim()
                ));
            }

            this.mRssFeedModels = mRssFeedModels;
        }

        this.mContext = context;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("Korkmaz", "Type" + viewType);
        if (viewType == TYPE_AD) {
            return new NativeAdViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.item_native_ad, parent, false));
        }
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.activity_feed_custom, parent, false));
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 3)
            return TYPE_AD;
        else

            return TYPE_POST;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        {

            if (holder.getItemViewType() == 0) {

                ViewHolder viewHolder = (ViewHolder) holder;

                final RssFeedModel rssFeedModel = (RssFeedModel) this.mRssFeedModels.get(position);

                int progress = 0;

                progress = RssFeedVariables.mGameSettings.getInt(rssFeedModel.title, progress);
                viewHolder.sb_Time.setProgress(progress);

                if (progress != 99) {
                    viewHolder.rl_Progress.setVisibility(View.VISIBLE);
                    viewHolder.tv_progress.setText("%" + progress);
                    viewHolder.img_Progress.setVisibility(View.GONE);
                } else {
                    viewHolder.rl_Progress.setVisibility(View.GONE);
                    viewHolder.img_Progress.setVisibility(View.VISIBLE);
                }


                viewHolder.textViewFeedTitle.setText(rssFeedModel.title);
                viewHolder.textViewFeedDescription.setText(rssFeedModel.description);
                Glide.with(mContext)
                        .load(rssFeedModel.imgUrl)
                        .asBitmap()
                        .error(R.drawable.ic_menu_camera)
                        //.override(250, 250)
                        .centerCrop()
                        .into(new BitmapImageViewTarget(viewHolder.imageViewFeed) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                //Play with bitmap
                                super.setResource(resource);
                            }
                        });

                viewHolder.ly.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {

                        if (m_Title.equals("Drama")) {
                            Intent intent = new Intent(mContext, FeedDetailActivity.class);
                            RssFeedVariables.mRssFeedModelsGlobal = new ArrayList<RssFeedModel>();
                            RssFeedVariables.mRssFeedModelsGlobal = mRssFeedModelsGlobal;
                            intent.putExtra("url", rssFeedModel.link);
                            intent.putExtra("title", rssFeedModel.title);
                            intent.putExtra("imgUrl", rssFeedModel.imgUrl);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            mContext.startActivity(intent);
                        } else {
                            Intent intent = new Intent(mContext, DramaDetailActivity.class);
                            intent.putExtra("url", rssFeedModel.link);
                            intent.putExtra("title", rssFeedModel.title);
                            intent.putExtra("imgUrl", rssFeedModel.imgUrl);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                });


                if (m_Title.equals("Drama")) {
                    viewHolder.img_Favorite.setVisibility(View.GONE);
                } else {
                    viewHolder.img_Favorite.setVisibility(View.VISIBLE);

                    if (checkFavoriteItem(rssFeedModel)) {
                        viewHolder.img_Favorite.setImageResource(android.R.drawable.btn_star_big_on);
                    } else {
                        viewHolder.img_Favorite.setImageResource(android.R.drawable.btn_star_big_off);
                    }

                    viewHolder.img_Favorite.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!checkFavoriteItem(rssFeedModel)) {
                                sharedPreference.addFavorite(mContext, rssFeedModel);
                                notifyDataSetChanged();
                            } else {
                                sharedPreference.removeFavorite(mContext, rssFeedModel);
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

            } else {

                if (nativeAdLoaded.get(Integer.toString(position)) == null
                        || !nativeAdLoaded.get(Integer.toString(position)).equals("Y")) {
                    // Instantiate a NativeAd object.
                    // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
                    // now, while you are testing and replace it later when you have signed up.
                    // While you are using this temporary code you will only get test ads and if you release
                    // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
                    nativeAd = new NativeAd(mContext, "651032438571643_651048201903400");

                    List<String> testDevices = new ArrayList<>();
                    testDevices.add("B85E3B305DFD350CBAEE82C5133FC392");
                    AdSettings.addTestDevices(testDevices);

                    nativeAd.setAdListener(new NativeAdListener() {
                        @Override
                        public void onMediaDownloaded(Ad ad) {
                            // Native ad finished downloading all assets
                            Log.e(TAG, "Native ad finished downloading all assets.");
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            // Native ad failed to load
                            Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            NativeAdViewHolder nativeAdViewHolder = (NativeAdViewHolder) holder;


                            AdIconView adIconView = nativeAdViewHolder.adIconView;
                            TextView tvAdTitle = nativeAdViewHolder.tvAdTitle;
                            TextView tvAdBody = nativeAdViewHolder.tvAdBody;
                            Button btnCTA = nativeAdViewHolder.btnCTA;
                            LinearLayout adChoicesContainer = nativeAdViewHolder.adChoicesContainer;
                            MediaView mediaView = nativeAdViewHolder.mediaView;
                            TextView sponsorLabel = nativeAdViewHolder.sponsorLabel;

                            tvAdTitle.setText(nativeAd.getAdvertiserName());
                            tvAdBody.setText(nativeAd.getAdBodyText());
                            btnCTA.setText(nativeAd.getAdCallToAction());
                            sponsorLabel.setText(nativeAd.getSponsoredTranslation());

                            AdChoicesView adChoicesView = new AdChoicesView(mContext, nativeAd, true);
                            adChoicesContainer.addView(adChoicesView);

                            List<View> clickableViews = new ArrayList<>();
                            clickableViews.add(btnCTA);
                            clickableViews.add(mediaView);
                            nativeAd.registerViewForInteraction(nativeAdViewHolder.container, mediaView, adIconView, clickableViews);

                            Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            // Native ad clicked
                            Log.d(TAG, "Native ad clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            // Native ad impression
                            Log.d(TAG, "Native ad impression logged!");
                        }
                    });

                    if (nativeAdLoaded.get("loaded") == null
                            || !nativeAdLoaded.get("loaded").equals("Y")) {
                        // Request an ad
                        nativeAd.loadAd();
                    }
                    nativeAdLoaded.put(Integer.toString(position), "Y");
                    nativeAdLoaded.put("loaded", "Y");

                }
            }
        }
    }

    public boolean checkFavoriteItem(RssFeedModel checkProduct) {
        boolean check = false;
        List<RssFeedModel> favorites = sharedPreference.getFavorites(mContext);
        if (favorites != null) {
            for (RssFeedModel product : favorites) {
                if (product.title.equals(checkProduct.title)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    public int getItemCount() {
        return this.mRssFeedModels.size();
    }

    public void onResume() {
        notifyDataSetChanged();
    }

    private void loadNativeAd() {

    }
}


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private Context mContext;
    private List<RssFeedModel> mRssFeedModels;
    private List<RssFeedModel> mRssFeedModelsGlobal;
    private String m_Title;

    Fragment fragment = null;
    FragmentManager fragmentManager;
    Bundle bundle = new Bundle();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Button mButton;
        //TextView textViewFeedDescription;
        TextView textViewFeedTitle;
        ImageView imageViewFeed;
        LinearLayout ly;

        public ViewHolder(View v) {
            super(v);
            this.textViewFeedTitle = v.findViewById(R.id.textView_feedTitle);
            this.imageViewFeed = v.findViewById(R.id.imageView_feed);
            //this.textViewFeedDescription = v.findViewById(R.id.textView_feedDescription);
            this.ly = v.findViewById(R.id.layout_feed);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FeedAdapter(Context context, List<RssFeedModel> dataSet, String title) {

        this.m_Title = title;
        this.mRssFeedModelsGlobal = new ArrayList<RssFeedModel>();


        for (RssFeedModel o : dataSet) {
            mRssFeedModelsGlobal.add(new RssFeedModel(
                    o.getTitle().trim(), o.getLink().trim(), o.getDescription().trim(), o.getImgUrl().trim()
            ));
        }

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

        SortedSet<RssFeedModel> set = new TreeSet<RssFeedModel>(new Comparator<RssFeedModel>() {

            public int compare(RssFeedModel o1, RssFeedModel o2) {
                return o2.getTitle().trim().equals(o1.getTitle().trim()) ? 0 : -1;
            }
        });
        set.addAll(dataSet);

        List<RssFeedModel> list = new ArrayList<>(set);

        this.mRssFeedModels = list;
        this.mContext = context;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.activity_feed_custom, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = (RssFeedModel) this.mRssFeedModels.get(position);

        holder.textViewFeedTitle.setText(rssFeedModel.title);
        //holder.textViewFeedDescription.setText(rssFeedModel.description);
        Glide.with(mContext)
                .load(rssFeedModel.imgUrl)
                .asBitmap()
                .error(R.drawable.ic_menu_camera)
                //.override(250, 250)
                .centerCrop()
                .into(new BitmapImageViewTarget(holder.imageViewFeed) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        //Play with bitmap
                        super.setResource(resource);
                    }
                });

        holder.ly.setOnClickListener(new OnClickListener() {
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
    }

    public int getItemCount() {
        return this.mRssFeedModels.size();
    }
} 

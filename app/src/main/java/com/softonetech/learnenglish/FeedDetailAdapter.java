package com.softonetech.learnenglish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class FeedDetailAdapter extends RecyclerView.Adapter<FeedDetailAdapter.ViewHolder> {

    private Context mContext;
    private List<RssFeedModel> mRssFeedModels;
    private String mTitle;
    Fragment fragment = null;
    FragmentManager fragmentManager;
    Bundle bundle = new Bundle();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Button mButton;
        TextView textViewFeedDescription;
        TextView textViewFeedTitle;
        ImageView imageViewFeed;
        LinearLayout ly;

        public ViewHolder(View v) {
            super(v);
            this.textViewFeedTitle = v.findViewById(R.id.textView_feedTitle);
            this.imageViewFeed = v.findViewById(R.id.imageView_feed);
            this.textViewFeedDescription = v.findViewById(R.id.textView_feedDescription);
            this.ly = v.findViewById(R.id.layout_feed);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FeedDetailAdapter(Context context, List<RssFeedModel> dataSet, String title) {

        mTitle=title;

        for (RssFeedModel feed : dataSet) {
            String rs = feed.title;
            int firstIndex = rs.indexOf("/");

            if (firstIndex != -1) {
                rs = rs.substring(firstIndex + 1, rs.length()).trim();
            }
            feed.title = rs;
        }

        /*List<RssFeedModel> list = dataSet.stream()
                .filter(p -> p.getTitle().contains(title)).collect(Collectors.toList());*/

        List<RssFeedModel> list=new ArrayList<RssFeedModel>();

        for (RssFeedModel o : dataSet) {
            if (o.getTitle().contains(title)) {
                list.add(new RssFeedModel(
                        o.getTitle().trim(),
                        o.getLink().trim(),
                        o.getDescription().trim(),
                        o.getImgUrl().trim()
                ));
            }
        }


        SortedSet<RssFeedModel> set = new TreeSet<RssFeedModel>(new Comparator<RssFeedModel>() {
            public int compare(RssFeedModel o1, RssFeedModel o2) {
                return o2.getTitle().equals(o1.getTitle()) ? 0 : -1;
            }
        });
        set.addAll(list);

        List<RssFeedModel> list2 = new ArrayList<>(set);

        this.mRssFeedModels = list2;
        this.mContext = context;
        //this.fragmentManager = fragmentManager;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.custom_view_feed, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = (RssFeedModel) this.mRssFeedModels.get(position);

        holder.textViewFeedTitle.setText(rssFeedModel.title);
        holder.textViewFeedDescription.setText(rssFeedModel.description);
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

                Intent intent = new Intent(mContext, DramaDetailActivity.class);
                intent.putExtra("url", rssFeedModel.link);
                intent.putExtra("title", rssFeedModel.title);
                intent.putExtra("imgUrl", rssFeedModel.imgUrl);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    public int getItemCount() {
        return this.mRssFeedModels.size();
    }
} 

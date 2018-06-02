package com.softonetech.learnenglish;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SharedPreference {

    public static final String PREFS_NAME = "APP";
    public static final String FAVORITES = "Favorite";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveFavorites(Context context, List<RssFeedModel> favorites) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, RssFeedModel product) {
        List<RssFeedModel> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<RssFeedModel>();
        favorites.add(product);
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, RssFeedModel product) {
        ArrayList<RssFeedModel> favorites = getFavorites(context);
        if (favorites != null) {
            for (RssFeedModel a : favorites) {
                if (a.title.equals(product.title)) {
                    favorites.remove(a);
                    saveFavorites(context, favorites);
                    break;
                }
            }

            /*for (Iterator<RssFeedModel> iterator = favorites.iterator(); iterator.hasNext(); ) {
                RssFeedModel value = iterator.next();
                if (value.title.equals(product.title)) {
                    iterator.remove();
                }
            }*/

        }
    }

    public ArrayList<RssFeedModel> getFavorites(Context context) {
        SharedPreferences settings;
        List<RssFeedModel> favorites = new ArrayList<RssFeedModel>();

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            RssFeedModel[] favoriteItems = gson.fromJson(jsonFavorites,
                    RssFeedModel[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<RssFeedModel>(favorites);
        }

        return (ArrayList<RssFeedModel>) favorites;
    }
}
package com.softonetech.learnenglish;


import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

class RssFeedVariables {

    public static List<RssFeedModel> mRssFeedModelsGlobal;

    public static SharedPreferences mGameSettings=new SharedPreferences() {
        @Override
        public Map<String, ?> getAll() {
            return null;
        }

        @Nullable
        @Override
        public String getString(String s, @Nullable String s1) {
            return null;
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String s, @Nullable Set<String> set) {
            return null;
        }

        @Override
        public int getInt(String s, int i) {
            return 0;
        }

        @Override
        public long getLong(String s, long l) {
            return 0;
        }

        @Override
        public float getFloat(String s, float v) {
            return 0;
        }

        @Override
        public boolean getBoolean(String s, boolean b) {
            return false;
        }

        @Override
        public boolean contains(String s) {
            return false;
        }

        @Override
        public Editor edit() {
            return null;
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

        }
    };
    
    public static SharedPreferences.Editor mPrefEditor=new SharedPreferences.Editor() {
        @Override
        public SharedPreferences.Editor putString(String s, @Nullable String s1) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String s, @Nullable Set<String> set) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putInt(String s, int i) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putLong(String s, long l) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putFloat(String s, float v) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String s, boolean b) {
            return null;
        }

        @Override
        public SharedPreferences.Editor remove(String s) {
            return null;
        }

        @Override
        public SharedPreferences.Editor clear() {
            return null;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public void apply() {

        }
    };

}

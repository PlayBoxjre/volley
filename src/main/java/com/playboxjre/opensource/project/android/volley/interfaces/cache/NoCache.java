package com.playboxjre.opensource.project.android.volley.interfaces.cache;

public class NoCache implements Cache {
    @Override
    public Entry get(String key) {
        return null;
    }

    @Override
    public void put(String key, Entry entry) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void invalidate(String key, boolean fullExpire) {

    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void clear() {

    }
}

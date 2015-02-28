package fr.s13d.photobackup;

import android.database.Cursor;

import java.io.Serializable;

public class PBMedia implements Serializable {
    private final static String LOG_TAG = "PBMedia";
    final private int id;
    final private int width;
    final private int height;
    final private int orientation;
    final private String path;
    final private String timestamp;
    private PBMediaState state;
    public enum PBMediaState { WAITING, SYNCED, ERROR }


    //////////////////
    // Constructors //
    //////////////////
    public PBMedia(Cursor mediaCursor) {
        this.id = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow("_id"));
        this.path = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_data"));
        this.timestamp = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("date_added"));
        this.width = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow("width"));
        this.height = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow("height"));
        this.orientation = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow("orientation"));
        this.state = null;
    }


    ////////////
    // Methods//
    ////////////

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return "PBMedia: " + this.path;
    }


    /////////////////////////////////////////
    // Getters/Setters are the Java fun... //
    /////////////////////////////////////////
    public int getId() {
        return this.id;
    }


    public String getPath() {
        return this.path;
    }

/*
    public PBMediaState getState() {
        return this.state;
    }


    public void setState(PBMediaState mediaState) {
        String stateString = picturesPreferences.getString(String.valueOf(media.getId()), PBMedia.PBMediaState.WAITING.name());
        this.state = mediaState;
    }*/

}

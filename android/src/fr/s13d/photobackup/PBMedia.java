package fr.s13d.photobackup;

import android.database.Cursor;

import java.io.Serializable;

public class PBMedia implements Serializable {
    private final static String LOG_TAG = "PBMedia";
    private int id;
    private int width;
    private int height;
    private int orientation;
    private String path;
    private String timestamp;
    private Boolean uploaded;


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
        this.uploaded = Boolean.FALSE;
    }


    ////////////
    // Methods//
    ////////////

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return "PBMedia: " + this.path;
    }


    public void uploadDidSucceed() {
        this.uploaded = Boolean.TRUE;
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


    public String getTimestamp() {
        return this.timestamp;
    }


    public Boolean getUploaded() {
        return this.uploaded;
    }


    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

}

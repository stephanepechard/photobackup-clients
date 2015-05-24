package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.io.Serializable;

public class PBMedia implements Serializable {
    final private int id;
    final private String path;
    private PBMediaState state;
    Context context;
    public enum PBMediaState { WAITING, SYNCED, ERROR }


    //////////////////
    // Constructors //
    //////////////////
    public PBMedia(Context context, Cursor mediaCursor) {
        this.id = mediaCursor.getInt(mediaCursor.getColumnIndexOrThrow("_id"));
        this.path = mediaCursor.getString(mediaCursor.getColumnIndexOrThrow("_data"));

        // Find state from the shared preferences
        SharedPreferences preferences = context.getSharedPreferences(PBMediaStore.PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
        String stateString = preferences.getString(String.valueOf(this.id), PBMedia.PBMediaState.WAITING.name());
        this.state = PBMedia.PBMediaState.valueOf(stateString);
        this.context = context;
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

    public PBMediaState getState() {
        return this.state;
    }

    public void setState(PBMediaState mediaState) {
        if (this.state != mediaState) {
            this.state = mediaState;
            Log.i("PBMedia", "Setting state " + mediaState.toString() + " to " + this.getPath());

            SharedPreferences preferences = context.getSharedPreferences(PBMediaStore.PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
            preferences.edit().putString(String.valueOf(this.getId()), mediaState.name()).apply();
        }
    }

}

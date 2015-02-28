package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PBMediaStore {

    private static final String LOG_TAG = "PBMediaStore";
    private static Cursor idCursor;
    private static Context context;
    private static SharedPreferences picturesPreferences;
    private static SharedPreferences.Editor picturesPreferencesEditor;
    private static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String PhotoBackupPicturesSharedPreferences = "PhotoBackupPicturesSharedPreferences";

    public PBMediaStore(Context theContext) {
        context = theContext;
        picturesPreferences = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
        picturesPreferencesEditor = picturesPreferences.edit();
        updateStore();
    }


    public void close() {
        if(idCursor != null && !idCursor.isClosed()) {
            idCursor.close();
        }
        picturesPreferences = null;
        picturesPreferencesEditor = null;
    }


    private void updateStore() {
        final String[] projection = new String[] { "_id" };
        idCursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");
        syncPreferences();
    }


    public void syncPreferences() {
        Log.d(LOG_TAG, "syncPreferences");
        // Remove pictures in preferences that were removed from store
        Map<String, ?> mediasMap = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll();
        for (String key : new ArrayList<>(mediasMap.keySet())) {
            int mediaId = Integer.parseInt(key);
            final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + mediaId, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                Log.d(LOG_TAG, "Remove media " + key + " from preference");
                picturesPreferencesEditor.remove(key);
            }
        }
    }


    public void setMediaState(PBMedia media, PBMedia.PBMediaState mediaState) {
        Log.i(LOG_TAG, "setMediaState: " + media);
        picturesPreferencesEditor.putString(String.valueOf(media.getId()), mediaState.name()).commit();
    }


    public PBMedia getMedia(int id) {

        PBMedia media = null;
        if (id != 0) {
            final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + id, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                media = new PBMedia(cursor);

                try {
                    String stateString = picturesPreferences.getString(String.valueOf(media.getId()), PBMedia.PBMediaState.WAITING.name());
                    setMediaState(media, PBMedia.PBMediaState.SYNCED);
                    //media.setState(PBMedia.PBMediaState.valueOf(stateString));
                }
                catch (java.lang.ClassCastException e) {
                    Log.e(LOG_TAG, "Explosion!!");
                }
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return media;
    }


    public List<PBMedia> getMedias() {
        Map<String, ?> mediasMap = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll();
        List<PBMedia> list = new ArrayList<>();
        for (String key : new ArrayList<>(mediasMap.keySet())) {
            int mediaId = Integer.parseInt(key);
            list.add(getMedia(mediaId));
        }

        Collections.sort(list, mediaComparator); // order depending on the path
        return list;
    }


    public PBMedia getLastMediaInStore() {
        int id = 0;
        if (idCursor.moveToFirst()) {
            int idColumn = idCursor.getColumnIndexOrThrow("_id");
            id = idCursor.getInt(idColumn);
        }
        return getMedia(id);
    }


    public int getMediaCount() {
        return context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll().size();
    }


    private Comparator<PBMedia> mediaComparator = new Comparator<PBMedia>() {
        public int compare(PBMedia obj1, PBMedia obj2) {
            if (obj1 == null || obj2 == null) {
                return 0;
            }
            return obj1.getPath().compareTo(obj2.getPath());
        }
    };
}

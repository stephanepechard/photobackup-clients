package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.s13d.photobackup.interfaces.PBMediaStoreInterface;

public class PBMediaStore {

    private static final String LOG_TAG = "PBMediaStore";
    private static Context context;
    private static List<PBMedia> mediaList;
    private static SyncMediaStoreTask syncTask;
    private static SharedPreferences picturesPreferences;
    private static SharedPreferences.Editor picturesPreferencesEditor;
    private static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final String PhotoBackupPicturesSharedPreferences = "PhotoBackupPicturesSharedPreferences";
    private List<PBMediaStoreInterface> interfaces = new ArrayList<>();


    public PBMediaStore(Context theContext) {
        context = theContext;
        mediaList = new ArrayList<>();
        picturesPreferences = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
        picturesPreferencesEditor = picturesPreferences.edit();
        picturesPreferencesEditor.apply();
    }


    public void addInterface(PBMediaStoreInterface storeInterface) {
        interfaces.add(storeInterface);
    }


    public void close() {
        if (syncTask != null) {
            syncTask.cancel(true);
        }

        mediaList = null;
        picturesPreferences = null;
        picturesPreferencesEditor = null;
    }


    public PBMedia getMedia(int id) {

        PBMedia media = null;
        if (id != 0) {
            final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + id, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                media = new PBMedia(context, cursor);

                try {
                    String stateString = picturesPreferences.getString(String.valueOf(media.getId()), PBMedia.PBMediaState.WAITING.name());
                    media.setState(PBMedia.PBMediaState.valueOf(stateString));
                }
                catch (Exception e) {
                    Log.e(LOG_TAG, "Explosion!!");
                }
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return media;
    }


    public PBMedia getMediaAt(int position) {
        Map<String, ?> mediasMap = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll();
        List<String> keys = new ArrayList<>(mediasMap.keySet());
        int mediaId = Integer.parseInt(keys.get(position));
        return getMedia(mediaId);
    }


    public List<PBMedia> getMedias() {
        return mediaList;
    }


    public PBMedia getLastMediaInStore() {
        int id = 0;
        final String[] projection = new String[] { "_id" };
        final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");
        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndexOrThrow("_id");
            id = cursor.getInt(idColumn);
            cursor.close();
        }
        return getMedia(id);
    }


    /////////////////////////////////
    // Synchronize the media store //
    /////////////////////////////////
    public void sync() {
        if (syncTask != null) {
            syncTask.cancel(true);
        }
        syncTask = new SyncMediaStoreTask();
        syncTask.execute();
        Log.i(LOG_TAG, "Start SyncMediaStoreTask");
    }


    private class SyncMediaStoreTask extends AsyncTask<Void, Void, Void> {

        /////////////////////////////////
        // What makes you an AsyncTask //
        /////////////////////////////////
        protected Void doInBackground(Void... voids) {

            // Get all known pictures in PB
            Map<String, ?> mediasMap = context.getSharedPreferences(PBMediaStore.PhotoBackupPicturesSharedPreferences,
                    Context.MODE_PRIVATE).getAll();
            Set<String> inCursor = new HashSet<>();

            // Get all pictures on device
            final String[] projection = new String[] { "_id", "_data" };
            final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");

            // loop through them to sync
            PBMedia media;
            String stateString;
            PBMedia.PBMediaState state;
            while (cursor != null && cursor.moveToNext()) {
                if(syncTask.isCancelled()) {
                    Log.i(LOG_TAG, "SyncMediaStoreTask cancelled");
                    return null;
                }
                // build media
                media = new PBMedia(context, cursor);
                stateString = (String)mediasMap.get(Integer.toString(media.getId()));
                state = (stateString != null) ?
                        PBMedia.PBMediaState.valueOf(stateString) : PBMedia.PBMediaState.WAITING;
                media.setState(state);
                mediaList.add(media); // populate list
                inCursor.add(Integer.toString(media.getId()));
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            // purge pictures in preferences that were removed from device
            Set<String> inCursorCopy = new HashSet<>(inCursor);
            Set<String> inMap = new HashSet<>(mediasMap.keySet());
            inMap.removeAll(inCursor);
            inCursor.removeAll(inCursorCopy);
            inMap.addAll(inCursor);

            for (String key : inMap) {
                Log.d(LOG_TAG, "Remove media " + key + " from preference");
                picturesPreferencesEditor.remove(key).apply();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            for(PBMediaStoreInterface storeInterface : interfaces) {
                storeInterface.onSyncMediaStoreTaskPostExecute();
            }
            Log.i(LOG_TAG, "Stop SyncMediaStoreTask");
        }
    }

}

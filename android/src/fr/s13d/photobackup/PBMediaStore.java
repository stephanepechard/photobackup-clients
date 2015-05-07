package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PBMediaStore {

    private static final String LOG_TAG = "PBMediaStore";
    private final PBMediaStore store;
    private static Context context;
    private static SharedPreferences picturesPreferences;
    private static SharedPreferences.Editor picturesPreferencesEditor;
    private static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final String PhotoBackupPicturesSharedPreferences = "PhotoBackupPicturesSharedPreferences";
    private static List<PBMedia> mediaList;


    public PBMediaStore(Context theContext) {
        store = this;
        context = theContext;
        mediaList = new ArrayList<>();
        picturesPreferences = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
        picturesPreferencesEditor = picturesPreferences.edit();
        picturesPreferencesEditor.apply();
        //new AddAllMediasTask().execute();
    }


    public void close() {
        mediaList = null;
        picturesPreferences = null;
        picturesPreferencesEditor = null;
    }


    public void setMediaState(PBMedia media, PBMedia.PBMediaState mediaState) {
        if (media.getState() != mediaState) {
            Log.i(LOG_TAG, "setMediaState: " + media);
            picturesPreferencesEditor.putString(String.valueOf(media.getId()), mediaState.name()).apply();
        }
    }


    public PBMedia getMedia(int id) {

        PBMedia media = null;
        if (id != 0) {
            final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + id, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                media = new PBMedia(context, cursor);

                try {
                    String stateString = picturesPreferences.getString(String.valueOf(media.getId()), PBMedia.PBMediaState.WAITING.name());
                    setMediaState(media, PBMedia.PBMediaState.SYNCED);
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


    private Comparator<PBMedia> mediaComparator = new Comparator<PBMedia>() {
        public int compare(PBMedia obj1, PBMedia obj2) {
            if (obj1 == null || obj2 == null) {
                return 0;
            }
            return obj1.getPath().compareTo(obj2.getPath());
        }
    };


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


    public int getMediaCount() {
        return context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll().size();
    }


    public void sync(PBMediaStoreListener listener) {
        new SyncMediaStoreTask(listener).execute();
    }


    private class SyncMediaStoreTask extends AsyncTask<Void, Void, Void> {
        final private PBMediaStoreListener listener;

        public SyncMediaStoreTask(PBMediaStoreListener listener) {
            this.listener = listener;
        }

        protected Void doInBackground(Void... voids) {

            // Remove pictures in preferences that were removed from store
            Map<String, ?> mediasMap = context.getSharedPreferences(PBMediaStore.PhotoBackupPicturesSharedPreferences,
                    Context.MODE_PRIVATE).getAll();
            for (String key : new ArrayList<>(mediasMap.keySet())) {
                final int mediaId = Integer.parseInt(key);
                final PBMedia media = store.getMedia(mediaId);
                final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + mediaId, null, null);
                if (cursor == null || !cursor.moveToFirst() || media == null || media.getPath().isEmpty()) {
                    Log.d(LOG_TAG, "Remove media " + key + " from preference");
                    picturesPreferencesEditor.remove(key).apply();
                }
                else {
                    mediaList.add(media); // populate list
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            Collections.sort(mediaList, mediaComparator); // order depending on the path
            return null;
        }

        protected void onPostExecute(Void result) {
            if (listener != null) {
                listener.onSyncMediaStoreTaskPostExecute();
            }
        }
    }


    private class AddAllMediasTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            final String[] projection = new String[] { "_id", "_data" };
            final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");
            PBMedia media;
            while (cursor != null && cursor.moveToNext()) {
                media = new PBMedia(context, cursor);
                setMediaState(media, PBMedia.PBMediaState.WAITING);
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d(LOG_TAG, "All medias added!");
        }
    }

}

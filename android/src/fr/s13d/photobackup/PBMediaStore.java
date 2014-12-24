package fr.s13d.photobackup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PBMediaStore {

    private static final String LOG_TAG = "PBMediaStore";
    private static Cursor idCursor;
    private static Context context;
    private static SharedPreferences picturesPreferences;
    private static SharedPreferences.Editor picturesSharedPreferences;
    private static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String PhotoBackupPicturesSharedPreferences = "PhotoBackupPicturesSharedPreferences";


    public PBMediaStore(Context theContext) {
        context = theContext;
        picturesPreferences = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences, Context.MODE_PRIVATE);
        picturesSharedPreferences = picturesPreferences.edit();
        updateStore();
    }


    public void close() {
        idCursor.close();
        picturesPreferences = null;
        picturesSharedPreferences = null;
    }


    private void updateStore() {
        final String[] projection = new String[] { "_id" };
        idCursor = context.getContentResolver().query(uri, projection, null, null, "date_added DESC");
    }


    public void markMediaForUpload(PBMedia media) {
        Log.i(LOG_TAG, "markMediaForUpload: " + media);
        picturesSharedPreferences.putBoolean(String.valueOf(media.getId()), Boolean.FALSE).commit();
    }


    public PBMedia getMedia(int id) {

        PBMedia picture = null;
        if (id != 0) {
            final Cursor cursor = context.getContentResolver().query(uri, null, "_id = " + id, null, null);
            if (cursor.moveToFirst()) {
                picture = new PBMedia(cursor);
                Boolean bool = picturesPreferences.getBoolean(String.valueOf(picture.getId()), Boolean.FALSE);
                picture.setUploaded(bool);
            }
        }

        return picture;
    }


    public List<String> getMediaIds() {
        Map<String, ?> mediasMap = context.getSharedPreferences(PhotoBackupPicturesSharedPreferences,
                Context.MODE_PRIVATE).getAll();
        return new ArrayList<>(mediasMap.keySet());
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

}

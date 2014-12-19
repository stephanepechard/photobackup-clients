package fr.s13d.photobackup;

import com.orm.SugarRecord;

import java.io.File;
import java.util.Date;

public class PBPicture extends SugarRecord<PBPicture> {
    private final static String LOG_TAG = "PBPicture";
    private File file;
    private Date date;
    private Boolean uploaded;

    //////////////////
    // Constructors //
    //////////////////
    // necessary for SugarORM, see: https://guides.codepath.com/android/Clean-Persistence-with-Sugar-ORM
    //public PBPicture() {}

    public PBPicture(File file) {
        this.file = file;
        this.date = new Date();
        this.uploaded = Boolean.FALSE;
    }


    ////////////
    // Methods//
    ////////////
    @Override
    public void save() {
        Log.i("PBPicture", "saving " + this.toString());
        if (this.file == null) {
            Log.e(LOG_TAG, "PBPicture has no File!!");
        } else {
            super.save();
        }
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return file.getName();
    }

    public void uploadDidSucceed() {
        this.uploaded = Boolean.TRUE;
    }


    /////////////////////////////////////////
    // Getters/Setters are the Java fun... //
    /////////////////////////////////////////
    public File getFile() {
        return file;
    }

    public Date getDate() {
        return date;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

}

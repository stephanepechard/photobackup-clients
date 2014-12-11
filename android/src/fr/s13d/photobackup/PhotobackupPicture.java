package fr.s13d.photobackup;

import com.orm.SugarRecord;

import java.io.File;

public class PhotoBackupPicture extends SugarRecord<PhotoBackupPicture> {
    File file;
//	String date;
//    enum State { ON_, BACKUPED_UP };


    public PhotoBackupPicture(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

/*	public void setId(long newId) {
		id = newId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String newDate) {
		date = newDate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String newFilename) {
		filename = newFilename;
	}

	public long getUploaded() {
		return uploaded;
	}

	public void setUploaded(long newUploaded) {
		uploaded = newUploaded;
	}
*/

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return file.getName();
	}

}

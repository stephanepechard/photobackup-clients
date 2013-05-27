package fr.s13d.photobackup.journal;


public class JournalEntry {
	private long id = 0;
	private String date = null;
	private String filename = null;
	private long uploaded = 0;

	public long getId() {
		return id;
	}

	public void setId(long newId) {
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


	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return filename;
	}

}

package fr.s13d.photobackup.journal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class JournalEntriesDataSource {
	private static final String TAG = "JournalEntriesDataSource";

	// Database fields
	private SQLiteDatabase database;
	private final JournalEntryOpenHelper dbHelper;
	private final String[] allColumns = {
			JournalEntryOpenHelper.COLUMN_ID,
			JournalEntryOpenHelper.COLUMN_DATE,
			JournalEntryOpenHelper.COLUMN_FILENAME,
			JournalEntryOpenHelper.COLUMN_UPLOADED };

	public JournalEntriesDataSource(Context context) {
		dbHelper = new JournalEntryOpenHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public JournalEntry createEntry(String date, String filename, long uploaded) {
		ContentValues values = new ContentValues();
		values.put(JournalEntryOpenHelper.COLUMN_DATE, date);
		values.put(JournalEntryOpenHelper.COLUMN_FILENAME, filename);
		values.put(JournalEntryOpenHelper.COLUMN_UPLOADED, uploaded);

		long insertId = database.insert(JournalEntryOpenHelper.TABLE_ENTRIES, null, values);
		Cursor cursor = database.query(JournalEntryOpenHelper.TABLE_ENTRIES,
				allColumns, JournalEntryOpenHelper.COLUMN_ID + " = " + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		JournalEntry newEntry = cursorToEntry(cursor);
		cursor.close();
		Log.v(TAG, "new entry: " + newEntry.toString());
		return newEntry;
	}

	public void deleteEntry(JournalEntry entry) {
		long id = entry.getId();
		Log.v(TAG, "Deleted id: " + id);
		database.delete(JournalEntryOpenHelper.TABLE_ENTRIES,
				JournalEntryOpenHelper.COLUMN_ID + " = " + id, null);
	}

	public List<JournalEntry> getAllEntries() {
		List<JournalEntry> entries = new ArrayList<JournalEntry>();

		Cursor cursor = database.query(JournalEntryOpenHelper.TABLE_ENTRIES,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			JournalEntry entry = cursorToEntry(cursor);
			entries.add(entry);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return entries;
	}

	private JournalEntry cursorToEntry(Cursor cursor) {
		JournalEntry entry = new JournalEntry();
		entry.setId(cursor.getLong(0));
		entry.setDate(cursor.getString(1));
		entry.setFilename(cursor.getString(2));
		entry.setUploaded(cursor.getLong(3));
		return entry;
	}
}

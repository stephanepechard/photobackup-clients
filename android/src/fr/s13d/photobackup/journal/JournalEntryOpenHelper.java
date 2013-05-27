package fr.s13d.photobackup.journal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class JournalEntryOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "JournalEntries.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_ENTRIES = "entries";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_FILENAME = "filename";
	public static final String COLUMN_UPLOADED = "uploaded";

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ENTRIES + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_DATE + " text not null, "
			+ COLUMN_FILENAME + " text not null, "
			+ COLUMN_UPLOADED + " integer default '0');";


	public JournalEntryOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(JournalEntryOpenHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data.");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
		onCreate(db);
	}

}

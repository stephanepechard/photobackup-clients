package fr.s13d.photobackup.journal;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import fr.s13d.photobackup.R;

public class JournalActivity extends ListActivity {
	private JournalEntriesDataSource datasource;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		datasource = new JournalEntriesDataSource(this);
		datasource.open();

		List<JournalEntry> values = datasource.getAllEntries();

		// Use the SimpleCursorAdapter to show the elements in a ListView
		//		ArrayAdapter<JournalEntry> adapter = new ArrayAdapter<JournalEntry>(this,
		//				android.R.layout.simple_list_item_1, values);
		JournalAdapter adapter = new JournalAdapter(this, values);
		setListAdapter(adapter);
	}


	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}
}

/*package fr.s13d.photobackup.journal;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;

import fr.s13d.photobackup.PhotobackupPicture;
import fr.s13d.photobackup.R;

public class JournalActivity extends ListActivity {
	private JournalEntriesDataSource datasource;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		datasource = new JournalEntriesDataSource(this);
		datasource.open();

		List<PhotobackupPicture> values = datasource.getAllEntries();
		JournalAdapter adapter = new JournalAdapter(this, values, getResources());
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
*/
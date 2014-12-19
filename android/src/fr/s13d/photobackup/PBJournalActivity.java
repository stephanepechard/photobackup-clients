package fr.s13d.photobackup;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.List;

public class PBJournalActivity extends ListActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

        List<PBPicture> pictures = PBPicture.listAll(PBPicture.class);
		PBJournalAdapter adapter = new PBJournalAdapter(this, pictures, getResources());
		setListAdapter(adapter);
	}

}

package fr.s13d.photobackup;

        import android.app.ListActivity;
import android.os.Bundle;

public class PBJournalActivity extends ListActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        PBJournalAdapter adapter = new PBJournalAdapter(this);
        setListAdapter(adapter);
    }

}

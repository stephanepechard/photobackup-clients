package fr.s13d.photobackup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class PBJournalActivity extends ListActivity {

    private PBMediaSender mediaSender;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);


        // on click listener
        final Activity self = this;
        mediaSender = new PBMediaSender(this);
        final ListView listView = (ListView)findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final PBMedia media = PBActivity.getMediaStore().getMedias().get(position);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(self);
                    builder.setMessage("You can backup this picture now!").setTitle("Manual backup");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mediaSender.send(media);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().show();
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        // adapter
        final PBJournalAdapter adapter = new PBJournalAdapter(this);
        setListAdapter(adapter);
    }

}

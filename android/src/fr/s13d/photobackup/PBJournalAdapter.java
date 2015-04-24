package fr.s13d.photobackup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;


public class PBJournalAdapter extends BaseAdapter {
    private final List<PBMedia> medias;
	private static LayoutInflater inflater;
    private Context context = null;

	public PBJournalAdapter(final Activity activity) {
        medias = PBActivity.mediaStore.getMedias();
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = activity;
	}


	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(view == null) {
            Log.w("PBJournalAdapter", "inflater.inflate");
			view = inflater.inflate(R.layout.list_row, parent, false);
		}
        final PBMedia media = medias.get(position);
        if (media == null) {
            Log.w("PBJournalAdapter", "media is null");
            return view;
        }

        // thumbnail
		final ImageView thumbImageView = (ImageView)view.findViewById(R.id.thumbnail);
        // set a resource to show something nice in recycled views
        thumbImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        final PBThumbnailTask task = new PBThumbnailTask(context, thumbImageView);
        task.execute(media.getId());

		// filename
		final TextView textView = (TextView)view.findViewById(R.id.filename);
        if (media.getPath() != null) {
            final File file = new File(media.getPath());
            textView.setText(file.getName());
        } else {
            textView.setText("Error on picture data");
        }

		// indicator
        final ImageView imageView = (ImageView)view.findViewById(R.id.state);
        if (media.getState() == PBMedia.PBMediaState.WAITING) {
            imageView.setImageResource(android.R.drawable.presence_away);
        } else if (media.getState() == PBMedia.PBMediaState.SYNCED) {
            imageView.setImageResource(android.R.drawable.presence_online);
        } else if (media.getState() == PBMedia.PBMediaState.ERROR) {
            imageView.setImageResource(android.R.drawable.presence_busy);
        }

		return view;
	}

    @Override
    public int getCount() {
        return medias.size();
    }

    @Override
    public Object getItem(final int position) {
        return medias.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

}

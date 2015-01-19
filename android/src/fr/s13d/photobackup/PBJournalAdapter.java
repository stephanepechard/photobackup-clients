package fr.s13d.photobackup;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;


public class PBJournalAdapter extends BaseAdapter {
    private final List<String> medias;
	private static LayoutInflater inflater;
	private Resources resources = null;

	public PBJournalAdapter(Activity activity) {
        medias = PBActivity.mediaStore.getMediaIds();
		resources = activity.getResources();
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public int getCount() {
        return medias.size();
	}

	@Override
	public Object getItem(int position) {
		return medias.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(view == null) {
			view = inflater.inflate(R.layout.list_row, null);
		}
        String mediaIdString = medias.get(position);
        int mediaId = Integer.parseInt(mediaIdString);
        PBMedia media = PBActivity.mediaStore.getMedia(mediaId);

		// thumbnail
		ImageView thumbImageView = (ImageView)view.findViewById(R.id.thumbnail);
		if (BitmapWorkerTask.cancelPotentialWork(media.getPath(), thumbImageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(thumbImageView, resources, view);
			Bitmap placeholderBitmap = null;
			final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, placeholderBitmap, task);
			thumbImageView.setImageDrawable(asyncDrawable);
			task.execute(media.getPath());
		}

		// filename
		TextView textView = (TextView)view.findViewById(R.id.filename);
        if (media.getPath() != null) {
            File file = new File(media.getPath());
            textView.setText(file.getName());
        } else {
            textView.setText("Error on picture data");
        }

		// indicator
		ImageView imageView = (ImageView)view.findViewById(R.id.state);
        if (media.getState() == PBMedia.PBMediaState.WAITING) {
            imageView.setImageResource(android.R.drawable.presence_away);
        } else if (media.getState() == PBMedia.PBMediaState.SYNCED) {
            imageView.setImageResource(android.R.drawable.presence_online);
        } else if (media.getState() == PBMedia.PBMediaState.ERROR) {
            imageView.setImageResource(android.R.drawable.presence_busy);
        }

		return view;
	}

}

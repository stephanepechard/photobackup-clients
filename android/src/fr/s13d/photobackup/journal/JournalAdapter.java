package fr.s13d.photobackup.journal;

import java.io.File;
import java.util.List;

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
import fr.s13d.photobackup.R;

public class JournalAdapter extends BaseAdapter {
	private final Activity activity;
	private final List<JournalEntry> values;
	private static LayoutInflater inflater=null;
	private Resources resources = null;

	public JournalAdapter(Activity newActivity, List<JournalEntry> newValues, Resources newResources) {
		activity = newActivity;
		values = newValues;
		resources = newResources;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Object getItem(int position) {
		return values.get(position);
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
		JournalEntry entry = values.get(position);

		// thumbnail
		ImageView thumbImageView = (ImageView)view.findViewById(R.id.thumbnail);
		if (BitmapWorkerTask.cancelPotentialWork(entry, thumbImageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(thumbImageView, resources, view);
			Bitmap placeholderBitmap = null;
			final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, placeholderBitmap, task);
			thumbImageView.setImageDrawable(asyncDrawable);
			task.execute(entry);
		}

		// filename
		TextView textView = (TextView)view.findViewById(R.id.filename);
		File file = new File(entry.getFilename());
		textView.setText(file.getName());

		// error
		ImageView errorImageView = (ImageView)view.findViewById(R.id.error);
		if (entry.getUploaded() == 1) {
			errorImageView.setVisibility(View.INVISIBLE);
		} else {
			errorImageView.setVisibility(View.VISIBLE);
		}

		return view;
	}

}

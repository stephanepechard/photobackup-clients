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

import java.util.List;


public class PBJournalAdapter extends BaseAdapter {
	private final List<PBPicture> pictures;
	private static LayoutInflater inflater = null;
	private Resources resources = null;

	public PBJournalAdapter(Activity newActivity, List<PBPicture> newPictures, Resources newResources) {
		pictures = newPictures;
		resources = newResources;
		inflater = (LayoutInflater)newActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public int getCount() {
        return pictures.size();
	}

	@Override
	public Object getItem(int position) {
		return pictures.get(position);
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
        PBPicture picture = pictures.get(position);

		// thumbnail
		ImageView thumbImageView = (ImageView)view.findViewById(R.id.thumbnail);
		if (BitmapWorkerTask.cancelPotentialWork(picture, thumbImageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(thumbImageView, resources, view);
			Bitmap placeholderBitmap = null;
			final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, placeholderBitmap, task);
			thumbImageView.setImageDrawable(asyncDrawable);
			task.execute(picture);
		}

		// filename
		TextView textView = (TextView)view.findViewById(R.id.filename);
        if (picture.getFile().getName() != null) {
            textView.setText(picture.getFile().getName());
        }

		// error
		ImageView errorImageView = (ImageView)view.findViewById(R.id.error);
		if (picture.getUploaded()) {
			errorImageView.setVisibility(View.INVISIBLE);
		} else {
			errorImageView.setVisibility(View.VISIBLE);
		}

		return view;
	}

}

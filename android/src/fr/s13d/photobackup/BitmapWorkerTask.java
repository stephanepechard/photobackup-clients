package fr.s13d.photobackup;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	private Resources resources = null;
	//private ProgressBar progressBar = null;
	public String picturePath = null;


	public BitmapWorkerTask(ImageView imageView, Resources newResources, View view) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<>(imageView);
		resources = newResources;
		//progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		//progressBar.setVisibility(View.VISIBLE);
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		picturePath = params[0];
        if (picturePath != null) {
            Bitmap fullResolutionPicture = BitmapFactory.decodeFile(picturePath);
            Bitmap thumbnailPicture;
            if (fullResolutionPicture == null) { // picture is absent
                thumbnailPicture = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_gallery);
            } else {
                thumbnailPicture = ThumbnailUtils.extractThumbnail(fullResolutionPicture, 90, 90);
            }
            return thumbnailPicture;
        }
        return null;
	}


	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (!isCancelled()) {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			if ((this == bitmapWorkerTask) && (imageView != null)) {
				imageView.setImageBitmap(bitmap);
				//progressBar.setVisibility(View.INVISIBLE);
			}
		}
	}


	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}


	public static boolean cancelPotentialWork(String entry, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.picturePath;
			if (bitmapData != null && entry != null && bitmapData.equals(entry)) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}
}


final class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
		super(res, bitmap);
		bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
	}

	public BitmapWorkerTask getBitmapWorkerTask() {
		return bitmapWorkerTaskReference.get();
	}
}

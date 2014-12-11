/*package fr.s13d.photobackup.journal;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import fr.s13d.photobackup.PhotobackupPicture;
import fr.s13d.photobackup.R;

class BitmapWorkerTask extends AsyncTask<PhotobackupPicture, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	private Resources resources = null;
	private ProgressBar progressBar = null;
	public PhotobackupPicture entry = null;

	public BitmapWorkerTask(ImageView imageView, Resources newResources, View view) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		resources = newResources;
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(PhotobackupPicture... params) {
		entry = params[0];
		Bitmap fullResolutionPicture = BitmapFactory.decodeFile(entry.getFilename());
		Bitmap thumbnailPicture = null;
		if (fullResolutionPicture == null) { // picture is absent
			thumbnailPicture = BitmapFactory.decodeResource(resources, R.drawable.navigation_cancel);
		} else {
			thumbnailPicture = ThumbnailUtils.extractThumbnail(fullResolutionPicture, 90, 90);
		}
		return thumbnailPicture;
	}


	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
			bitmap = null;
		}

		if ((imageViewReference != null) && (bitmap != null)) {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			if ((this == bitmapWorkerTask) && (imageView != null)) {
				imageView.setImageBitmap(bitmap);
				progressBar.setVisibility(View.INVISIBLE);
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


	public static boolean cancelPotentialWork(PhotobackupPicture entry, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final PhotobackupPicture bitmapData = bitmapWorkerTask.entry;
			if (bitmapData != entry) {
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
		bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	}

	public BitmapWorkerTask getBitmapWorkerTask() {
		return bitmapWorkerTaskReference.get();
	}
}
*/
package fr.s13d.photobackup;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


class PBThumbnailTask extends AsyncTask<Integer, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
    private Context context;


	public PBThumbnailTask(Context theContext, ImageView imageView) {
		imageViewReference = new WeakReference<>(imageView);
        context = theContext;
	}


	@Override
	protected Bitmap doInBackground(Integer... params) {
        return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                params[0], MediaStore.Images.Thumbnails.MINI_KIND, null);
	}


	@Override
	protected void onPostExecute(final Bitmap bitmap) {
		if (!isCancelled()) {
            final ImageView imageView = imageViewReference.get();
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
		}
	}

}

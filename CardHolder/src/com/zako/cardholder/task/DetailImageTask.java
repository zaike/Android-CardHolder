package com.zako.cardholder.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class DetailImageTask extends AsyncTask<byte[], Void, Bitmap> {

	String id;
	float width;
	float height;
	float imageWidth;
	float imageHeight;
	byte[] blob;
	Matrix matrix;
	ImageView imageView;
	ProgressBar loading;

	public DetailImageTask(ImageView imageView, ProgressBar loading, String id, int width, int height, Matrix matrix) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.imageView = imageView;
		this.loading = loading;
		this.matrix = matrix;
	}

	@Override
	protected Bitmap doInBackground(byte[]... params) {
		BitmapFactory.Options opt = null;
		Bitmap bmp = null;
		
		blob = params[0];
		
		opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		
		// サイズ情報のみロード
		BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);

		this.imageWidth = opt.outWidth;
		this.imageHeight = opt.outHeight;
		
		opt.inJustDecodeBounds = false;

		this.matrix.postScale((float)(this.width / this.imageWidth), (float)(this.height / this.imageHeight));

		bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (this.id.equals(imageView.getTag())) {
			this.imageView.setImageBitmap(bitmap);

			this.imageView.setImageMatrix(matrix);
			
			this.imageView.setVisibility(View.VISIBLE);
			this.loading.setVisibility(View.GONE);

			AlphaAnimation anim = new AlphaAnimation(0, 1);
			anim.setDuration(300);
			anim.setFillAfter(true);
			this.imageView.startAnimation(anim);
		}
	}
}

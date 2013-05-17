package com.example.cardholder.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ListBitmapTask extends AsyncTask<byte[], Void, Bitmap> {
	
	String id;
	int width;
	int height;
	byte[] blob;
	ImageView imageView;
	ProgressBar loading;

	public ListBitmapTask(ImageView imageView, ProgressBar loading, String id) {
		this.id = id;
		width = imageView.getWidth();
		height = imageView.getHeight();
		this.imageView = imageView;
		this.loading = loading;
	}

	@Override
	protected Bitmap doInBackground(byte[]... params) {

		int orgWidth = 0;
		int orgHeight = 0;
		int scale = 0;
		BitmapFactory.Options opt = null;
		Bitmap bmp = null;

		blob = params[0];

		opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		
		// サイズ情報のみロード
		BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);
		
		orgWidth = opt.outWidth;
		orgHeight = opt.outHeight;
/*
		if (width < orgWidth || height < orgHeight) {
			scale = Math.max(orgWidth, orgHeight) / Math.min(width, height) + 1;
			opt.inSampleSize = scale;
		}
*/
		opt.inJustDecodeBounds = false;
		
		bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (this.id.equals(imageView.getTag())) {
			imageView.setImageBitmap(bitmap);
			this.imageView.setVisibility(View.VISIBLE);
			this.loading.setVisibility(View.GONE);
		}
	}
}

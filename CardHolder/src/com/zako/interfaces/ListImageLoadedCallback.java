package com.zako.interfaces;

import android.graphics.Bitmap;
import android.widget.ProgressBar;

public interface ListImageLoadedCallback {
	public void onListImageLoaded(Bitmap bitmap, ProgressBar loading, String id);
}

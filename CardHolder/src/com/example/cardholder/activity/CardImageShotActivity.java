package com.example.cardholder.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardholder.R;
import com.zako.view.CameraPreviewView;

public class CardImageShotActivity extends Activity implements OnTouchListener, AutoFocusCallback, Camera.PictureCallback {

	FrameLayout preview;

	// カメラインスタンス
	private Camera camera;

	// カメラプレビュークラス
	private CameraPreviewView cameraPreview;

	int viewWidth = 0;
	int viewHeight = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// タイトルを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_card_shot);

		findViews();

		cameraInit();
		
		setListener();
	}

	protected void findViews() {

		int wWidth = 0;
		int wHeight = 0;

		Rect rect = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);

		int height = 0;

		DisplayMetrics dMatrix = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dMatrix);

		switch (dMatrix.densityDpi) {
		case DisplayMetrics.DENSITY_HIGH:
			height = 38;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			height = 25;
			break;
		case DisplayMetrics.DENSITY_LOW:
			height = 19;
			break;
		default:
			height = 25;
			break;
		}

		if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
			wWidth = wm.getDefaultDisplay().getWidth();
			wHeight = wm.getDefaultDisplay().getHeight() - (height * 2);
		} else {
			Point size = new Point();
			wm.getDefaultDisplay().getSize(size);
			wWidth = size.x;
			wHeight = size.y - (height * 2);
		}

		viewWidth = (int)(wHeight / 5.4f * 8.5f);
		viewHeight = wHeight;

		this.preview = (FrameLayout)findViewById(R.id.preview);

		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(viewWidth, viewHeight);
		this.preview.setLayoutParams(layoutParam);
		
		View toastView = getLayoutInflater().inflate(R.layout.custom_toast_layout, (ViewGroup)findViewById(R.id.customToastLayout));

		Toast toast = new Toast(this);
		TextView text = (TextView)toastView.findViewById(R.id.textToast);
		text.setText("画面タッチで撮影します。");
		toast.setView(toastView);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.show();
//		Toast.makeText(this, "画面タッチで撮影します。", Toast.LENGTH_LONG).show();
	}

	protected void cameraInit() {
		
		// カメラインスタンス取得
		try {
			camera = Camera.open();

			Camera.Parameters parameters = camera.getParameters();
			List<Camera.Size> availablePrevSizes = parameters.getSupportedPreviewSizes();
			Camera.Size prvSize = availablePrevSizes.get(0);
			parameters.setPreviewSize(prvSize.width, prvSize.height);
			
			List<Camera.Size> availablePictSizes = parameters.getSupportedPictureSizes();
			Camera.Size pctSize = availablePictSizes.get(0);
			
			for (Camera.Size size:availablePictSizes) {
				if (1280 >= Math.max(size.width, size.height)) {
					pctSize = size;
					break;
				}
			}
			parameters.setPictureSize(pctSize.width, pctSize.height);
			camera.setParameters(parameters);
			
//			camParam.setPictureSize(viewWidth * 2, viewHeight * 2);
//			this.camera.setParameters(camParam);

		} catch (Exception e) {
			setResult(RESULT_CANCELED);
			this.finish();
		}

		this.cameraPreview = new CameraPreviewView(this, this.camera);
		this.preview.addView(this.cameraPreview);
	}
	
	protected void setListener() {
		this.cameraPreview.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.camera.autoFocus(this);
		}
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO 自動生成されたメソッド・スタブ
		// ここでプレビュー中画像を撮影し、byteデータをintentに突っ込んで、画面終了
		this.camera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		Intent intent = new Intent();
		intent.putExtra("imageData", data);

		setResult(RESULT_OK, intent);
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
	
	@Override 
	protected void onDestroy() {
		super.onDestroy();

		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
}

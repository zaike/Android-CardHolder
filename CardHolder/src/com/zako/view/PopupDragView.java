package com.zako.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class PopupDragView extends ImageView {
	private static final Bitmap.Config DRAG_BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
	private static final int BACKGROUND_COLOR = Color.argb(0, 0x00, 0x00, 0x00);
	private static final int Y_GAP = 20;

	private WindowManager windowManager;
	private WindowManager.LayoutParams layoutParams;
	private boolean dragging = false;
	private int baseX;
	private int baseY;
	private int[] itemLocation = new int[2];

	public PopupDragView(Context context) {
		super(context);
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		// レイアウトを初期化する
		initLayoutParams();
	}

	/**
	 * ドラッグ開始
	 *
	 * @param itemView
	 */
	public void startDrag(int x, int y, View itemView) {
		// ドラッグ終了処理が未完了の場合、前回のドラッグ処理が不正に終了しているかもしれない。
		// 念のため、ドラッグ終了処理を行う。
		if (dragging) {
			stopDrag();
		}

		// ドラッグ開始座標を保持する
		baseX = x;
		baseY = y;

		// ドラッグする項目の初期位置を保持する
		itemView.getLocationInWindow(itemLocation);

		// ドラッグ中の画像イメージを設定する
		setBitmap(itemView);

		// WindowManagerに登録する
		updateLayoutParams(x, y);
		layoutParams.height = (int)(itemView.getHeight() * 1.5);
		layoutParams.width = (int)(itemView.getWidth() * 1.5);
		
//		layoutParams.height = itemView.getHeight() * 2;

		windowManager.addView(this, layoutParams);
		dragging = true;
	}

	/**
	 * ドラッグ中処理
	 *
	 * @param x
	 * @param y
	 */
	public void doDrag(int x, int y) {
		// ドラッグ開始していなければ中止
		if (dragging == false) {
			return;
		}

		// ImageViewの位置を更新
		updateLayoutParams(x, y);
		windowManager.updateViewLayout(this, layoutParams);
	}

	/**
	 * ドラッグ項目の描画を終了する
	 */
	public void stopDrag() {
		// ドラッグ開始していなければ中止
		if (dragging == false) {
			return;
		}

		// WindowManagerから除去する
		windowManager.removeView(this);
		dragging = false;
	}

	/**
	 * ドラッグ中の項目を表す画像を作成する
	 */
	private void setBitmap(View itemView) {
		Bitmap bitmap = Bitmap.createBitmap(itemView.getWidth(),
				itemView.getHeight(), DRAG_BITMAP_CONFIG);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap);
		itemView.draw(canvas);
		setImageBitmap(bitmap);
		setBackgroundColor(BACKGROUND_COLOR);
	}

	/**
	 * ImageView 用 LayoutParams の初期化
	 */
	private void initLayoutParams() {
		// getLocationInWindow()と座標系を合わせるためFLAG_LAYOUT_IN_SCREENを設定する。
		// FLAG_LAYOUT_IN_SCREENを設定すると、端末ディスプレイ全体の左上を原点とする座標系となる。
		// 設定しない場合、ステータスバーを含まない左上を原点とする。
		layoutParams = new WindowManager.LayoutParams();
		layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		layoutParams.format = PixelFormat.TRANSLUCENT;
		layoutParams.windowAnimations = 0;
	}

	/**
	 * ImageView 用 LayoutParams の座標情報を更新
	 */
	private void updateLayoutParams(int x, int y) {
		// ドラッグ中であることが分かるように少し上にずらす
		layoutParams.x = itemLocation[0] + x - baseX;
		layoutParams.y = itemLocation[1] + y - baseY - Y_GAP;
	}
}

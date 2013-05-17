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

		// ���C�A�E�g������������
		initLayoutParams();
	}

	/**
	 * �h���b�O�J�n
	 *
	 * @param itemView
	 */
	public void startDrag(int x, int y, View itemView) {
		// �h���b�O�I���������������̏ꍇ�A�O��̃h���b�O�������s���ɏI�����Ă��邩������Ȃ��B
		// �O�̂��߁A�h���b�O�I���������s���B
		if (dragging) {
			stopDrag();
		}

		// �h���b�O�J�n���W��ێ�����
		baseX = x;
		baseY = y;

		// �h���b�O���鍀�ڂ̏����ʒu��ێ�����
		itemView.getLocationInWindow(itemLocation);

		// �h���b�O���̉摜�C���[�W��ݒ肷��
		setBitmap(itemView);

		// WindowManager�ɓo�^����
		updateLayoutParams(x, y);
		layoutParams.height = (int)(itemView.getHeight() * 1.5);
		layoutParams.width = (int)(itemView.getWidth() * 1.5);
		
//		layoutParams.height = itemView.getHeight() * 2;

		windowManager.addView(this, layoutParams);
		dragging = true;
	}

	/**
	 * �h���b�O������
	 *
	 * @param x
	 * @param y
	 */
	public void doDrag(int x, int y) {
		// �h���b�O�J�n���Ă��Ȃ���Β��~
		if (dragging == false) {
			return;
		}

		// ImageView�̈ʒu���X�V
		updateLayoutParams(x, y);
		windowManager.updateViewLayout(this, layoutParams);
	}

	/**
	 * �h���b�O���ڂ̕`����I������
	 */
	public void stopDrag() {
		// �h���b�O�J�n���Ă��Ȃ���Β��~
		if (dragging == false) {
			return;
		}

		// WindowManager���珜������
		windowManager.removeView(this);
		dragging = false;
	}

	/**
	 * �h���b�O���̍��ڂ�\���摜���쐬����
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
	 * ImageView �p LayoutParams �̏�����
	 */
	private void initLayoutParams() {
		// getLocationInWindow()�ƍ��W�n�����킹�邽��FLAG_LAYOUT_IN_SCREEN��ݒ肷��B
		// FLAG_LAYOUT_IN_SCREEN��ݒ肷��ƁA�[���f�B�X�v���C�S�̂̍�������_�Ƃ�����W�n�ƂȂ�B
		// �ݒ肵�Ȃ��ꍇ�A�X�e�[�^�X�o�[���܂܂Ȃ���������_�Ƃ���B
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
	 * ImageView �p LayoutParams �̍��W�����X�V
	 */
	private void updateLayoutParams(int x, int y) {
		// �h���b�O���ł��邱�Ƃ�������悤�ɏ�����ɂ��炷
		layoutParams.x = itemLocation[0] + x - baseX;
		layoutParams.y = itemLocation[1] + y - baseY - Y_GAP;
	}
}

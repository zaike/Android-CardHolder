package com.zako.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.zako.custom.adapter.DragListAdapter;

public class DragListView extends ListView implements AdapterView.OnItemLongClickListener {

	private static final int SCROLL_SPEED_FAST = 50;
	private static final int SCROLL_SPEED_SLOW = 25;

	private DragListAdapter adapter;
	private PopupDragView popupView;
	private MotionEvent downEvent;
	private boolean dragging = false;

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		popupView = new PopupDragView(context);
		setOnItemLongClickListener(this);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (adapter instanceof DragListAdapter == false) {
			throw new RuntimeException("引数adapterがDragListAdapterクラスではありません。");
		}
		super.setAdapter(adapter);
		this.adapter = (DragListAdapter) adapter;
	}

	/**
	 * 長押しイベント<br>
	 * ドラッグを開始する。当イベントの前に、タッチイベント（ACTION_DOWN）が呼ばれている前提。
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return startDrag(downEvent, position);
	}

	/**
	 * タッチイベント<br>
	 * ドラッグしている項目の移動や、ドラッグ終了の制御を行う。
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			storeMotionEvent(event);
			break;
		case MotionEvent.ACTION_MOVE:
			result = doDrag(event);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			result = stopDrag(event);	
			break;
		}

		// イベントを処理していなければ、親のイベント処理を呼ぶ。
		// 長押しイベントを発生させるため、ACTION_DOWNイベント時は、親のイベント処理を呼ぶ。
		if (result == false) {
			result = super.onTouchEvent(event);
		}
		return result;
	}

	/**
	 * 長押しイベント時に、タッチ位置を取得するため、ACTION_DOWN時のMotionEventを保持する。
	 */
	private void storeMotionEvent(MotionEvent event) {
		downEvent = event;
	}

	/**
	 * ドラッグ開始
	 */
	private boolean startDrag(MotionEvent event, int position) {
		dragging = false;
		int x = (int) event.getX();
		int y = (int) event.getY();

		if (position < 0) {
			return false;
		}

		// アダプターにドラッグ対象項目位置を渡す
		adapter.startDrag(position);

		// ドラッグ中のリスト項目の描画を開始する
		popupView.startDrag(x, y, getChildByIndex(position));
		
		// リストビューを再描画する
		invalidateViews();
		dragging = true;
		return true;
	}

	/**
	 * ドラッグ処理
	 */
	private boolean doDrag(MotionEvent event) {
		if (!dragging) {
			return false;
		}
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		int position = pointToPosition(x, y);

		// ドラッグの移動先リスト項目が存在する場合
		if (position != AdapterView.INVALID_POSITION) {
			// アダプターのデータを並び替える
			adapter.doDrag(position);
		}

		// ドラッグ中のリスト項目の描画を更新する
		popupView.doDrag(x, y);
		
		// リストビューを再描画する
		invalidateViews();
		
		// 必要あればスクロールさせる
		// 注意：invalidateViews()後に処理しないとスクロールしなかった
		setScroll(event);
		return true;
	}

	/**
	 * ドラッグ終了
	 */
	private boolean stopDrag(MotionEvent event) {
		if (!dragging) {
			return false;
		}

		// アダプターにドラッグ対象なしを渡す
		adapter.stopDrag();

		// ドラッグ中のリスト項目の描画を終了する
		popupView.stopDrag();

		// リストビューを再描画する
		invalidateViews();
		dragging = false;
		return true;
	}

	/**
	 * 必要あればスクロールさせる。<br>
	 * 座標の計算が煩雑になるので当Viewのマージンとパディングはゼロの前提とする。
	 */
	private void setScroll(MotionEvent event) {
		int y = (int) event.getY();
		int height = getHeight();
		int harfHeight = height / 2;
		int harfWidth = getWidth() / 2;

		// スクロール速度の決定
		int speed;
		int fastBound = height / 9;
		int slowBound = height / 4;
		if (event.getEventTime() - event.getDownTime() < 300) {
			// ドラッグの開始から500ミリ秒の間はスクロールしない
			speed = 0;
		} else if (y < slowBound) {
			speed = y < fastBound ? -SCROLL_SPEED_FAST : -SCROLL_SPEED_SLOW;
		} else if (y > height - slowBound) {
			speed = y > height - fastBound ? SCROLL_SPEED_FAST : SCROLL_SPEED_SLOW;
		} else {
			// スクロールなしのため処理終了
			return;
		}
		
		// 画面の中央にあるリスト項目位置を求める
		// 横方向はとりあえず考えない
		// 中央がちょうどリスト項目間の境界の場合は、位置が取得できないので、
		// 境界からずらして再取得する。
		int middlePosition = pointToPosition(harfWidth, harfHeight);
		if (middlePosition == AdapterView.INVALID_POSITION) {
			middlePosition = pointToPosition(harfWidth, harfHeight + getDividerHeight());
		}

		// スクロール実施
		final View middleView = getChildByIndex(middlePosition);
		if (middleView != null) {
			setSelectionFromTop(middlePosition, middleView.getTop() - speed);
		}
	}

	/**
	 * 指定インデックスのView要素を取得する
	 */
	private View getChildByIndex(int index) {
		return getChildAt(index - getFirstVisiblePosition());
	}
}
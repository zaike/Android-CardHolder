package com.zako.custom.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zako.cardholder.R;
import com.zako.cardholder.task.ListBitmapTask;
import com.zako.custom.object.CardInfo;
import com.zako.interfaces.DragListViewCallback;

public class DragListAdapter extends ArrayAdapter<CardInfo> {
	
	private DragListViewCallback mCallback;
	
	private List<CardInfo> items;
	private LayoutInflater inflater;
	private int viewResourceId;
	private CardInfo draggingItem;

	private int animItemIndex = -1;
	private boolean animFlg = false;

	private Context context;
	private int currentPosition = -1;

	public DragListAdapter(Context context, int resId, List<CardInfo> items, DragListViewCallback callback) {
		super(context, resId, items);
		this.mCallback = callback;
		this.context = context;
		this.items = items;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.viewResourceId = resId;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public CardInfo getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * ���X�g���ڂ�View���擾����
	 */
	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			v = inflater.inflate(this.viewResourceId, null);
		}

		CardInfo cardInfo = (CardInfo)items.get(position);

		// �J�[�h���̂��Z�b�g
		TextView cardInfoName = (TextView)v.findViewById(R.id.cardName);
		cardInfoName.setText(cardInfo.getCardName());

		// �J�[�hID���Z�b�g
		TextView cardInfoId = (TextView)v.findViewById(R.id.cardId);
		cardInfoId.setText(cardInfo.getCardId());
		
		// �J�[�h�������Z�b�g
		TextView cardInfoMemo = (TextView)v.findViewById(R.id.cardMemo);
		cardInfoMemo.setText(cardInfo.getCardMemo());
		
		// �J�[�h�X�V�����Z�b�g
		TextView cardInfoModDate = (TextView)v.findViewById(R.id.cardModDate);
		cardInfoModDate.setText(cardInfo.getCardModDate());
		
		// �{�^�����Z�b�g
		Button button = (Button)v.findViewById(R.id.buttonEdit);

		// �Z�����Z�b�g
		LinearLayout cell = (LinearLayout)v.findViewById(R.id.cell);

		if (button.getTag() == null) {
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int pos = (Integer)v.getTag();
				    ((ListView)parent).performItemClick(v, pos, v.getId());
				}
			});
		}

		if (this.animFlg) {

			if (this.animItemIndex == position) {

				cardInfoName.setVisibility(View.VISIBLE);

				button.setVisibility(View.VISIBLE);

				ScaleAnimation scale = new ScaleAnimation(1.5f, 1f, 1.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scale.setDuration(150);

				cell.startAnimation(scale);

				this.animFlg = false;
				this.animItemIndex = -1;

				this.mCallback.onDragExit();
			} else {
				cardInfoName.setVisibility(View.VISIBLE);

				button.setVisibility(View.VISIBLE);
			}
		} else {
			if (this.currentPosition == -1) {
				cardInfoName.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);
			} else {
				if (this.currentPosition == position) {
					cardInfoName.setVisibility(View.INVISIBLE);
					button.setVisibility(View.INVISIBLE);
				} else {
					cardInfoName.setVisibility(View.VISIBLE);
					button.setVisibility(View.VISIBLE);
				}
			}
		}
		button.setTag((Integer)position);

		return v;
	}

	/**
	 * �h���b�O�J�n
	 *
	 * @param position
	 */
	public void startDrag(int position) {
		Log.d("startDrag = ", position + ", currentPosition = " + currentPosition);
		this.currentPosition = position;
		this.draggingItem = items.get(currentPosition);
	}

	/**
	 * �h���b�O�ɏ]���ăf�[�^����ёւ���
	 *
	 * @param newPosition
	 */
	public void doDrag(int newPosition) {
		if (currentPosition < newPosition) {
			for (int i = currentPosition; i < newPosition; i++) {
				items.set(i, items.get(i + 1));
			}
		} else if (currentPosition > newPosition) {
			for (int i = currentPosition; i > newPosition; i--) {
				items.set(i, items.get(i - 1));
			}
		}
		currentPosition = newPosition;
	}

	/**
	 * �h���b�O�I��
	 */
	public void stopDrag() {
		items.set(currentPosition, this.draggingItem);
		this.animItemIndex = currentPosition;
		this.animFlg = true;
		this.draggingItem = null;
		this.currentPosition = -1;
	}
}
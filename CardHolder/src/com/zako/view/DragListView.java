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
			throw new RuntimeException("����adapter��DragListAdapter�N���X�ł͂���܂���B");
		}
		super.setAdapter(adapter);
		this.adapter = (DragListAdapter) adapter;
	}

	/**
	 * �������C�x���g<br>
	 * �h���b�O���J�n����B���C�x���g�̑O�ɁA�^�b�`�C�x���g�iACTION_DOWN�j���Ă΂�Ă���O��B
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return startDrag(downEvent, position);
	}

	/**
	 * �^�b�`�C�x���g<br>
	 * �h���b�O���Ă��鍀�ڂ̈ړ���A�h���b�O�I���̐�����s���B
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

		// �C�x���g���������Ă��Ȃ���΁A�e�̃C�x���g�������ĂԁB
		// �������C�x���g�𔭐������邽�߁AACTION_DOWN�C�x���g���́A�e�̃C�x���g�������ĂԁB
		if (result == false) {
			result = super.onTouchEvent(event);
		}
		return result;
	}

	/**
	 * �������C�x���g���ɁA�^�b�`�ʒu���擾���邽�߁AACTION_DOWN����MotionEvent��ێ�����B
	 */
	private void storeMotionEvent(MotionEvent event) {
		downEvent = event;
	}

	/**
	 * �h���b�O�J�n
	 */
	private boolean startDrag(MotionEvent event, int position) {
		dragging = false;
		int x = (int) event.getX();
		int y = (int) event.getY();

		if (position < 0) {
			return false;
		}

		// �A�_�v�^�[�Ƀh���b�O�Ώۍ��ڈʒu��n��
		adapter.startDrag(position);

		// �h���b�O���̃��X�g���ڂ̕`����J�n����
		popupView.startDrag(x, y, getChildByIndex(position));
		
		// ���X�g�r���[���ĕ`�悷��
		invalidateViews();
		dragging = true;
		return true;
	}

	/**
	 * �h���b�O����
	 */
	private boolean doDrag(MotionEvent event) {
		if (!dragging) {
			return false;
		}
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		int position = pointToPosition(x, y);

		// �h���b�O�̈ړ��惊�X�g���ڂ����݂���ꍇ
		if (position != AdapterView.INVALID_POSITION) {
			// �A�_�v�^�[�̃f�[�^����ёւ���
			adapter.doDrag(position);
		}

		// �h���b�O���̃��X�g���ڂ̕`����X�V����
		popupView.doDrag(x, y);
		
		// ���X�g�r���[���ĕ`�悷��
		invalidateViews();
		
		// �K�v����΃X�N���[��������
		// ���ӁFinvalidateViews()��ɏ������Ȃ��ƃX�N���[�����Ȃ�����
		setScroll(event);
		return true;
	}

	/**
	 * �h���b�O�I��
	 */
	private boolean stopDrag(MotionEvent event) {
		if (!dragging) {
			return false;
		}

		// �A�_�v�^�[�Ƀh���b�O�ΏۂȂ���n��
		adapter.stopDrag();

		// �h���b�O���̃��X�g���ڂ̕`����I������
		popupView.stopDrag();

		// ���X�g�r���[���ĕ`�悷��
		invalidateViews();
		dragging = false;
		return true;
	}

	/**
	 * �K�v����΃X�N���[��������B<br>
	 * ���W�̌v�Z���ώG�ɂȂ�̂œ�View�̃}�[�W���ƃp�f�B���O�̓[���̑O��Ƃ���B
	 */
	private void setScroll(MotionEvent event) {
		int y = (int) event.getY();
		int height = getHeight();
		int harfHeight = height / 2;
		int harfWidth = getWidth() / 2;

		// �X�N���[�����x�̌���
		int speed;
		int fastBound = height / 9;
		int slowBound = height / 4;
		if (event.getEventTime() - event.getDownTime() < 300) {
			// �h���b�O�̊J�n����500�~���b�̊Ԃ̓X�N���[�����Ȃ�
			speed = 0;
		} else if (y < slowBound) {
			speed = y < fastBound ? -SCROLL_SPEED_FAST : -SCROLL_SPEED_SLOW;
		} else if (y > height - slowBound) {
			speed = y > height - fastBound ? SCROLL_SPEED_FAST : SCROLL_SPEED_SLOW;
		} else {
			// �X�N���[���Ȃ��̂��ߏ����I��
			return;
		}
		
		// ��ʂ̒����ɂ��郊�X�g���ڈʒu�����߂�
		// �������͂Ƃ肠�����l���Ȃ�
		// ���������傤�ǃ��X�g���ڊԂ̋��E�̏ꍇ�́A�ʒu���擾�ł��Ȃ��̂ŁA
		// ���E���炸�炵�čĎ擾����B
		int middlePosition = pointToPosition(harfWidth, harfHeight);
		if (middlePosition == AdapterView.INVALID_POSITION) {
			middlePosition = pointToPosition(harfWidth, harfHeight + getDividerHeight());
		}

		// �X�N���[�����{
		final View middleView = getChildByIndex(middlePosition);
		if (middleView != null) {
			setSelectionFromTop(middlePosition, middleView.getTop() - speed);
		}
	}

	/**
	 * �w��C���f�b�N�X��View�v�f���擾����
	 */
	private View getChildByIndex(int index) {
		return getChildAt(index - getFirstVisiblePosition());
	}
}
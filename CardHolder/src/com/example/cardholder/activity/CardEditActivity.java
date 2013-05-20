package com.example.cardholder.activity;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cardholder.R;
import com.example.cardholder.common.Const;
import com.example.cardholder.task.CardDataAddTask;
import com.example.cardholder.task.CardDataDelTask;
import com.example.cardholder.task.CardDataEditTask;
import com.example.cardholder.task.ListBitmapTask;
import com.zako.custom.object.CardInfo;
import com.zako.dialog.CustomAlertDialog;
import com.zako.dialog.CustomProgressDialog;
import com.zako.interfaces.CustomAlertDialogCallback;

/**
 * �J�[�h���ҏW��ʗpActivity
 */
public class CardEditActivity extends Activity implements OnClickListener, OnTouchListener, Handler.Callback, CustomAlertDialogCallback {

	// ��ʃp�[�c�F�x�[�X���C�A�E�g
	LinearLayout linearLayout;

	// ��ʃp�[�c�F�J�[�h�\�ʃ��C�A�E�g
	RelativeLayout frameFrontLayout;
	
	// ��ʃp�[�c�F�J�[�h���ʃ��C�A�E�g
	RelativeLayout frameBackLayout;

	// ��ʃp�[�c�FCancel�{�^��
	Button buttonCancel;

	// ��ʃp�[�c�F�폜�{�^��
	Button buttonDelete;
	
	// ��ʃp�[�c�FOK�{�^��
	Button buttonOk;
	
	// ��ʃp�[�c�F�J�[�h���̓��͗�
	EditText editName;
	
	// ��ʃp�[�c�F�J�[�h���l���͗�
	EditText editMemo;
	
	// ��ʃp�[�c�F�J�[�h���̃��x��
	TextView textName;
	
	// ��ʃp�[�c�F�J�[�h�\�ʃ��x��
	TextView textFrontImage;
	
	// ���j�p�[�c�F�J�[�h���ʃ��x��
	TextView textBackImage;
	
	// ��ʃp�[�c�F�J�[�h���l���x��
	TextView textMemo;

	// ��ʃp�[�c�F�J�[�h�\�ʃ��[�f�B���O
	ProgressBar progressFront;
	
	//�@��ʃp�[�c�F�J�[�h���ʃ��[�f�B���O
	ProgressBar progressBack;

	// ��ʃp�[�c�F�J�[�h�\�ʉ摜
	ImageView imageViewFront;

	// ��ʃp�[�c�F�J�[�h���ʉ摜
	ImageView imageViewBack;
	
	// �J�[�h�o�^�r�W�l�X
	private CardDataAddTask taskAdd;

	// �J�[�h�폜�r�W�l�X
	private CardDataDelTask taskDel;
	
	// �J�[�h�ҏW�r�W�l�X
	private CardDataEditTask taskEdit;

	// �^�X�N���s�p�X���b�h
	private Thread thread;
	
	// �X���b�h���s�����Ď��p�n���h���[
	private Handler handler;

	// �v���O���X�_�C�A���O
	private CustomProgressDialog dialogProgress;

	// �A���[�g�_�C�A���O
	private CustomAlertDialog dialogAlert;
	
	// ��ʃ��[�h
	private int mode;

	// ����ΏۃJ�[�h���
	private CardInfo cardInfo;
	
	// ��ʃ��[�h�F�ǉ�
	private static final int MODE_ADD = 1;

	// ��ʃ��[�h�F�ҏW
	private static final int MODE_EDIT = 2;

	// ��ʃ��[�h�F�폜
	private static final int MODE_DEL = 3;

	byte[] imageDataFront = null;
	
	byte[] imageDataBack = null;

	/**
	 * ��������
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_edit);

		// ��ʃp�[�c�̔z�u
		findViews();

		// �C�x���g���X�i�[�̒ǉ�
		setListener();
	}

	/**
	 * ��ʃp�[�c�z�u����
	 */
	protected void findViews() {
		
		// ���C�A�E�g�p�[�c�̔z�u
		this.linearLayout = (LinearLayout)findViewById(R.id.editLinearLayout);
		this.frameFrontLayout = (RelativeLayout)findViewById(R.id.editTextFrontFrame);
		this.frameBackLayout = (RelativeLayout)findViewById(R.id.editTextBackFrame);

		// �{�^���p�[�c�̔z�u
		this.buttonCancel = (Button)findViewById(R.id.editButtonCancel);
		this.buttonDelete = (Button)findViewById(R.id.editButtonDelete);
		this.buttonOk = (Button)findViewById(R.id.editButtonOk);

		// ���̓p�[�c�̔z�u
		this.editName = (EditText)findViewById(R.id.editEditName);
		this.editMemo = (EditText)findViewById(R.id.editEditMemo);

		this.editName.setLongClickable(false);
		this.editMemo.setLongClickable(false);

		this.textName = (TextView)findViewById(R.id.editTextName);
		this.textMemo = (TextView)findViewById(R.id.editTextMemo);

		// �摜�p�[�c�̔z�u
		this.imageViewFront = (ImageView)findViewById(R.id.editImageFront);
		this.imageViewBack = (ImageView)findViewById(R.id.editImageBack);
		
		this.progressFront = (ProgressBar)findViewById(R.id.progressFront);
		this.progressBack = (ProgressBar)findViewById(R.id.progressBack);

		// �J�ڌ���ʂ���̃f�[�^���󂯎��
		Intent intent = getIntent();

		CardInfo cardInfo = (CardInfo)intent.getSerializableExtra("cardInfo");

		if (cardInfo != null) {
			this.cardInfo = new CardInfo();
			this.cardInfo.setCardId(cardInfo.getCardId());
			this.cardInfo.setCardName(cardInfo.getCardName());
			this.cardInfo.setCardFrontImage(cardInfo.getCardFrontImage());
			this.cardInfo.setCardBackImage(cardInfo.getCardBackImage());
			this.cardInfo.setCardMemo(cardInfo.getCardMemo());
			this.cardInfo.setCardModDate(cardInfo.getCardModDate());

			this.setTitle(R.string.editViewTitle);
			this.buttonDelete.setVisibility(View.VISIBLE);
			this.buttonOk.setText("�ҏW");
			this.mode = MODE_EDIT;
		} else {
			this.cardInfo = new CardInfo();

			this.setTitle(R.string.addViewTitle);
			this.buttonDelete.setVisibility(View.GONE);
			this.buttonOk.setText("�o�^");
			this.mode = MODE_ADD;
		}
		this.editName.setText(this.cardInfo.getCardName());
		this.editMemo.setText(this.cardInfo.getCardMemo());

		int imageWidth = 0;
		int imageHeight = 0;

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		Point size = new Point();
		wm.getDefaultDisplay().getSize(size);
		imageWidth = size.x / 2 - 60;

		// �J�[�h�̑傫��(5.4*8.5)�䗦�ɃJ�[�h�C���[�W���T�C�Y����
		imageHeight = (int)((imageWidth / 8.5f) * 6.5f);
		RelativeLayout.LayoutParams imageParam = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
		this.imageViewFront.setLayoutParams(imageParam);
		this.imageViewBack.setLayoutParams(imageParam);

		LinearLayout.LayoutParams frameLeftParam = new LinearLayout.LayoutParams(imageWidth, imageHeight);
		LinearLayout.LayoutParams frameRightParam = new LinearLayout.LayoutParams(imageWidth, imageHeight);
		frameLeftParam.rightMargin = 15;
		frameRightParam.leftMargin = 15;
		this.frameFrontLayout.setLayoutParams(frameLeftParam);
		this.frameBackLayout.setLayoutParams(frameRightParam);

		if (this.cardInfo.getCardFrontImage() != null) {
			this.imageDataFront = this.cardInfo.getCardFrontImage();

			this.imageViewFront.setVisibility(View.GONE);
			if (this.imageViewFront.getTag() == null) {
				this.imageViewFront.setTag(String.valueOf(this.imageViewFront.getId()));
			}
			ListBitmapTask loadTaskFront = new ListBitmapTask(this.imageViewFront, this.progressFront, String.valueOf(this.imageViewFront.getId()));
			loadTaskFront.execute(cardInfo.getCardFrontImage());
		} else {
			this.progressFront.setVisibility(View.GONE);
		}

		if (this.cardInfo.getCardBackImage() != null) {
			this.imageDataBack = this.cardInfo.getCardBackImage();

			this.imageViewBack.setVisibility(View.GONE);
			if (this.imageViewBack.getTag() == null) {
				this.imageViewBack.setTag(String.valueOf(this.imageViewBack.getId()));
			}
			ListBitmapTask loadTaskBack = new ListBitmapTask(this.imageViewBack, this.progressBack, String.valueOf(this.imageViewBack.getId()));
			loadTaskBack.execute(cardInfo.getCardBackImage());
		} else {
			this.progressBack.setVisibility(View.GONE);
		}
	}

	/**
	 * �C�x���g���X�i�[�ݒ菈��
	 */
	protected void setListener() {
		this.imageViewFront.setOnClickListener(this);
		this.imageViewBack.setOnClickListener(this);
		this.buttonCancel.setOnClickListener(this);
		this.buttonDelete.setOnClickListener(this);
		this.buttonOk.setOnClickListener(this);

		this.linearLayout.setOnTouchListener(this);
		
		this.editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				
				if (hasFocus) {
					imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
				} else {
					imm.hideSoftInputFromInputMethod(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});

		this.editMemo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				if (hasFocus) {
					imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
				} else {
					imm.hideSoftInputFromInputMethod(v.getWindowToken(), 0);
				}
			}
		});

		this.handler = new Handler(this);
	}

	/**
	 * �x�[�X���C�A�E�g�^�b�`�C�x���g���X�i�[
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
	
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			InputMethodManager imm = null;
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			view.clearFocus();
		}
		return false;
	}
	
	/**
	 * �N���b�N�C�x���g���X�i�[
	 */
	@Override
	public void onClick(View v) {
		InputMethodManager imm = null;

		switch (v.getId()) {
		case R.id.editImageFront:
			// �\�ʎB�e��
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			Intent intentFront = new Intent(CardEditActivity.this, CardImageShotActivity.class);
			startActivityForResult(intentFront, Const.ACTIVITY_CARD_FRONT_SHOT);
			
			break;
		case R.id.editImageBack:
			// ���ʎB�e��
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			Intent intentBack = new Intent(CardEditActivity.this, CardImageShotActivity.class);
			startActivityForResult(intentBack, Const.ACTIVITY_CARD_BACK_SHOT);
			break;
		case R.id.editButtonCancel:
			// �߂�{�^��
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.editButtonDelete:
			// �폜�{�^��
			this.mode = MODE_DEL;
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			this.dialogAlert = new CustomAlertDialog(this, "�폜�m�F", "�J�[�h�����폜���܂����H", true, true, this, Const.DIALOG_DELETE_CONFIRM);
			this.dialogAlert.show();

			break;
		case R.id.editButtonOk:
			// OK�{�^��
			// �\�t�g�L�[�{�[�h���B��
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
			CardInfo cardInfo = new CardInfo();
			cardInfo.setCardName(this.editName.getText().toString());
			cardInfo.setCardMemo(this.editMemo.getText().toString());
/*
			BitmapDrawable bdFront = (BitmapDrawable)this.imageViewFront.getDrawable();
			Bitmap bmpFront = bdFront.getBitmap();
			ByteArrayOutputStream baosFront = new ByteArrayOutputStream();
			bmpFront.compress(CompressFormat.PNG, 100, baosFront);
			this.imageDataFront = baosFront.toByteArray();
*/
			if (this.imageDataFront != null) {
				cardInfo.setCardFrontImage(this.imageDataFront);
			}

/*			
			BitmapDrawable bdBack = (BitmapDrawable)this.imageViewBack.getDrawable();
			Bitmap bmpBack = bdBack.getBitmap();
			ByteArrayOutputStream baosBack = new ByteArrayOutputStream();
			bmpBack.compress(CompressFormat.PNG, 100, baosBack);
			this.imageDataBack = baosBack.toByteArray();
*/
			if (this.imageDataBack != null) {
				cardInfo.setCardBackImage(this.imageDataBack);
			}

			if (mode == MODE_ADD) {
				showProgressDialog("�f�[�^�o�^���E�E�E");
				this.taskAdd = new CardDataAddTask(this.handler, this, cardInfo);
				this.thread = new Thread(this.taskAdd);
				this.thread.start();
			} else if (mode == MODE_EDIT) {
				cardInfo.setCardId(this.cardInfo.getCardId());

				showProgressDialog("�f�[�^�X�V���E�E�E");
				this.taskEdit = new CardDataEditTask(this.handler, this, cardInfo);
				this.thread = new Thread(this.taskEdit);
				this.thread.start();
			}

			break;
		}
	}

	/**
	 * �v���O���X�_�C�A���O�\������
	 * @param strMess
	 */
	private void showProgressDialog(String strMess) {
		this.dialogProgress = new CustomProgressDialog(this, strMess);
		this.dialogProgress.show();
	}
	
	/**
	 * �X���b�h���s�����C�x���g�n���h���[
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case Const.EDIT_REQUIRED_ERROR:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.taskAdd = null;
			this.taskEdit = null;
			this.taskDel = null;

			this.dialogAlert = new CustomAlertDialog(this, "���̓G���[", "�J�[�h���͕̂K�{�ł��B", false, true, this, Const.DIALOG_ERROR);
			this.dialogAlert.show();
			return true;
		case Const.EDIT_TASK_SUCCEED:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.taskAdd = null;
			this.taskEdit = null;
			this.taskDel = null;

			setResult(RESULT_OK);
			finish();
			return true;
		case Const.EDIT_TASK_FAILED:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.taskAdd = null;
			this.taskEdit = null;
			this.taskDel = null;

			if (this.mode == MODE_ADD) {
				this.dialogAlert = new CustomAlertDialog(this, "�G���[", "�J�[�h���̓o�^�Ɏ��s���܂����B", false, true, this, Const.DIALOG_ERROR);
			} else if (this.mode == MODE_EDIT) {
				this.dialogAlert = new CustomAlertDialog(this, "�G���[", "�J�[�h���̍X�V�Ɏ��s���܂����B", false, true, this, Const.DIALOG_ERROR);
			} else if (this.mode == MODE_DEL) {
				this.dialogAlert = new CustomAlertDialog(this, "�G���[", "�J�[�h���̍폜�Ɏ��s���܂����B", false, true, this, Const.DIALOG_ERROR);
			}
			this.dialogAlert.show();
			return true;
		default:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.taskAdd = null;
			this.taskEdit = null;
			this.taskDel = null;
			return false;
		}
	}
	
	/**
	 * �A���[�g�_�C�A���OOK�C�x���g�n���h���[
	 */
	public void onAlertOk(int id) {
		this.dialogAlert = null;

		if (id == Const.DIALOG_DELETE_CONFIRM) {
			showProgressDialog("�f�[�^�폜���E�E�E");

			this.taskDel = new CardDataDelTask(this.handler, this, this.cardInfo);
			this.thread = new Thread(this.taskDel);
			this.thread.start();
		}
	}

	/**
	 * �A���[�g�_�C�A���OCancel�C�x���g�n���h���[
	 */
	public void onAlertCancel(int id) {
		this.dialogAlert = null;
	}

	/**
	 * �J�ڐ��ʂ���̋A�҃C�x���g���X�i�[
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == Const.ACTIVITY_CARD_FRONT_SHOT && resultCode == RESULT_OK) {
			this.imageDataFront = data.getByteArrayExtra("imageData");;
			setImageView(this.imageViewFront, data);

		} else if (requestCode == Const.ACTIVITY_CARD_BACK_SHOT && resultCode == RESULT_OK) {
			this.imageDataBack = data.getByteArrayExtra("imageData");;
			setImageView(this.imageViewBack, data);
		}
	}

	private void setImageView(ImageView imageView, Intent intent) {
		int orgWidth = 0;
		int orgHeight = 0;
		int w = 0;
		int h = 0;
		int scale = 0;
		byte[] imageData = null;
		BitmapFactory.Options opt = null;
		Bitmap bmp = null;
		
		imageData = intent.getByteArrayExtra("imageData");

		opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		
		// �T�C�Y���̂݃��[�h
		BitmapFactory.decodeByteArray(imageData, 0, imageData.length, opt);
		
		orgWidth = opt.outWidth;
		orgHeight = opt.outHeight;

		w = imageView.getWidth();
		h = imageView.getHeight();

		if (w < orgWidth || h < orgHeight) {
			scale = Math.max(orgWidth, orgHeight) / Math.min(w, h) + 1;
			opt.inSampleSize = scale;
		}

		opt.inJustDecodeBounds = false;
		
		bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, opt);

		imageView.setImageBitmap(bmp);
	}
}

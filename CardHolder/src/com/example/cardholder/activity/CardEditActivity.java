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
 * カード情報編集画面用Activity
 */
public class CardEditActivity extends Activity implements OnClickListener, OnTouchListener, Handler.Callback, CustomAlertDialogCallback {

	// 画面パーツ：ベースレイアウト
	LinearLayout linearLayout;

	// 画面パーツ：カード表面レイアウト
	RelativeLayout frameFrontLayout;
	
	// 画面パーツ：カード裏面レイアウト
	RelativeLayout frameBackLayout;

	// 画面パーツ：Cancelボタン
	Button buttonCancel;

	// 画面パーツ：削除ボタン
	Button buttonDelete;
	
	// 画面パーツ：OKボタン
	Button buttonOk;
	
	// 画面パーツ：カード名称入力欄
	EditText editName;
	
	// 画面パーツ：カード備考入力欄
	EditText editMemo;
	
	// 画面パーツ：カード名称ラベル
	TextView textName;
	
	// 画面パーツ：カード表面ラベル
	TextView textFrontImage;
	
	// 画面jパーツ：カード裏面ラベル
	TextView textBackImage;
	
	// 画面パーツ：カード備考ラベル
	TextView textMemo;

	// 画面パーツ：カード表面ローディング
	ProgressBar progressFront;
	
	//　画面パーツ：カード裏面ローディング
	ProgressBar progressBack;

	// 画面パーツ：カード表面画像
	ImageView imageViewFront;

	// 画面パーツ：カード裏面画像
	ImageView imageViewBack;
	
	// カード登録ビジネス
	private CardDataAddTask taskAdd;

	// カード削除ビジネス
	private CardDataDelTask taskDel;
	
	// カード編集ビジネス
	private CardDataEditTask taskEdit;

	// タスク実行用スレッド
	private Thread thread;
	
	// スレッド実行完了監視用ハンドラー
	private Handler handler;

	// プログレスダイアログ
	private CustomProgressDialog dialogProgress;

	// アラートダイアログ
	private CustomAlertDialog dialogAlert;
	
	// 画面モード
	private int mode;

	// 操作対象カード情報
	private CardInfo cardInfo;
	
	// 画面モード：追加
	private static final int MODE_ADD = 1;

	// 画面モード：編集
	private static final int MODE_EDIT = 2;

	// 画面モード：削除
	private static final int MODE_DEL = 3;

	byte[] imageDataFront = null;
	
	byte[] imageDataBack = null;

	/**
	 * 初期処理
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_edit);

		// 画面パーツの配置
		findViews();

		// イベントリスナーの追加
		setListener();
	}

	/**
	 * 画面パーツ配置処理
	 */
	protected void findViews() {
		
		// レイアウトパーツの配置
		this.linearLayout = (LinearLayout)findViewById(R.id.editLinearLayout);
		this.frameFrontLayout = (RelativeLayout)findViewById(R.id.editTextFrontFrame);
		this.frameBackLayout = (RelativeLayout)findViewById(R.id.editTextBackFrame);

		// ボタンパーツの配置
		this.buttonCancel = (Button)findViewById(R.id.editButtonCancel);
		this.buttonDelete = (Button)findViewById(R.id.editButtonDelete);
		this.buttonOk = (Button)findViewById(R.id.editButtonOk);

		// 入力パーツの配置
		this.editName = (EditText)findViewById(R.id.editEditName);
		this.editMemo = (EditText)findViewById(R.id.editEditMemo);

		this.editName.setLongClickable(false);
		this.editMemo.setLongClickable(false);

		this.textName = (TextView)findViewById(R.id.editTextName);
		this.textMemo = (TextView)findViewById(R.id.editTextMemo);

		// 画像パーツの配置
		this.imageViewFront = (ImageView)findViewById(R.id.editImageFront);
		this.imageViewBack = (ImageView)findViewById(R.id.editImageBack);
		
		this.progressFront = (ProgressBar)findViewById(R.id.progressFront);
		this.progressBack = (ProgressBar)findViewById(R.id.progressBack);

		// 遷移元画面からのデータを受け取る
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
			this.buttonOk.setText("編集");
			this.mode = MODE_EDIT;
		} else {
			this.cardInfo = new CardInfo();

			this.setTitle(R.string.addViewTitle);
			this.buttonDelete.setVisibility(View.GONE);
			this.buttonOk.setText("登録");
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

		// カードの大きさ(5.4*8.5)比率にカードイメージをサイズ調整
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
	 * イベントリスナー設定処理
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
	 * ベースレイアウトタッチイベントリスナー
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
	 * クリックイベントリスナー
	 */
	@Override
	public void onClick(View v) {
		InputMethodManager imm = null;

		switch (v.getId()) {
		case R.id.editImageFront:
			// 表面撮影時
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			Intent intentFront = new Intent(CardEditActivity.this, CardImageShotActivity.class);
			startActivityForResult(intentFront, Const.ACTIVITY_CARD_FRONT_SHOT);
			
			break;
		case R.id.editImageBack:
			// 裏面撮影時
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			Intent intentBack = new Intent(CardEditActivity.this, CardImageShotActivity.class);
			startActivityForResult(intentBack, Const.ACTIVITY_CARD_BACK_SHOT);
			break;
		case R.id.editButtonCancel:
			// 戻るボタン
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.editButtonDelete:
			// 削除ボタン
			this.mode = MODE_DEL;
			imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			this.dialogAlert = new CustomAlertDialog(this, "削除確認", "カード情報を削除しますか？", true, true, this, Const.DIALOG_DELETE_CONFIRM);
			this.dialogAlert.show();

			break;
		case R.id.editButtonOk:
			// OKボタン
			// ソフトキーボードを隠す
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
				showProgressDialog("データ登録中・・・");
				this.taskAdd = new CardDataAddTask(this.handler, this, cardInfo);
				this.thread = new Thread(this.taskAdd);
				this.thread.start();
			} else if (mode == MODE_EDIT) {
				cardInfo.setCardId(this.cardInfo.getCardId());

				showProgressDialog("データ更新中・・・");
				this.taskEdit = new CardDataEditTask(this.handler, this, cardInfo);
				this.thread = new Thread(this.taskEdit);
				this.thread.start();
			}

			break;
		}
	}

	/**
	 * プログレスダイアログ表示処理
	 * @param strMess
	 */
	private void showProgressDialog(String strMess) {
		this.dialogProgress = new CustomProgressDialog(this, strMess);
		this.dialogProgress.show();
	}
	
	/**
	 * スレッド実行完了イベントハンドラー
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

			this.dialogAlert = new CustomAlertDialog(this, "入力エラー", "カード名称は必須です。", false, true, this, Const.DIALOG_ERROR);
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
				this.dialogAlert = new CustomAlertDialog(this, "エラー", "カード情報の登録に失敗しました。", false, true, this, Const.DIALOG_ERROR);
			} else if (this.mode == MODE_EDIT) {
				this.dialogAlert = new CustomAlertDialog(this, "エラー", "カード情報の更新に失敗しました。", false, true, this, Const.DIALOG_ERROR);
			} else if (this.mode == MODE_DEL) {
				this.dialogAlert = new CustomAlertDialog(this, "エラー", "カード情報の削除に失敗しました。", false, true, this, Const.DIALOG_ERROR);
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
	 * アラートダイアログOKイベントハンドラー
	 */
	public void onAlertOk(int id) {
		this.dialogAlert = null;

		if (id == Const.DIALOG_DELETE_CONFIRM) {
			showProgressDialog("データ削除中・・・");

			this.taskDel = new CardDataDelTask(this.handler, this, this.cardInfo);
			this.thread = new Thread(this.taskDel);
			this.thread.start();
		}
	}

	/**
	 * アラートダイアログCancelイベントハンドラー
	 */
	public void onAlertCancel(int id) {
		this.dialogAlert = null;
	}

	/**
	 * 遷移先画面からの帰還イベントリスナー
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
		
		// サイズ情報のみロード
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

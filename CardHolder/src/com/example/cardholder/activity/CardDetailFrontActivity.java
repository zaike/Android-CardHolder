package com.example.cardholder.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.cardholder.R;
import com.example.cardholder.anim.FrontBackSwitchAnimator;
import com.example.cardholder.task.DetailImageTask;
import com.zako.custom.object.CardInfo;

public class CardDetailFrontActivity extends Activity implements OnTouchListener, OnClickListener {

	// 画面パーツ：レイアウト
	RelativeLayout layoutFront;

	// 画面パーツ：ローディング
	ProgressBar progressDetailFront;

	// 画面パーツ：表面画像
	ImageView imageViewDetailFront;

	// 画面パーツ：裏面画像
	ImageView imageViewDetailBack;

	// 画面パーツ：メモ欄
	EditText editMemo;
	
	// 画面パーツ：右回転ボタン
	Button buttonRight;
	
	// 画面パーツ：左回転ボタン
	Button buttonLeft;

	CardInfo cardInfo;

	private Matrix matrixFront = new Matrix();
	private Matrix matrixBack = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start       = new PointF();
    private float oldDist      = 0f;
    private PointF mid         = new PointF();
    private float curRatio     = 1f;
        
    // 以下の状態を取り得る
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2; 
    private int mode = NONE;
    
    private static final int DISPLAY_FRONT = 0;
    private static final int DISPLAY_BACK = 1;
    private int displaying = -1;

    /**
     * 初期処理
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_detail_front);

		findViews();
		
		setListener();
	}

    /**
     * 画面パーツ配置処理
     */
	protected void findViews() {
		this.layoutFront = (RelativeLayout)findViewById(R.id.layoutDetailFront);
		this.imageViewDetailFront = (ImageView)findViewById(R.id.imageViewDetailFront);
		this.imageViewDetailBack = (ImageView)findViewById(R.id.imageViewDetailBack);
		this.progressDetailFront = (ProgressBar)findViewById(R.id.progressDetailFront);
		this.editMemo = (EditText)findViewById(R.id.editMemoDetailFront);
		this.buttonRight = (Button)findViewById(R.id.buttonDetailNext);
		this.buttonLeft = (Button)findViewById(R.id.buttonDetailPrev);

		// 遷移元画面からのデータを受け取る
		Intent intent = getIntent();
		
		CardInfo cardInfo = (CardInfo)intent.getSerializableExtra("cardInfo");

		this.cardInfo = new CardInfo();
		this.cardInfo.setCardId(cardInfo.getCardId());
		this.cardInfo.setCardName(cardInfo.getCardName());
		this.cardInfo.setCardFrontImage(cardInfo.getCardFrontImage());
		this.cardInfo.setCardBackImage(cardInfo.getCardBackImage());
		this.cardInfo.setCardMemo(cardInfo.getCardMemo());
		this.cardInfo.setCardModDate(cardInfo.getCardModDate());

		int imageWidth = 0;
		int imageHeight = 0;
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		imageWidth = wm.getDefaultDisplay().getWidth() - 150;

		// カードの大きさ(5.4*8.5)比率にカードイメージをサイズ調整
		imageHeight = (int)(imageWidth / 8.5f * 6.5f);
		
		RelativeLayout.LayoutParams imageParam = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
		imageParam.leftMargin = 2;
		imageParam.rightMargin = 2;
		this.imageViewDetailFront.setLayoutParams(imageParam);
		this.imageViewDetailBack.setLayoutParams(imageParam);

		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(imageWidth, imageHeight);
		layoutParam.topMargin = 30;
		layoutParam.leftMargin = 0;
		layoutParam.rightMargin = 0;
		this.layoutFront.setLayoutParams(layoutParam);

		if (this.cardInfo.getCardBackImage() != null) {
			this.imageViewDetailBack.setVisibility(View.GONE);

			Bitmap bitmapBack = loadImage(this.cardInfo.getCardBackImage(), imageWidth, imageHeight, this.matrixBack);
			this.imageViewDetailBack.setImageBitmap(bitmapBack);

			this.imageViewDetailBack.setImageMatrix(matrixBack);
			
			this.imageViewDetailBack.setVisibility(View.INVISIBLE);
		}
		
		if (this.cardInfo.getCardFrontImage() != null) {
			this.imageViewDetailFront.setVisibility(View.GONE);

			Bitmap bitmapFront = loadImage(this.cardInfo.getCardFrontImage(), imageWidth, imageHeight, this.matrixFront);
			this.imageViewDetailFront.setImageBitmap(bitmapFront);

			this.imageViewDetailFront.setImageMatrix(matrixFront);
			
			this.imageViewDetailFront.setVisibility(View.VISIBLE);

			AlphaAnimation anim = new AlphaAnimation(0, 1);
			anim.setDuration(300);
			anim.setFillAfter(true);
			this.imageViewDetailFront.startAnimation(anim);
			
/*			
			if (this.imageViewDetailFront.getTag() == null) {
				this.imageViewDetailFront.setTag(String.valueOf(this.imageViewDetailFront.getId()));
			}

			DetailImageTask imageTask = new DetailImageTask(
					this.imageViewDetailFront, this.progressDetailFront,
					String.valueOf(this.imageViewDetailFront.getId()), imageWidth, imageHeight, matrix);
			imageTask.execute(cardInfo.getCardFrontImage());

		} else {
			this.progressDetailFront.setVisibility(View.GONE);
*/
		}

		this.progressDetailFront.setVisibility(View.GONE);
		
		if (this.cardInfo.getCardMemo() != null) {
			this.editMemo.setText(this.cardInfo.getCardMemo());
		}

		this.displaying = this.DISPLAY_FRONT;
	}

	private Bitmap loadImage(byte[] blob, int width, int height, Matrix matrix) {
		BitmapFactory.Options opt = null;
		Bitmap bmp = null;
		
		opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		
		// サイズ情報のみロード
		BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);
		
		float w = opt.outWidth;
		float h = opt.outHeight;
		opt.inJustDecodeBounds = false;

		matrix.postScale((float)(width / w), (float)(height / h));

		bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length, opt);

		return bmp;
	}
	
	/**
	 * イベントリスナー登録処理
	 */
	protected void setListener() {
		this.imageViewDetailFront.setOnTouchListener(this);
		this.buttonRight.setOnClickListener(this);
		this.buttonLeft.setOnClickListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView imageView = null;
		Matrix matrix = null;

		if (this.displaying == DISPLAY_BACK) {
			imageView = this.imageViewDetailBack;
			matrix = this.matrixBack;
		} else {
			imageView = this.imageViewDetailFront;
			matrix = this.matrixFront;
		}

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			}
			break;
		}

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				oldDist = spacing(event);
			
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode != DRAG && event.getPointerCount() == 2) {
				float newDist = spacing(event);
				float scale = newDist / oldDist;
				float tmpRatio = curRatio * scale;
				
				if (1.0f < tmpRatio && tmpRatio < 3.0f) {
					curRatio = tmpRatio;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		imageView.setImageMatrix(matrix);

		return true;
	}

	@Override
	public void onClick(View view) {

		ImageView nowDisp = null;
		ImageView nextDisp = null;
		
		if (this.displaying == DISPLAY_FRONT || this.displaying == -1) {
			this.displaying = DISPLAY_BACK;
			
			nowDisp = this.imageViewDetailFront;
			nextDisp = this.imageViewDetailBack;
		} else {
			this.displaying = DISPLAY_FRONT;

			nowDisp = this.imageViewDetailBack;
			nextDisp = this.imageViewDetailFront;
		}

		FrontBackSwitchAnimator anim = new FrontBackSwitchAnimator(nowDisp, nextDisp, nowDisp.getWidth() / 2, nowDisp.getHeight() / 2);
		
		switch (view.getId()) {
		case R.id.buttonDetailNext:
			this.layoutFront.startAnimation(anim);
			break;
		case R.id.buttonDetailPrev:
			anim.reverse();
			this.layoutFront.startAnimation(anim);
			break;
		}
	}	

    /**
     * 2点間の距離を計算
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    /**
     * 2点間の中間点を計算
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}

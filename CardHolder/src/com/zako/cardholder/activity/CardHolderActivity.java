package com.zako.cardholder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.zako.cardholder.R;

/**
 * �N�����X�v���b�V����ʗpActivity�N���X
 */
public class CardHolderActivity extends Activity {

	/**
	 * ��������
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// �^�C�g�����\��
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_card_holder);

		// 500ms�x��������Handler�����s
		Handler hdl = new Handler();
		hdl.postDelayed(new cardHolderHandler(), 500);
	}

	class cardHolderHandler implements Runnable {
		public void run() {
			// �X�v���b�V��������Ɏ��s����Activity���w�肷��B
			Intent intent = new Intent(getApplication(), CardListActivity.class);
			startActivity(intent);

			CardHolderActivity.this.finish();
		}
	}
}

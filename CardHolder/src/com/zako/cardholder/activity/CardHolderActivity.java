package com.zako.cardholder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.zako.cardholder.R;

/**
 * 起動時スプラッシュ画面用Activityクラス
 */
public class CardHolderActivity extends Activity {

	/**
	 * 初期処理
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// タイトルを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_card_holder);

		// 500ms遅延させてHandlerを実行
		Handler hdl = new Handler();
		hdl.postDelayed(new cardHolderHandler(), 500);
	}

	class cardHolderHandler implements Runnable {
		public void run() {
			// スプラッシュ完了後に実行するActivityを指定する。
			Intent intent = new Intent(getApplication(), CardListActivity.class);
			startActivity(intent);

			CardHolderActivity.this.finish();
		}
	}
}

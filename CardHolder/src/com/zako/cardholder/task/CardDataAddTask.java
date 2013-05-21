package com.zako.cardholder.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.zako.cardholder.common.Const;
import com.zako.cardholder.helper.DatabaseHelper;
import com.zako.custom.object.CardInfo;

public class CardDataAddTask implements Runnable {

	private Handler handler;
	private Message message;
	private Context context;
	private CardInfo cardInfo;

	public CardDataAddTask(Handler handler, Context context, CardInfo cardInfo) {
		super();
		this.handler = handler;
		this.context = context;
		this.cardInfo = cardInfo;
	}

	@Override
	public void run() {
		if (!checkCardInfo()) {
			// 入力チェックに不備があったのでエラー
			this.message = new Message();
			this.message.what = Const.EDIT_REQUIRED_ERROR;
			
			this.handler.sendMessage(this.message);
			return;
		}

		// 入力チェックに不備がなかったので、データ登録
		if (insertData()) {
			// 登録成功
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_SUCCEED;
			
			this.handler.sendMessage(this.message);
		} else {
			// 登録失敗
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_FAILED;
			
			this.handler.sendMessage(this.message);
		}
		
	}

	private boolean checkCardInfo() {
		if (cardInfo.getCardName() == null || cardInfo.getCardName().trim().length() < 1) {
			return false;
		}
		return true;
	}

	private boolean insertData() {
		boolean bRet = false;

		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("cardName", cardInfo.getCardName());
		values.put("memo", cardInfo.getCardMemo());
		
		if (cardInfo.getCardFrontImage() != null) {
			
			System.out.println("hoge");
			values.put("frontImage", cardInfo.getCardFrontImage());
		}
		
		if (cardInfo.getCardBackImage() != null) {
			System.out.println("hoge");
			values.put("backImage", cardInfo.getCardBackImage());
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN); 
		Date date = new Date();
		values.put("date", dateFormat.format(date));

		if (dbHelper.insertData(db, "card", values) > -1) {
			bRet = true;
		} else {
			bRet = false;
		}
		db.close();

		return bRet;
	}
}

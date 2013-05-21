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

public class CardDataEditTask implements Runnable {

	private Handler handler;
	private Message message;
	private Context context;
	private CardInfo cardInfo;

	/**
	 * コンストラクタ
	 * @param handler
	 * @param context
	 * @param cardInfo
	 */
	public CardDataEditTask(Handler handler, Context context, CardInfo cardInfo) {
		super();
		this.handler = handler;
		this.context = context;
		this.cardInfo = cardInfo;
	}

	/**
	 * メイン処理
	 */
	@Override
	public void run() {
		if (!checkCardInfo()) {
			// 入力チェックに不備があったのでエラー
			this.message = new Message();
			this.message.what = Const.EDIT_REQUIRED_ERROR;
			
			this.handler.sendMessage(this.message);
			return;
		}
		
		if (cardDataUpdate()) {
			// カード情報更新成功
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_SUCCEED;
			
			this.handler.sendMessage(message);
		} else {
			// カード情報更新失敗
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_FAILED;
			
			this.handler.sendMessage(this.message);
		}
	}

	/**
	 * 更新対象カード情報のチェック
	 * @return
	 */
	private boolean checkCardInfo() {
		if (this.cardInfo.getCardName() == null || cardInfo.getCardName().trim().length() < 1) {
			return false;
		}
		return true;
	}

	/**
	 * カード情報更新処理
	 * @return
	 */
	private boolean cardDataUpdate() {
		boolean bRet = false;
		
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN); 
		Date date = new Date();

		ContentValues values = new ContentValues();
		values.put("cardId", cardInfo.getCardId());
		values.put("frontImage", cardInfo.getCardFrontImage());
		values.put("backImage", cardInfo.getCardBackImage());
		values.put("cardName", cardInfo.getCardName());
		values.put("memo", cardInfo.getCardMemo());
		values.put("date", dateFormat.format(date));
		
		String strWhere = "cardId = ?";
		String[] whereArgs = {this.cardInfo.getCardId()};

		if (dbHelper.updateData(db, "card", strWhere, whereArgs, values) != 1) {
			bRet = false;
		} else {
			bRet = true;
		}
		
		db.close();
		return bRet;
	}
}

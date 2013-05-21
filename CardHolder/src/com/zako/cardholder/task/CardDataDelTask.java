package com.zako.cardholder.task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.zako.cardholder.common.Const;
import com.zako.cardholder.helper.DatabaseHelper;
import com.zako.custom.object.CardInfo;

public class CardDataDelTask implements Runnable {
	
	private Handler handler;
	private Message message;
	private Context context;
	private CardInfo cardInfo;
	
	public CardDataDelTask(Handler handler, Context context, CardInfo cardInfo) {
		super();
		this.handler = handler;
		this.context = context;
		this.cardInfo = cardInfo;
	}

	@Override
	public void run() {
	
		if (deleteData()) {
			// çÌèúê¨å˜
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_SUCCEED;
			
			this.handler.sendMessage(this.message);
		} else {
			this.message = new Message();
			this.message.what = Const.EDIT_TASK_FAILED;
			
			this.handler.sendMessage(this.message);
		}
	}

	private boolean deleteData() {
		boolean bRet = false;
		
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String[] strValues = new String[]{cardInfo.getCardId()};
		int delCount = dbHelper.deleteData(db, "card", "cardId = ?", strValues); 
		
		if (delCount == strValues.length) {
			bRet = true;
		} else {
			bRet = false;
		}
		db.close();

		return bRet;
	}
}

package com.example.cardholder.task;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.example.cardholder.common.Const;
import com.example.cardholder.helper.DatabaseHelper;
import com.zako.custom.object.CardInfo;

public class CardSortEditTask implements Runnable {

	private Handler handler;
	private Message message;
	private Context context;
	private List<CardInfo> dataList; 
	
	public CardSortEditTask(Handler handler, Context context, List<CardInfo> dataList) {
		super();
		this.handler = handler;
		this.context = context;
		this.dataList = dataList;
	}

	@Override
	public void run() {
		if (sortEdit()) {
			// ソート順更新成功
			this.message = new Message();
			this.message.what = Const.SORT_EDIT_TASK_SUCCEED;
			
			this.handler.sendMessage(this.message);
		} else {
			// ソート順更新失敗
			this.message = new Message();
			this.message.what = Const.SORT_EDIT_TASK_FAILED;
			
			this.handler.sendMessage(message);
		}
	}

	private boolean sortEdit() {
		boolean bRet = false;

		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values;
		String strWhere = "cardId = ?";
		String[] whereArgs = {""};

		for (int i = 0; i < this.dataList.size(); i++) {
			CardInfo data = this.dataList.get(i);

			values = new ContentValues();
			values.put("sortNum", String.valueOf(i));
			
			whereArgs[0] = data.getCardId();

			if (dbHelper.updateData(db, "card", strWhere, whereArgs, values) > 0) {
				bRet = true;
			} else {
				bRet = false;
				break;
			}
		}

		db.close();
		return bRet;
	}
}

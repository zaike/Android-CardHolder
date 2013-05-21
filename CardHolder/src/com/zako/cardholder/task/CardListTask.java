package com.zako.cardholder.task;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.zako.cardholder.common.Const;
import com.zako.cardholder.helper.DatabaseHelper;
import com.zako.custom.object.CardInfo;

public class CardListTask implements Runnable {

	private Handler handler;
	private Message message;
	private Context context;

	public CardListTask(Handler handler, Context context) {
		super();
		this.handler = handler;
		this.context = context;
	}

	@Override
	public void run() {
		onRunning();

		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ArrayList<CardInfo> listData = dbHelper.selectAllData(db);

		db.close();

		onSuccess(listData);
	}

	private void onRunning() {
		this.message = new Message();
		this.message.what = Const.LIST_GET_LIST_TASK_RUNNING;
		
		this.handler.sendMessage(this.message);
	}

	private void onSuccess(ArrayList<CardInfo> listData) {
		this.message = new Message();
		this.message.what = Const.LIST_GET_LIST_TASK_SUCCEED;
		this.message.obj = listData;

		this.handler.sendMessage(this.message);
	}
}

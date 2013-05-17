package com.example.cardholder.task;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import com.example.cardholder.common.Const;
import com.example.cardholder.helper.DatabaseHelper;
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
		
		String[] cols = {"cardId", "frontImage", "backImage", "cardName", "sortNum", "date", "memo"};

		ArrayList<CardInfo> listData = dbHelper.selectAllData(db);
		//dbHelper.selectData(db, "card", cols, null, null, null, null, "");

		db.close();

		onSuccess(listData);
	}

	private void onRunning() {
		this.message = new Message();
		this.message.what = Const.LIST_GET_LIST_TASK_RUNNING;
		
		this.handler.sendMessage(this.message);
	}

	private void onError() {
		this.message = new Message();
		this.message.what = Const.LIST_GET_LIST_TASK_FAILED;

		this.handler.sendMessage(this.message);
	}

	private void onSuccess(ArrayList<CardInfo> listData) {
		this.message = new Message();
		this.message.what = Const.LIST_GET_LIST_TASK_SUCCEED;
		this.message.obj = listData;

		this.handler.sendMessage(this.message);
	}
}

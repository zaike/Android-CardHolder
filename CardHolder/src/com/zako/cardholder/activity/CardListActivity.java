package com.zako.cardholder.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.zako.cardholder.R;
import com.zako.cardholder.common.Const;
import com.zako.cardholder.dialog.SettingDialog;
import com.zako.cardholder.task.CardListTask;
import com.zako.cardholder.task.CardSortEditTask;
import com.zako.custom.adapter.DragListAdapter;
import com.zako.custom.object.CardInfo;
import com.zako.dialog.CustomAlertDialog;
import com.zako.dialog.CustomProgressDialog;
import com.zako.interfaces.CustomAlertDialogCallback;
import com.zako.interfaces.DragListViewCallback;
import com.zako.interfaces.SettingDialogCallback;
import com.zako.view.DragListView;

/**
 * カード一覧画面用Activityクラス 
 */
public class CardListActivity extends Activity implements OnClickListener, Handler.Callback, CustomAlertDialogCallback, DragListViewCallback, SettingDialogCallback {

	// 画面パーツ：一覧
	private DragListView dragListView;

	// 画面パーツ：登録ボタン
	private Button buttonAdd;
	
	// 画面パーツ：設定ボタン
	private Button buttonSetting;

	// 一覧データ用変数
	private static List<CardInfo> dataList = new ArrayList<CardInfo>();

	// カスタムリスト用アダプター
	private static DragListAdapter adapter;

	// カード一覧画面取得ビジネス
	private CardListTask taskList;

	// カードソート順変更ビジネス
	private CardSortEditTask taskSort;

	// タスク実行用スレッド
	private Thread thread;

	// スレッド実行完了監視ハンドラー
	private Handler handler;

	// カスタムプログレスダイアログ
	private CustomProgressDialog dialogProgress;
	
	// カスタムアラートダイアログ
	private CustomAlertDialog dialogAlert;

	// 設定ダイアログ
	private SettingDialog settingDialog;

	// 画面幅
	private int screenWidth;
	
	// 画面高さ
	private int screenHeight;

	/**
	 * 初期処理
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// タイトルバーのカスタマイズを可能にする
		getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_card_list);
		
		// タイトルバーのレイアウトを設定
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.card_list_titlebar);

		// 画面幅の取得
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		this.screenWidth = metrics.widthPixels;
		this.screenHeight = metrics.heightPixels;
		
		// 画面パーツの配置
		findViews();

		// リストをアダプターと紐付ける
		setAdapters();

		// イベントリスナーの追加
		setListener();

		// メイン処理開始
		doMainProcess(this);
	}
	
	/**
	 * 画面パーツ配置処理
	 */
	protected void findViews() {
		this.dragListView = (DragListView) findViewById(R.id.listView);
		buttonAdd = (Button)findViewById(R.id.buttonAdd);
		buttonSetting = (Button)findViewById(R.id.buttonSetting);
	}

	/**
	 * イベントリスナー設定処理
	 */
	protected void setListener() {
		this.buttonAdd.setOnClickListener(this);
		this.buttonSetting.setOnClickListener(this);

		this.dragListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;

				CardInfo cardInfo = (CardInfo)listView.getItemAtPosition(position);

				Intent intent = null;
				
				if (id == R.id.buttonEdit) {
					intent = new Intent(CardListActivity.this, CardEditActivity.class);
					intent.putExtra("cardInfo", cardInfo);
					startActivityForResult(intent, Const.ACTIVITY_CARD_EDIT);
				} else {
					intent = new Intent(CardListActivity.this, CardDetailFrontActivity.class);
					intent.putExtra("cardInfo", cardInfo);
					startActivity(intent);
				}

			}
		});
	}
	
	/**
	 * クリックイベントリスナー
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonAdd:
			Intent intent = new Intent(CardListActivity.this, CardEditActivity.class);
			startActivityForResult(intent, Const.ACTIVITY_CARD_EDIT);
			break;
		case R.id.buttonSetting:
			this.settingDialog = new SettingDialog(this, this.screenWidth, this.screenHeight, this);
			this.settingDialog.show();
			break;
		}
	}

	/**
	 * アダプター紐付け処理
	 */
	protected void setAdapters() {
		if (dataList.size() > 0) {
			dataList.clear();
		}
		adapter = new DragListAdapter(this, R.layout.card_list_cell, dataList, this);
	}

	/**
	 * 遷移先画面からの帰還イベントリスナー
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Const.ACTIVITY_CARD_EDIT && resultCode == RESULT_OK) {
			doMainProcess(this);
		}
	}
	
	/**
	 * メインタスク処理
	 * @param context
	 */
	private void doMainProcess(Context context) {

		adapter.clear();

		// 一覧取得中ダイアログの表示
		showProgressDialog("一覧取得中・・・");

		// スレッド呼び出し
		this.handler = new Handler(this);
		this.taskList = new CardListTask(this.handler, context);
		this.thread = new Thread(this.taskList);
		this.thread.start();
	}

	/**
	 * アラートダイアログ表示
	 * @param title
	 * @param mess
	 * @param bCancel
	 * @param bOk
	 * @param dialogType
	 */
	private void showAlertDialog(String title, String mess, boolean bCancel, boolean bOk, int dialogType) {
		this.dialogAlert = new CustomAlertDialog(this, title, mess, bCancel, bOk, this, dialogType);
		this.dialogAlert.show();
	}

	/**
	 * 処理中ダイアログ表示処理
	 * @param strMess
	 */
	private void showProgressDialog(String strMess) {
		this.dialogProgress = new CustomProgressDialog(this, strMess);
		this.dialogProgress.show();
	}

	/**
	 * タスクメッセージ取得イベントハンドラー
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case Const.LIST_GET_LIST_TASK_RUNNING:
			return true;
		case Const.LIST_GET_LIST_TASK_SUCCEED:

			adapter.clear();

			ArrayList<CardInfo> listData = (ArrayList<CardInfo>)msg.obj;

			for (int i = 0; i < listData.size(); i++) {
				CardInfo item = listData.get(i);
				adapter.add(item);
			}
	
			this.dragListView.setAdapter(adapter);

			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.handler = null;
			return true;
		case Const.LIST_GET_LIST_TASK_FAILED:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.handler = null;

			this.showAlertDialog("エラー", "一覧の取得に失敗しました。", false, true, Const.DIALOG_ERROR);
			return true;
		case Const.SORT_EDIT_TASK_SUCCEED:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.handler = null;
			return true;
		case Const.SORT_EDIT_TASK_FAILED:
			this.dialogProgress.dismiss();
			this.dialogProgress = null;
			this.thread = null;
			this.handler = null;
			
			this.showAlertDialog("エラー", "ソート順の更新に失敗しました。", false, true, Const.DIALOG_ERROR);
			return true;
		default:
			this.thread = null;
			return false;
		}
	}
	
	/**
	 * アラートダイアログOKイベントハンドラー
	 */
	public void onAlertOk(int id) {
		this.dialogAlert = null;
	}

	/**
	 * アラートダイアログCancelイベントハンドラー
	 */
	public void onAlertCancel(int id) {
		this.dialogAlert = null;
	}

	/**
	 * リスト要素ドラッグ完了イベントハンドラー
	 */
	public void onDragExit() {

		showProgressDialog("ソート順更新中・・・");

		this.handler = new Handler(this);
		this.taskSort = new CardSortEditTask(this.handler, this, dataList); 
		this.thread = new Thread(this.taskSort);
		this.thread.start();
	}

	public void onDataInitializeComp() {
		doMainProcess(this);
	}
	
	public void onDataInitializeFailed() {
		this.showAlertDialog("データ初期化失敗", "アプリ内のデータ削除に失敗しました。", false, true, Const.DIALOG_ERROR);
	}
}

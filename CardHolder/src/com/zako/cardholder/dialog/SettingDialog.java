package com.zako.cardholder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.zako.cardholder.R;
import com.zako.cardholder.common.Const;
import com.zako.cardholder.helper.DatabaseHelper;
import com.zako.dialog.CustomAlertDialog;
import com.zako.dialog.CustomProgressDialog;
import com.zako.interfaces.CustomAlertDialogCallback;
import com.zako.interfaces.SettingDialogCallback;

public class SettingDialog extends Dialog implements OnClickListener, CustomAlertDialogCallback {

	private SettingDialogCallback mCallback;

	Button buttonInit;
	Context context;

	// アラートダイアログ
	private CustomAlertDialog dialogAlert;

	// プログレスダイアログ
	private CustomProgressDialog dialogProgress;

	public SettingDialog(Context context, int width, int height, SettingDialogCallback callback) {
		super(context, R.style.Theme_CustomProgressDialog);

		setContentView(R.layout.setting_dialog);
		
		this.context = context;
		
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.width = (int)(width * 0.9);
		lp.height = (int)(height * 0.9);
		this.getWindow().setAttributes(lp);

		this.mCallback = callback;

		findViews();
		
		setListener();
	}

	protected void findViews() {
		this.buttonInit = (Button)findViewById(R.id.buttonInit);
	}

	protected void setListener() {
		this.buttonInit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonInit:
			
			this.dialogAlert = new CustomAlertDialog(this.context, "データ初期化確認", "アプリ内の全てのデータを初期化しますか？", true, true, this, Const.DIALOG_INIT_CONFIRM);
			this.dialogAlert.show();
			break;
		}
	}
	
	/**
	 * アラートダイアログOKイベントハンドラー
	 */
	public void onAlertOk(int id) {
		this.dialogAlert = null;
		
		if (id == Const.DIALOG_INIT_CONFIRM) {
			showProgressDialog("データ削除中・・・");

			DatabaseHelper dbHelper = new DatabaseHelper(this.context);
			
			if (dbHelper.initialize()) {
				this.dialogProgress.dismiss();
				this.dialogProgress = null;
				
				this.mCallback.onDataInitializeComp();
				this.dismiss();
			} else {
				this.dialogProgress.dismiss();
				this.dialogProgress = null;
				
				this.mCallback.onDataInitializeFailed();
				this.dismiss();
			}

		}
	}

	/**
	 * アラートダイアログCancelイベントハンドラー
	 */
	public void onAlertCancel(int id) {
		this.dialogAlert = null;
	}

	/**
	 * プログレスダイアログ表示処理
	 * @param strMess
	 */
	private void showProgressDialog(String strMess) {
		this.dialogProgress = new CustomProgressDialog(this.context, strMess);
		this.dialogProgress.show();
	}
}

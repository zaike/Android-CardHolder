package com.zako.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.cardholder.R;
import com.zako.interfaces.CustomAlertDialogCallback;

public class CustomAlertDialog extends Dialog implements OnClickListener {

	private CustomAlertDialogCallback mCallback;
	private int mId;
	
	TextView textTitle;
	TextView textMessage;
	Button buttonCancel;
	Button buttonOk;
	Context context;
	
	public CustomAlertDialog(Context context, String title, String mess, boolean bCancel, boolean bOk, CustomAlertDialogCallback callback, int id) {
		super(context, R.style.Theme_CustomProgressDialog);
		
		this.mCallback = callback;
		this.mId = id;
		
		setContentView(R.layout.custom_alert_dialog);
		setCancelable(false);
		
		this.context = context;

		findViews();

		setListener();

		if (bCancel) {
			this.buttonCancel.setVisibility(View.VISIBLE);
		} else {
			this.buttonCancel.setVisibility(View.GONE);
		}

		if (bOk) {
			this.buttonOk.setVisibility(View.VISIBLE);
		} else {
			this.buttonOk.setVisibility(View.GONE);
		}

		this.textTitle.setText(title);
		this.textMessage.setText(mess);
	}
	
	protected void findViews() {
		this.textTitle = (TextView)findViewById(R.id.textAlertTitle);
		this.textMessage = (TextView)findViewById(R.id.textAlertMessage);
		this.buttonCancel = (Button)findViewById(R.id.buttonAlertNegative);
		this.buttonOk = (Button)findViewById(R.id.buttonAlertPositive);
	}

	protected void setListener() {
		this.buttonCancel.setOnClickListener(this);
		this.buttonOk.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonAlertNegative:
			this.mCallback.onAlertCancel(this.mId);
			this.dismiss();
			break;
		case R.id.buttonAlertPositive:
			this.mCallback.onAlertOk(this.mId);
			this.dismiss();
			break;
		}
	}
}

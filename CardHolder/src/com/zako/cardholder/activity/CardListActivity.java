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
 * �J�[�h�ꗗ��ʗpActivity�N���X 
 */
public class CardListActivity extends Activity implements OnClickListener, Handler.Callback, CustomAlertDialogCallback, DragListViewCallback, SettingDialogCallback {

	// ��ʃp�[�c�F�ꗗ
	private DragListView dragListView;

	// ��ʃp�[�c�F�o�^�{�^��
	private Button buttonAdd;
	
	// ��ʃp�[�c�F�ݒ�{�^��
	private Button buttonSetting;

	// �ꗗ�f�[�^�p�ϐ�
	private static List<CardInfo> dataList = new ArrayList<CardInfo>();

	// �J�X�^�����X�g�p�A�_�v�^�[
	private static DragListAdapter adapter;

	// �J�[�h�ꗗ��ʎ擾�r�W�l�X
	private CardListTask taskList;

	// �J�[�h�\�[�g���ύX�r�W�l�X
	private CardSortEditTask taskSort;

	// �^�X�N���s�p�X���b�h
	private Thread thread;

	// �X���b�h���s�����Ď��n���h���[
	private Handler handler;

	// �J�X�^���v���O���X�_�C�A���O
	private CustomProgressDialog dialogProgress;
	
	// �J�X�^���A���[�g�_�C�A���O
	private CustomAlertDialog dialogAlert;

	// �ݒ�_�C�A���O
	private SettingDialog settingDialog;

	// ��ʕ�
	private int screenWidth;
	
	// ��ʍ���
	private int screenHeight;

	/**
	 * ��������
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// �^�C�g���o�[�̃J�X�^�}�C�Y���\�ɂ���
		getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_card_list);
		
		// �^�C�g���o�[�̃��C�A�E�g��ݒ�
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.card_list_titlebar);

		// ��ʕ��̎擾
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		this.screenWidth = metrics.widthPixels;
		this.screenHeight = metrics.heightPixels;
		
		// ��ʃp�[�c�̔z�u
		findViews();

		// ���X�g���A�_�v�^�[�ƕR�t����
		setAdapters();

		// �C�x���g���X�i�[�̒ǉ�
		setListener();

		// ���C�������J�n
		doMainProcess(this);
	}
	
	/**
	 * ��ʃp�[�c�z�u����
	 */
	protected void findViews() {
		this.dragListView = (DragListView) findViewById(R.id.listView);
		buttonAdd = (Button)findViewById(R.id.buttonAdd);
		buttonSetting = (Button)findViewById(R.id.buttonSetting);
	}

	/**
	 * �C�x���g���X�i�[�ݒ菈��
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
	 * �N���b�N�C�x���g���X�i�[
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
	 * �A�_�v�^�[�R�t������
	 */
	protected void setAdapters() {
		if (dataList.size() > 0) {
			dataList.clear();
		}
		adapter = new DragListAdapter(this, R.layout.card_list_cell, dataList, this);
	}

	/**
	 * �J�ڐ��ʂ���̋A�҃C�x���g���X�i�[
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Const.ACTIVITY_CARD_EDIT && resultCode == RESULT_OK) {
			doMainProcess(this);
		}
	}
	
	/**
	 * ���C���^�X�N����
	 * @param context
	 */
	private void doMainProcess(Context context) {

		adapter.clear();

		// �ꗗ�擾���_�C�A���O�̕\��
		showProgressDialog("�ꗗ�擾���E�E�E");

		// �X���b�h�Ăяo��
		this.handler = new Handler(this);
		this.taskList = new CardListTask(this.handler, context);
		this.thread = new Thread(this.taskList);
		this.thread.start();
	}

	/**
	 * �A���[�g�_�C�A���O�\��
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
	 * �������_�C�A���O�\������
	 * @param strMess
	 */
	private void showProgressDialog(String strMess) {
		this.dialogProgress = new CustomProgressDialog(this, strMess);
		this.dialogProgress.show();
	}

	/**
	 * �^�X�N���b�Z�[�W�擾�C�x���g�n���h���[
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

			this.showAlertDialog("�G���[", "�ꗗ�̎擾�Ɏ��s���܂����B", false, true, Const.DIALOG_ERROR);
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
			
			this.showAlertDialog("�G���[", "�\�[�g���̍X�V�Ɏ��s���܂����B", false, true, Const.DIALOG_ERROR);
			return true;
		default:
			this.thread = null;
			return false;
		}
	}
	
	/**
	 * �A���[�g�_�C�A���OOK�C�x���g�n���h���[
	 */
	public void onAlertOk(int id) {
		this.dialogAlert = null;
	}

	/**
	 * �A���[�g�_�C�A���OCancel�C�x���g�n���h���[
	 */
	public void onAlertCancel(int id) {
		this.dialogAlert = null;
	}

	/**
	 * ���X�g�v�f�h���b�O�����C�x���g�n���h���[
	 */
	public void onDragExit() {

		showProgressDialog("�\�[�g���X�V���E�E�E");

		this.handler = new Handler(this);
		this.taskSort = new CardSortEditTask(this.handler, this, dataList); 
		this.thread = new Thread(this.taskSort);
		this.thread.start();
	}

	public void onDataInitializeComp() {
		doMainProcess(this);
	}
	
	public void onDataInitializeFailed() {
		this.showAlertDialog("�f�[�^���������s", "�A�v�����̃f�[�^�폜�Ɏ��s���܂����B", false, true, Const.DIALOG_ERROR);
	}
}

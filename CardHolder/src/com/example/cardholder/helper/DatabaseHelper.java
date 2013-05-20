package com.example.cardholder.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zako.custom.object.CardInfo;

public class DatabaseHelper extends SQLiteOpenHelper {
	   
    /* �f�[�^�x�[�X�� */  
	private static final String DB_NAME = "cardHolder.db";//Environment.getExternalStorageDirectory() + File.separator + "cardHolder.db";
    /* �f�[�^�x�[�X�̃o�[�W���� */  
    private final static int DB_VER = 1;      
    /* �R���e�L�X�g */  
    private Context mContext;   
      
    /** 
     * �R���X�g���N�^ 
     */  
    public DatabaseHelper(Context context) {  
        super(context, DB_NAME, null, DB_VER);  
        mContext = context;  
    }  
  
    /** 
     * �f�[�^�x�[�X���쐬���ꂽ���ɌĂ΂�܂��B 
     * assets/sql/create���ɒ�`����Ă���sql�����s���܂��B 
     */  
    @Override  
    public void onCreate(SQLiteDatabase db) {       
        try {  
            execSql(db,"sql/create");
            
        } catch (IOException e) {  
            e.printStackTrace();  
        }      
    }  
  
    /** 
     * �f�[�^�x�[�X���o�[�W�����A�b�v�������ɌĂ΂�܂��B 
     * assets/sql/drop���ɒ�`����Ă���sql�����s���܂��B 
     * ���̌�onCreate()���\�b�h���Ăяo���܂��B 
     */  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {     
        try {  
            execSql(db,"sql/drop");  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        onCreate(db);  
    }  

    /**
     * DB�t�@�C������������
     */
    public boolean initialize() {
    	File file = new File(DB_NAME);
    	if (file.exists()) {
    		return file.delete();
    	}
    	return false;
    }
    
    /**
     * �J�[�h�ꗗ�擾
     * @param db
     * @return
     */
    public ArrayList<CardInfo> selectAllData(SQLiteDatabase db) {
    	ArrayList<CardInfo> retData = new ArrayList<CardInfo>();
    	
    	String sql = "SELECT " +
    				 " cardId," +
    				 " cardName," +
    				 " memo," +
    				 " frontImage," +
    				 " backImage," +
    				 " date," + 
    				 " COALESCE(sortNum, -1) AS sortNum " +
    				 "FROM 'card' " +
    				 "ORDER BY sortNum ASC";
    	
    	
    	Cursor cursor = db.rawQuery(sql, null);
    	
    	while (cursor.moveToNext()) {
    		CardInfo cardInfo = new CardInfo();
    		cardInfo.setCardId(cursor.getString(0));
    		cardInfo.setCardName(cursor.getString(1));
    		cardInfo.setCardMemo(cursor.getString(2));
    		cardInfo.setCardFrontImage(cursor.getBlob(3));
    		cardInfo.setCardBackImage(cursor.getBlob(4));
    		cardInfo.setCardModDate(cursor.getString(5));
    		retData.add(cardInfo);
    	}
    	
    	cursor.close();
    	db.close();
    	return retData;
    }

    /**
     * �J�[�h���o�^
     * @param db
     * @param table
     * @param values
     * @return
     */
    public long insertData(SQLiteDatabase db, String table, ContentValues values) {
    	return db.insert(table, null, values);
    }

    /**
     * �J�[�h���X�V
     * @param db
     * @param table
     * @param strWhere
     * @param whereArgs
     * @param values
     * @return
     */
    public int updateData(SQLiteDatabase db, String table, String strWhere, String[] whereArgs, ContentValues values) {
    	return db.update(table, values, strWhere, whereArgs);
    }

    /**
     * �J�[�h���폜
     * @param db
     * @param table
     * @param strWhere
     * @param strValues
     * @return
     */
    public int deleteData(SQLiteDatabase db, String table, String strWhere, String[] strValues) {
    	return db.delete(table, strWhere, strValues);
    }

    /** 
     * �����Ɏw�肵��assets�t�H���_����sql�����s���܂��B 
     * @param db �f�[�^�x�[�X 
     * @param assetsDir assets�t�H���_���̃t�H���_�̃p�X 
     * @throws IOException 
     */  
    private void execSql(SQLiteDatabase db,String assetsDir) throws IOException {  
        AssetManager as = mContext.getResources().getAssets();      
        try {  
            String files[] = as.list(assetsDir);  
            for (int i = 0; i < files.length; i++) {      
                String str = readFile(as.open(assetsDir + "/" + files[i]));  
                for (String sql: str.split("/")){  
                    db.execSQL(sql);  
                }   
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     * �t�@�C�����當�����ǂݍ��݂܂��B 
     * @param is 
     * @return �t�@�C���̕����� 
     * @throws IOException 
     */  
    private String readFile(InputStream is) throws IOException{  
        BufferedReader br = null;  
        try {  
            br = new BufferedReader(new InputStreamReader(is,"SJIS"));  
  
            StringBuilder sb = new StringBuilder();      
            String str;        
            while((str = br.readLine()) != null){        
                sb.append(str +"\n");       
            }      
            return sb.toString();  
        } finally {  
            if (br != null) br.close();  
        }  
    }  
}

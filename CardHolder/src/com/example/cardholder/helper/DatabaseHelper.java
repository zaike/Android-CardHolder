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
	   
    /* データベース名 */  
	private static final String DB_NAME = "cardHolder.db";//Environment.getExternalStorageDirectory() + File.separator + "cardHolder.db";
    /* データベースのバージョン */  
    private final static int DB_VER = 1;      
    /* コンテキスト */  
    private Context mContext;   
      
    /** 
     * コンストラクタ 
     */  
    public DatabaseHelper(Context context) {  
        super(context, DB_NAME, null, DB_VER);  
        mContext = context;  
    }  
  
    /** 
     * データベースが作成された時に呼ばれます。 
     * assets/sql/create内に定義されているsqlを実行します。 
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
     * データベースをバージョンアップした時に呼ばれます。 
     * assets/sql/drop内に定義されているsqlを実行します。 
     * その後onCreate()メソッドを呼び出します。 
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
     * DBファイル初期化処理
     */
    public boolean initialize() {
    	File file = new File(DB_NAME);
    	if (file.exists()) {
    		return file.delete();
    	}
    	return false;
    }
    
    /**
     * カード一覧取得
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
     * カード情報登録
     * @param db
     * @param table
     * @param values
     * @return
     */
    public long insertData(SQLiteDatabase db, String table, ContentValues values) {
    	return db.insert(table, null, values);
    }

    /**
     * カード情報更新
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
     * カード情報削除
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
     * 引数に指定したassetsフォルダ内のsqlを実行します。 
     * @param db データベース 
     * @param assetsDir assetsフォルダ内のフォルダのパス 
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
     * ファイルから文字列を読み込みます。 
     * @param is 
     * @return ファイルの文字列 
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

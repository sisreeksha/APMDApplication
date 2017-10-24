package com.tenpitech.apmd.apmdsdk.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by rishav on 3/10/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private String TABLE_NAME="TimeStamp";
    private String ID="id";
    private String TIMESTAMP="timestamp";

    public DatabaseHelper(Context context) {
        super(context, "PracticeProject", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        SQLiteDatabase sql=sqLiteDatabase;
        sql.execSQL("create table "+TABLE_NAME+" ( "+ID+" text ,"+TIMESTAMP+" text )");
        for(int i=1;i<=ApplicationStringHolder.MAX_STORAGE;i++){
            ContentValues contentValues=new ContentValues();
            contentValues.put(ID,i+"");
            contentValues.put(TIMESTAMP,"");
            sql.insert(TABLE_NAME,null,contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertResult(String string){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(TIMESTAMP,string);
        ApplicationStringHolder.VAL+=1;
        db.update(TABLE_NAME,contentValues,ID+"=?",new String[]{String.valueOf(ApplicationStringHolder.VAL)});
        Log.i("Test inserted",string);
    }

    public ArrayList<String> getResult(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_NAME,new String[]{TIMESTAMP},null,null,null,null,null);
        ArrayList<String> list=new ArrayList<>();
        while(cursor.moveToNext()){
            if(cursor.getString(0).compareTo("")==0) {
                break;
            }
            list.add(cursor.getString(0));
        }
        //Log.i("Test","getResult");
        return list;
    }

    public void removeResult(String string){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(TIMESTAMP,"");
        db.update(TABLE_NAME,contentValues,TIMESTAMP+"=?",new String[]{string});
        ApplicationStringHolder.VAL-=1;
        //Log.i("Test deleted",string);
    }
}

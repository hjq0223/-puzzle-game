package com.zhidi.flypuzzle.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhidi.flypuzzle.db.MySQLiteOpenHelper;
import com.zhidi.flypuzzle.entity.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/10/30.
 */

public class ScoreDao {

    MySQLiteOpenHelper helper;
    private SQLiteDatabase db;

    public ScoreDao(Context mContext) {
        helper= new MySQLiteOpenHelper(mContext);
    }

    public List<Score> getAllScores(){
        List<Score> mylist = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "select * from tb_scores";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("u_name"));
            int level =cursor.getInt(cursor.getColumnIndex("u_level"));
            int type =cursor.getInt(cursor.getColumnIndex("u_type"));
            int step =cursor.getInt(cursor.getColumnIndex("u_step"));
            int time =cursor.getInt(cursor.getColumnIndex("u_time"));
            mylist.add(new Score(name,level,type,step,time));
        }
        cursor.close();
        db.close();
        return mylist;
    }


    public void addScore(String name, int level,int type,int step,int time) {
        db = helper.getWritableDatabase();
        String sql = "insert into tb_scores (u_name,u_level,u_type,u_step,u_time) values(?,?,?,?,?)";
        db.execSQL(sql, new Object[]{name, level,type,step,time});
        db.close();
    }

    public void clearScore() {
        db = helper.getWritableDatabase();
        String sql = "delete from tb_scores";
        db.execSQL(sql);
        db.close();
    }

}

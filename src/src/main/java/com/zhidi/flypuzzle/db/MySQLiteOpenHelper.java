package com.zhidi.flypuzzle.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2018/10/31.
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public MySQLiteOpenHelper(Context context) {
        super(context, "db_scores", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table tb_scores (id integer primary key autoincrement, u_name varchar(255),u_level int, u_type int,u_step int,u_time int)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

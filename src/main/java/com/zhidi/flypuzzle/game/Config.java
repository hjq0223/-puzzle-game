package com.zhidi.flypuzzle.game;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2018/11/1.
 * 配置
 */

public final class Config {
    private static final String APP_NAME = "拼图游戏";
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_TIME = 1;

    public static final int LEVEL_MIN = 3;
    public static final int LEVEL_NORMAL = 5;
    public static final int LEVEL_MAX = 10;

    public static int gameLevel ; // 难度
    public static int puzzleType = TYPE_IMAGE; //类型
    public static int gameMode = MODE_NORMAL;       //模式
    public static boolean isHighlightPic = false;   //高亮显示未还原的拼板
    public static boolean isGameSound; //声音
    public static boolean isSaveProgress; //进度
    public static boolean isQuestionExit;

    private Config(){}

    /**
     * 保存配置
     * @param context
     */
    public static void saveConfig(Context context){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("gameLevel",gameLevel);
        editor.putInt("puzzleType",puzzleType);
        editor.putInt("gameMode",gameMode);
        editor.putBoolean("isHighlightPic",isHighlightPic);
        editor.putBoolean("isGameSound",isGameSound);
        editor.putBoolean("isSaveProgress",isSaveProgress);
        editor.putBoolean("isQuestionExit",isQuestionExit);
        if(isSaveProgress){
            editor.putInt("progress_step", GameControl.Progress.step);
            editor.putInt("progress_time", GameControl.Progress.time);
            editor.putInt("progress_spacepos", GameControl.Progress.spacePos);
            editor.putString("progress_matrixs", GameControl.Progress.matrix);
        }
        editor.commit();
    }

    /**
     * 获取配置
     * @param context
     */
    public static void getConfig(Context context){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        gameLevel = sp.getInt("gameLevel",LEVEL_NORMAL);
        puzzleType = sp.getInt("puzzleType",TYPE_IMAGE);
        gameMode = sp.getInt("gameMode",MODE_NORMAL);
        isHighlightPic = sp.getBoolean("isHighlightPic",false);
        isGameSound = sp.getBoolean("isGameSound",true);
        isSaveProgress = sp.getBoolean("isSaveProgress",true);
        isQuestionExit = sp.getBoolean("isQuestionExit",false);
        if(isSaveProgress){
            GameControl.Progress.step=sp.getInt("progress_step",0);
            GameControl.Progress.time=sp.getInt("progress_time",0);
            GameControl.Progress.spacePos=sp.getInt("progress_spacepos",0);
            GameControl.Progress.matrix=sp.getString("progress_matrixs",null);
        }

    }

    public static void saveUserImage(Context context,String path){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userImage" + gameLevel,path);
        editor.commit();
    }

    public static String getUserImage(Context context){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        return sp.getString("userImage" + gameLevel,null);
    }

    public static void cancelUserImage(Context context, int imageId){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("userImage" + imageId);
        editor.commit();
    }

    public static void deleteProgress(Context context ){
        SharedPreferences sp = context.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("progress_step");
        editor.remove("progress_time");
        editor.remove("progress_spacepos");
        editor.remove("progress_matrixs");
        editor.commit();
    }
}

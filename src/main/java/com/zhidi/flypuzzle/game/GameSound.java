package com.zhidi.flypuzzle.game;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.zhidi.flypuzzle.R;

/**
 * Created by Administrator on 2018/10/31.
 * 声音
 */

public final class GameSound {

    private static SoundPool mSoundPool;
    private static boolean isInited;
    private static Context mContext;

    public static void m_InitSound(Context mContext) {
        if (isInited) {
            return;
        }
        GameSound.mContext = mContext;
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        isInited = true;
    }

    public static void play(int soundId) {
        mSoundPool.play(soundId, 1, 1, 0, 0, 1);
    }

    public static final class Sound {
        public static final int ID_MOVE = mSoundPool.load(mContext, R.raw.move_pic, 1);
        public static final int ID_SUCCESS = mSoundPool.load(mContext, R.raw.success, 1);
        public static final int ID_CLICK = mSoundPool.load(mContext, R.raw.click, 1);
    }
}

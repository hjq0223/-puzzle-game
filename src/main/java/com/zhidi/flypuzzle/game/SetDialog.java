package com.zhidi.flypuzzle.game;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zhidi.flypuzzle.R;
import com.zhidi.flypuzzle.activity.MainActivity;

import static com.zhidi.flypuzzle.game.Config.MODE_NORMAL;
import static com.zhidi.flypuzzle.game.Config.MODE_TIME;
import static com.zhidi.flypuzzle.game.Config.TYPE_IMAGE;
import static com.zhidi.flypuzzle.game.Config.TYPE_NUMBER;
import static com.zhidi.flypuzzle.game.Config.gameLevel;
import static com.zhidi.flypuzzle.game.Config.gameMode;
import static com.zhidi.flypuzzle.game.Config.isGameSound;
import static com.zhidi.flypuzzle.game.Config.isHighlightPic;
import static com.zhidi.flypuzzle.game.Config.isQuestionExit;
import static com.zhidi.flypuzzle.game.Config.isSaveProgress;
import static com.zhidi.flypuzzle.game.Config.puzzleType;

/**
 * Created by Administrator on 2018/11/1.
 */

public class SetDialog implements View.OnClickListener {

    private PopupWindow pw;
    private Context mContext;
    private int layoutId;
    private SeekBar sb_level;
    private TextView tv_level;
    private RadioGroup rg_puzzle_type, rg_game_mode;
    private CheckBox cb_highlight_pic,cb_sound,cb_save_progress,cb_question_exit;
    private Button bn_ok, bn_cancel,bn_custom_bg;
    private View mView;

    public SetDialog(Context mContext, int layoutId) {
        this.mContext = mContext;
        this.layoutId = layoutId;
        initView();
        initData();
    }

    private void initData() {
        sb_level.setProgress(gameLevel - 3);
        rg_puzzle_type.check(Config.puzzleType == Config.TYPE_IMAGE ? R.id.rb_type_image : R.id.rb_type_number);
        rg_game_mode.check(gameMode == 0 ? R.id.rb_mode_normal : R.id.rb_mode_time);
        cb_highlight_pic.setChecked(isHighlightPic);
        cb_sound.setChecked(isGameSound);
        cb_save_progress.setChecked(isSaveProgress);
        cb_question_exit.setChecked(isQuestionExit);
    }

    private void initView() {
        mView = LayoutInflater.from(mContext).inflate(layoutId, null);
        pw = new PopupWindow(mView, LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //pw.setBackgroundDrawable(new ColorDrawable(0x00000000));
        pw.setOutsideTouchable(false);
        sb_level = (SeekBar) mView.findViewById(R.id.sb_level);
        tv_level = (TextView) mView.findViewById(R.id.tv_level);
        rg_puzzle_type = (RadioGroup) mView.findViewById(R.id.rg_puzzle_type);
        rg_game_mode = (RadioGroup) mView.findViewById(R.id.rg_game_mode);
        cb_highlight_pic= (CheckBox) mView.findViewById(R.id.cb_highlight_pic);
        cb_sound= (CheckBox) mView.findViewById(R.id.cb_sound);
        cb_save_progress= (CheckBox) mView.findViewById(R.id.cb_save_progress);
        cb_question_exit= (CheckBox) mView.findViewById(R.id.cb_question_exit);

        bn_ok = (Button) mView.findViewById(R.id.bn_ok);
        bn_cancel = (Button) mView.findViewById(R.id.bn_cancel);
        bn_custom_bg = (Button) mView.findViewById(R.id.bn_custom_bg);

        bn_ok.setOnClickListener(this);
        bn_cancel.setOnClickListener(this);
        bn_custom_bg.setOnClickListener(this);

        sb_level.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_level.setText(String.valueOf(progress + 3));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void show() {
        pw.showAtLocation(new View(mContext), Gravity.CENTER, 0, 0);
    }

    public void close() {
        pw.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_ok:
                boolean isChangeSet = false;
                boolean isChangeHighlightPic = false;

                int level = sb_level.getProgress();
                int id = rg_puzzle_type.getCheckedRadioButtonId();
                int type = id == R.id.rb_type_image ? TYPE_IMAGE : TYPE_NUMBER;
                id = rg_game_mode.getCheckedRadioButtonId();
                int mode = id == R.id.rb_mode_normal ? MODE_NORMAL : MODE_TIME;
                boolean highlight =cb_highlight_pic.isChecked();
                isGameSound =cb_sound.isChecked();
                boolean progress=cb_save_progress.isChecked();
                isQuestionExit=cb_question_exit.isChecked();

                if (gameLevel != level+3 || puzzleType != type || gameMode != mode) {
                    gameLevel = level+3;
                    puzzleType = type;
                    gameMode = mode;
                    isChangeSet = true;
                }
                if(highlight!=isHighlightPic){
                    isHighlightPic=highlight;
                    isChangeHighlightPic=true;
                }
                if(isGameSound){
                    GameSound.m_InitSound(mContext);
                }
                if (isChangeSet) {
                    ((MainActivity) mContext).m_InitGame(true);
                }else{
                     if(isChangeHighlightPic){
                        GameControl.updateAllPicState();
                     }
                }
                if(isSaveProgress!=progress){
                    isSaveProgress=progress;
                    if(!isSaveProgress){
                        Config.deleteProgress(mContext);
                    }
                }
                break;
            case R.id.bn_cancel:
                break;
            case R.id.bn_custom_bg:
                ((MainActivity) mContext).showSelectBkgoundDlg();
                break;
        }
        pw.dismiss();
    }
}

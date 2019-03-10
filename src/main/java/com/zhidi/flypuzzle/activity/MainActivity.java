package com.zhidi.flypuzzle.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhidi.flypuzzle.R;
import com.zhidi.flypuzzle.adapter.ScoreLvwAdapter;
import com.zhidi.flypuzzle.dao.ScoreDao;
import com.zhidi.flypuzzle.entity.Pic;
import com.zhidi.flypuzzle.entity.Score;
import com.zhidi.flypuzzle.game.Config;
import com.zhidi.flypuzzle.game.GameControl;
import com.zhidi.flypuzzle.game.GameSound;
import com.zhidi.flypuzzle.game.SetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static com.zhidi.flypuzzle.game.Config.LEVEL_MAX;
import static com.zhidi.flypuzzle.game.Config.LEVEL_MIN;
import static com.zhidi.flypuzzle.game.Config.MODE_NORMAL;
import static com.zhidi.flypuzzle.game.Config.MODE_TIME;
import static com.zhidi.flypuzzle.game.Config.TYPE_IMAGE;
import static com.zhidi.flypuzzle.game.Config.gameLevel;
import static com.zhidi.flypuzzle.game.Config.gameMode;
import static com.zhidi.flypuzzle.game.Config.isGameSound;
import static com.zhidi.flypuzzle.game.Config.isHighlightPic;
import static com.zhidi.flypuzzle.game.Config.isQuestionExit;
import static com.zhidi.flypuzzle.game.Config.isSaveProgress;
import static com.zhidi.flypuzzle.game.Config.puzzleType;
import static com.zhidi.flypuzzle.game.GameControl.Location;
import static com.zhidi.flypuzzle.game.GameControl.PADDING;
import static com.zhidi.flypuzzle.game.GameControl.bmpImage;
import static com.zhidi.flypuzzle.game.GameControl.cellSize;
import static com.zhidi.flypuzzle.game.GameControl.drawImage;
import static com.zhidi.flypuzzle.game.GameControl.frameWidth;
import static com.zhidi.flypuzzle.game.GameControl.highlightPic;
import static com.zhidi.flypuzzle.game.GameControl.imageHeight;
import static com.zhidi.flypuzzle.game.GameControl.imageSize;
import static com.zhidi.flypuzzle.game.GameControl.imageWidth;
import static com.zhidi.flypuzzle.game.GameControl.listPics;
import static com.zhidi.flypuzzle.game.GameControl.orderPoses;
import static com.zhidi.flypuzzle.game.GameControl.picCount;
import static com.zhidi.flypuzzle.game.GameControl.picMax;
import static com.zhidi.flypuzzle.game.GameControl.screenWidth;
import static com.zhidi.flypuzzle.game.GameControl.showMsg;
import static com.zhidi.flypuzzle.game.GameControl.sortList;
import static com.zhidi.flypuzzle.game.GameControl.spacing;
import static com.zhidi.flypuzzle.game.GameControl.stepCount;
import static com.zhidi.flypuzzle.game.GameControl.useTime;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private static final int REQUEST_PICK = 1;
    private static final int REQUEST_CROP = 2;
    private static final int REQUEST_PERMISSION = 3;

    private FrameLayout fl_main;
    private LinearLayout.LayoutParams lpFrame;
    private ImageView iv_preview;

    private Button bn_start, bn_set, bn_about, bn_exit, bn_save;
    private TextView tv_step, tv_time, tv_scores;

    private SetDialog setDlg;

    private boolean isGameBegined = false;
    private Handler mHandler;
    private Timer mTimer;
    private ScoreDao mScoreDao;
    private boolean isPause = false;
    private File cropFile;

    /**
     * 入口点
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config.getConfig(this); //配置
        m_InitPermissions();  //权限

        mScoreDao = new ScoreDao(this);

        m_InitView();
        m_InitSet();
        m_initEvent();

        m_InitGame(true);

        if (isSaveProgress) {
            readProgress(); //读取进度
        }

        updateGame();
    }

    /**
     * 初始化游戏
     */
    public void m_InitGame(boolean restartGetImage) {
        m_InitParams();
        m_InitPic(restartGetImage);
        startGame();
        updateGame();
    }

    private void updateGame() {
        updateState();
        updateTimeShow();
        isGameBegined = true;
    }

    public void m_InitGame() {
        m_InitGame(false);
    }

    /**
     * 初始化设置
     */
    private void m_InitSet() {
        fl_main.setPadding(PADDING, PADDING, PADDING, PADDING);
        fl_main.setBackgroundColor(Color.WHITE);
        listPics = new ArrayList<>();
        if (isGameSound) {
            GameSound.m_InitSound(this);
        }
    }

    /**
     * 初始化事件
     */
    private void m_initEvent() {
        bn_start.setOnClickListener(this);
        bn_set.setOnClickListener(this);
        bn_save.setOnClickListener(this);
        bn_about.setOnClickListener(this);
        bn_exit.setOnClickListener(this);
        iv_preview.setOnClickListener(this);
        tv_scores.setOnClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void m_InitView() {
        fl_main = getViewById(R.id.fl_main);
        iv_preview = getViewById(R.id.iv_preview);

        bn_start = getViewById(R.id.bn_start);
        bn_set = getViewById(R.id.bn_set);
        bn_save = getViewById(R.id.bn_save);
        bn_about = getViewById(R.id.bn_about);
        bn_exit = getViewById(R.id.bn_exit);

        tv_step = getViewById(R.id.tv_step);
        tv_time = getViewById(R.id.tv_time);
        tv_scores = getViewById(R.id.tv_scores);
    }

    /**
     * 初始化参数
     */
    private void m_InitParams() {
        //基本参数
        picCount = gameLevel * gameLevel;
        picMax = picCount - 1;
        screenWidth = GameControl.getScreenWidth(this);
        spacing = 0;
        frameWidth = screenWidth - spacing * 2;
        cellSize = frameWidth / gameLevel;
        imageSize = cellSize;
        //初始化框架
        lpFrame = (LinearLayout.LayoutParams) fl_main.getLayoutParams();
        lpFrame.width = frameWidth;
        lpFrame.height = frameWidth;
        fl_main.setLayoutParams(lpFrame);
        stepCount = 0;
        useTime = 0;
    }

    /**
     * 更新计时状态
     */
    private void updateTimeShow() {
        stopTimer(); //先停止
        if (gameMode == MODE_NORMAL) {
            tv_time.setVisibility(View.INVISIBLE);
            tv_time.setText("");
        } else {
            tv_time.setVisibility(View.VISIBLE);
            updateUseTime();
            startTimer(); //计时
        }
    }


    /**
     * 开始计时
     */
    private void startTimer() {
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 1:
                            if (!isPause) {
                                useTime++;
                            }
                            //Log.e("tag",useTime +"");
                            updateUseTime();
                            break;
                    }
                }
            };
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        }, 1000, 1000);
    }

    /**
     * 停止计时
     */
    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    /**
     * 更新时间
     */
    private void updateUseTime() {
        if (useTime >= 3600) {
            stopTimer();
            isGameBegined = false;
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("超时").create().show();
            useTime = 0;
        }
        tv_time.setText(getString(R.string.used_time) + GameControl.getTimeText(useTime));
    }

    /**
     * 初始化图片
     *
     * @param restartGetImage
     */
    private void m_InitPic(boolean restartGetImage) {
        fl_main.removeAllViews();
        listPics.clear();
        if (puzzleType == TYPE_IMAGE) {
            //图片
            if (restartGetImage) {
                bmpImage = null;
            }
            if (bmpImage == null) {
                m_InitBackgroundImage();
            }
            int x = 0, y = 0;
            Bitmap bmpCell = null;
            for (int i = 0; i < picCount; i++) {
                ImageView iv_image = MakeView();
                fl_main.addView(iv_image);
                bmpCell = GameControl.getCellImageByPos(i, false);
                iv_image.setScaleType(ImageView.ScaleType.FIT_XY);
                iv_image.setImageBitmap(bmpCell);
                iv_image.setBackgroundColor(Color.WHITE);
                iv_image.setTag(i);
                iv_image.setOnTouchListener(this);
                Pic pic = new Pic(iv_image, i, i);
                listPics.add(pic);
            }
        } else {
            //数字
            if (drawImage == null || restartGetImage) {
                drawImage = ContextCompat.getDrawable(this, R.drawable.icon);
                iv_preview.setImageDrawable(drawImage);
                iv_preview.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            for (int i = 0; i < picCount; i++) {
                TextView tv_image = MakeView();
                fl_main.addView(tv_image);
                tv_image.setGravity(Gravity.CENTER);
                tv_image.setText(String.valueOf(i + 1));
                tv_image.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (imageSize / 2.5f));
                tv_image.setTextColor(Color.WHITE);
                tv_image.setBackgroundColor(getRandomBgColor());
                tv_image.setTag(i);
                tv_image.setOnTouchListener(this);
                Pic pic = new Pic(tv_image, i, i);
                listPics.add(pic);
            }
        }

    }


    /**
     * 初始化背景图片
     */
    private void m_InitBackgroundImage() {
        String path = Config.getUserImage(this);
        boolean isRequest = false;
        if (path != null && !path.equals("")) {
            getUserBgImage(path);
        }
        getSystemBgImage();
    }

    /**
     * 请求权限
     */
    private void m_InitPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int granted = PackageManager.PERMISSION_GRANTED;
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != granted ||
                    ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != granted) {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    /**
     * 获取用户图像
     *
     * @param path
     */
    private void getUserBgImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            bmpImage = BitmapFactory.decodeFile(path);
        }
    }

    /**
     * 返回
     *
     * @param keyCode
     * @param event
     * @return
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isQuestionExit) {
            new AlertDialog.Builder(this)
                    .setTitle("确定退出？")
                    .setPositiveButton("取消", null)
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取系统默认图像
     */
    private void getSystemBgImage() {
        if (bmpImage == null) {
            int id = 0;
            switch (gameLevel) {
                case 3:
                case 5:
                    id = R.drawable.bg_1;
                    break;
                case 4:
                    id = R.drawable.bg_5;
                    break;
                case 6:
                    id = R.drawable.bg_2;
                    break;
                case 7:
                    id = R.drawable.bg_3;
                    break;
                case 8:
                    id = R.drawable.bg_7;
                    break;
                case 10:
                    id = R.drawable.bg_4;
                    break;
                case 9:
                    id = R.drawable.bg_6;
                    break;
                default:
                    id = R.drawable.bg_1;
                    break;
            }
            bmpImage = BitmapFactory.decodeResource(getResources(), id);
        }
        iv_preview.setImageBitmap(bmpImage);
        iv_preview.setScaleType(ImageView.ScaleType.FIT_XY);
        imageWidth = (bmpImage.getWidth() + PADDING) / gameLevel;
        imageHeight = (bmpImage.getHeight() + PADDING) / gameLevel;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean permission = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permission = false;
                    break;
                }
            }
            if (!permission) {
                showMsg(this, "获取权限失败");
            }
        }
    }


    /**
     * 获取随机颜色
     *
     * @return
     */
    private int getRandomBgColor() {
        int index = (int) (GameControl.BG_COLORS.length * Math.random());
        return GameControl.BG_COLORS[index];
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        List<Integer> listPoses = new ArrayList<>();
        orderPoses = new int[picMax];
        for (int i = 0; i < picMax; i++) {
            listPoses.add(i);
        }
        for (int i = 0; i < picMax; i++) {
            int index = (int) (listPoses.size() * Math.random());
            int pos = listPoses.get(index);
            setImagePos(listPics.get(i), pos, true);
            if (isHighlightPic && i != pos) {
                highlightPic(listPics.get(i), true);
            }
            listPoses.remove(index);
            orderPoses[pos] = i;
            //Log.e("abc",pos + "\t" + i);
        }
        //最后一个空白元素
        setImagePos(listPics.get(picMax), picMax);
        getViewByPos(picMax).setVisibility(GONE);
        Pic.setSpacePos(picMax);

        //测试用
        /*  int[] pos = {7,2,3,4,5,6,8,1};
        for (int i = 0; i < picMax; i++) {
            setImagePos(listPics.get(pos[i]-1),i);
        }*/

        if (isNoSolution()) {
            //逆序数为奇排列，无解，交换任意两元素位置，成为偶排列
            //Log.e("msg","无解");
            setImagePos(listPics.get(orderPoses[0]), picMax - 1);
            setImagePos(listPics.get(orderPoses[picMax - 1]), 0);
        }

    }

    /**
     * 是否无解
     *如果这个拼图序列中存在的逆序为偶数，那么这个拼图可以被还原到正确的顺序，
     * 如果不为偶数，那么不能被正确还原
     * @return
     */
    private boolean isNoSolution() {
        int count = 0;
        for (int i = 0; i < picMax - 1; i++) {
            for (int j = i + 1; j < picMax; j++) {
                if (orderPoses[i] > orderPoses[j]) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }

    private View getViewByPos(int position) {
        return listPics.get(position).getV_image();
    }


    private void setImagePos(Pic pic, int position) {
        setImagePos(pic, position, false);
    }

    /**
     * 设置图像坐标
     *
     * @param pic
     * @param position
     */
    private void setImagePos(Pic pic, int position, boolean isNoHighlight) {
        pic.setCurPosition(position);
        Point mPoint = getPointByPosition(position);
        View mView = pic.getV_image();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mView.getLayoutParams();
        lp.leftMargin = mPoint.x;
        lp.topMargin = mPoint.y;
        if (puzzleType == TYPE_IMAGE) {
            lp.width = imageSize;
            lp.height = imageSize;
        } else {
            lp.width = imageSize - PADDING * 2;
            lp.height = imageSize - PADDING * 2;
        }
        mView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mView.setLayoutParams(lp);
        mView.setVisibility(View.VISIBLE);
        if (!isNoHighlight) {
            if (isHighlightPic) {
                updateImage(pic);
            }
        }
    }

    /**
     * 通过坐标获取点
     *
     * @param position
     * @return
     */
    private Point getPointByPosition(int position) {
        int x = position % gameLevel * cellSize;
        int y = position / gameLevel * cellSize;
        return new Point(x, y);
    }

    /**
     * 图片是否能移动
     *
     * @param pic
     * @return
     */
    private boolean isEnabledMove(Pic pic) {
        for (int i = 0; i < 4; i++) {
            if (locationIsSpace(pic, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方向是否为空坐标
     */
    private boolean locationIsSpace(Pic pic, int location) {
        int pos = pic.getCurPosition();
        boolean isSpace = false;
        switch (location) {
            case Location.LEFT:
                if (pos % gameLevel != 0) {
                    isSpace = --pos == Pic.getSpacePos();
                }
                break;
            case Location.RIGHT:
                if ((pos + 1) % gameLevel != 0) {
                    isSpace = ++pos == Pic.getSpacePos();
                }
                break;
            case Location.TOP:
                pos -= gameLevel;
                isSpace = pos == Pic.getSpacePos();
                break;
            case Location.BOTTOM:
                pos += gameLevel;
                isSpace = pos == Pic.getSpacePos();
                break;
        }
        return isSpace;
    }

    private <T extends View> T MakeView() {
        View mView = null;
        if (puzzleType == TYPE_IMAGE) {
            mView = new ImageView(this);
        } else {
            mView = new TextView(this);
        }
        return (T) mView;
    }


    /**
     * 退出
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isSaveProgress) {
            saveProgress();
        }
        Config.saveConfig(this); //保存配置
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameMode == MODE_TIME) {
            stopTimer();
        }
    }

    /**
     * findViewById
     *
     * @param id
     * @param <T>
     * @return
     */
    private <T extends View> T getViewById(int id) {
        return (T) findViewById(id);
    }

    /**
     * 用户点击
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isGameBegined) {
                int position = (int) v.getTag();
                Pic pic = listPics.get(position);
                if (isEnabledMove(pic)) {
                    movePic(pic); //移动图片
                    if (isSuccess()) {
                        //成功
                        isGameBegined = false;
                        if (gameMode == MODE_TIME) {
                            stopTimer();
                        }
                        getViewByPos(picMax).setVisibility(View.VISIBLE);
                        //Toast.makeText(this, "获胜！", Toast.LENGTH_SHORT).show();
                        playSound(GameSound.Sound.ID_SUCCESS);
                        showSuccessMsg();
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 显示成功消息
     */
    private void showSuccessMsg() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.success_title)
                .setMessage(getString(R.string.used) + stepCount + " " + getString(R.string.step) + (gameMode == MODE_NORMAL ? "" : "\n" + getUseTimeText()))
                .setPositiveButton(R.string.s_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_InitGame();
                    }
                })
                .setNegativeButton(R.string.s_next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (gameLevel < LEVEL_MAX) {
                            gameLevel++;
                        } else {
                            gameLevel = LEVEL_MIN;
                        }
                        m_InitGame(true);
                    }
                })
                .setNeutralButton(R.string.save_score, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveUserName();
                    }
                })
                .create()
                .show();
    }

    /**
     * 保存用户名
     */
    private void saveUserName() {
        AlertDialog.Builder mDlg = new AlertDialog.Builder(this);
        View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_user_dlg, null);
        final EditText et_username = (EditText) mView.findViewById(R.id.et_username);
        mDlg.setView(mView);
        mDlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = et_username.getText().toString().trim();
                if (!"".equals(name)) {
                    mScoreDao.addScore(name, gameLevel, puzzleType, stepCount, useTime);
                }
            }
        });
        mDlg.show();
    }

    private String getUseTimeText() {
        return getString(R.string.used_time) + " " + useTime / 60 + " " + getString(R.string.s_minute) + " " + (useTime % 60) + " " + getString(R.string.s_second);
    }

    private void updateState() {
        tv_step.setText(getString(R.string.stepcount) + stepCount);
    }

    private void updateImage(Pic pic) {
        if (pic.getPosition() == pic.getCurPosition()) {
            highlightPic(pic, false);
        } else {
            highlightPic(pic, true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_start:
                playSound(GameSound.Sound.ID_CLICK);
                m_InitGame();
                break;
            case R.id.bn_set:
                setDlg = new SetDialog(this, R.layout.set_dlg);
                setDlg.show();
                break;
            case R.id.bn_save:
                isSaveProgress = true;
                saveProgress();
                Config.saveConfig(this);
                Toast.makeText(this, "进度已保存", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bn_about:
                showAbout();
                break;
            case R.id.bn_exit:
                finish();
                break;
            case R.id.iv_preview:
                if (puzzleType == TYPE_IMAGE) {
                    showSelectBkgoundDlg();
                }
                break;
            case R.id.tv_scores:
                showScoreDialog();
                break;
        }
    }

    /**
     * 显示分数对话框
     */
    private void showScoreDialog() {
        View mView = LayoutInflater.from(this).inflate(R.layout.score_dlg, null);
        ListView lv_scores = (ListView) mView.findViewById(R.id.lv_scores);
        List listScores = mScoreDao.getAllScores();
        if (listScores.size() != 0) {
            sortList(listScores);
        }
        String[] columns = getResources().getStringArray(R.array.score_title);
        Score score = new Score(columns[0], columns[1], columns[2], columns[3], columns[4]);
        listScores.add(0, score);
        lv_scores.setAdapter(new ScoreLvwAdapter(this, listScores));
        AlertDialog mDlg = new AlertDialog.Builder(this)
                .setView(mView)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mScoreDao.clearScore();
                    }
                })
                .create();
        mDlg.show();
    }


    /**
     * 关于
     */
    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name) + " " + getVersion())
                .setIcon(R.drawable.icon)
                .setMessage(getString(R.string.author) )
                .setNeutralButton(R.string.access_web, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.fly_url))));
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create().show();
    }

    /**
     * 获取app版本
     *
     * @return
     */
    private String getVersion() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info != null ? info.versionName : "";
    }

    /**
     * 选择背景
     */
    public void showSelectBkgoundDlg() {
        AlertDialog mDlg = new AlertDialog.Builder(this)
                .setMessage(R.string.sel_image_bg)
                .setPositiveButton(R.string.custom, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent data = new Intent();
                        data.setAction(Intent.ACTION_PICK);
                        data.setType("image/*");
                        startActivityForResult(data, REQUEST_PICK);
                    }
                })
                .setNegativeButton(R.string.restore_def, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Config.cancelUserImage(getApplicationContext(), gameLevel);
                        m_InitGame(true);
                    }
                })
                .setNeutralButton(R.string.restore_def_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(R.string.restore_confirm)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (int j = LEVEL_MIN; j <= LEVEL_MAX; j++) {
                                            Config.cancelUserImage(getApplicationContext(), j);
                                        }
                                        m_InitGame(true);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .create().show();

                    }
                })
                .create();
        mDlg.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK:
                    Uri mUri = data.getData();
                    mUri = GameControl.getRealUri(this, mUri);
                    cropImage(mUri);
                    break;
                case REQUEST_CROP:
                    if (cropFile != null) {
                        Config.saveUserImage(this, cropFile.getAbsolutePath());
                        puzzleType = Config.TYPE_IMAGE;
                        m_InitGame(true);
                    } else {
                        GameControl.showMsg(this, R.string.createfile_failed);
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图像
     *
     * @param mUri
     */
    private void cropImage(Uri mUri) {
        Intent data = new Intent("com.android.camera.action.CROP");
        data.setDataAndType(mUri, "image/*");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            data.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            data.putExtra("noFaceDetection", true);//去除默认的人脸识别，否则和剪裁匡重叠
        }
        data.putExtra("crop", "true");
        if (Build.BRAND.equals("HUAWEI")) {
            data.putExtra("aspectX", 9998);
            data.putExtra("aspectY", 9999);
        } else {
            data.putExtra("aspectX", 1);
            data.putExtra("aspectY", 1);
        }
        //data.putExtra("outputX",1080);
        //data.putExtra("outputY",1080);
        data.putExtra("return-data", false);
        cropFile = getImageFile();
        data.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropFile));
        startActivityForResult(data, REQUEST_CROP);
    }



    private File getImageFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory() + "/yuanfang235/flypuzzle");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return new File(dir, "bg_" + gameLevel + ".jpg");
        }
        return null;
    }

    /**
     * 移动图片
     *
     * @param pic
     */
    private void movePic(Pic pic) {
        stepCount++; //每移动一次，步数加一
        int curPos = pic.getCurPosition();
        int spacePos = pic.getSpacePos();
        pic.setSpacePos(curPos);
        setImagePos(pic, spacePos);
        updateState();
        playSound(GameSound.Sound.ID_MOVE);
    }

    private void playSound(int id) {
        if (isGameSound) {
            GameSound.play(id);
        }
    }

    /**
     * 获胜
     *
     * @return
     */
    private boolean isSuccess() {
        for (Pic pic : listPics) {
            if (pic.getPosition() != pic.getCurPosition()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 保存进度
     */
    public void saveProgress() {
        GameControl.Progress.step = stepCount;
        GameControl.Progress.time = useTime;
        GameControl.Progress.spacePos = Pic.getSpacePos();
        StringBuffer pics = new StringBuffer();
        for (int i = 0; i < picMax; i++) {
            pics.append(listPics.get(i).getCurPosition() + "|");
        }
        GameControl.Progress.matrix = pics.toString();
    }

    /**
     * 读取进度
     */
    public void readProgress() {
        if (GameControl.Progress.matrix != null) {
            stepCount = GameControl.Progress.step;
            useTime = GameControl.Progress.time;
            Pic.setSpacePos(GameControl.Progress.spacePos);
            String[] matrixs = GameControl.Progress.matrix.split("\\|");
            if (matrixs != null) {
                //List<Integer> listMatrixs = new ArrayList<>();
                //Log.e("tag", matrixs.length + "");
                for (int i = 0; i < picMax; i++) {
                    setImagePos(listPics.get(i), Integer.parseInt(matrixs[i]));
                }
            }
            Config.deleteProgress(this);
        }
    }

}

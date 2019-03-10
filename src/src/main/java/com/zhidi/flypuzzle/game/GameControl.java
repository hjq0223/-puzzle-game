package com.zhidi.flypuzzle.game;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhidi.flypuzzle.entity.Pic;
import com.zhidi.flypuzzle.entity.Score;

import java.io.File;
import java.util.List;

import static com.zhidi.flypuzzle.game.Config.TYPE_IMAGE;
import static com.zhidi.flypuzzle.game.Config.gameLevel;
import static com.zhidi.flypuzzle.game.Config.isHighlightPic;
import static com.zhidi.flypuzzle.game.Config.puzzleType;

/**
 * Created by Administrator on 2018/11/1.
 */

public final class GameControl {

    public static final int BG_COLORS[] = {0xFF0093A4, 0xff00806A, 0xffFA6E00, 0xffFE4080};
    public static final int PADDING = 1;

    public static int picCount;
    public static int picMax;
    public static int screenWidth;
    public static int cellSize;
    public static int imageSize;
    public static int spacing;
    public static int frameWidth;
    public static int stepCount;
    public static int useTime;

    public static int imageWidth, imageHeight;

    public static Drawable drawImage;
    public static Bitmap bmpImage;

    public static List<Pic> listPics;
    public static int[] orderPoses;
    public static int spacePos;

    /**
     * 方位
     */
    public static final class Location {
        public static final int LEFT = 0;
        public static final int TOP = 1;
        public static final int RIGHT = 2;
        public static final int BOTTOM = 3;
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static <T extends View> T getViewById(AppCompatActivity activity, int id) {
        return (T) activity.findViewById(id);
    }

    /**
     * 高亮显示图像
     *
     * @param pic
     * @param isHighlight
     */
    public static void highlightPic(Pic pic, boolean isHighlight) {
        if (puzzleType == TYPE_IMAGE) {
            Bitmap bmp = getCellImageByPos(pic.getPosition(), isHighlight);
            ((ImageView) pic.getV_image()).setImageBitmap(bmp);
        } else {
            /*TextView tv = (TextView) pic.getV_image();
            ColorDrawable drawable = (ColorDrawable) tv.getBackground();
            int color = drawable.getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            if (isHighlight) {
                hsv[1] = 0.2f;
                hsv[2] = 0.5f;
            } else {
                hsv[1] = 1f;
                hsv[2] = 0.75f;
            }
            color = Color.HSVToColor(hsv);
            tv.setBackgroundColor(color);*/
        }

    }

    /**
     * 更新所有高亮
     */
    public static void updateAllPicState() {
        for (Pic pic : listPics) {
            if (isHighlightPic && pic.getPosition() != pic.getCurPosition()) {
                highlightPic(pic, true);
            } else {
                highlightPic(pic, false);
            }
        }
    }

    /**
     * 根据坐标获取单元图像
     *
     * @param pos
     * @param isHighLight
     * @return
     */
    public static Bitmap getCellImageByPos(int pos, boolean isHighLight) {
        int x = pos % gameLevel * imageWidth;
        int y = pos / gameLevel * imageHeight;
        Bitmap bmpCell = Bitmap.createBitmap(bmpImage, x, y, imageWidth - PADDING, imageHeight - PADDING);
        if (isHighLight) {
            bmpCell = makeHighLightImage(bmpCell);
        }
        return bmpCell;
    }

    /**
     * 计算高亮图像
     *
     * @param srcBitmap
     * @return
     */
    public static Bitmap makeHighLightImage(Bitmap srcBitmap) {
        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight,
                Bitmap.Config.ARGB_8888);
        ColorMatrix cMatrix = new ColorMatrix();
        //亮度
        float brightness = 30;
        float lum = (brightness - 50) * 2 * 255 * 0.01f;

        cMatrix.set(new float[]{1, 0, 0, 0, lum, 0,
                1, 0, 0, lum,// 改变对比度
                0, 0, 1, 0, lum, 0, 0, 0, 1, 0
        });

        //对比度
        float contrast = 20;
        float scale = 0;
        if (contrast < 50) {
            scale = contrast / 50f;
        } else if (contrast > 50) {
            scale = (contrast - 50) / 50f * 2.5f + 1;
        }
        lum = 256 * brightness / 100 * (1f - scale);
        cMatrix.set(new float[]{
                scale, 0, 0, 0, lum,
                0, scale, 0, 0, lum,
                0, 0, scale, 0, lum,
                0, 0, 0, 1, 0
        });

        //cMatrix.setSaturation(0.0f);   //饱和度

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return bmp;
    }

    /**
     * 获取有效Uri
     *
     * @param uri
     * @return
     */
    public static Uri getRealUri(Context mContext, Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            return uri;
        } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            return uri;
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            final String _DATA = MediaStore.Images.ImageColumns.DATA;
            Cursor cursor = mContext.getContentResolver().query(uri, new String[]{_DATA}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(_DATA));
                File file = new File(path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判断版本是否在7.0以上
                    uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".FileProvider", file);

                } else {
                    uri = Uri.fromFile(file);
                }
                return uri;
            }
        }
        return null;
    }

    public static String getTimeText(int time) {
        int minute = time / 60;
        int second = time % 60;
        String sMinute = "" + (minute >= 10 ? minute : "0" + minute);
        String sSecond = "" + (second >= 10 ? second : "0" + second);
        return sMinute + ":" + sSecond;
    }

    public static void sortList(List<Score> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                int x = new Integer(list.get(j).getStep());
                int y = new Integer(list.get(j + 1).getStep());
                if (x > y) {
                    Score swap = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, swap);
                }
            }
        }
    }

    /**
     * 保存进度
     */


    public static final class Progress {
        public static int step;
        public static int time;
        public static int spacePos;
        public static String matrix;
    }

    public static void showMsg(Context mContext, CharSequence msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showMsg(Context mContext, int resId){
        Toast.makeText(mContext, mContext.getString(resId), Toast.LENGTH_SHORT).show();
    }
}

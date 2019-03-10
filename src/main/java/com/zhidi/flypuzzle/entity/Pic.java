package com.zhidi.flypuzzle.entity;

import android.view.View;

/**
 * Created by Administrator on 2018/10/30.
 */

public class Pic<V extends View> {

    private static int spacePos;

    private V v_image;
    private int position;
    private int curPosition;

    public Pic() {
    }

    public Pic(V v_image, int position, int curPosition) {
        this.v_image = v_image;
        this.position = position;
        this.curPosition = curPosition;
    }

    public static int getSpacePos() {
        return spacePos;
    }

    public static void setSpacePos(int spacePos) {
        Pic.spacePos = spacePos;
    }

    public V getV_image() {
        return v_image;
    }

    public void setV_image(V v_image) {
        this.v_image = v_image;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCurPosition() {
        return curPosition;
    }

    public void setCurPosition(int curPosition) {
        this.curPosition = curPosition;
    }



}

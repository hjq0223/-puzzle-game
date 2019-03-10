package com.zhidi.flypuzzle.entity;

/**
 * Created by Administrator on 2018/10/30.
 */

public class Score {
    private String name;
    private String level;
    private String type;
    private String step;
    private String time;
    public Score() { }

    public Score(String name, Object level,Object type, Object step, Object time) {
        this.name = name;
        this.level = level+"";
        this.type= type+"";
        this.step = step+"";
        this.time = time+"";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }
}

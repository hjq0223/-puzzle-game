package com.zhidi.flypuzzle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhidi.flypuzzle.R;
import com.zhidi.flypuzzle.entity.Score;
import com.zhidi.flypuzzle.game.Config;
import com.zhidi.flypuzzle.game.GameControl;

import java.util.List;

/**
 * Created by Administrator on 2018/10/31.
 */

public class ScoreLvwAdapter extends BaseAdapter {

    private Context mContext;
    private List<Score> listScores;

    public ScoreLvwAdapter(Context mContext, List<Score> listScores) {
        this.mContext = mContext;
        this.listScores = listScores;
    }

    @Override
    public int getCount() {
        return listScores == null ? 0 : listScores.size();
    }

    @Override
    public Object getItem(int position) {
        return listScores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.score_item, null);
            //holder.tv_ranking = (TextView) convertView.findViewById(R.id.tv_ranking);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_level = (TextView) convertView.findViewById(R.id.tv_level);
            holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            holder.tv_step = (TextView) convertView.findViewById(R.id.tv_step);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Score score = listScores.get(position);
        if (position == 0) {
            //holder.tv_ranking.setText("名次");
            holder.tv_name.setText(score.getName());
            holder.tv_level.setText(score.getLevel());
            holder.tv_type.setText(score.getType());
            holder.tv_step.setText(score.getStep());
            holder.tv_time.setText(score.getTime());
        } else {
            //holder.tv_ranking.setText(position+"");
            holder.tv_name.setText(score.getName());
            holder.tv_level.setText(score.getLevel() + "级");
            holder.tv_type.setText(new Integer(score.getType()).equals(Config.TYPE_IMAGE) ? "pic":"num");
            holder.tv_step.setText(score.getStep() + "");
            int time = new Integer(score.getTime());
            holder.tv_time.setText(time == 0 ? "*" : GameControl.getTimeText(time));
        }

        return convertView;
    }

    private class ViewHolder {
        TextView tv_ranking,tv_name, tv_level, tv_type,tv_step, tv_time;
    }
}

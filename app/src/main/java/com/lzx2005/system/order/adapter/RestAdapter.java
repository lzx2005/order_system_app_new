package com.lzx2005.system.order.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lzx2005.system.order.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by john on 2017/5/12.
 */

public class RestAdapter extends BaseAdapter{

    private LayoutInflater inflater;

    private List<HashMap<String,Object>> list;// 数据

    public RestAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public List<HashMap<String, Object>> getList() {
        return list;
    }

    public void setList(List<HashMap<String, Object>> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //重写getView方法
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rest_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        //填入需要显示的菜品名称和价格
        HashMap<String, Object> hashMap = list.get(position);
        Log.i("lzx",hashMap.toString());
        holder.restName.setText((String)hashMap.get("restName"));
        holder.restDis.setText((String)hashMap.get("restDis"));
        holder.restAvatar.setImageBitmap((Bitmap)hashMap.get("restAvatar"));

        float score = (float)hashMap.get("score");
        holder.restRating.setRating(score);
        holder.restScore.setText(score+"");
        Integer soldPerMonth = (Integer) hashMap.get("soldPerMonth");
        holder.restSold.setText("月售"+soldPerMonth+"份");
        String tag = (String) hashMap.get("tag");
        if(TextUtils.isEmpty(tag)){
            LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.rest_first_ll);
            linearLayout.removeView(holder.restTag);
        }else{
            holder.restTag.setText(tag);
        }
        return convertView;
    }


    //初始化View实例
    public final class ViewHolder {
        public ImageView restAvatar;
        public TextView restTag;
        public TextView restName;
        public TextView restDis;
        public RatingBar restRating;
        public TextView restScore;
        public TextView restSold;

        public ViewHolder(View view) {
            this.restAvatar = (ImageView)view.findViewById(R.id.rest_avatar);
            this.restTag = (TextView)view.findViewById(R.id.rest_tag);
            this.restName = (TextView)view.findViewById(R.id.rest_name);
            this.restDis = (TextView) view.findViewById(R.id.rest_dis);
            this.restRating = (RatingBar) view.findViewById(R.id.rest_rating);
            this.restScore = (TextView) view.findViewById(R.id.rest_score);
            this.restSold = (TextView) view.findViewById(R.id.rest_sold);
        }
    }
}

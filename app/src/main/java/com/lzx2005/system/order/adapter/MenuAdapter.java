package com.lzx2005.system.order.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx2005.system.order.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by john on 2017/5/12.
 */

public class MenuAdapter extends BaseAdapter{

    private LayoutInflater inflater;

    private OnItemViewClickCallback cb;

    private List<HashMap<String,Object>> list;// 数据

    public MenuAdapter(Context context, OnItemViewClickCallback cb) {
        inflater = LayoutInflater.from(context);
        this.cb = cb;
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
            convertView = inflater.inflate(R.layout.menu_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        //填入需要显示的菜品名称和价格
        holder.dishName.setText((String)list.get(position).get("name"));
        holder.dishPrice.setText((String)list.get(position).get("price"));

        //新建事件监听器
        MyListener listener = new MyListener(holder,position);

        //给每个按钮绑定事件
        holder.add.setTag(position);
        holder.add.setOnClickListener(listener);
        holder.minus.setTag(position);
        holder.minus.setOnClickListener(listener);
        return convertView;
    }

    //重写点击事件
    private class MyListener implements View.OnClickListener {
        int mPosition;
        ViewHolder viewHolder;
        public MyListener(ViewHolder viewHolder,int inPosition){
            this.viewHolder = viewHolder;
            this.mPosition = inPosition;
        }
        @Override
        public void onClick(View v) {
            //绑定的事件调用回调函数，该函数在Activity中实现
            cb.click(viewHolder,v,mPosition);
        }
    }

    //初始化View实例
    public final class ViewHolder {
        public TextView dishName;
        public TextView dishPrice;
        public ImageButton minus;
        public EditText sum;
        public ImageButton add;

        public ViewHolder(View view) {
            this.dishName = (TextView)view.findViewById(R.id.dish_name);
            this.dishPrice = (TextView)view.findViewById(R.id.dish_price);
            this.add = (ImageButton) view.findViewById(R.id.dish_add_btn);
            this.minus = (ImageButton) view.findViewById(R.id.dish_minus_btn);
            this.sum = (EditText) view.findViewById(R.id.dish_order_sum);
        }
    }

    //回调函数
    public interface OnItemViewClickCallback{
        void click(ViewHolder viewHolder,View clickView,int position);
    }
}

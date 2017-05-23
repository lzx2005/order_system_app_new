package com.lzx2005.system.order.activity;


import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.GetTask;

import java.text.DecimalFormat;


public class OrderActivity extends AppCompatActivity {

    SharedPreferences loginInfo;

    //LinearLayout
    LinearLayout orderList;

    //TextView
    TextView orderTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        loadView();
        loginInfo = getSharedPreferences("loginInfo", 0);
        String token = loginInfo.getString("token", "no");//找到登录信息
        if (token.equals("no")) {
            //未登录
            Toast.makeText(this,"暂未登录",Toast.LENGTH_SHORT).show();
        } else {
            String host = getResources().getString(R.string.server_host);
            String url = host + "/rest/order/find?token=" + token;
            GetTask task = new GetTask(url, handler);
            new Thread(task).start();
        }
    }

/*
*
*
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="3dp">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="6"
                android:textColor="@color/grey"
                android:text="蛋炒饭"/>

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="2"
                android:gravity="right"
                android:textColor="@color/grey"
                android:text="×1"/>
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="2"
                android:gravity="right"
                android:textColor="@color/black"
                android:text="¥15"/>
        </LinearLayout>

* */

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject jsonObject = JSONObject.parseObject(val);
            if(jsonObject.getInteger("code")==0){
                //成功
                JSONObject root = jsonObject.getJSONObject("data");
                JSONArray dishes = root.getJSONArray("dishes");
                double totalPrice = 0;
                for(int i=0;i<dishes.size();i++){
                    JSONObject dish = dishes.getJSONObject(i);
                    LinearLayout dishLayout = new LinearLayout(OrderActivity.this);
                    dishLayout.setPadding(0,0,0,3);
                    dishLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView dishName = new TextView(OrderActivity.this);
                    dishName.setText(dish.getString("name"));
                    dishName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,6));
                    dishName.setTextColor(getResources().getColor(R.color.grey));

                    TextView dishSum = new TextView(OrderActivity.this);
                    Integer count = dish.getInteger("count");
                    dishSum.setText("×"+count);
                    dishSum.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
                    dishSum.setTextColor(getResources().getColor(R.color.grey));
                    dishSum.setGravity(Gravity.RIGHT);

                    TextView dishPrice = new TextView(OrderActivity.this);
                    Double price = dish.getDouble("price");
                    dishPrice.setText("¥"+price);
                    dishPrice.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
                    dishPrice.setTextColor(getResources().getColor(R.color.black));
                    dishPrice.setGravity(Gravity.RIGHT);
                    totalPrice += count*price;

                    dishLayout.addView(dishName);
                    dishLayout.addView(dishSum);
                    dishLayout.addView(dishPrice);
                    orderList.addView(dishLayout);
                }


                DecimalFormat df = new DecimalFormat("######0.00");
                orderTotalPrice.setText("¥"+df.format(totalPrice));
            }else{
                Toast.makeText(OrderActivity.this,jsonObject.getString("msg"),Toast.LENGTH_LONG).show();
            }
        }
    };

    void loadView(){
        orderList = (LinearLayout) findViewById(R.id.order_list);
        orderTotalPrice = (TextView) findViewById(R.id.order_total_price);
    }

}

package com.lzx2005.system.order.activity;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.GetTask;
import com.lzx2005.system.order.http.task.PostTask;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;


public class OrderActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences loginInfo;
    String token;

    //LinearLayout
    LinearLayout orderList;
    LinearLayout orderPay;

    //TextView
    TextView orderTotalPrice;
    TextView orderId;
    TextView orderCreateTime;
    //提示
    ProgressDialog progressDialog;
    AlertDialog alertDialog;

    //支付按钮
    Button payButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        progressDialog = ProgressDialog.show(this, "正在加载订单...", "请稍后...", true, false);
        loadView();
        loginInfo = getSharedPreferences("loginInfo", 0);
        token = loginInfo.getString("token", "no");//找到登录信息
        if (token.equals("no")) {
            //未登录
            progressDialog.dismiss();
            Toast.makeText(this,"暂未登录",Toast.LENGTH_SHORT).show();
        } else {
            String host = getResources().getString(R.string.server_host);
            String url = host + "/rest/order/find?token=" + token;
            GetTask task = new GetTask(url, handler);
            new Thread(task).start();
        }
    }


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
                orderId.setText("订单ID："+root.getString("orderId"));
                Long createTimeLong = root.getLong("createTime");
                Date date = new Date(createTimeLong);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                orderCreateTime.setText(simpleDateFormat.format(date));
                double totalPrice = 0;
                for(int i=0;i<dishes.size();i++){
                    JSONObject dish = dishes.getJSONObject(i);
                    LinearLayout dishLayout = new LinearLayout(OrderActivity.this);
                    dishLayout.setPadding(0,0,0,5);
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

                orderPay.setVisibility(View.VISIBLE);
                payButton.setOnClickListener(OrderActivity.this);

                progressDialog.dismiss();
            }else{
                progressDialog.dismiss();
                Toast.makeText(OrderActivity.this,jsonObject.getString("msg"),Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };


    Handler payHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject result = JSONObject.parseObject(val);
            AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
            alertDialog = builder
                    .setTitle("结果")
                    .setMessage(result.getString("msg"))
                    .setPositiveButton("确定", (dialog, which) -> finish())
                    .show();

            progressDialog.dismiss();
        }
    };

    void loadView(){
        orderList = (LinearLayout) findViewById(R.id.order_list);
        orderTotalPrice = (TextView) findViewById(R.id.order_total_price);
        orderPay = (LinearLayout) findViewById(R.id.order_pay);
        payButton = (Button) findViewById(R.id.pay_button);
        orderId = (TextView) findViewById(R.id.order_id);
        orderCreateTime = (TextView) findViewById(R.id.order_createTime);
    }

    @Override
    public void onClick(View v) {

        progressDialog = ProgressDialog.show(this, "正在支付...", "请稍后...", true, false);
        if (token.equals("no")) {
            //未登录
            progressDialog.dismiss();
            Toast.makeText(this,"暂未登录",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else {
            String host = getResources().getString(R.string.server_host);
            String url = host + "/rest/order/pay?token=" + token;
            PostTask task = new PostTask(url, payHandler, RequestBody.create(MediaType.parse("application/json;charset=utf-8"),""));
            new Thread(task).start();
        }
    }
}

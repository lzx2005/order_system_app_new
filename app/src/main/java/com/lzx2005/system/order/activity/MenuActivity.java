package com.lzx2005.system.order.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.adapter.MenuAdapter;
import com.lzx2005.system.order.http.task.GetTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnItemViewClickCallback {

    SharedPreferences loginInfo;
    List<HashMap<String,Object>> list;
    MenuAdapter menuAdapter;
    TextView totalPriceView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle("点菜");

        progressDialog = ProgressDialog.show(MenuActivity.this, "正在加载菜单...", "请稍后...", true, false);
        loginInfo = getSharedPreferences("loginInfo", 0);
        list = new ArrayList<>();
        totalPriceView = (TextView) findViewById(R.id.total_price);
        menuAdapter = new MenuAdapter(this, this);
        String token = loginInfo.getString("token", "");
        String restaurantId = this.getIntent().getExtras().getString("restaurantId");
        String host = getResources().getString(R.string.server_host);
        String url = host + "/rest/dish/getByRestId?token="+token+"&restId="+restaurantId;

        GetTask getTask = new GetTask(url, showMenuHandler);

        new Thread(getTask).start();
    }


    Handler showMenuHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject result = JSONObject.parseObject(val);
            JSONObject root = result.getJSONObject("data");
            for(String key : root.keySet()){
                JSONArray array = root.getJSONArray(key);
                for(int i=0;i<array.size();i++){
                    JSONObject dish = array.getJSONObject(i);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("name",dish.getString("name"));
                    hashMap.put("price",dish.getString("price"));
                    hashMap.put("id",dish.getInteger("id"));
                    hashMap.put("count",0);
                    list.add(hashMap);
                }
            }
            ListView listView = (ListView) findViewById(R.id.menu_list);
            menuAdapter.setList(list);
            listView.setAdapter(menuAdapter);
            progressDialog.dismiss();
        }
    };

    @Override
    public void click(MenuAdapter.ViewHolder viewHolder, View clickView, int position) {
        HashMap<String, Object> hashMap = list.get(position);
        int count = (int) hashMap.get("count");
        switch (clickView.getId()){
            case R.id.dish_minus_btn:
                Log.i("lzx",viewHolder.dishName.getText().toString()+"减少一个");
                if(count>0){
                    count -= 1;
                }
                break;
            case R.id.dish_add_btn:
                Log.i("lzx",viewHolder.dishName.getText().toString()+"增加一个");
                count += 1;
                break;
        }
        if(count<0){
            count=0;
        }
        hashMap.put("count",count);
        viewHolder.sum.setText(count+"");

        //计算总价
        double totalPrice = 0.0;
        for(HashMap<String,Object> map : list){
            int count1 = (int) map.get("count");
            String priceStr1 = (String) map.get("price");
            double price1 = Double.parseDouble(priceStr1);
            totalPrice = totalPrice + (count1*price1);
        }
        DecimalFormat df = new DecimalFormat("######0.00");
        totalPriceView.setText("总价："+df.format(totalPrice)+"元");
    }
}

package com.lzx2005.system.order.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.GetTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    SharedPreferences loginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle("点菜");
        loginInfo = getSharedPreferences("loginInfo", 0);

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
            List<HashMap<String,Object>> list = new ArrayList<>();
            for(String key : root.keySet()){
                JSONArray array = root.getJSONArray(key);
                for(int i=0;i<array.size();i++){
                    JSONObject dish = array.getJSONObject(i);

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("name",dish.getString("name"));
                    hashMap.put("price",dish.getString("price"));
                    list.add(hashMap);
                }
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(MenuActivity.this,
                    list,
                    R.layout.menu_list_item,
                    new String[]{"name", "price"},
                    new int[]{R.id.dish_name, R.id.dish_price});
            ListView listView = (ListView) findViewById(R.id.menu_list);
            listView.setAdapter(simpleAdapter);
        }
    };

}

package com.lzx2005.system.order.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lzx2005.system.order.R;

public class MenuActivity extends AppCompatActivity {

    private String[] data = {"wt","t1","teaasda"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MenuActivity.this,R.layout.menu_list_item,data);

        ListView listView = (ListView) findViewById(R.id.menuList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            //todo 对每次点击进行添加到"购物篮"操作
        });
    }
}

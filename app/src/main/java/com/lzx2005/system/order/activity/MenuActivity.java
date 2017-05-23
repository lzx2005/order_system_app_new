package com.lzx2005.system.order.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DrawableUtils;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.adapter.MenuAdapter;
import com.lzx2005.system.order.entity.Order;
import com.lzx2005.system.order.http.task.GetTask;
import com.lzx2005.system.order.http.task.PostTask;
import com.lzx2005.system.order.utils.SUID;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnItemViewClickCallback,View.OnClickListener,DialogInterface.OnClickListener {

    SharedPreferences loginInfo;
    List<HashMap<String,Object>> list;
    String restaurantId;

    LinearLayout menuHeader;
    ImageView menuAvatar;
    TextView restTitle;

    MenuAdapter menuAdapter;
    TextView totalPriceView;
    FloatingActionButton fab;
    ProgressDialog progressDialog;

    AlertDialog alertDialog;

    String host;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle("点菜");

        progressDialog = ProgressDialog.show(MenuActivity.this, "正在加载菜单...", "请稍后...", true, false);
        loginInfo = getSharedPreferences("loginInfo", 0);
        list = new ArrayList<>();
        totalPriceView = (TextView) findViewById(R.id.total_price);
        menuHeader = (LinearLayout) findViewById(R.id.menu_header);
        menuAvatar = (ImageView) findViewById(R.id.menu_avatar);
        restTitle = (TextView) findViewById(R.id.rest_title);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        menuAdapter = new MenuAdapter(this, this);

        token = loginInfo.getString("token", "");
        restaurantId = this.getIntent().getExtras().getString("restaurantId");
        host = getResources().getString(R.string.server_host);

        String restInfoUrl =  host + "/rest/restaurant/info?token="+token+"&restaurantId="+restaurantId;

        String url = host + "/rest/dish/getByRestId?token="+token+"&restId="+restaurantId;


        GetTask getTask = new GetTask(restInfoUrl, showHeaderHandler);
        new Thread(getTask).start();

        GetTask getTask1 = new GetTask(url, showMenuHandler);
        new Thread(getTask1).start();
    }

    Handler showHeaderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject result = JSONObject.parseObject(val);
            JSONObject root = result.getJSONObject("data");
            Log.i("lzx",root.toString());

            Bitmap bitmap;
            String avatar = root.getString("avatar");
            if(!TextUtils.isEmpty(avatar)){
                byte[] decode = Base64.decode(avatar,Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            }else{
                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }
            menuAvatar.setImageBitmap(bitmap);
            restTitle.setText(root.getString("restaurantName"));

            //menuHeader.setBackgroundTintMode(PorterDuff.Mode.SRC_OUT);
            //menuHeader.setBackground(new BitmapDrawable(bitmap));
        }
    };

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

            fab.setOnClickListener(MenuActivity.this);
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
        totalPriceView.setText("总价："+df.format(countTotalPrice())+"元");
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(countTotalPrice()==0){
            alertDialog = builder
                    .setTitle("信息")
                    .setMessage("暂未点任何菜品，请选择您喜欢的菜品")
                    .setPositiveButton("好的", (dialog, which) -> {})
                    .show();
        }else{
            StringBuilder stringBuilder = new StringBuilder();
            for(HashMap<String,Object> hashMap : list){
                int count = (int)hashMap.get("count");
                if(count>0){
                    stringBuilder.append(hashMap.get("name")+":"+count+"份\n");
                }
            }
            DecimalFormat df = new DecimalFormat("######0.00");
            stringBuilder.append("总价为："+df.format(countTotalPrice())+"元");
            alertDialog = builder
                    .setTitle("已点详情")
                    .setMessage(stringBuilder.toString())
                    .setPositiveButton("下单",this)
                    .setNegativeButton("取消",this)
                    .show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case -1:

                progressDialog = ProgressDialog.show(MenuActivity.this, "操作中...", "订单正在生成，请稍后...", true, false);
                Order order = new Order();
                order.setRestaurantId(restaurantId);
                order.setOrderId(SUID.getUUID());
                order.setCreateTime(new Date());
                order.setStatus(0);
                List<HashMap<String,Object>> dishes = new ArrayList<>();
                for(HashMap<String,Object> hashMap : list){
                    int count = (int)hashMap.get("count");
                    if(count>0){
                        dishes.add(hashMap);
                    }
                }
                order.setDishes(dishes);
                Log.i("lzx","下单");


                String url = host + "/rest/order/create?token="+token;
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), order.toString());
                PostTask postTask = new PostTask(url, orderSubmitedHandler, requestBody);
                new Thread(postTask).start();
                //todo 生成订单
                break;
            case -2:
                Log.i("lzx","暂不下单");
                break;
        }
    }

    Handler orderSubmitedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject result = JSONObject.parseObject(val);
            Integer code = result.getInteger("code");
            progressDialog.dismiss();
            if(code!=0){
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                alertDialog = builder
                        .setTitle("创建订单失败")
                        .setMessage(result.getString("msg"))
                        .setPositiveButton("好的", (dialog, which) -> {})
                        .show();
            }else{
                //订单创建成功
                Toast.makeText(MenuActivity.this,"订单创建成功",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MenuActivity.this,OrderActivity.class);
                startActivity(intent);
            }
        }
    };


    public Bitmap blurBitmap(Bitmap bitmap){
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(getApplicationContext());
        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.F64_4(rs));
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        //Set the radius of the blur
        blurScript.setRadius(25.f);
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);
        //recycle the original bitmap
        bitmap.recycle();
        //After finishing everything, we destroy the Renderscript.
        rs.destroy();
        return outBitmap;
    }


    private double countTotalPrice(){
        double totalPrice = 0.0;
        for(HashMap<String,Object> map : list){
            int count1 = (int) map.get("count");
            String priceStr1 = (String) map.get("price");
            double price1 = Double.parseDouble(priceStr1);
            totalPrice = totalPrice + (count1*price1);
        }
        return totalPrice;
    }
}

package com.lzx2005.system.order.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.GetNearRestautantTask;
import com.lzx2005.system.order.http.task.GetUserInfoTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity
        extends
            AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            AMapLocationListener, AdapterView.OnItemClickListener{

    SharedPreferences loginInfo;

    Toolbar toolbar;
    FloatingActionButton fab;
    DrawerLayout drawer;
    NavigationView navigationView;
    LinearLayout linearLayout;

    MenuItem exitMenuItem;

    TextView usernameHead;
    TextView userinfoHead;

    ImageView userNoLoginImage;

    Menu slideMenu;
    MenuItem navExitMenuItem;

    ListView restList;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;

    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;

    List<HashMap<String,Object>> restDataList = new ArrayList<>();


    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginInfo = getSharedPreferences("loginInfo", 0);
        setContentView(R.layout.activity_main);
        progressDialog = ProgressDialog.show(MainActivity.this, "正在查找附近的餐厅...", "请稍后...", true, false);
        loadView();
        setSupportActionBar(toolbar);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("知道了", v -> Log.e("lzx2005", "1")).show());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        String token = loginInfo.getString("token", "no");//找到登录信息
        if (token.equals("no")) {
            //未登录
            navExitMenuItem.setVisible(false);
            linearLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            });
        } else {
            Log.i("lzx", "已经登录，从服务器获取信息");
            String host = getResources().getString(R.string.server_host);
            String url = host + "/user/userInfo?token=" + token;
            GetUserInfoTask getUserInfoTask = new GetUserInfoTask(url, handler);
            new Thread(getUserInfoTask).start();

        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_near_rest) {
            Intent intent = new Intent(MainActivity.this,MapActivity.class);
            startActivityForResult(intent,2);
        } else if (id == R.id.nav_my_order) {

        } else if (id == R.id.nav_menu) {
            Intent intent = new Intent(MainActivity.this,MenuActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            loginInfo.edit().remove("token").apply();
            MainActivity.this.recreate();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                //更新
                String token = loginInfo.getString("token", "");
                if(TextUtils.isEmpty(token)){
                    Log.i("lzx","未登录，不获取用户信息");
                    //未登录
                }else{
                    Log.i("lzx","登录成功，开始获取用户信息");
                    String host = getResources().getString(R.string.server_host);
                    String url = host + "/user/userInfo?token="+token;
                    GetUserInfoTask getUserInfoTask = new GetUserInfoTask(url, handler);
                    new Thread(getUserInfoTask).start();
                }
                break;
            case 2:
                Intent intent = new Intent(MainActivity.this,MenuActivity.class);
                intent.putExtra("restaurantId",data.getStringExtra("restaurantId"));
                startActivity(intent);
                break;
            default:
        }
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            navExitMenuItem.setVisible(true);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject jsonObject = JSONObject.parseObject(val);
            if(jsonObject==null){
                Log.i("lzx2005","未登录");
            }else if(jsonObject.getInteger("code")==0){
                JSONObject data1 = jsonObject.getJSONObject("data");
                String username = data1.getString("username");
                usernameHead.setText(username);
                userinfoHead.setText("欢迎回来！");
            }else{
                Log.e(MainActivity.class.getName(),jsonObject.toJSONString());
            }
        }
    };



    Handler showNearRestauranthandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("lzx", val);
            JSONObject jsonObject = JSONObject.parseObject(val);
            if(jsonObject==null){
                Log.e(this.getClass().getName(),"JSON渲染错误");
            }else if(jsonObject.getInteger("code")==0){
                JSONObject data1 = jsonObject.getJSONObject("data");
                JSONArray content = data1.getJSONArray("content");


                for(int i =0;i<content.size();i++){
                    JSONObject re = content.getJSONObject(i);
                    JSONObject distance = re.getJSONObject("distance");
                    Double value = distance.getDouble("value");
                    String dis;
                    DecimalFormat df = new DecimalFormat("######0.00");
                    if(value<1.0){
                        value = value*1000;
                        dis = df.format(value);
                        dis = dis+"米";
                    }else{
                        dis = df.format(value);
                        dis = dis + "千米";
                    }
                    JSONObject restaurant = re.getJSONObject("content");
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("restName",restaurant.getString("restaurantName"));
                    hashMap.put("restDis","距您"+dis);
                    hashMap.put("restaurantId",restaurant.getString("restaurantId"));
                    restDataList.add(hashMap);
                    Log.i("lzx",re.toJSONString());
                    //JSONArray position = restaurant.getJSONArray("position");
                }

                SimpleAdapter simpleAdapter = new SimpleAdapter(
                        MainActivity.this,
                        restDataList,
                        R.layout.rest_list_item,
                        new String[]{"restName","restDis"},
                        new int[]{R.id.rest_name,R.id.rest_dis}
                );
                restList.setAdapter(simpleAdapter);
                restList.setOnItemClickListener(MainActivity.this);
                progressDialog.dismiss();
            }else{
                String msg1 = jsonObject.getString("msg");
                Log.e(this.getClass().getName(),msg1);
                Toast.makeText(MainActivity.this,msg1,Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    };
    private void loadView(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        linearLayout = (LinearLayout)navigationView.getHeaderView(0);


        usernameHead = (TextView) linearLayout.findViewById(R.id.username_head);
        userinfoHead = (TextView) linearLayout.findViewById(R.id.userinfo_head);


        slideMenu = navigationView.getMenu();
        navExitMenuItem = slideMenu.findItem(R.id.nav_exit);

        restList = (ListView) findViewById(R.id.rest_list);
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);


        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        double longitude = aMapLocation.getLongitude();
        double latitude = aMapLocation.getLatitude();
        Log.i("lzx","lng:"+longitude+",lat:"+latitude);



        String token = loginInfo.getString("token", "no");//找到登录信息
        if(token.equals("no")){
            Toast.makeText(this,"暂未登录",Toast.LENGTH_SHORT).show();
            return;
        }
        String host = getResources().getString(R.string.server_host);
        String url = host + "/rest/restaurant/near?token="+token+"&lng="+longitude+"&lat="+latitude+"&length="+10;
        GetNearRestautantTask getUserInfoTask = new GetNearRestautantTask(url, showNearRestauranthandler);
        new Thread(getUserInfoTask).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this,MenuActivity.class);
        intent.putExtra("restaurantId",(String)restDataList.get(position).get("restaurantId"));
        startActivity(intent);
    }
}

package com.lzx2005.system.order.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MarkerOptionsCreator;
import com.amap.api.maps.model.MyLocationStyle;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.GetNearRestautantTask;
import com.lzx2005.system.order.http.task.GetUserInfoTask;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements AMap.OnCameraChangeListener,AMap.OnMarkerClickListener,DialogInterface.OnClickListener{

    SharedPreferences loginInfo;
    MapView mMapView = null;
    AMap aMap;
    AlertDialog alertDialog;

    //TextView restName;
    List<Marker> markers = new ArrayList<>();

    //被选中的RestaurantId
    String restaurantId=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        loginInfo = getSharedPreferences("loginInfo", 0);

        //linearLayout = (LinearLayout) findViewById(R.id.rest_info);
        //restName = (TextView) findViewById(R.id.rest_name);
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //获取地图控件引用
        setTitle("附近的餐厅");
        aMap = mMapView.getMap();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //Log.i("lzx2005","camera开始移动了"+cameraPosition.toString());
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        //todo 去服务器获取附近的餐馆
        Log.i("lzx2005","camera结束移动了"+cameraPosition.toString());

        // 获取比例尺
        float scalePerPixel = aMap.getScalePerPixel();

        String host = getResources().getString(R.string.server_host);
        String token = loginInfo.getString("token", "");
        String url = host + "/rest/restaurant/near?token="+token+"&lng="+cameraPosition.target.longitude+"&lat="+cameraPosition.target.latitude+"&length="+10;
        GetNearRestautantTask getUserInfoTask = new GetNearRestautantTask(url, handler);
        new Thread(getUserInfoTask).start();
    }


    Handler handler = new Handler() {
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
                ArrayList<Marker> newMarkers = new ArrayList<>();
                for(int i =0;i<content.size();i++){
                    JSONObject re = content.getJSONObject(i);
                    JSONObject content1 = re.getJSONObject("content");

                    Log.i("lzx",re.toJSONString());
                    JSONArray position = content1.getJSONArray("position");
                    LatLng latLng = new LatLng(position.getDouble(1),position.getDouble(0));
                    final Marker marker = aMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title(content1.getString("restaurantName"))
                                    .snippet(content1.getString("restaurantId"))
                    );
                    newMarkers.add(marker);
                }

                //在地图上删除所有marker
                for(Marker marker : markers){
                    marker.remove();
                }
                //清空list中旧的marker
                markers.clear();
                //将新的markers引用赋值给全局变量
                markers = newMarkers;

            }else{
                Log.e(this.getClass().getName(),jsonObject.getString("msg"));
            }
        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("lzx","Marker被点击");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        restaurantId = marker.getSnippet();
        alertDialog = builder
                .setTitle("餐馆详情")
                .setMessage(marker.getTitle())
                .setPositiveButton("我要点菜",this)
                .setNegativeButton("取消",this)
                .setNeutralButton("中间",this)
                .show();

        //restName.setText(marker.getTitle());
        //linearLayout.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case -1:
                Log.i("lzx","我要点菜"+which);

                Intent intent = new Intent();
                intent.putExtra("restaurantId",restaurantId);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case -2:
                Log.i("lzx","取消"+which);
                restaurantId=null;
                break;
            case -3:
                Log.i("lzx","其他"+which);
                restaurantId=null;
                break;
        }
    }
}

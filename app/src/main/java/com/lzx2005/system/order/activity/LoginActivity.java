package com.lzx2005.system.order.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.lzx2005.system.order.R;
import com.lzx2005.system.order.http.task.LoginTask;



public class LoginActivity extends AppCompatActivity {
    private TextView username;
    private TextView password;
    private Button loginSubmit;
    private LinearLayout linearLayout;
    private SharedPreferences loginInfo;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        loginInfo = getSharedPreferences("loginInfo", 0);
        String token = loginInfo.getString("token","no");
        if(!token.equals("no")){
            //已经登录
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            LoginActivity.this.startActivity(intent);
        }else{

            username = (TextView) findViewById(R.id.username);
            password = (TextView) findViewById(R.id.password);
            loginSubmit = (Button) findViewById(R.id.sign_in_button);
            linearLayout = (LinearLayout)findViewById(R.id.username_login_form);

            //loginSubmit.setEnabled(false);
            loginSubmit.setOnClickListener(v ->{
                if(TextUtils.isEmpty(username.getText())){
                    alert("请输入用户名");
                    //loginSubmit.setEnabled(false);
                    return;
                }
                if(TextUtils.isEmpty(password.getText())){
                    alert("请输入密码");
                    //loginSubmit.setEnabled(false);
                    return;
                }
                progressDialog = ProgressDialog.show(LoginActivity.this, "正在登录...", "请稍后...", true, false);
                //alert(username.getText()+","+password.getText());
                String host = getResources().getString(R.string.server_host);
                String url = host + "/user/login?username="+username.getText()+"&password="+password.getText();
                LoginTask loginTask = new LoginTask(url, handler);
                new Thread(loginTask).start();
            });
        }


    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.e("lzx2005", val);
            //alert(val);

            JSONObject jsonObject = JSONObject.parseObject(val);
            if(jsonObject.getInteger("code")==0){
                String token = jsonObject.getString("data");
                if(TextUtils.isEmpty(token)){
                    alert("无法获取服务器返回的登录验证信息");
                    progressDialog.dismiss();
                }else{
                    alert("登录成功！");
                    loginInfo.edit().putString("token",token).apply();
                    //跳转
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("isSuccess",true);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }else{
                alert(jsonObject.getString("msg"));
                progressDialog.dismiss();
            }
        }
    };


    private void alert(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
    }
}

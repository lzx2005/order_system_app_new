package com.lzx2005.system.order.http.task;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by john on 2017/4/23.
 */

public class PostTask implements Runnable {
    private Handler handler;
    private String url;
    private RequestBody requestBody;

    public PostTask(String url, Handler handler,RequestBody requestBody) {
        this.url = url;
        this.handler = handler;
        this.requestBody = requestBody;
    }

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient();
        Message msg = new Message();
        Bundle data = new Bundle();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            data.putString("value", response.body().string());
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.lzx2005.system.order.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by john on 2017/5/22.
 */

public class Order {
    private String orderId;
    private String restaurantId;
    private int userId;
    private List<HashMap<String,Object>> dishes;
    private Date createTime;
    private double score;
    private String msg;
    private int status;//0:未付款，1:已付款

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<HashMap<String, Object>> getDishes() {
        return dishes;
    }

    public void setDishes(List<HashMap<String, Object>> dishes) {
        this.dishes = dishes;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

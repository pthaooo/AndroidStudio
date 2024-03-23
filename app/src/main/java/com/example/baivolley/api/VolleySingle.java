package com.example.baivolley.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingle {
    private static VolleySingle mInstance;
    private static RequestQueue mRequestQueue;
    private static Context mCtx;

    //Khoi tạo constructor
    private VolleySingle(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    //hàm lấy context
    public static synchronized VolleySingle getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingle(context);
        }
        return mInstance;
    }

    //Hàm RequestQueue
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    //Ham addtoRequest
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}





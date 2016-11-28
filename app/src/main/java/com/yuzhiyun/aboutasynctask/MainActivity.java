package com.yuzhiyun.aboutasynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Context context;
    Button btnVolley;
    Button btnAsynctask;

    /**
     * 查看手机相关信息的一个API
     *
     * 不知道为什么用http://www.baidu.com的时候，
     * Asynctask+HttpPost的实现就会返回404错误，
     * 使用volley框架则正常
     */
    private String url = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=15850781443";

    HttpClient httpClient;
    HttpPost httpPostRequest;

    //        进度对话框
    ProgressDialog progressDialog;

    /**
     * 用于在asynctask里面弹出一个对话框，由于asynctask不是主线程，
     * 所以handler可以解决这个问题
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = msg.getData().getString("msg");
            showDialog(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnVolley = (Button) findViewById(R.id.btnVolley);
        btnAsynctask = (Button) findViewById(R.id.btnAsynctask);

        context = MainActivity.this;

        btnVolley.setOnClickListener(this);
        btnAsynctask.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在连接...");
        progressDialog.show();

        switch (view.getId()) {

            case R.id.btnVolley:
                RequestQueue requestQueue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                progressDialog.dismiss();
                                showDialog(s);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                progressDialog.dismiss();
                                showDialog("错误`_`\n" + volleyError.toString());
                            }
                        });
                requestQueue.add(stringRequest);

                break;
            case R.id.btnAsynctask:
//                Toast.makeText(MainActivity.this, "Asynctask", Toast.LENGTH_SHORT).show();

                new myAnyctask().execute();
                break;

        }

    }

    /**
     * 弹出对话框
     *
     * @param msg
     */
    public void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("返回结果:\n" + msg)
                .setCancelable(false)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 执行网络操作
     */
    class myAnyctask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                httpClient = new DefaultHttpClient();
                httpPostRequest = new HttpPost(url);
                HttpResponse httpResponse = httpClient.execute(httpPostRequest);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //获取结果
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    Log.i("result", "这是获取到的服务器的结果" + result);
                    progressDialog.dismiss();
//                    showDialog(result);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", result);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } else {
//                    把错误信息给用户看
                    String error = httpResponse.getStatusLine().toString();
                    progressDialog.dismiss();
//                    showDialog(error);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", error);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    Log.e("sendPOSTRequest", "错误" + error);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

package com.yetwish.customsearchdemo.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yetwish.customsearchdemo.R;
import com.yetwish.customsearchdemo.activity.adapter.SearchAdapter;
import com.yetwish.customsearchdemo.activity.model.Bean;
import com.yetwish.customsearchdemo.activity.widge.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity implements SearchView.SearchViewListener {
    private static final String TAG = "MainActivity";

    private String mSearchText = "";

    private EditText mTvSearch;

    /**
     * 搜索结果列表view
     */
    private ListView lvResults;

    /**
     * 搜索view
     */
    private SearchView searchView;


    /**
     * 热搜框列表adapter
     */
    private ArrayAdapter<String> hintAdapter;

    /**
     * 自动补全列表adapter
     */
    private ArrayAdapter<String> autoCompleteAdapter;

    /**
     * 搜索结果列表adapter
     */
//    private SearchAdapter resultAdapter;
    private ArrayAdapter<String> resultAdapter;

    /**
     * 搜索过程中自动补全数据
     */
    private List<String> autoCompleteData;

    /**
     * 默认提示框显示项的个数
     */
    private static int DEFAULT_HINT_SIZE = 5;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            for (String s : autoCompleteData){
                Log.d(TAG, s);
            }
            getAutoCompleteData();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initViews();

    }

    /**
     * 初始化视图
     */
    private void initViews() {
        autoCompleteData = new ArrayList<>();
        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, autoCompleteData);

        resultAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, autoCompleteData);

        lvResults = (ListView) findViewById(R.id.main_lv_search_results);

        searchView = (SearchView)findViewById(R.id.main_search_layout);
        //设置监听
        searchView.setSearchViewListener(this);
        //设置adapter
        searchView.setTipsHintAdapter(hintAdapter);
        searchView.setAutoCompleteAdapter(autoCompleteAdapter);

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });

        mTvSearch = (EditText)findViewById(R.id.search_et_input);

    }

    /**
     * 获取自动补全data 和adapter
     */
    private void getAutoCompleteData() {
        autoCompleteAdapter.notifyDataSetChanged();
    }

    /**
     * 获取搜索结果data和adapter
     */
    private void getResultData() {
        resultAdapter.notifyDataSetChanged();
    }

    /**
     * 当搜索框 文本改变时 触发的回调 ,更新自动补全数据
     * @param text
     */
    @Override
    public void onRefreshAutoComplete(String text) {
        //更新数据
        mSearchText = mTvSearch.getText().toString();
        getSearchData();
    }

    /**
     * 点击搜索键时edit text触发的回调
     *
     * @param text
     */
    @Override
    public void onSearch(String text) {
        getSearchData();
        //更新result数据
        getResultData();
        lvResults.setVisibility(View.VISIBLE);
        //第一次获取结果 还未配置适配器
        if (lvResults.getAdapter() == null) {
            //获取搜索数据 设置适配器
            lvResults.setAdapter(resultAdapter);
        } else {
            //更新搜索数据
            resultAdapter.notifyDataSetChanged();
        }
        Toast.makeText(this, "完成搜索", Toast.LENGTH_SHORT).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        return true;
    }

    private void getSearchData(){

        final String url = "http://120.79.178.50:18080/entities/_search/full-text?keyword=" + mSearchText;

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                try {
                    Log.d(TAG, "----------------- 开始发数据 -----------------------");
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    Log.d(TAG, responseString);

                    parseData(responseString);


                }catch (IOException e){
                    e.printStackTrace();

                }
            }
        }).start();
    }

    private void parseData(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            String status = jsonObject.getString("statusCode");

            if (! "200".equals(status)){

            }else {
                JSONArray dataJson = jsonObject.getJSONArray("data");

                autoCompleteData.clear();
                for (int i=0;i < dataJson.length();i++){
                    JSONObject object = dataJson.getJSONObject(i);
                    String name = object.getString("FQDN");
                    autoCompleteData.add(name);
                }

                Message msg =new Message();
                mHandler.sendMessage(msg);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }


    }

}

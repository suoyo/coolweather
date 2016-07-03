package com.suoyo.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suoyo.coolweather.R;
import com.suoyo.coolweather.service.AutoUpdateService;
import com.suoyo.coolweather.util.HttpCallbackListener;
import com.suoyo.coolweather.util.HttpUtil;
import com.suoyo.coolweather.util.Utility;

/**
 * Created by SUOYC on 2016/7/3.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityName;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示当前日期
     */
    private TextView currentDate;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDesp;
    /**
     * 用于显示气温1
     */
    private TextView temp1;
    /**
     * 用于显示气温2
     */
    private TextView temp2;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 刷新天气按钮
     */
    private Button refreshWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        cityName = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        currentDate = (TextView) findViewById(R.id.current_date);
        weatherDesp = (TextView) findViewById(R.id.weather_desp);
        temp1 = (TextView) findViewById(R.id.temp1);
        temp2 = (TextView) findViewById(R.id.temp2);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // 有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weacherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weacherCode)) {
                    queryWeatherInfo(weacherCode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" +
                countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // 从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // 处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(prefs.getString("city_name", ""));
        temp1.setText(prefs.getString("temp1", ""));
        temp2.setText(prefs.getString("temp2", ""));
        weatherDesp.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDate.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startActivity(intent);
    }


}

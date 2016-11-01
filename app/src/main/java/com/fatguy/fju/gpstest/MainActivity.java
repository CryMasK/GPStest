package com.fatguy.fju.gpstest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private boolean getService = false;        //是否已開啟定位服務
    private LocationManager locationManager;
    private String bestProvider;    //最佳資訊提供者
    private String allProvider;
    private long MIN_TIME_BW_UPDATES = 3000;
    private float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private int userState = 0;
    private String Url = "http://140.136.150.80/project_D/ajax/posttest.php";

    static final Integer LOCATION = 0x1;
    static final Integer CALL = 0x2;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;
    static final Integer CAMERA = 0x5;
    static final Integer ACCOUNTS = 0x6;
    static final Integer GPS_SETTINGS = 0x7;

    private static final Pattern validPattern = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch stateSwitch = (Switch) findViewById(R.id.stateSwitch);
        stateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked) {
                    // Switch is ON
                    userState = 1;
                    //Toast.makeText(getApplicationContext(), "忙碌", Toast.LENGTH_LONG).show();
                }
                else {
                    // Switch if OFF
                    userState = 0;
                    //Toast.makeText(getApplicationContext(), "正常", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (getService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    sendBtn_onClick(findViewById(R.id.sendBtn)); // 取得權限後會呼叫onResume，得locationServiceInitial()，不然下一行request會on null object
                    locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    //服務提供者、更新頻率60000毫秒=1分鐘、最短距離、地點改變時呼叫物件
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //This is called if user has denied the permission before
                        //In this case I am just asking the permission again
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    }
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                }
            } else {
                sendBtn_onClick(findViewById(R.id.sendBtn)); // 取得權限後會呼叫onResume，得locationServiceInitial()，不然下一行request會on null object
                locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (getService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.removeUpdates(this);    //離開頁面時停止更新
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //This is called if user has denied the permission before
                        //In this case I am just asking the permission again
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    }
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                }
            }
            else {
                //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                locationManager.removeUpdates(this);    //離開頁面時停止更新
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {    //當地點改變時
        // TODO Auto-generated method stub
        getLocation(location);
    }

    @Override
    public void onProviderDisabled(String arg0) {    //當GPS或網路定位功能關閉時
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {    //當GPS或網路定位功能開啟
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    //定位狀態改變
        //status=OUT_OF_SERVICE 供應商停止服務
        //status=TEMPORARILY_UNAVAILABLE 供應商暫停服務
    }

    private void locationServiceInitial() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);    //取得系統定位服務
        Criteria criteria = new Criteria();    //資訊提供者選取標準
        bestProvider = locationManager.getBestProvider(criteria, true);    //選擇精準度最高的提供者
        Location location = new Location("");

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));    //開啟設定頁面
        }
        else if (isGPSEnabled && isNetworkEnabled){
            // 如果GPS或網路定位開啟，呼叫requestLocationUpdates()更新位置
            locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        else if (isGPSEnabled && !isNetworkEnabled){
            // 如果GPS訊號穩定，但網路定位無訊號，則使用GPS提供
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        else if (!isGPSEnabled && isNetworkEnabled){
            // 如果網路訊號穩定，但GPS定位無訊號，則使用網路提供
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        else {
            // 不知道意義何在
            locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            Toast.makeText(this, "GPS Signal Not Found.", Toast.LENGTH_LONG).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(bestProvider);
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                }
                //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
            }
        }
        else {
            //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
            location = locationManager.getLastKnownLocation(bestProvider);
        }
        getLocation(location);
    }

    private void getLocation(Location location) {    //將定位資訊顯示在畫面中
        if (location != null) {
            EditText ETuID = (EditText) findViewById(R.id.ID_input);
            String uID = ETuID.getText().toString().trim();

            TextView TVlongitude = (TextView) findViewById(R.id.longitude);
            TextView TVlatitude = (TextView) findViewById(R.id.latitude);

            Double longitude = location.getLongitude();    //取得經度
            Double latitude = location.getLatitude();    //取得緯度

            TVlongitude.setText(String.valueOf(longitude));
            TVlatitude.setText(String.valueOf(latitude));

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("uID", uID));
            data.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
            data.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
            data.add(new BasicNameValuePair("state", String.valueOf(userState)));
            //httpPostData(uID, String.valueOf(longitude), String.valueOf(latitude));
            //Toast.makeText(this, userState, Toast.LENGTH_LONG).show(); // test refresh

            httpPostData post = new httpPostData();
            post.execute(data);


            /*String result = httpPostData(uID, String.valueOf(longitude), String.valueOf(latitude));
                            // 印出網路回傳的文字
                            if (result != null) {
                                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                             }*/
        } else {
            Toast.makeText(this, "正在定位座標...", Toast.LENGTH_LONG).show(); // 無法定位座標
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this); // 強制由網路更新
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //This is called if user has denied the permission before
                        //In this case I am just asking the permission again
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    }
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this); // 強制由網路更新
            }
            Toast.makeText(this, "無法抓取GPS訊號，改由網路定位座標", Toast.LENGTH_LONG).show(); // 無法定位座標
        }
    }

    protected void stopBtn_onClick(View v) {
        EditText ETuID = (EditText) findViewById(R.id.ID_input);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        TextView TVstate = (TextView) findViewById(R.id.loadingState);

        if (getService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.removeUpdates(this);    //離開頁面時停止更新
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //This is called if user has denied the permission before
                        //In this case I am just asking the permission again
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                    }
                    //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                }
            } else {
                locationManager.removeUpdates(this);    //離開頁面時停止更新
            }

            getService = false; // 定位服務flipflop
            //ETuID.setFocusableInTouchMode(true);
            ETuID.setEnabled(true);
            sendBtn.setClickable(true);
            TVstate.setText("停止發送座標");
        }
    }

    protected void sendBtn_onClick(View v) {
        getService = true;    //確認開啟定位服務

        EditText ETuID = (EditText) findViewById(R.id.ID_input);
        Button sendBtn = (Button) findViewById(R.id.sendBtn);
        String uID = ETuID.getText().toString().trim();

        if (!(uID.isEmpty())) { // 確認ID是否空白
            if ( isValid(uID) ){ // 確認ID是否為合法字元
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 如果系統版本為Marshmallow以上
                    if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // 確認權限是否開啟
                        //ETuID.setFocusable(false); // lock uID input
                        ETuID.setEnabled(false); // lock uID input
                        //ETuID.setInputType(InputType.TYPE_NULL);
                        sendBtn.setClickable(false); // 取消sendBtn的點擊能力

                        locationServiceInitial();
                    }
                    else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            //This is called if user has denied the permission before
                            //In this case I am just asking the permission again
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                        }
                        else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                        }
                        //askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
                    }
                }
                else {
                    ETuID.setEnabled(false); // lock uID input
                    sendBtn.setClickable(false); // 取消sendBtn的點擊能力

                    locationServiceInitial();
                }
            }
            else{
                Toast.makeText(this, "用戶ID僅能使用a-z,A-Z,0-9,-_字元", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, "請輸入用戶ID", Toast.LENGTH_LONG).show();
        }
    }

    private class httpPostData extends AsyncTask<List<NameValuePair>, Integer, String> { // <傳入 doInBackground() 的參數型別, 傳入onProgressUpdate() 的參數型別, doInBackground() 的回傳值型別>

        @Override
        protected void onPreExecute() {
            TextView TVstate = (TextView) findViewById(R.id.loadingState);
            TVstate.setText("正在發送座標...");
        }

        @Override
        protected String doInBackground(List<NameValuePair>... params) {
            /* 建立HTTP Post連線 */
            HttpPost httpRequest = new HttpPost(Url);

            try {
            /* 發出HTTP request */
                httpRequest.setEntity(new UrlEncodedFormEntity(params[0], HTTP.UTF_8));

            /* 取得HTTP response */
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

            /* 若狀態碼為200 ok */
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 取出回應字串 */
                    String strResult = EntityUtils.toString(httpResponse.getEntity());

                    // 回傳回應字串
                    return strResult;
                }
            } catch (ClientProtocolException e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return e.getMessage().toString();
            } catch (IOException e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return e.getMessage().toString();
            } catch (Exception e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return e.getMessage().toString();
            }
            return "failed";
        }

        @Override
        protected void onPostExecute(String result) { // result 為doInBackground() 的回傳值
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValid(String s) {
        return validPattern.matcher(s).matches();
    }
}

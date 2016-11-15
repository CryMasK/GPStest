package com.fatguy.fju.gpstest;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final Pattern validPattern = Pattern.compile("^[A-Za-z0-9_-]+$");
    private final String Url = "http://140.136.150.80/project_D/ajax/LogIn.php";
    private View mProgressView;
    private View mLoginFormView;
    private UserLoginTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressView = findViewById(R.id.login_progressBar);
        mLoginFormView = findViewById(R.id.login_scrollForm);
    }

    protected void loginBtn_onClick(View v) {
        EditText ETaccount = (EditText) findViewById(R.id.input_account);
        EditText ETpassword = (EditText) findViewById(R.id.input_password);
        TextView TVloginStatus = (TextView) findViewById(R.id.login_status);

        String account = ETaccount.getText().toString().trim();
        String password = ETpassword.getText().toString().trim();

        if (!(account.isEmpty()) && !(password.isEmpty())) { // 確認帳號、密碼是否留空
            if (isValid(account) && isValid(password)) { // 確認帳號、密碼是否為合法字元
                if (mAuthTask != null) {
                    return;
                }

                mAuthTask = new UserLoginTask(account, password);
                mAuthTask.execute((Void) null);
            }
            else {
                TVloginStatus.setText(R.string.error_invalid_field);
            }
        }
        else {
            TVloginStatus.setText(R.string.error_empty_field);
        }
    }

    protected void cancelBtn_onClick(View v) {
        this.finish();
    }

    private boolean isValid(String s) {
        return validPattern.matcher(s).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mAccount;
        private final String mPassword;

        UserLoginTask(String account, String password) {
            mAccount = account;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            /* 建立HTTP Post連線 */
            HttpPost httpRequest = new HttpPost(Url);

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("account", mAccount));
            data.add(new BasicNameValuePair("password", mPassword));

            try {
            /* 發出HTTP request */
                httpRequest.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));

            /* 取得HTTP response */
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

            /* 若狀態碼為200 ok */
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 取出回應字串 */
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    if (strResult.equals("nice try")){ // 判斷回應字串
                        return true;
                    }
                    Toast.makeText(getApplicationContext(), strResult, Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (ClientProtocolException e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                //return e.getMessage().toString();
                return false;
            } catch (IOException e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                //return e.getMessage().toString();
                return false;
            } catch (Exception e) {
                //Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                //return e.getMessage().toString();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                /* 登入成功 */
                Intent Login = new Intent();
                Login.setClass(LoginActivity.this, MainActivity.class);

                /*Bundle extras = new Bundle();
                extras.putString("account", mAccount); // 傳出帳號當作uID
                extras.putBoolean("login_success", true); // 用來確認登入成功
                Login.putExtras(extras);*/
                Login.putExtra("account", mAccount);

                //startActivity(Login);    //觸發換頁
                setResult(RESULT_OK, Login);

                finish(); // 結束登入畫面
            } else {
                TextView TVloginStatus = (TextView) findViewById(R.id.login_status);
                TVloginStatus.setText(R.string.error_incorrect_password);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

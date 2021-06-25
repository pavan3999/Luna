package studio.orchard.luna.LoginActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.Map;

import studio.orchard.luna.Component.BaseActivity;
import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;
import studio.orchard.luna.Component.Resolver.Connector;
import studio.orchard.luna.MainActivity.MainActivity;
import studio.orchard.luna.R;

public class LoginActivity extends BaseActivity {
    private EditText userName;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarNavigationBar(
                Constants.ActivityMode.NORMAL,
                Constants.ActivityMode.LIGHT_STATUS_BAR,
                Constants.ActivityMode.LIGHT_NAVIGATION_BAR);
        setContentView(R.layout.login_activity);
        initView();
    }

    private void initView(){
        setTitle("");
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        ImageView imgBackgroundCover = findViewById(R.id.login_background_cover);
        Bitmap backgroundCover = (Bitmap) DataHolder.getInstance().getData("loginActivityBackground");
        imgBackgroundCover.setImageBitmap(backgroundCover);

        TextView title = findViewById(R.id.login_title);
        title.setTextColor(getResources().getColor(R.color.color_text_black, getTheme()));
        title.setText("登录到亲小说");

        userName = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> login());

    }

    private void login(){
        if(userName.getText().toString().equals("") || password.getText().toString().equals("")){
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        Connector.getInstance().login(
                userName.getText().toString(),
                password.getText().toString(),
                new Connector.ConnectorListener() {
                    @Override
                    public void onFinish(Object obj) {
                        try{
                            Connection.Response response = (Connection.Response)obj;
                            JSONObject jsonObject = new JSONObject(response.body());
                            Map<String, String> cookies = UserSettingDataHolder.getInstance().getUserSetting().cookies;
                            cookies.clear();
                            cookies.putAll(response.cookies());
                            cookies.put("token", jsonObject.getString("Token"));
                            UserSettingDataHolder.getInstance().getUserSetting().userName = jsonObject.getJSONObject("User").getString("Username");
                            UserSettingDataHolder.getInstance().getUserSetting().userEmail = jsonObject.getJSONObject("User").getString("Email");
                            UserSettingDataHolder.getInstance().getUserSetting().showInfo = false;
                            UserSettingDataHolder.getInstance().saveUserSettingToFile(getBaseContext());
                        }catch(Exception ignored){ }
                        userName.setText("");
                        password.setText("");
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    }
                    @Override
                    public void onExceptionThrown(Exception e) {
                        password.setText("");
                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity(){
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.popup_fadeout);
    }
}

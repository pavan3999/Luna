package studio.orchard.luna.Component.Resolver;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;

public class Connector {
    private static volatile Connector instance;

    private Map<String, String> cookies;
    private Map<String, String> headers;

    public static Connector getInstance() {
        if (instance == null) {
            synchronized (Connector.class) {
                if (instance == null) {
                    instance = new Connector();
                }
            }
        }
        return instance;
    }

    public interface ConnectorListener{
        default void onFinish(Object obj) { }
        default void onExceptionThrown(Exception e) { e.printStackTrace(); }
    }

    private Connector(){
        headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        cookies = UserSettingDataHolder.getInstance().getUserSetting().cookies;
    }

    public void login(final String userName, final String password, ConnectorListener listener){
        final ConnectorHandler handler = new ConnectorHandler(listener);
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Message msg = new Message();
                try {
                    Map<String, String> data = new HashMap<>();
                    data.put("username", userName);
                    data.put("password", password);
                    Connection.Response response = Jsoup
                            .connect(Constants.Connector.API_LOGIN)
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .timeout(1000 * 3)
                            .method(Connection.Method.POST)
                            .headers(headers)
                            .data(data)
                            .execute();
                    if(new JSONObject(response.body()).getBoolean("Success")){
                        msg.what = Constants.MessageType.SUCCESS;
                        msg.obj = response;
                        handler.sendMessage(msg);
                    }else{
                        throw new Exception(Constants.ExceptionType.LOGIN_FAILED);
                    }
                } catch(Exception e) {
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void getProfile(ConnectorListener listener){
        final ConnectorHandler handler = new ConnectorHandler(listener);
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Message msg = new Message();
                try {
                    Connection.Response response = getResponse(Constants.Connector.PROFILE, Constants.Connector.METHOD_GET);
                    JSONObject jsonObject = new JSONObject(response.body());
                    msg.what = Constants.MessageType.SUCCESS;
                    msg.obj = jsonObject;
                    handler.sendMessage(msg);
                } catch(Exception e) {
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void getTime(ConnectorListener listener){
        final ConnectorHandler handler = new ConnectorHandler(listener);
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Message msg = new Message();
                try {
                    Connection.Response response = getResponse(Constants.Connector.TIME, Constants.Connector.METHOD_GET);
                    JSONObject jsonObject = new JSONObject(response.body());
                    msg.what = Constants.MessageType.SUCCESS;
                    msg.obj = jsonObject;
                    handler.sendMessage(msg);
                } catch(Exception e) {
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    Connection.Response getResponse(String url, int method) throws Exception{
        return Jsoup
                .connect(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .timeout(1000 * 3)
                .method(method == Constants.Connector.METHOD_GET ? Connection.Method.GET : Connection.Method.POST)
                .headers(headers)
                .cookies(cookies)
                .execute();
    }

    Document getDocument(String url) throws Exception{
        Document document = null;
        while(document == null){
            document = Jsoup
                    .connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .timeout(1000 * 3)
                    .headers(headers)
                    .cookies(cookies)
                    .get();
        }
        return document;
    }

    private static final class ConnectorHandler extends Handler {
        private ConnectorListener listener;
        private ConnectorHandler(ConnectorListener listener){ this.listener = listener; }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MessageType.EXCEPTION:
                    listener.onExceptionThrown((Exception)msg.obj);
                    break;
                case Constants.MessageType.SUCCESS:
                    listener.onFinish(msg.obj);
                    break;
            }
        }
    }
}

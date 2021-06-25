package studio.orchard.luna.Component.DataHolder;

import android.content.Context;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.IO;
import studio.orchard.luna.Component.SerializedClass.v0.UserSetting;

public class UserSettingDataHolder {
    private static volatile UserSettingDataHolder instance;
    private static UserSetting userSetting;

    private UserSettingDataHolder(){ }

    public static UserSettingDataHolder getInstance(){
        if (instance == null) {
            synchronized (UserSettingDataHolder.class) {
                if (instance == null) {
                    instance = new UserSettingDataHolder();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        if (instance == null){
            getInstance();
        }else{
            IO.init(context);
            userSetting = (UserSetting)IO.getSerializedData(context, Constants.Application.USER_SETTING_FILE_NAME);
            if(userSetting == null){
                IO.saveSerializedData(context, new UserSetting(), Constants.Application.USER_SETTING_FILE_NAME);
                userSetting = (UserSetting)IO.getSerializedData(context, Constants.Application.USER_SETTING_FILE_NAME);
            }
        }
    }

    public boolean hasLogin(){
        return !userSetting.cookies.isEmpty();
    }

    public String getUserSettingFileName(){
        return Constants.Application.USER_SETTING_FILE_NAME;
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }
    public void saveUserSettingToFile(Context context){
        IO.saveSerializedData(context, userSetting, Constants.Application.USER_SETTING_FILE_NAME);
    }
}

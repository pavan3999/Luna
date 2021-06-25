package studio.orchard.luna.Component.DataHolder;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

//关于Activity交换数据的单例
public class DataHolder {
    private static volatile DataHolder instance;
    private Map<String, Object> dataList;
    public boolean internetConnection;
    public boolean verifyEnablePassed;
    public boolean verifyVersionPassed;
    public boolean verifyTimePassed;

    private DataHolder(){
        dataList = new HashMap<>();
    }

    public static DataHolder getInstance() {
        if (instance == null) {
            synchronized (DataHolder.class) {
                if (instance == null) {
                    instance = new DataHolder();
                }
            }
        }
        return instance;
    }

    public void putData(String key, Object object){
        dataList.put(key, object);
    }

    public Object getData(String key){
        return dataList.get(key);
    }

    public void putWeakReferenceData(String key, Object object){
        WeakReference<?> value = new WeakReference<>(object);
        dataList.put(key, value);
    }

    public Object getWeakReferenceData(String key){
        WeakReference<?> value = (WeakReference<?>) dataList.get(key);
        if(value != null){
            return value.get();
        }
        return null;
    }

    public void putSoftReferenceData(String key, Object object){
        SoftReference<?> value = new SoftReference<>(object);
        dataList.put(key, value);
    }

    public Object getSoftReferenceData(String key){
        SoftReference<?> value = (SoftReference<?>) dataList.get(key);
        if(value != null){
            return value.get();
        }
        return null;
    }

    public void clear(){
        dataList.clear();
    }

}

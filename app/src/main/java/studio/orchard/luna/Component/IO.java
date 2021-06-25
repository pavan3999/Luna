package studio.orchard.luna.Component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.UserSettingDataHolder;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;
import studio.orchard.luna.Component.SerializedClass.v0.UserSetting;


public class IO {

    public static Serializable getSerializedData(Context context, String objectName) {
        Serializable object;
        File file;
        FileInputStream fileInputStream;
        try {
            file = new File(context.getExternalFilesDir(""), objectName);
            fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            object = (Serializable)objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Boolean saveSerializedData(Context context, Serializable bookShelf, String objectName){
        File file = new File(context.getExternalFilesDir(""), objectName);
        FileOutputStream fileOutputStream;
        try {
            if (!file.exists()) file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(bookShelf);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void init(Context context){
        File file = new File(context.getExternalFilesDir(""), BookShelfDataHolder.getInstance().getBookShelfFileName());
        if (!file.exists()) saveSerializedData(context, new BookShelf(), BookShelfDataHolder.getInstance().getBookShelfFileName());
        file = new File(context.getExternalFilesDir(""), UserSettingDataHolder.getInstance().getUserSettingFileName());
        if (!file.exists()) saveSerializedData(context, new UserSetting(), UserSettingDataHolder.getInstance().getUserSettingFileName());
        //file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Luna");
        //if (!file.exists()) copyAssets(context, "Documents",context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString());
    }


    private static void copyAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            assert fileNames != null;
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static BufferedReader fileToStreamBuffer(String file){
        try{
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return new BufferedReader(inputStreamReader);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static Bitmap fileToBitmap(String file){
        return BitmapFactory.decodeFile(file);

    }

    public static void saveChapterToFile(Context context, int bookSeriesID,
                                         int bookIndex, int chapterIndex, byte[] bytes) throws IOException{
        String path = bookSeriesID + "/" + bookIndex + "/" + chapterIndex;
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
        if (!file.exists()) file.mkdirs();
        path += "/" + chapterIndex + ".txt";
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
        if (!file.exists()) file.createNewFile();

        FileOutputStream FOS = new FileOutputStream(file);
        FOS.write(bytes);
        FOS.close();
    }

    public static void saveBitmapToFile(Context context, int bookSeriesID,
                                        int bookIndex, int chapterIndex, int bitmapIndex, Bitmap bitmap) throws IOException{
        String path = bookSeriesID + "/" + bookIndex + "/" + chapterIndex + "/" + "Illustration";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
        if (!file.exists()) file.mkdirs();
        path += "/" + bitmapIndex + ".jpg";
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
        if (!file.exists()) file.createNewFile();
        FileOutputStream FOS = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FOS);
        FOS.close();
    }

    public static void saveFile(Context context, String str)throws Exception{
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Luna");
        if (!file.exists()) file.mkdir();
        String path = "Luna/123.txt";
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), path);
        if (!file.exists()) file.createNewFile();
        FileOutputStream FOS = new FileOutputStream(file);
        FOS.write(str.getBytes());
        FOS.close();
    }
}

package studio.orchard.luna.Component.Resolver;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.net.URL;
import java.net.URLConnection;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;

public class BookCoverResolver implements Runnable {
    private Handler handler;
    private String bookCoverAddress;
    private int args;

    public BookCoverResolver(String bookCoverAddress, Handler handler, int args){
        this.bookCoverAddress = bookCoverAddress;
        this.handler = handler;
        this.args = args;
    }

    @Override
    public void run(){
        Message msg = new Message();
        Bitmap coverCache = (Bitmap) DataHolder.getInstance().getSoftReferenceData(bookCoverAddress);
        if(coverCache != null){
            msg.what = Constants.MessageType.BOOK_COVER;
            msg.arg1 = args;
            msg.obj = coverCache;
            handler.sendMessage(msg);
            return;
        }
        try {
            Bitmap bookCover = null;
            for (int retryCount = 0; retryCount < 2 && bookCover == null; retryCount++) {
                URL url = new URL(bookCoverAddress);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(5000);
                bookCover = BitmapFactory.decodeStream(urlConnection.getInputStream());
            }
            if(bookCover != null){
                DataHolder.getInstance().putSoftReferenceData(bookCoverAddress, bookCover);
                msg.what = Constants.MessageType.BOOK_COVER;
                msg.arg1 = args;
                msg.obj = bookCover;
                handler.sendMessage(msg);
            } else {
                throw new Exception();
            }
        }catch(Exception e){
            e.printStackTrace();
            msg.what = Constants.MessageType.BOOK_COVER;
            msg.arg1 = args;
            msg.obj = null;
            handler.sendMessage(msg);
            /*
            msg.what = 0;
            msg.obj = "获取书籍封面错误:无法连接到服务器";
            handler.sendMessage(msg);
             */
        }

    }
}

package studio.orchard.luna.Component.Resolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.IO;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;

public class BookDownloader {
    private boolean enable;
    private BookShelf.BookSeries bookSeries;
    private Context context;
    private boolean isDownloading;
    private boolean downloadAll;
    private int bookIndex;
    private int chapterOpenIndex;
    private BookDownloaderListener listener;

    public interface BookDownloaderListener{
        default void onExceptionThrown(Exception e) { e.printStackTrace(); }
        default void onBookDownloadFinished(int bookIndex, int chapterOpenIndex) { }
    }

    public BookDownloader(Context context, BookShelf.BookSeries bookSeries){
        init(context, bookSeries);
    }

    public BookDownloader(Context context, BookShelf.BookSeries bookSeries, BookDownloaderListener listener){
        init(context, bookSeries);
        setBookDownloaderListener(listener);
    }

    private void init(Context context, BookShelf.BookSeries bookSeries){
        this.enable = DataHolder.getInstance().internetConnection
                && DataHolder.getInstance().verifyEnablePassed
                && DataHolder.getInstance().verifyVersionPassed
                && DataHolder.getInstance().verifyTimePassed;
        this.context = context;
        this.bookSeries = bookSeries;
        this.isDownloading = false;
        this.downloadAll = false;
        this.bookIndex = 0;
        this.chapterOpenIndex = 0;
    }

    public void setBookDownloaderListener(BookDownloaderListener listener){
        this.listener = listener;
    }

    public boolean isDownloading(){
        return isDownloading;
    }

    public void downloadBook(int bookIndex){
        if(!enable
                || isDownloading
                || bookIndex >= bookSeries.bookList.size()
                || bookSeries.bookList.get(bookIndex) == null
                || listener == null){
            return;
        }
        isDownloading = true;
        downloadAll = false;
        this.bookIndex = bookIndex;
        this.chapterOpenIndex = 0;
        startDownloadBook(bookIndex);
    }

    public void downloadBook(int bookIndex, int chapterOpenIndex){
        if(!enable
                || isDownloading
                || bookIndex >= bookSeries.bookList.size()
                || bookSeries.bookList.get(bookIndex) == null
                || listener == null){
            return;
        }
        isDownloading = true;
        downloadAll = false;
        this.bookIndex = bookIndex;
        this.chapterOpenIndex = chapterOpenIndex;
        startDownloadBook(bookIndex);
    }


    private void startDownloadBook(int bookIndex){
        final BookShelf.Book book = bookSeries.bookList.get(bookIndex);
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(book.chapterTitle.size());
        ChapterDownloadHandler chapterDownloadHandler
                = new ChapterDownloadHandler(this, book.chapterTitle.size());
        for(int i = 0; i < book.chapterTitle.size(); i++){
            fixedThreadPool.execute(new ChapterDownloader(this, book, i, chapterDownloadHandler));
        }
    }

    private void BookFinishDownloaded(){
        if(!downloadAll){
            isDownloading = false;
            //downloadAll = false;
            BookShelf.Book book = bookSeries.bookList.get(bookIndex);
            book.isLocalized = 1;
            book.chapterReadingProgressNum = 0;
            listener.onBookDownloadFinished(bookIndex, chapterOpenIndex);
        }
    }

    private static class ChapterDownloader implements Runnable{
        private BookDownloader bookDownloader;
        private BookShelf.Book book;
        private Handler chapterDownloadHandler;
        private int chapterIndex;
        private List<String> illustrationAddress;
        private ChapterDownloader(BookDownloader bookDownloader, BookShelf.Book book, int chapterIndex, Handler handler){
            this.bookDownloader = bookDownloader;
            this.book = book;
            this.chapterIndex = chapterIndex;
            this.chapterDownloadHandler = handler;
            this.illustrationAddress = new ArrayList<>();
        }

        @Override
        public void run(){
            String chapterAddress = book.chapterAddress.get(chapterIndex);
            StringBuilder stringBuilder = new StringBuilder();
            Message msg = new Message();
            try{
                Document document = Connector.getInstance().getDocument(chapterAddress);
                resolveDocumentToStringBuilder(document, stringBuilder);
                String nextPageAddressSuffix;
                while((nextPageAddressSuffix = getNextPageAddressSuffix(document)) != null){
                    String nextPageAddress = chapterAddress + nextPageAddressSuffix;
                    document = Connector.getInstance().getDocument(nextPageAddress);
                    resolveDocumentToStringBuilder(document, stringBuilder);
                }
                downloadIllustration();
                IO.saveChapterToFile(bookDownloader.context, book.bookSeriesID, book.index, chapterIndex, stringBuilder.toString().getBytes());
                msg.what = Constants.MessageType.BOOK_CHAPTER_DOWNLOAD;
                chapterDownloadHandler.sendMessage(msg);
            }catch (Exception e){
                msg.what = Constants.MessageType.EXCEPTION;
                msg.obj = e;
                chapterDownloadHandler.sendMessage(msg);
            }
        }

        private void resolveDocumentToStringBuilder(Document document, StringBuilder stringBuilder){
            Elements illustrations = document.select("div[id=chapter_content]").select("img");
            for(Element illustration : illustrations) {
                illustrationAddress.add(illustration.attr("src"));
            }
            document.outputSettings(new Document.OutputSettings().prettyPrint(false));
            //select all <br> tags and append \n after that
            document.select("br").after("\\n");
            //select all <p> tags and prepend \n before that
            document.select("p").before("\\n");
            //get the HTML from the document, and retaining original new lines
            stringBuilder.append(
                    document.select("div[id=chapter_content]").
                            text().replaceAll("\\\\n", "\n"));
        }
        private String getNextPageAddressSuffix(Document document){
            Elements elements = document.select("a[class=qxs_btn]");
            for(int i = 0; i < elements.size(); i++){
                if(elements.get(i).text().equals("下一页")){
                    return elements.get(i).attr("href");
                }
            }
            return null;
        }
        private void downloadIllustration(){
            book.illustrationNum.set(chapterIndex, illustrationAddress.size());
            for(int i = 0; i < illustrationAddress.size(); i++){
                final int illustrationIndex = i;
                new Thread(()-> {
                    //Message msg = new Message();
                    try{
                        Bitmap illustration = null;
                        for(int retryCount = 0; retryCount < 2 && illustration == null; retryCount++) {
                            URL url = new URL(illustrationAddress.get(illustrationIndex));
                            URLConnection urlConnection = url.openConnection();
                            urlConnection.setConnectTimeout(5000);
                            illustration = BitmapFactory.decodeStream(urlConnection.getInputStream());
                        }
                        if(illustration != null){
                            IO.saveBitmapToFile(bookDownloader.context, book.bookSeriesID, book.index, chapterIndex, illustrationIndex, illustration);
                        }else{
                            throw new Exception();
                        }
                    }catch(Exception e){
                        //msg.what = Constants.MessageType.EXCEPTION;
                        //msg.obj = e;
                        //chapterDownloadHandler.sendMessage(msg);
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        /* 用于同步通知插图与书籍下载完毕的类，当插图与书籍均下载完毕时再发送消息给外部句柄
        private static class ChapterDownloadThreadHandler extends Handler{
            private ChapterDownloader chapterDownloader;
            private Handler chapterDownloadHandler;
            private int illustrationDownloadCount;
            private boolean chapterDownloadFinish;
            private boolean illustrationDownloadFinish;
            public ChapterDownloadThreadHandler(ChapterDownloader chapterDownloader, Handler chapterDownloadHandler){
                this.chapterDownloader = chapterDownloader;
                this.chapterDownloadHandler = chapterDownloadHandler;
            }
            @Override
            public void handleMessage(@NonNull Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case Constants.MessageType.EXCEPTION:
                        Message message = new Message();
                        message.what = Constants.MessageType.EXCEPTION;
                        message.obj = msg.obj;
                        chapterDownloadHandler.sendMessage(message);
                        break;
                    case Constants.MessageType.BOOK_CHAPTER_DOWNLOAD:
                        chapterDownloadFinish = true;
                        break;
                    case Constants.MessageType.BOOK_ILLUSTRATION_DOWNLOAD:
                        if(++illustrationDownloadCount == chapterDownloader.illustrationAddress.size()){
                            illustrationDownloadFinish = true;
                        }
                        break;
                }
                if(chapterDownloadFinish && (chapterDownloader.illustrationAddress.size() == 0 || illustrationDownloadFinish)){
                    Message message = new Message();
                    message.what = Constants.MessageType.BOOK_CHAPTER_DOWNLOAD;
                    chapterDownloadHandler.sendMessage(message);
                }
            }
        }*/
    }


    private static class ChapterDownloadHandler extends Handler{
        private int count;
        private BookDownloader bookDownloader;
        private int totalCount;
        private ChapterDownloadHandler(BookDownloader bookDownloader, int totalCount){
            this.count = 0;
            this.bookDownloader = bookDownloader;
            this.totalCount = totalCount;
        }

        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MessageType.EXCEPTION:
                    bookDownloader.isDownloading = false;
                    bookDownloader.listener.onExceptionThrown((Exception)msg.obj);
                    break;
                case Constants.MessageType.BOOK_CHAPTER_DOWNLOAD:
                    if(++count >= totalCount){
                        bookDownloader.BookFinishDownloaded();
                    }
                    break;
            }
        }
    }
}

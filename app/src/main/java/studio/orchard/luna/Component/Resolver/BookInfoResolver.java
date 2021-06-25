package studio.orchard.luna.Component.Resolver;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.Component.SerializedClass.v0.BookShelf;

public class BookInfoResolver{
    private boolean enable;
    private BookShelf.BookSeries bookSeries;
    private int bookSeriesIndex;
    private BookItemInfo bookItemInfo;
    private BookInfoResolverHandler handler;
    private BookInfoResolverListener listener;


    public interface BookInfoResolverListener{
        default void onExceptionThrown(Exception e) { e.printStackTrace(); }
        default void onBookInfoResolveFinished(BookShelf.BookSeries bookSeries) { }
    }

    public BookInfoResolver(){

    }

    public BookInfoResolver(BookItemInfo bookItemInfo){
       init(bookItemInfo);
    }

    public BookInfoResolver(BookItemInfo bookItemInfo, BookInfoResolverListener listener){
        init(bookItemInfo);
        this.setBookInfoResolverListener(listener);
    }

    private void init(BookItemInfo bookItemInfo){
        this.enable = DataHolder.getInstance().internetConnection
                && DataHolder.getInstance().verifyEnablePassed
                && DataHolder.getInstance().verifyVersionPassed
                && DataHolder.getInstance().verifyTimePassed;
        this.bookItemInfo = bookItemInfo;
        this.handler = new BookInfoResolverHandler(this);
        this.bookSeries = BookShelfDataHolder.getInstance().newBookSeries();
        this.bookSeriesIndex = BookShelfDataHolder.getInstance().getPositionOnBookShelf(bookItemInfo.bookName);
    }

    public void setBookInfoResolverListener(BookInfoResolverListener listener){
        this.listener = listener;
    }

    public BookShelf.BookSeries getBookSeries(){
        return bookSeries;
    }

    public void getBookInfo() {
        final BookShelf.BookSeries bookSeriesCache =
                (BookShelf.BookSeries) DataHolder.getInstance().getSoftReferenceData(bookItemInfo.bookName);
        if(bookSeriesCache != null){
            //有缓存
            bookSeries = bookSeriesIndex == -1
                    ? bookSeriesCache
                    : BookShelfDataHolder.getInstance().getBookSeries(bookSeriesIndex);
            listener.onBookInfoResolveFinished(bookSeries);
            return;
        }
        if(bookSeriesIndex != -1){
            //无缓存，且书籍在书架上，先返回书架上的书籍，后台开始检查更新
            listener.onBookInfoResolveFinished(BookShelfDataHolder.getInstance().getBookSeries(bookSeriesIndex));
            return;
        }

        if(!enable || listener == null) return;
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                try{
                    Document document = studio.orchard.luna.Component.Resolver.Connector.getInstance().getDocument(bookItemInfo.bookAddress);
                    resolveDocumentToBookSeries(bookItemInfo, document);
                }catch (Exception e){
                    Message msg = new Message();
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }finally{
                    if(bookSeries != null){
                        DataHolder.getInstance().putSoftReferenceData(bookItemInfo.bookName, bookSeries);
                        Message msg = new Message();
                        msg.what = Constants.MessageType.BOOK_INFO;
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    private void resolveDocumentToBookSeries(BookItemInfo item, Document document) throws Exception{
        if(document == null) return;
        bookSeries.bookSeriesAddress = item.bookAddress;
        bookSeries.bookSeriesCoverAddress = item.bookCoverAddress;
        bookSeries.bookSeriesCover = null;
        Elements infoElement = document.select("div[class=right]");
        bookSeries.bookSeriesName = infoElement.select("h1").text();
        bookSeries.bookSeriesAuthor = infoElement.select("div[class=info_item]").get(0).select("div").get(1).select("a").text();
        bookSeries.bookSeriesAuthorAddress = infoElement.select("div[class=info_item]").get(0).select("div").get(1).select("a").attr("href");
        bookSeries.bookSeriesStatus = infoElement.select("div[class=info_item]").get(0).select("div").get(2).select("span").text();
        bookSeries.bookSeriesUpdatedTime = infoElement.select("div[class=info_item]").get(2).select("div").get(1).text();
        bookSeries.bookSeriesLatestTitle = infoElement.select("div[class=info_item]").get(2).select("div").get(2).text();
        bookSeries.bookSeriesIntroduction = infoElement.select("textarea[class=intro]").text();
        Elements elements = infoElement.select("div[class=tags]").select("a");
        for (int i = 0; i < elements.size() - 1; i++) {
            bookSeries.bookSeriesTag.add(elements.get(i).text());
            bookSeries.bookSeriesTagAddress.add(Constants.Connector.MAIN + elements.get(i).attr("href"));
        }
        String tmp = bookSeries.bookSeriesCoverAddress;
        bookSeries.bookSeriesID = Integer.parseInt(
                bookSeries.bookSeriesCoverAddress.substring(tmp.indexOf("bookimg/") + 8, tmp.indexOf(".jpg")));

        Connection.Response response = studio.orchard.luna.Component.Resolver.Connector.getInstance().getResponse(
                Constants.Connector.API_GET + bookSeries.bookSeriesID, Constants.Connector.METHOD_POST);
        JSONObject jsonObject = new JSONObject(response.body());
        JSONArray volumes = jsonObject.getJSONArray("Volumes");
        for(int i = 0; i < volumes.length(); i++){
            BookShelf.Book book = BookShelfDataHolder.getInstance().newBook();
            book.index = i;
            JSONObject volume = (JSONObject)volumes.get(i);
            book.bookSeriesID = bookSeries.bookSeriesID;
            book.bookTitle = volume.getString("Volume_name");
            JSONArray chapters = new JSONArray(volume.getString("Chapters"));
            if(chapters != null){
                for(int j = 0; j < chapters.length(); j++){
                    JSONObject chapter = (JSONObject)chapters.get(j);
                    book.chapterTitle.add(chapter.getString("Chapter_name"));
                    String stringBuilder = Constants.Connector.MAIN +
                            "/read/0/" +
                            book.bookSeriesID +
                            "/" +
                            chapter.getString("Chapter_id") +
                            ".html";
                    book.chapterAddress.add(stringBuilder);
                    book.chapterReadingProgress.add(0.00f);
                    book.illustrationNum.add(0);
                }
                bookSeries.bookList.add(book);
            }
        }
    }

    private static final class BookInfoResolverHandler extends Handler{
        private BookInfoResolver bookInfoResolver;
        private BookInfoResolverHandler(BookInfoResolver bookInfoResolver){
            this.bookInfoResolver = bookInfoResolver;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MessageType.EXCEPTION:
                    bookInfoResolver.listener.onExceptionThrown((Exception)msg.obj);
                    break;
                case Constants.MessageType.BOOK_INFO:
                    int bookSeriesIndex = BookShelfDataHolder.getInstance().getPositionOnBookShelf(bookInfoResolver.bookItemInfo.bookName);
                    if(bookSeriesIndex == -1){
                        bookInfoResolver.listener.onBookInfoResolveFinished(bookInfoResolver.bookSeries);
                    }else if(BookShelfDataHolder.getInstance().mergeBookSeries(bookInfoResolver.bookSeries)){
                        bookInfoResolver.bookSeries = BookShelfDataHolder.getInstance().getBookSeries(bookSeriesIndex);
                        bookInfoResolver.listener.onBookInfoResolveFinished(bookInfoResolver.bookSeries);
                    }
                    break;

                case Constants.MessageType.BOOK_COVER:
                    BookItemInfo bookItemInfo = bookInfoResolver.bookItemInfo;
                    bookItemInfo.bookCover = (Bitmap)msg.obj;
                    bookItemInfo.isBookCoverResolved = true;
                    //bookInfoResolver.listener.onBookCoverResolveFinished(bookInfoResolver.bookSeries);
                    break;
            }
        }
    }


    public BookShelf.BookSeries getBookUpdate(int bookSeriesID) throws Exception{
        BookShelf.BookSeries bookSeries = BookShelfDataHolder.getInstance().newBookSeries();
        bookSeries.bookSeriesID = bookSeriesID;
        Connection.Response response = studio.orchard.luna.Component.Resolver.Connector.getInstance().getResponse(
                Constants.Connector.API_GET + bookSeriesID, Constants.Connector.METHOD_POST);
        JSONObject jsonObject = new JSONObject(response.body());
        JSONArray volumes = jsonObject.getJSONArray("Volumes");
        for(int i = 0; i < volumes.length(); i++){
            BookShelf.Book book = BookShelfDataHolder.getInstance().newBook();
            book.index = i;
            JSONObject volume = (JSONObject)volumes.get(i);
            book.bookSeriesID = bookSeriesID;
            book.bookTitle = volume.getString("Volume_name");
            JSONArray chapters = new JSONArray(volume.getString("Chapters"));
            if(chapters != null){
                for(int j = 0; j < chapters.length(); j++){
                    JSONObject chapter = chapters.getJSONObject(j);
                    book.chapterTitle.add(chapter.getString("Chapter_name"));
                    String stringBuilder = Constants.Connector.MAIN +
                            "/read/0/" +
                            book.bookSeriesID +
                            "/" +
                            chapter.getString("Chapter_id") +
                            ".html";
                    book.chapterAddress.add(stringBuilder);
                    book.chapterReadingProgress.add(0.00f);
                    book.illustrationNum.add(0);
                }
                bookSeries.bookList.add(book);
            }
        }
        /*
        BookShelf.Book book = BookShelfDataHolder.getInstance().newBook();
        book.bookTitle = "Volume_name";
        bookSeries.bookList.add(book);
        */

        return bookSeries;
    }
}

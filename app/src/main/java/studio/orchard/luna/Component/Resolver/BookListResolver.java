package studio.orchard.luna.Component.Resolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.orchard.luna.Component.Constants;
import studio.orchard.luna.Component.DataHolder.BookShelfDataHolder;
import studio.orchard.luna.Component.DataHolder.DataHolder;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.R;

public class BookListResolver{
    private boolean enable;
    private boolean searchEnable;
    private boolean isResolving;
    private int bookPage;
    private Bitmap bookDefaultCover;
    private String searchAddress;
    private List<BookItemInfo> itemList;
    private BookListResolverHandler handler;
    private BookListResolverListener listener;
    private List<String> blackList;

    public interface BookListResolverListener{
        Context getContext();
        default void onExceptionThrown(Exception e) { e.printStackTrace(); }
        default void onBookListResolveFinished() { }
        default void onBookListResolveReachedEnd() { }
        default void onBookCoverResolveFinished(int index) { }
    }

    public BookListResolver(){
        init();
    }

    public BookListResolver(BookListResolverListener bookListResolverListener){
        init();
        setBookListResolverListener(bookListResolverListener);
    }

    private void init(){
        enable = DataHolder.getInstance().internetConnection
                && DataHolder.getInstance().verifyEnablePassed
                && DataHolder.getInstance().verifyVersionPassed
                && DataHolder.getInstance().verifyTimePassed;
        searchEnable = true;
        isResolving = false;
        searchAddress = "";
        bookPage = 0;
        itemList = new ArrayList<>();
        bookDefaultCover = Bitmap.createBitmap(105,140,Bitmap.Config.ARGB_8888);
        bookDefaultCover.eraseColor(android.graphics.Color.rgb(255, 255, 255));
        handler = new BookListResolverHandler(this);

        JSONObject profile = (JSONObject)DataHolder.getInstance().getData("profile");
        blackList = new ArrayList<>();
        if(profile != null){
            try{
                JSONArray jsonBlackList = profile.getJSONArray("blackList");
                for(int i = 0; i < jsonBlackList.length(); i++){
                    blackList.add(jsonBlackList.getJSONObject(i).getString("bookName"));
                }
            }catch (Exception ignored){ }
        }

    }

    public void setBookListResolverListener(BookListResolverListener bookListResolverListener){
        listener = bookListResolverListener;
    }

    public boolean isResolving(){
        return isResolving;
    }

    public List<BookItemInfo> getItemList(){
        return itemList;
    }

    public void clearItemList(){
        itemList.clear();
        searchEnable = true;
        isResolving = false;
        bookPage = 0;
    }

    public void getLatestBookList(){
        if(!enable || bookPage >= 10 || isResolving || listener == null){
            return;
        }
        isResolving = true;
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Message msg = new Message();
                try {
                    StringBuilder url = new StringBuilder();
                    url.append(Constants.Connector.LATEST).append(++bookPage).append(".html");
                    Document document = Connector.getInstance().getDocument(url.toString());
                    url = new StringBuilder();
                    url.append(Constants.Connector.LATEST).append(++bookPage).append(".html");
                    Document document2 = Connector.getInstance().getDocument(url.toString());
                    resolveDocumentToItemList(document, itemList);
                    resolveDocumentToItemList(document2, itemList);
                    msg.what = Constants.MessageType.BOOK_LIST;
                    handler.sendMessage(msg);
                } catch(Exception e) {
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                } finally {
                    isResolving = false;
                }

            }
        }).start();

    }

    public void getBookList(final String searchAddress, final String name){
        if(!this.searchAddress.equals(searchAddress)){
            //变更搜索类别时需要重置一下bookPage和enable
            this.searchAddress = searchAddress;
            bookPage = 0;
            searchEnable = true;
        }

        if(!enable || isResolving || !searchEnable || listener == null){
            return;
        }

        final String address = searchAddress + name + "/";
        isResolving = true;
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Message msg = new Message();
                try {
                    StringBuilder url = new StringBuilder();
                    url.append(address).append(++bookPage).append(".html");
                    Document document = studio.orchard.luna.Component.Resolver.Connector.getInstance().getDocument(url.toString());
                    //避免越界造成的重复加载
                    if(!document.select("span[class=current]").text().equals(String.valueOf(bookPage))){
                        msg.what = Constants.MessageType.BOOK_LIST_REACHED_END;
                        handler.sendMessage(msg);
                        searchEnable = false;
                        return; }
                    resolveDocumentToItemList(document, itemList);
                    url = new StringBuilder();
                    url.append(address).append(++bookPage).append(".html");
                    Document document2 = studio.orchard.luna.Component.Resolver.Connector.getInstance().getDocument(url.toString());
                    if(!document2.select("span[class=current]").text().equals(String.valueOf(bookPage))){
                        msg.what = Constants.MessageType.BOOK_LIST_REACHED_END;
                        handler.sendMessage(msg);
                        searchEnable = false;
                        return; }
                    resolveDocumentToItemList(document2, itemList);
                    msg.what = Constants.MessageType.BOOK_LIST;
                    handler.sendMessage(msg);
                } catch(Exception e) {
                    msg.what = Constants.MessageType.EXCEPTION;
                    msg.obj = e;
                    handler.sendMessage(msg);
                } finally {
                    isResolving = false;
                }
            }
        }).start();
    }

    private void resolveDocumentToItemList(Document document, List<BookItemInfo> itemList) throws Exception{
        if(document == null) return;
        Elements elements = document.getElementsByClass("book");
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        for(int i = 0; i < elements.size(); i++){
            Element element = elements.get(i);
            BookItemInfo item = new BookItemInfo();
            item.bookName = element.select("div[class=book_info]").select("div[class=items]").select("h3").select("a").text();
            item.bookTitleNew = element.select("div[class=book_info]").select("div[class=items]").select("div").get(3).text();
            item.bookAddress = Constants.Connector.MAIN + element.select("a").attr("href");
            item.bookCoverAddress = element.select("img[class=book_img]").attr("src");
            item.bookCover = bookDefaultCover;
            item.index = itemList.size();
            if(contains(blackList, item.bookName)) continue;
            itemList.add(item.index, item);
            int index = BookShelfDataHolder.getInstance().getPositionOnBookShelf(item.bookName);
            if(index != -1){
                byte[] coverByte = BookShelfDataHolder.getInstance().getBookSeries(index).bookSeriesCover;
                Message msg = new Message();
                msg.what = Constants.MessageType.BOOK_COVER;
                msg.arg1 = item.index;
                msg.obj = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
                handler.sendMessage(msg);
            } else {
                fixedThreadPool.execute(new BookCoverResolver(item.bookCoverAddress, handler, item.index));
            }
        }
    }

    public void getRecommendationBookList(){
        if(!enable || isResolving || listener == null){
            return;
        }
        new Thread(() -> {
            Message msg = new Message();
            try{
                Document document = Connector.getInstance().getDocument(Constants.Connector.MAIN + "/");
                resolveRecommendationToBookList(document, itemList);
                msg.what = Constants.MessageType.BOOK_LIST;
                handler.sendMessage(msg);
            } catch(Exception e) {
                msg.what = Constants.MessageType.EXCEPTION;
                msg.obj = e;
                handler.sendMessage(msg);
            } finally {
                isResolving = false;
            }

        }).start();
    }

    private void resolveRecommendationToBookList(Document document, List<BookItemInfo> itemList) throws Exception{
        if(document == null) return;
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
        Elements elements = document.select("div[id=content_right]")
                .select("div[class=index_books]").get(1).select("div[class=book]");
        for(int i = 0; i < elements.size(); i++){
            BookItemInfo item = new BookItemInfo();
            item.bookName = elements.get(i).select("div[class=items]").select("h3").select("a").text();
            item.bookAddress = Constants.Connector.MAIN +
                    elements.get(i).select("div[class=items]").select("h3").select("a").attr("href");
            item.bookTitleNew = elements.get(i).select("div[class=items]").select("div[class=less]").get(1).text();
            item.bookAuthor = elements.get(i).select("div[class=items]").select("div[class=less]").get(0).text();
            item.bookIntroduction = elements.get(i).select("div[class=items]").select("div[class=less]").get(2).text();
            item.bookCoverAddress = elements.get(i).select("img[class=book_img]").attr("src");
            item.bookCover = bookDefaultCover;
            item.background = bookDefaultCover;
            item.index = itemList.size();
            if(contains(blackList, item.bookName)) continue;
            itemList.add(item.index, item);
            int index = BookShelfDataHolder.getInstance().getPositionOnBookShelf(item.bookName);
            if(index != -1){
                byte[] coverByte = BookShelfDataHolder.getInstance().getBookSeries(index).bookSeriesCover;
                Message msg = new Message();
                msg.what = Constants.MessageType.BOOK_COVER;
                msg.arg1 = item.index;
                msg.obj = BitmapFactory.decodeByteArray(coverByte, 0, coverByte.length);
                handler.sendMessage(msg);
            } else {
                fixedThreadPool.execute(new BookCoverResolver(item.bookCoverAddress, handler, item.index));
            }
        }
    }

    private <T> boolean contains(List<T> list, T o){
        for(T obj : list){
            if(obj.equals(o)){
                return true;
            }
        }
        return false;
    }

    private static final class BookListResolverHandler extends Handler{
        private BookListResolver bookListResolver;
        private BookListResolverHandler(BookListResolver bookListResolver){
            this.bookListResolver = bookListResolver;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case Constants.MessageType.EXCEPTION:
                    bookListResolver.listener.onExceptionThrown((Exception)msg.obj);
                    break;
                case Constants.MessageType.BOOK_LIST:
                    bookListResolver.listener.onBookListResolveFinished();
                    break;
                case Constants.MessageType.BOOK_LIST_REACHED_END:
                    bookListResolver.listener.onBookListResolveReachedEnd();
                    break;
                case Constants.MessageType.BOOK_COVER:
                    int index = msg.arg1;
                    if(index < bookListResolver.getItemList().size()){
                        BookItemInfo bookItemInfo = bookListResolver.getItemList().get(index);
                        if(msg.obj != null){
                            bookItemInfo.bookCover = (Bitmap)msg.obj;
                        }else{
                            bookItemInfo.bookCover = BitmapFactory.decodeResource(
                                    bookListResolver.listener.getContext().getResources(),
                                    R.drawable.ic_default_cover);
                        }
                        bookItemInfo.isBookCoverResolved = true;
                        bookListResolver.listener.onBookCoverResolveFinished(index);
                    }
                    break;
            }
        }
    }
}

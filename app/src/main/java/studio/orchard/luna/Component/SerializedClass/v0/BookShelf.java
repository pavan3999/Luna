package studio.orchard.luna.Component.SerializedClass.v0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class BookShelf implements Serializable {
    private static final long serialVersionUID = -6661691739738513021L;
    public int readingProgress;
    public List<BookSeries> bookSeriesList;
    public BookShelf(){
        readingProgress = -1;
        bookSeriesList = new ArrayList<>();
    }
    public final class BookSeries implements Serializable{
        private static final long serialVersionUID = -6109754325823423879L;
        public int index; //书的序列号
        public int bookReadingProgress; //阅读书籍进度
        public int bookSeriesID;
        public boolean updateNotification;
        public String bookSeriesName;
        public String bookSeriesIntroduction;
        public String bookSeriesAuthor;
        public String bookSeriesAuthorAddress;
        public String bookSeriesStatus;
        public String bookSeriesUpdatedTime;
        public String bookSeriesLatestTitle;
        public String bookSeriesAddress;
        public String bookSeriesCoverAddress;
        public List<String> bookSeriesTag;
        public List<String> bookSeriesTagAddress;
        public byte[] bookSeriesCover;
        public List<Book> bookList;
        public BookSeries(){
            bookSeriesTag = new ArrayList<>();
            bookSeriesTagAddress = new ArrayList<>();
            bookList = new ArrayList<>();
        }
    }
    public final class Book implements Serializable{
        private static final long serialVersionUID = -6683944880494994011L;
        public int index;   //在BookSeries里的序列
        public int isLocalized; //本书是否缓存
        public int chapterReadingProgressNum; //阅读到第几章了  v1更新
        public List<Float> chapterReadingProgress; //每章节的阅读进度
        public int bookSeriesID;
        public String bookTitle; //该卷的名字
        public List<String> chapterTitle;
        public List<String> chapterAddress;
        public List<Integer> illustrationNum;
        public Book(){
            chapterReadingProgress = new ArrayList<>();
            chapterTitle = new ArrayList<>();
            chapterAddress = new ArrayList<>();
            illustrationNum = new ArrayList<>();
        }
    }
}

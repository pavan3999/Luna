package studio.orchard.luna.Component;

public class Constants {

    public static final class Application{
        public static final float VERSION = 0.2f;
        public static final long EXPIRE_DATE = 20220101000000L;
        public static final String BOOKSHELF_FILE_NAME = "bookshelf_1.0.dat";
        public static final String USER_SETTING_FILE_NAME = "usersetting_1.0.dat";
        public static final int VERIFY_SUCCESS = 0;
        public static final int SERVICE_NOT_AVAILABLE = 1;
        public static final int VERSION_NOT_EXPECT = 2;
        public static final int DATE_EXPIRE = 3;
    }

    public static final class ActivityMode {
        public static final int NORMAL = 0;
        public static final int FULLSCREEN = 1;
        public static final int LIGHT_STATUS_BAR = 2;
        public static final int DARK_STATUS_BAR = 3;
        public static final int LIGHT_NAVIGATION_BAR = 4;
        public static final int DARK_NAVIGATION_BAR = 5;
    }

    public static final class SearchType{
        public static final int SHOW = 0;
        public static final int SEARCH = 1;
        public static final int BOOK_AUTHOR = 2;
        public static final int BOOK = 3;
        public static final int AUTHOR = 4;
        public static final int TAG = 5;
    }

    public static final class Connector {
        public static final int METHOD_GET = 0;
        public static final int METHOD_POST = 1;
        public static final String PROFILE = "https://cdn.jsdelivr.net/gh/orcharddu/Luna@pages/data/profile.json";
        public static final String TIME = "https://quan.suning.com/getSysTime.do";

        public static final String MAIN = "http://www.qinxiaoshuo.com";
        public static final String API_GET = "http://www.qinxiaoshuo.com/api/user/book/get/";
        public static final String API_LOGIN = "http://www.qinxiaoshuo.com/api/user/login";
        public static final String LATEST = "http://www.qinxiaoshuo.com/tag/日轻/";
        public static final String SEARCH_BOOK = "http://www.qinxiaoshuo.com/search/";
        public static final String SEARCH_AUTHOR = "http://www.qinxiaoshuo.com/author/";
        public static final String SEARCH_TAG = "http://www.qinxiaoshuo.com/tag/";

        public static final String CATEGORY_LIANAI = "http://www.qinxiaoshuo.com/tag/恋爱/";
        public static final String CATEGORY_XIAOYUAN = "http://www.qinxiaoshuo.com/tag/校园/";
        public static final String CATEGORY_GAOXIAO = "http://www.qinxiaoshuo.com/tag/搞笑/";
        public static final String CATEGORY_HOUGONG = "http://www.qinxiaoshuo.com/tag/后宫/";
        public static final String CATEGORY_MAOXIAN = "http://www.qinxiaoshuo.com/tag/冒险/";
        public static final String CATEGORY_YISHIJIE = "http://www.qinxiaoshuo.com/tag/异世界/";
        public static final String CATEGORY_MOFA = "http://www.qinxiaoshuo.com/tag/魔法/";
        public static final String CATEGORY_SHENGUI = "http://www.qinxiaoshuo.com/tag/神鬼/";
        public static final String CATEGORY_ZHENTAN = "http://www.qinxiaoshuo.com/tag/侦探/";
        public static final String CATEGORY_KONGBU = "http://www.qinxiaoshuo.com/tag/恐怖/";
    }

    public static final class MessageType{
        public static final int EXIT = -1;
        public static final int EXCEPTION = 0;
        public static final int SUCCESS = 1;
        public static final int BOOK_LIST = 2;
        public static final int BOOK_LIST_REACHED_END = 3;
        public static final int BOOK_COVER = 4;
        public static final int BOOK_INFO = 5;
        public static final int BOOK_DOWNLOAD = 6;
        public static final int BOOK_CHAPTER_DOWNLOAD = 7;
        public static final int BOOK_ILLUSTRATION_DOWNLOAD = 8;
        public static final int BOOK_PROGRESS = 9;
        public static final int BOOK_ADD_FAVOR = 10;
        public static final int BOOK_REMOVE_FAVOR = 11;
        public static final int BOOK_UPDATE = 12;
        public static final int BOOK_UPDATE_FINISH = 13;
    }

    public static final class ExceptionType{
        public static final String LOGIN_FAILED = "LOGIN_FAILED";

    }


    public static final class Category{
        public static final int LIANAI = 0;
        public static final int XIAOYUAN = 1;
        public static final int GAOXIAO = 2;
        public static final int HOUGONG = 3;
        public static final int MAOXIAN = 4;
        public static final int YISHIJIE = 5;
        public static final int MOFA = 6;
        public static final int SHENGUI = 7;
        public static final int ZHENTAN = 8;
        public static final int KONGBU = 9;
    }
}

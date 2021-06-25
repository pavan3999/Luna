package studio.orchard.luna.Component.SerializedClass;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public final class BookItemInfo implements Parcelable {
    //An adapter class for data exchanging among BookListResolver, MainActivity and BookActivity
    public int index = 0;
    public int type = 0;
    public String bookName = null;
    public String bookTitleNew = null;
    public String bookAuthor = null;
    public String bookIntroduction = null;
    public String bookAddress = null;
    public String bookCoverAddress = null;
    public String entrance = null;
    public Bitmap bookCover = null;
    public Bitmap background = null;
    public Boolean isBookCoverResolved = false;
    public Boolean updateNotification = false;

    public BookItemInfo(){
    }

    public BookItemInfo(int type){
        this.type = type;
    }

    private BookItemInfo(Parcel source){
        index = source.readInt();
        type = source.readInt();
        bookName = source.readString();
        bookTitleNew = source.readString();
        bookAuthor = source.readString();
        bookIntroduction = source.readString();
        bookAddress = source.readString();
        bookCoverAddress = source.readString();
        entrance = source.readString();
        bookCover = Bitmap.CREATOR.createFromParcel(source);
        background = Bitmap.CREATOR.createFromParcel(source);
        isBookCoverResolved = source.readInt() == 1;
        updateNotification = source.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeInt(type);
        dest.writeString(bookName);
        dest.writeString(bookTitleNew);
        dest.writeString(bookAuthor);
        dest.writeString(bookIntroduction);
        dest.writeString(bookAddress);
        dest.writeString(bookCoverAddress);
        dest.writeString(entrance);
        if(bookCover != null)bookCover.writeToParcel(dest, 0);
        if(background != null)background.writeToParcel(dest, 0);
        dest.writeInt(isBookCoverResolved ? 1 : 0);
        dest.writeInt(updateNotification ? 1 : 0);
    }

    public static final Creator<BookItemInfo> CREATOR = new Creator<BookItemInfo>() {
        @Override
        public BookItemInfo createFromParcel(Parcel parcel) {
            return new BookItemInfo(parcel);
        }

        @Override
        public BookItemInfo[] newArray(int i) {
            return new BookItemInfo[i];
        }
    };
}

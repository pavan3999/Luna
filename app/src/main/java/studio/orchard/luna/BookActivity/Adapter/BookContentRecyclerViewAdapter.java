package studio.orchard.luna.BookActivity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import studio.orchard.luna.BookActivity.Fragment.BookFragmentContent;
import studio.orchard.luna.R;

public class BookContentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<BookFragmentContent.ItemAdapter> itemList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private int marginTop;

    public BookContentRecyclerViewAdapter(List<BookFragmentContent.ItemAdapter> itemList, int marginTop){
        this.itemList = itemList;
        this.marginTop = marginTop;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View headerView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.book_fragment_content_header, parent, false);
        View bookTitleView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.book_fragment_content_item_cover, parent, false);
        View chapterTitleView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.book_fragment_content_item, parent, false);
        //加载Item View的时候根据不同Type加载不同的布局

        switch (viewType){
            case 0:
                return new HeaderViewHolder(headerView);
            case 1:
                return new BookTitleViewHolder(bookTitleView);
            default:
                return new ChapterTitleViewHolder(chapterTitleView);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder{
        private FrameLayout frameLayout;
        //private ImageView bookCover;
        //private Button downloadAll;
        //private Button startReading;
        private HeaderViewHolder(View itemView){
            super(itemView);
            frameLayout = itemView.findViewById(R.id.book_content_header);
            //bookCover = itemView.findViewById(R.id.book_content_img_cover);
            //downloadAll = itemView.findViewById(R.id.book_content_btn_download_all);
            //startReading = itemView.findViewById(R.id.book_content_btn_start_reading);
        }
    }

    private static class BookTitleViewHolder extends RecyclerView.ViewHolder {
        private TextView bookTitle;
        private TextView bookTitleDownload;
        private ImageView bookCover;
        private BookTitleViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_content_chapter_title);
            bookTitleDownload = itemView.findViewById(R.id.book_content_download);
            bookCover = itemView.findViewById(R.id.book_content_img_cover);
        }
    }

    private static class ChapterTitleViewHolder extends RecyclerView.ViewHolder {
        private TextView chapterTitle;
        private TextView chapterReadingProgress;

        private ChapterTitleViewHolder(View itemView) {
            super(itemView);
            chapterTitle = itemView.findViewById(R.id.book_content_chapter_title);
            chapterReadingProgress = itemView.findViewById(R.id.book_content_chapter_reading_progress);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        BookFragmentContent.ItemAdapter item = itemList.get(position);
        if(holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder)holder;
            headerViewHolder.frameLayout.setPadding(0, marginTop, 0, 0);
            /*
            headerViewHolder.bookCover.setImageBitmap(item.bookCover);
            if(onItemClickListener != null){
                headerViewHolder.downloadAll.setOnClickListener(view -> onItemClickListener.onDownloadAllClick(view));
                headerViewHolder.startReading.setOnClickListener(view -> onItemClickListener.onStartReadingClick(view));
            }

            if(onItemLongClickListener != null){
                headerViewHolder.itemView.setOnLongClickListener(view -> {
                    int position1 = holder.getLayoutPosition();
                    onItemLongClickListener.onItemLongClick(holder.itemView, position1);
                    return true;
                });
            }*/

        }else if(holder instanceof BookTitleViewHolder) {
            BookTitleViewHolder viewHolder = (BookTitleViewHolder)holder;
            //书卷标题的ViewHolder
            switch (item.isLocalized){
                case -1: //正在下载
                    viewHolder.bookTitleDownload.setText("正在下载");
                    break;
                case 0: //未下载
                    viewHolder.bookTitleDownload.setText("下载");
                    break;
                case 1: //下载完毕
                    viewHolder.bookTitleDownload.setText("阅读");
                    break;
            }
            viewHolder.bookCover.setImageBitmap(item.bookCover);
            viewHolder.bookTitle.setText(item.itemTitle);
            if(onItemClickListener != null){
                //为ItemView设置监听器
                viewHolder.itemView.setOnClickListener(view -> onItemClickListener.onItemClick(holder.itemView, viewHolder.getLayoutPosition()));
            }
            if(onItemLongClickListener != null){
                viewHolder.itemView.setOnLongClickListener(view -> {
                    onItemLongClickListener.onItemLongClick(holder.itemView, viewHolder.getLayoutPosition());
                    return true;
                });
            }
        } else if(holder instanceof ChapterTitleViewHolder){
            ChapterTitleViewHolder viewHolder= (ChapterTitleViewHolder)holder;
            //章节标题的ViewHolder
            viewHolder.chapterTitle.setText(item.itemTitle);
            if(item.isLocalized == 1 && item.chapterReadingProgress != 0){
                viewHolder.chapterReadingProgress.setText((item.chapterReadingProgress +  "%"));
            }else {
                viewHolder.chapterReadingProgress.setText("");
            }

            if(onItemClickListener != null){
                viewHolder.itemView.setOnClickListener(view -> onItemClickListener.onItemClick(holder.itemView, viewHolder.getLayoutPosition()));
            }
            if(onItemLongClickListener != null){
                viewHolder.itemView.setOnLongClickListener(view -> {
                    onItemLongClickListener.onItemLongClick(holder.itemView, viewHolder.getLayoutPosition());
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).itemType;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onDownloadAllClick(View view);
        void onStartReadingClick(View view);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}

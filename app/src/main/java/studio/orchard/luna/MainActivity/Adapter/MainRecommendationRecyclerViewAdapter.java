package studio.orchard.luna.MainActivity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.R;

public class MainRecommendationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<BookItemInfo> itemList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private int marginTop;

    public MainRecommendationRecyclerViewAdapter(List<BookItemInfo> itemList, int marginTop){
        this.itemList = itemList;
        this.marginTop = marginTop;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //加载布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_fragment_recommendation_item, parent, false);
        View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_fragment_recommendation_header, parent, false);
        if (viewType == 1) {
            return new HeaderViewHolder(headerView);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return itemList.size();}

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView bookTitle, bookTitleNew, bookIntroduction;
        private ImageView bookCover;

        private ViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.main_recommendation_title);
            bookTitleNew = itemView.findViewById(R.id.main_recommendation_title_new);
            bookIntroduction = itemView.findViewById(R.id.main_recommendation_introduction);
            bookCover = itemView.findViewById(R.id.main_recommendation_cover);
            bookCover.setElevation(10);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout header;
        private TextView headerTitle;
        private HeaderViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.main_recommendation_header);
            headerTitle = itemView.findViewById(R.id.main_recommendation_header_title);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(holder instanceof HeaderViewHolder){
            HeaderViewHolder viewHolder = (HeaderViewHolder)holder;
            if(payloads.isEmpty()){
                //如果是空，则全部更新
                onBindViewHolder(holder, position);
            } else {
                //更新局部
                if(position == 0){
                    viewHolder.header.setPadding(0, marginTop, 0, 0);
                    viewHolder.headerTitle.setText("随 / 机 / 推 / 荐");
                }
            }

        }else if(holder instanceof ViewHolder){
            ViewHolder viewHolder = (ViewHolder)holder;
            if(payloads.isEmpty()){
                //如果是空，则全部更新
                onBindViewHolder(holder, position);
            } else {
                //更新局部
                BookItemInfo item = (BookItemInfo)payloads.get(0);
                viewHolder.bookCover.setImageBitmap(item.bookCover);
                viewHolder.bookCover.setElevation(10);
            }
        }
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder){
            HeaderViewHolder viewHolder = (HeaderViewHolder)holder;
            if(position == 0){
                viewHolder.header.setPadding(0, marginTop, 0, 0);
                viewHolder.headerTitle.setText("随 / 机 / 推 / 荐");
            }
        }else if(holder instanceof ViewHolder){
            ViewHolder viewHolder = (ViewHolder)holder;
            BookItemInfo item = itemList.get(position);
            viewHolder.bookCover.setImageBitmap(item.bookCover);
            viewHolder.bookTitle.setText(item.bookName);
            viewHolder.bookTitleNew.setText(item.bookAuthor);
            viewHolder.bookIntroduction.setText(item.bookIntroduction);
            viewHolder.bookCover.setElevation(10);
            if (onItemClickListener != null){
                viewHolder.itemView.setOnClickListener(view -> onItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition()));
            }
            if (onItemLongClickListener != null){
                viewHolder.itemView.setOnLongClickListener(view -> {
                    onItemLongClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) { return itemList.get(position).type; }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.onItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.onItemLongClickListener = mOnItemLongClickListener;
    }

}

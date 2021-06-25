package studio.orchard.luna.MainActivity.Adapter;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import studio.orchard.luna.Component.RecyclerViewItemTouchHelper;
import studio.orchard.luna.Component.SerializedClass.BookItemInfo;
import studio.orchard.luna.R;


public class MainBookShelfRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RecyclerViewItemTouchHelper.Listener{
    private List<BookItemInfo> itemList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemMoveListener onItemMoveListener;
    private OnItemClear onItemClearListener;
    private int marginTop;

    public MainBookShelfRecyclerViewAdapter(List<BookItemInfo> itemList, int marginTop){
        this.itemList = itemList;
        this.marginTop = marginTop;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //加载布局文件
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_fragment_bookshelf_item, parent, false);
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_fragment_bookshelf_header, parent, false);
        if (viewType == 1) {
            return new HeaderViewHolder(headerView);
        }
        return new ViewHolder(view);
    }

    private class ViewHolder extends RecyclerView.ViewHolder
            implements MainBookShelfRecyclerViewAdapter.MainBookShelfRecyclerViewItemTouchHelper.onStateChangedListener{
        public TextView bookTitle;
        public ImageView bookCover;
        public CardView notificationHint;

        private ViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.main_bookshelf_title);
            bookCover = itemView.findViewById(R.id.main_bookshelf_cover);
            notificationHint = itemView.findViewById(R.id.main_bookshelf_update_hint);
            bookCover.setElevation(10);
        }

        @Override
        public void onItemSelected() {
            bookCover.setElevation(35);
        }

        @Override
        public void onItemClear() {
            //恢复item的背景颜色
            bookCover.setElevation(10);
            onItemClearListener.onItemClear();
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout header;
        private TextView headerTitle;
        private TextView headerCheckUpdate;
        private HeaderViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.main_bookshelf_header);
            headerTitle = itemView.findViewById(R.id.main_bookshelf_header_title);
            headerCheckUpdate = itemView.findViewById(R.id.main_bookshelf_header_checkupdate);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder){
            final HeaderViewHolder viewHolder = (HeaderViewHolder)holder;
            if(position == 0){
                viewHolder.header.setPadding(0, marginTop, 0, 0);
                viewHolder.headerTitle.setText("我 / 的 / 书 / 架");
                viewHolder.headerCheckUpdate.setOnClickListener(v -> onItemClickListener.onCheckUpdate(v));
            }

        }else if(holder instanceof ViewHolder){
            final ViewHolder viewHolder = (ViewHolder)holder;
            BookItemInfo item = itemList.get(position);
            viewHolder.bookCover.setImageBitmap(item.bookCover);
            viewHolder.bookTitle.setText(item.bookName);
            viewHolder.notificationHint.setVisibility(item.updateNotification ? View.VISIBLE : View.INVISIBLE);
            if (onItemClickListener != null){
                viewHolder.itemView.setOnClickListener(view -> onItemClickListener.onItemClick(viewHolder.itemView, viewHolder.getLayoutPosition()));
            }
            if (onItemLongClickListener != null){
                viewHolder.itemView.setOnLongClickListener(view -> {
                    onItemLongClickListener.onItemLongClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).type;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        onItemMoveListener.onItemMove(fromPosition, toPosition);
        notifyItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        itemList.remove(position);
        //onItemDismissListener.onItemDismiss();
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();}

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onCheckUpdate(View view);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    public interface OnItemMoveListener{
        void onItemMove(int fromPosition, int toPosition);
    }

    public interface OnItemClear{
        void onItemClear();
    }

    /*
    public interface OnItemDismiss{
        void onItemDismiss();
    }
*/
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemMoveListener(OnItemMoveListener onItemMoveListener) {
        this.onItemMoveListener = onItemMoveListener;
    }

    public void setOnItemClear(OnItemClear onItemClear) {
        this.onItemClearListener = onItemClear;
    }
/*
    public void setOnItemDismiss(OnItemDismiss onItemDismiss){
        this.onItemDismissListener = onItemDismiss;
    }
*/

    public static class MainBookShelfRecyclerViewItemTouchHelper extends RecyclerViewItemTouchHelper {
        public MainBookShelfRecyclerViewItemTouchHelper(Listener listener) {
            super(listener);
        }
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return viewHolder instanceof HeaderViewHolder
                    ? makeMovementFlags(0, 0)
                    : makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        private List<BookItemInfo> itemList;
        private int px;

        public ItemDecoration(Context context, List<BookItemInfo> itemList, int dp) {
            this.itemList = itemList;
            this.px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    context.getResources().getDisplayMetrics());
        }
        //public ItemDecoration(int px) { this.px = px; }
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            if(parent.getChildLayoutPosition(view) == 0) return;
            if (itemList.get(parent.getChildLayoutPosition(view)).index % 3 == 0) {
                outRect.left = px;
            }
            if (itemList.get(parent.getChildLayoutPosition(view)).index % 3 == 2) {
                outRect.right = px;
            }
        }
    }
}


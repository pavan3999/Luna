package studio.orchard.luna.ViewerActivity.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ViewerActivityViewPagerAdapter extends PagerAdapter {
    private List<View> viewList;
    public ViewerActivityViewPagerAdapter(List<View> viewList){
        this.viewList = viewList;
    }

    @Override
    public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }
    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        // TODO Auto-generated method stub
        container.removeView(viewList.get(position));
    }
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        container.addView(viewList.get(position));
        return viewList.get(position);
    }

}

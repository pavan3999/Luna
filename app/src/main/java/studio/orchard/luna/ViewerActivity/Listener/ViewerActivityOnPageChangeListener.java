package studio.orchard.luna.ViewerActivity.Listener;

import android.annotation.SuppressLint;

import androidx.viewpager.widget.ViewPager;

import studio.orchard.luna.ViewerActivity.ViewerActivity;


@SuppressLint("Registered")
public class ViewerActivityOnPageChangeListener implements ViewPager.OnPageChangeListener {
    private ViewerActivity v;
    public ViewerActivityOnPageChangeListener(ViewerActivity viewerActivity){
        this.v = viewerActivity;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO Auto-generated method stub
        if (positionOffset == 0 && v.isLoaded ) {
            if(position == 2){
                if(!v.currentPageViewer.isLastPage() || v.currentPageViewer.hasNextChapter()){
                    v.lastPageViewer.nextPage();
                    v.currentPageViewer.nextPage();
                    v.nextPageViewer.nextPage();
                    v.viewPager.setCurrentItem(1, false);
                    //Log.d("TAG_", "last chapter index:"+v.lastPageViewer.getChapterIndex() + "   c index:" +v.currentPageViewer.getChapterIndex()
                    //        + "  nex in:" + v.nextPageViewer.getChapterIndex());
                }
            }else if(position == 0){
                if(!v.currentPageViewer.isFirstPage() || v.currentPageViewer.hasLastChapter()){
                    v.lastPageViewer.lastPage();
                    v.currentPageViewer.lastPage();
                    v.nextPageViewer.lastPage();
                    //Log.d("TAG_", "last chapter index:"+v.lastPageViewer.getChapterIndex() + "   c index:" +v.currentPageViewer.getChapterIndex()
                    //       + "  nex in:" + v.nextPageViewer.getChapterIndex());
                    v.viewPager.setCurrentItem(1, false);
                }
            };
        }
    }
}

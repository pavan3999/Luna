package studio.orchard.luna.BookActivity.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class BookViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;

    public BookViewPagerAdapter(FragmentManager fragmentManager, int behavior, List<Fragment> fragmentList){
        super(fragmentManager, behavior);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int index){
        return fragmentList.get(index);
    }

    @Override
    public int getCount(){
        return fragmentList.size();
    }
}

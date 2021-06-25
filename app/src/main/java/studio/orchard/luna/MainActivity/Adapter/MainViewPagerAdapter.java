package studio.orchard.luna.MainActivity.Adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;

    public MainViewPagerAdapter(FragmentManager fragmentManager, int behavior, List<Fragment> fragmentList){
        //super(fragmentManager, fragmentList.size());
        super(fragmentManager, behavior);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int index){
        return fragmentList.get(index);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //保证page都不会被销毁
        //super.destroyItem(container, position, object);
    }

    @Override
    public int getCount(){
        return fragmentList.size();
    }
}

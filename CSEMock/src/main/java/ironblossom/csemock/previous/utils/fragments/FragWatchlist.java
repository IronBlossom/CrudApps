package ironblossom.csemock.previous.utils.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ironblossom.csemock.R;
import ironblossom.csemock.previous.utils.fragments.inner.ShareFragmet;

public class FragWatchlist extends Fragment {
    ViewPager t1ViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_watchlist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        t1ViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        t1ViewPager.setAdapter(new ShareViewPagerAdapter(getChildFragmentManager()));

    }

    private class ShareViewPagerAdapter extends FragmentPagerAdapter {


        public ShareViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ShareFragmet();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}

package ironblossom.csemock.experimental;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;

import ironblossom.csemock.R;
import ironblossom.csemock.experimental.ex_fragments.ExFragMarketinfo;
import ironblossom.csemock.experimental.ex_fragments.ExFragOverview;
import ironblossom.csemock.experimental.ex_fragments.ExFragPortfolio;
import ironblossom.csemock.experimental.ex_fragments.ExFragWatchlist;
import ironblossom.csemock.experimental.utils.SlidingTabLayout;
import ironblossom.csemock.experimental.utils.TitleStyler;

public class ExMainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {
    public static final String shareNames[] = {"ABBANK", "ACI", "BANKASIA", "BEXIMCO", "BRACBANK", "CITYBANK", "EBL", "IFIC", "EXIMBANK", "FIRSTBANK"};
    public static final String companyNames[] = {"AB Bank Ltd", "ACI Limited", "Bank Asia Ltd", "Bangladesh Export Import Company", "Brac Bank Ltd", "The City Bank", "Eastern Bank Ltd", "IFIC Bank Ltd",
            "Export Import Bank Ltd", "First Security Bank Ltd"};

    ActionBar actionBar;
    SlidingTabLayout slidingTabLayout;
    ViewPager viewPager;
    //utils
    public static float densityFactor;
    TitleStyler styler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ex_act_main);

        initBaseComponents();
    }

    private void initBaseComponents() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        densityFactor = metrics.density;

        styler = new TitleStyler(this);

        actionBar = getSupportActionBar();
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.slidingTab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        // actionBar.setTitle(Html.fromHtml(" Market<b>Trak</b><font size='24px'> | </font><font color='#548bd4'>" + "Overview" + "</font>"));
        actionBar.setTitle(styler.getStyled("Overview"));
        actionBar.setLogo(R.drawable.ic_launcher);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        slidingTabLayout.setViewPager(viewPager);
        //NOTE: Listener should be on tabLayout not on viewpager while using Tab/TabHost/SlidingTabStrip
        slidingTabLayout.setOnPageChangeListener(this);
        slidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ex_menu_main, menu);
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Nothing TODO
    }

    @Override
    public void onPageSelected(int position) {
        String subTitle = null;
        switch (position) {
            case 0:
                subTitle = "Overview";
                break;
            case 1:
                subTitle = "Market";
                break;
            case 2:
                subTitle = "Watch List";
                break;
            case 3:
                subTitle = "Portfolio";
                break;
        }
        actionBar.setTitle(Html.fromHtml(" Market<b>Trak</b><font color='#548bd4'> |" + subTitle + "</font>"));

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //Nothing TODO
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new ExFragOverview();
                    break;
                case 1:
                    fragment = new ExFragMarketinfo();
                    break;
                case 2:
                    fragment = new ExFragWatchlist();
                    break;
                case 3:
                    fragment = new ExFragPortfolio();
                    break;
            }
            return fragment;
        }
    }
}

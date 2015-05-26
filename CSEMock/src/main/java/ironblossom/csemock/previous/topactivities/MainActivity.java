package ironblossom.csemock.previous.topactivities;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import ironblossom.csemock.R;
import ironblossom.csemock.previous.utils.fragments.FragMarketInfo;
import ironblossom.csemock.previous.utils.fragments.FragOverview;
import ironblossom.csemock.previous.utils.fragments.FragPortfolio;
import ironblossom.csemock.previous.utils.fragments.FragWatchlist;


public class MainActivity extends ActionBarActivity {
    ActionBar actionBar;
    FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        initBaseComponent();


    }

    private void initBaseComponent() {
        actionBar = getSupportActionBar();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        final int[] tabIcons = new int[]{R.drawable.ic_overview,
                R.drawable.ic_market_info,
                R.drawable.ic_watchlist,
                R.drawable.ic_portfolio};
        final Class[] tabRealContent = new Class[]{FragOverview.class, FragMarketInfo.class, FragWatchlist.class, FragPortfolio.class};
        final LayoutInflater inflater = getLayoutInflater();
        for (int tabNo = 0; tabNo < tabIcons.length; tabNo++) {
            mTabHost.addTab(mTabHost.newTabSpec("Tab" + tabNo).setIndicator(null, getResources().getDrawable(tabIcons[tabNo])), tabRealContent[tabNo], null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Check item selected

        return true;
    }
}

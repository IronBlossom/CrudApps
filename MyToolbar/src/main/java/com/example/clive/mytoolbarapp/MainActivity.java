package com.example.clive.mytoolbarapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

/*created using Android Studio (Beta)0.8.14
www.101apps.co.za*/

public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        standalone toolbar
        toolbar = (Toolbar) findViewById(R.id.myToolbar);
        toolbar.setLogo(R.drawable.mushroom);
        toolbar.setLogoDescription(getResources().getString(R.string.bottom_toolbar_logo));
        toolbar.setSubtitle("Little Mushrooms");
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.blue));
        toolbar.setTitle("Big Mushrooms");
        toolbar.setTitleTextColor(getResources().getColor(R.color.red));
        toolbar.setNavigationIcon(R.drawable.ghost);
        toolbar.setNavigationContentDescription(getResources()
                .getString(R.string.navigation_icon_two_description));
        toolbar.inflateMenu(R.menu.menu_main);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Navigation Icon pressed",
                        Toast.LENGTH_SHORT).show();
                toolbar.setVisibility(View.GONE);
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(MainActivity.this, "WiFi Action item selected",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

            //        action bar toolbar
        Toolbar actionBarToolBar = (Toolbar) findViewById(R.id.my_action_bar_toolbar);
            //        important that this is set first
        setSupportActionBar(actionBarToolBar);
        actionBarToolBar.setNavigationIcon(R.drawable.back_arrow);
        actionBarToolBar.setNavigationContentDescription(getResources()
                .getString(R.string.navigation_icon_description));
        actionBarToolBar.setLogo(R.drawable.elephant);
        actionBarToolBar.setLogoDescription(getResources()
                .getString(R.string.logo_description));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.itemOne) {
            Toast.makeText(MainActivity.this, "Menu item One selected", Toast.LENGTH_SHORT).show();
            toolbar.setVisibility(View.VISIBLE);
            Animation fadeInAnimation = AnimationUtils.
                    loadAnimation(this, R.anim.fade_in);
            toolbar.startAnimation(fadeInAnimation);
        } else if (item.getItemId()==R.id.itemTwo) {
            Toast.makeText(MainActivity.this, "Menu item Two selected", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}

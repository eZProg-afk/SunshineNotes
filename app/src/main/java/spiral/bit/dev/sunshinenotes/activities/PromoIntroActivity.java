package spiral.bit.dev.sunshinenotes.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.adapters.IntroViewPagerAdapter;
import spiral.bit.dev.sunshinenotes.models.ScreenItem;

public class PromoIntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    private IntroViewPagerAdapter adapter;
    private TabLayout tabIndicator;
    private Button btnNext;
    private int position = 0;
    private Button getStarted;
    private Animation btnAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_promo_intro);

        if (restorePrefsData()) {
            Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(toMain);
            finish();
        }

        tabIndicator = findViewById(R.id.tab_indicator);
        btnNext = findViewById(R.id.btn_next);
        getStarted = findViewById(R.id.btn_get_started);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.get_started_anim);

        final List<ScreenItem> screenItems = new ArrayList<>();
        screenItems.add(new ScreenItem(getString(R.string.promo_title_1), getString(R.string.promo_description_1), R.drawable.search));
        screenItems.add(new ScreenItem(getString(R.string.promo_title_2), getString(R.string.promo_description_2), R.drawable.simple_create));
        screenItems.add(new ScreenItem(getString(R.string.promo_title_3), getString(R.string.promo_description_3), R.drawable.bottom_menu));

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = screenPager.getCurrentItem();
                if (position < screenItems.size()) {
                    position++;
                    screenPager.setCurrentItem(position);
                }
                if (position == screenItems.size() -1) {
                    loadLastScreen();
                }
            }
        });

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PromoIntroActivity.this, MainActivity.class);
                startActivity(intent);
                savePrefsData();
                finish();
            }
        });

        tabIndicator.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == screenItems.size() -1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        screenPager = findViewById(R.id.screenPager);
        adapter = new IntroViewPagerAdapter(this, screenItems);
        screenPager.setAdapter(adapter);
        tabIndicator.setupWithViewPager(screenPager);
    }

    private boolean restorePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", 0);
        boolean isIntroShowed = pref.getBoolean("isShowed", false);
        return isIntroShowed;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isShowed", true);
        editor.apply();
    }

    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        getStarted.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        getStarted.setAnimation(btnAnim);
    }
}
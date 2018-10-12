package com.bharathksunil.androidwebserver;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bharathksunil.androidwebserver.fragments.WebServerFragment;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements WebServerFragment
        .OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    //region View Objects
    private static final int UI_FAB_POWER_ON = 0;
    private static final int UI_FAB_POWER_OFF = 1;
    @BindViews({R.id.fab_main_power_on, R.id.fab_main_power_off})
    List<FloatingActionButton> mPowerButtonsList;
    //endregion

    WebServerFragment mWebServerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mWebServerFragment = WebServerFragment.newInstance("8080", true);
        mPowerButtonsList.get(UI_FAB_POWER_OFF).hide();
        mPowerButtonsList.get(UI_FAB_POWER_ON).hide();
        loadFragment(R.id.primary_frame,
                mWebServerFragment,
                WebServerFragment.class.getSimpleName(),
                false);
    }

    @OnClick(R.id.fab_main_power_on)
    public void onPowerOnButtonClicked() {
        Log.d(TAG, "onPowerOnButtonClicked");
        mPowerButtonsList.get(UI_FAB_POWER_ON).setEnabled(false);
        if (mWebServerFragment.isAdded())
            mWebServerFragment.powerOnWebServer();
        else
            Log.e(TAG, "onPowerOnButtonClicked: fragment Not in layout");
    }

    @OnClick(R.id.fab_main_power_off)
    public void onPowerOffButtonClicked() {
        Log.d(TAG, "onPowerOffButtonClicked");
        mPowerButtonsList.get(UI_FAB_POWER_OFF).setEnabled(false);
        if (mWebServerFragment.isAdded())
            mWebServerFragment.powerOffWebServer();
        else
            Log.d(TAG, "onPowerOffButtonClicked: fragment Not in layout");
    }

    @Override
    public void showPowerButton(boolean isPowerOn) {
        Log.d(TAG, "showPowerButton: isPowerOn: " + isPowerOn);
        if (isPowerOn) {
            mPowerButtonsList.get(UI_FAB_POWER_OFF).hide();
            mPowerButtonsList.get(UI_FAB_POWER_ON).show();
            mPowerButtonsList.get(UI_FAB_POWER_ON).setEnabled(true);
        } else {
            mPowerButtonsList.get(UI_FAB_POWER_ON).hide();
            mPowerButtonsList.get(UI_FAB_POWER_OFF).show();
            mPowerButtonsList.get(UI_FAB_POWER_OFF).setEnabled(true);
        }
    }

    private void loadFragment(@IdRes int frame,
                              @NonNull Fragment fragment,
                              @NonNull String tag,
                              boolean addToBackStack) {
        Log.d(TAG, "loadFragment: called");
        if (fragment.isAdded())
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(frame, fragment, tag);
        if (addToBackStack)
            transaction.addToBackStack(tag);
        transaction.commit();
    }
}

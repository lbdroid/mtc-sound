package com.sevenfloor.mtcsound.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;

import com.sevenfloor.mtcsound.R;

public class MainActivity extends Activity {
    private BroadcastReceiver broadcastReceiver;
    private AudioManager audioManager;
    private boolean i2cMode;

    View navigationButtonBalance, navigationButtonEqualizer, navigationButtonSettings;
    BaseFragment equalizerFragment, balanceFragment, settingsFragment;

    public MainActivity() {
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("com.microntek.inputchange") ||
                        action.equals("com.microntek.VOLUME_CHANGED") ||
                        action.equals("com.microntek.loundchange")) {
                    updateFragment(equalizerFragment);
                } else if (action.equals("com.microntek.balancechange")) {
                    updateFragment(balanceFragment);
                } else if (action.equals("com.microntek.ampclose")) {
                    finish();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String controlMode =audioManager.getParameters("av_control_mode=");
        i2cMode = controlMode.startsWith("i2c");

        navigationButtonEqualizer = findViewById(R.id.nav_button_equalizer);
        navigationButtonBalance = findViewById(R.id.nav_button_balance);
        navigationButtonSettings = findViewById(R.id.nav_button_settings);

        balanceFragment = new BalanceFragment();
        equalizerFragment = new EqualizerFragment();
        settingsFragment = new SettingsFragment();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.microntek.inputchange");
        intentFilter.addAction("com.microntek.VOLUME_CHANGED");
        intentFilter.addAction("com.microntek.eqchange");
        intentFilter.addAction("com.microntek.balancechange");
        intentFilter.addAction("com.microntek.loundchange");
        intentFilter.addAction("com.microntek.ampclose");
        registerReceiver(broadcastReceiver, intentFilter);

        navigationButtonEqualizer.setSelected(true);
        setActiveFragment(equalizerFragment);

        if(!i2cMode) {
            String reason = getString(controlMode.equals("") ? R.string.patch_info_reason_no_patch : R.string.patch_info_reason_no_i2c_control);

            DialogFragment dlg = PatchInfoDialogFragment.newInstance(reason);
            dlg.show(getFragmentManager(), "info");
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void navigationButtonClicked(View view) {
        navigationButtonEqualizer.setSelected(false);
        navigationButtonBalance.setSelected(false);
        navigationButtonSettings.setSelected(false);
        switch (view.getId())
        {
            case R.id.nav_button_equalizer:
                navigationButtonEqualizer.setSelected(true);
                setActiveFragment(equalizerFragment);
                break;
            case R.id.nav_button_balance:
                navigationButtonBalance.setSelected(true);
                setActiveFragment(balanceFragment);
                break;
            case R.id.nav_button_settings:
                navigationButtonSettings.setSelected(true);
                setActiveFragment(settingsFragment);
                break;
        }
    }

    private void updateFragment(BaseFragment fragment)
    {
        BaseFragment active = (BaseFragment)getFragmentManager().findFragmentByTag(fragment.getClass().getName());
        if (active != null && active.isVisible()) {
            fragment.update();
        }
    }

    private void setActiveFragment(BaseFragment fragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, fragment.getClass().getName());
        transaction.commit();
    }
}

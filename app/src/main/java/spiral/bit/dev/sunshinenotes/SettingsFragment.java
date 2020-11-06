package spiral.bit.dev.sunshinenotes;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import spiral.bit.dev.sunshinenotes.activities.MainActivity;
import spiral.bit.dev.sunshinenotes.activities.PinCodeActivity;

public class SettingsFragment extends PreferenceFragmentCompat implements
        BillingProcessor.IBillingHandler {
    boolean HIDE_RATE_MY_APP = false;
    String start;
    String menu;
    BillingProcessor bp;
    androidx.preference.Preference preferencepurchase;
    AlertDialog dialog;
    private static String PRODUCT_ID_BOUGHT = "item_1_bought";
    public static String SHOW_DIALOG = "show_dialog";
    private SharedPreferences preferenceSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceSettings = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        androidx.preference.Preference preferencerate = findPreference("rate");
        preferencerate.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(androidx.preference.Preference preference) {
                Uri uri = Uri.parse("market://details?id="
                        + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    if (!preferenceSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(getActivity(),
                                R.string.not_open_playstore,
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return true;
            }
        });

        final SwitchPreference preferenceTheme = (SwitchPreference) findPreference("theme");
        final SharedPreferences.Editor editorPrefs = preferenceSettings.edit();
        preferenceTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preferenceTheme.isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editorPrefs.putBoolean("dark", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editorPrefs.putBoolean("dark", true);
                }
                editorPrefs.apply();
                startActivity(new Intent(getContext(), MainActivity.class));
                return true;
            }
        });

        preferencepurchase = findPreference("purchase");
        String license = getResources().getString(R.string.google_play_license);
        if (license != null && !license.equals("")) {
            bp = new BillingProcessor(getActivity(),
                    license, this);
            bp.loadOwnedPurchasesFromGoogle();
            preferencepurchase
                    .setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            bp.purchase(getActivity(), PRODUCT_ID());
                            return true;
                        }
                    });
            if (getIsPurchased(getActivity())) {
                preferencepurchase.setIcon(R.drawable.ic_done);
            }
        } else {
            PreferenceScreen preferenceScreen = (androidx.preference.PreferenceScreen) findPreference("preferenceScreen");
            androidx.preference.PreferenceCategory billing = (PreferenceCategory) findPreference("billing");
            preferenceScreen.removePreference(billing);
        }
        if (HIDE_RATE_MY_APP) {
            androidx.preference.PreferenceCategory other = (androidx.preference.PreferenceCategory) findPreference("other");
            androidx.preference.Preference preference = findPreference("rate");
            other.removePreference(preference);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if (productId.equals(PRODUCT_ID())) {
            setIsPurchased(true, getActivity());
            preferencepurchase.setIcon(R.drawable.ic_done);
            if (!preferenceSettings.getBoolean("remove_toasts", true)) {
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_success), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (!preferenceSettings.getBoolean("remove_toasts", true)) {
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_fail), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (bp.isPurchased(PRODUCT_ID())) {
            setIsPurchased(true, getActivity());
            preferencepurchase.setIcon(R.drawable.ic_done);
            if (dialog != null) dialog.cancel();
            if (!preferenceSettings.getBoolean("remove_toasts", true)) {
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.settings_restore_purchase_success), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setIsPurchased(boolean purchased, Context c) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PRODUCT_ID_BOUGHT, purchased);
        editor.apply();
    }

    public static boolean getIsPurchased(Context c) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(c);
        boolean prefson = prefs.getBoolean(PRODUCT_ID_BOUGHT, false);
        return prefson;
    }

    private String PRODUCT_ID() {
        return getResources().getString(R.string.product_id);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        bp.handleActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }
}


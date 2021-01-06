package spiral.bit.dev.sunshinenotes.fragments.other;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.shashank.sony.fancytoastlib.FancyToast;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import static spiral.bit.dev.sunshinenotes.other.Utils.SWITCH_THEME_CODE;

public class SettingsFragment extends PreferenceFragmentCompat implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;
    boolean HIDE_RATE_MY_APP = false;
    private Preference preferencepurchase;
    private static final String PRODUCT_ID_BOUGHT = "item_1_bought";
    private SharedPreferences preferenceSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceSettings = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        androidx.preference.Preference preferenceRate = findPreference("rate");
        bp = new BillingProcessor(getContext(), getString(R.string.google_play_license), this);
        bp.initialize();

        preferenceRate.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(androidx.preference.Preference preference) {
                Uri uri = Uri.parse("market://details?id="
                        + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                            getContext(),
                            getString(R.string.not_open_playstore),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                    return true;
                }
                return true;
            }
        });

//        final SwitchPreference preferenceTheme = findPreference("theme");
//        final SharedPreferences.Editor editorPrefs = preferenceSettings.edit();
//        preferenceTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (preferenceTheme.isChecked()) {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    editorPrefs.putBoolean("dark", false);
//                } else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    editorPrefs.putBoolean("dark", true);
//                }
//                editorPrefs.apply();
//                startActivityForResult(new Intent(getContext(), BaseActivity.class), SWITCH_THEME_CODE);
//                return true;
//            }
//        });

//        final CheckBoxPreference preferenceBackUp = (CheckBoxPreference) findPreference("reserv_copy");
//        preferenceBackUp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                // startActivity(new Intent(getContext(), BackupActivity.class));
//                return true;
//            }
//        });

//        preferencepurchase = findPreference("purchase");
//        if (getIsPurchased(getContext())) preferencepurchase.setIcon(R.drawable.ic_done);
//        preferencepurchase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                bp.purchase(getActivity(), getString(R.string.product_id));
//                if (getIsPurchased(getContext())) {
//                    preferencepurchase.setIcon(R.drawable.ic_done);
//                }
//                if (HIDE_RATE_MY_APP) {
//                    androidx.preference.PreferenceCategory other = (androidx.preference.PreferenceCategory) findPreference("other");
//                    androidx.preference.Preference prefRate = findPreference("rate");
//                    other.removePreference(prefRate);
//                }
//                return true;
//            }
//        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.app_preferences);
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
        return prefs.getBoolean(PRODUCT_ID_BOUGHT, false);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        FancyToast.makeText(
                getContext(),
                getString(R.string.settings_purchase_success),
                FancyToast.LENGTH_LONG,
                FancyToast.SUCCESS,
                false).show();
        setIsPurchased(true, getContext());
        preferencepurchase.setIcon(R.drawable.ic_done);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        FancyToast.makeText(
                getContext(),
                "История покупок восстановлена! \n Перезагрузите приложение :)",
                FancyToast.LENGTH_LONG,
                FancyToast.SUCCESS,
                false).show();
        setIsPurchased(true, getContext());
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        FancyToast.makeText(
                getContext(),
                "При покупке произошла ошибка :(! \n Попробуйте ещё раз.",
                FancyToast.LENGTH_LONG,
                FancyToast.ERROR,
                false).show();
    }

    @Override
    public void onBillingInitialized() {
        bp.loadOwnedPurchasesFromGoogle();
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}


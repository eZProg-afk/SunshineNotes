package spiral.bit.dev.sunshinenotes.activities.other

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar
import spiral.bit.dev.sunshinenotes.R
import spiral.bit.dev.sunshinenotes.fragments.*
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment
import spiral.bit.dev.sunshinenotes.fragments.trash.TrashCheckListsFragment
import spiral.bit.dev.sunshinenotes.fragments.trash.TrashFoldersFragment
import spiral.bit.dev.sunshinenotes.fragments.trash.TrashNotesFragment

class TrashActivity : AppCompatActivity() {

    lateinit var fragment: Fragment
    private var preferenceSettings: SharedPreferences? = null
    private var bottomBar: SmoothBottomBar? = null
    private var layoutQuickActions: LinearLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)
        setUpQuickActions(this)
    }

    @SuppressLint("CommitPrefEdits")
    fun setUpQuickActions(context: Context?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (intent.hasExtra("fromFolderActivity")) {
            if (intent.getBooleanExtra("fromFolderActivity", false)) supportFragmentManager.beginTransaction()
                    .replace(R.id.replaced_container, FoldersFragment())
                    .commit()
        }
        val preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
//        if (preferenceSettings.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
        bottomBar = findViewById(R.id.bottomBar)
        layoutQuickActions = findViewById(R.id.layout_quick_actions)
        val billingImage = findViewById<ImageView>(R.id.icon_remove_ads)
        if (SettingsFragment.getIsPurchased(this@TrashActivity)) billingImage.visibility = View.GONE
        val backImg = findViewById<ImageView>(R.id.arrow_back)
        backImg.setOnClickListener { onBackPressed() }
        billingImage.setOnClickListener { startActivity(Intent(context, SettingsActivity::class.java)) }
        bottomBar?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelect(i: Int): Boolean {
                when (i) {
                    0 -> {
                        val trashNotesFragment = TrashNotesFragment()
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.replaced_trash, trashNotesFragment)
                                .commit()
                    }
                    1 -> {
                        val trashCheckListFragment = TrashCheckListsFragment()
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.replaced_trash, trashCheckListFragment)
                                .commit()
                    }
                    2 -> {
                        val trashFoldersFragment = TrashFoldersFragment()
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.replaced_trash, trashFoldersFragment)
                                .commit()
                    }
                }
                return true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragment = supportFragmentManager.findFragmentById(R.id.replaced_trash)!!
        fragment.onActivityResult(requestCode, resultCode, data)
    }

//    override fun onResume() {
//        super.onResume()
//        if (preferenceSettings!!.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
//    }

    override fun onBackPressed() {
        val intent = Intent(this@TrashActivity, BaseActivity::class.java)
        startActivity(intent)
    }
}
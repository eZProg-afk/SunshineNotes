package spiral.bit.dev.sunshinenotes.activities.other;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import java.util.ArrayList;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.adapter.BackAdapter;
import spiral.bit.dev.sunshinenotes.listeners.BackListener;
import spiral.bit.dev.sunshinenotes.models.other.BackgroundItem;

import static spiral.bit.dev.sunshinenotes.other.Utils.CHANGE_BACK_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.CODE_SELECT_IMG;
import static spiral.bit.dev.sunshinenotes.other.Utils.PERMISSION_STORAGE_CODE;

public class ChangeBackActivity extends AppCompatActivity implements BackListener {

    private BackAdapter adapter;
    private RecyclerView recyclerBackItems;
    private ArrayList<BackgroundItem> backgroundItems;
    private ImageView takePictureImg;
    private String from = "";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_back);
        if (getIntent().hasExtra("from")) {
            switch (getIntent().getStringExtra("from")) {
                case "notes":
                    from = "notes";
                    break;
                case "checklists":
                    from = "checklists";
                    break;
                case "folders":
                    from = "folders";
                    break;
            }
        }
        backgroundItems = new ArrayList<>();
        backgroundItems.add(new BackgroundItem(1, R.drawable.wall_1));
        backgroundItems.add(new BackgroundItem(2, R.drawable.wall_2));
        backgroundItems.add(new BackgroundItem(3, R.drawable.wall_3));
        backgroundItems.add(new BackgroundItem(4, R.drawable.wall_4));
        backgroundItems.add(new BackgroundItem(5, R.drawable.wall_5));
        backgroundItems.add(new BackgroundItem(7, R.drawable.wall_7));
        backgroundItems.add(new BackgroundItem(8, R.drawable.wall_8));
        backgroundItems.add(new BackgroundItem(9, R.drawable.wall_9));
        backgroundItems.add(new BackgroundItem(10, R.drawable.wall_10));
        recyclerBackItems = findViewById(R.id.background_recycler);
        recyclerBackItems.setHasFixedSize(true);
        adapter = new BackAdapter(backgroundItems, this);
        recyclerBackItems.setAdapter(adapter);
        recyclerBackItems.setLayoutManager(
                new StaggeredGridLayoutManager(3,
                        StaggeredGridLayoutManager.VERTICAL));
        takePictureImg = findViewById(R.id.btn_take_picture);
        takePictureImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChangeBackActivity.this, new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_STORAGE_CODE);
                } else {
                    selectImg();
                }
            }
        });
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                SharedPreferences preferenceSettings =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editorSettings = preferenceSettings.edit();
                if (fromUser) {
                    if (from.equals("notes")) {
                        editorSettings.putInt("color", envelope.getColor());
                        editorSettings.remove("back");
                        editorSettings.remove("picture");
                        editorSettings.apply();
                    } else if (from.equals("checklists")) {
                        editorSettings.putInt("color_check", envelope.getColor());
                        editorSettings.remove("back_check");
                        editorSettings.remove("picture_check");
                        editorSettings.apply();
                    } else if (from.equals("folders")) {
                        editorSettings.putInt("color_folder", envelope.getColor());
                        editorSettings.remove("back_folder");
                        editorSettings.remove("picture_folder");
                        editorSettings.apply();
                    }
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void selectImg() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CODE_SELECT_IMG);
        }
    }

    public String getPathFromUri(Uri uri) {
        String pathToFile;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            pathToFile = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            pathToFile = cursor.getString(index);
            cursor.close();
        }
        return pathToFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_SELECT_IMG && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null) {
                    try {
                        String selectedImgPath = getPathFromUri(selectedImgUri);
                        SharedPreferences preferenceSettings =
                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editorSettings = preferenceSettings.edit();
                        if (from.equals("notes")) {
                            editorSettings.putString("picture", selectedImgPath);
                            editorSettings.remove("color");
                            editorSettings.remove("back");
                            editorSettings.apply();
                        } else if (from.equals("checklists")) {
                            editorSettings.putString("picture_check", selectedImgPath);
                            editorSettings.remove("color_check");
                            editorSettings.remove("back_check");
                            editorSettings.apply();
                        } else if (from.equals("folders")) {
                            editorSettings.putString("picture_folder", selectedImgPath);
                            editorSettings.remove("color_folder");
                            editorSettings.remove("back_folder");
                            editorSettings.apply();
                        }
                    } catch (Exception e) {
                        if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext()
                        ).getBoolean("remove_toasts", false)) FancyToast.makeText(
                                ChangeBackActivity.this,
                                getString(R.string.error_add_img_toast),
                                FancyToast.LENGTH_LONG,
                                FancyToast.ERROR,
                                false).show();
                    }
                }
            }
            startActivityForResult(new Intent(ChangeBackActivity.this, BaseActivity.class),
                    CHANGE_BACK_CODE);
        }
    }

    @Override
    public void onNoteClicked(BackgroundItem backgroundItem, int position) {
        SharedPreferences preferenceSettings =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editorSettings = preferenceSettings.edit();
        switch (from) {
            case "notes":
                editorSettings.putInt("back", backgroundItem.getImageId());
                editorSettings.remove("color");
                editorSettings.remove("picture");
                editorSettings.apply();
                break;
            case "checklists":
                editorSettings.putInt("back_check", backgroundItem.getImageId());
                editorSettings.remove("color_check");
                editorSettings.remove("picture_check");
                editorSettings.apply();
                break;
            case "folders":
                editorSettings.putInt("back_folder", backgroundItem.getImageId());
                editorSettings.remove("color_folder");
                editorSettings.remove("picture_folder");
                editorSettings.apply();
                break;
        }
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLongNoteClicked(BackgroundItem backgroundItem, int position) {

    }


}
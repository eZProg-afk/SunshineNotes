package spiral.bit.dev.sunshinenotes.other;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utils {

    public static final int ADD_NOTE_CODE = 12;
    public static final int UPDATE_NOTE_CODE = 13;
    public static final int ADD_CHECK_LIST_CODE = 14;
    public static final int UPDATE_CHECK_LIST_CODE = 15;
    public static final int ADD_FOLDER_CODE = 16;
    public static final int UPDATE_FOLDER_CODE = 17;
    public static final int SHOW_CODE = 18;
    public static final int DELETE_NOTES_CODE = 19;
    public static final int SWITCH_THEME_CODE = 20;
    public static final int CHANGE_BACK_CODE = 21;
    public static final int ADD_DRAW_CODE = 22;
    public static final int REQUEST_CODE_SPEECH = 125;
    public static final int PERMISSION_STORAGE_CODE = 126;
    public static final int PERMISSION_RECORD_CODE = 127;
    public static final int CODE_SELECT_IMG = 128;

    public static Bitmap returnBitmap(Bitmap originalImage, int width, int height) {
        Bitmap background = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();
        Canvas canvas = new Canvas(background);
        float scale = width / originalWidth;
        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalImage, transformation, paint);
        return background;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

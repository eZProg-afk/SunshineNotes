package spiral.bit.dev.sunshinenotes.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import spiral.bit.dev.sunshinenotes.data.trash.TrashCheckListDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;

public class AdWorker extends Worker {
    public AdWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferenceSettings.edit();
        editor.remove("time_block_ads");
        editor.apply();
        return Result.success();
    }
}

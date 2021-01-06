package spiral.bit.dev.sunshinenotes.other;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import spiral.bit.dev.sunshinenotes.data.trash.TrashCheckListDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;

public class RoomWorker extends Worker {
    public RoomWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        TrashDatabase.getNoteDatabase(getApplicationContext())
                .getTrashDAO().autoClearTrash();
        TrashCheckListDatabase.getNoteDatabase(getApplicationContext())
                .getTrashCheckListDAO().autoClearTaskTrash();
        TrashCheckListDatabase.getNoteDatabase(getApplicationContext())
                .getTrashCheckListDAO().autoClearCheckListTrash();
        TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                .getNoteDAO().autoClearNotesTrash();
        TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                .getNoteDAO().autoClearFoldersTrash();
        return Result.success();
    }
}

package spiral.bit.dev.sunshinenotes.adapter.trash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.List;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.data.CheckListDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashCheckListDatabase;
import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.models.trash.TrashTask;

public class TrashTaskAdapter extends RecyclerView.Adapter<TrashTaskAdapter.CheckListViewHolder> {

    private final List<TrashTask> tasks;
    private Context context;

    public TrashTaskAdapter(List<TrashTask> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public CheckListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.check_item, viewGroup, false);
        context = viewGroup.getContext();
        return new CheckListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckListViewHolder holder, final int position) {
        final TrashTask task = tasks.get(position);
        holder.setNote(task);
        holder.checkBox.setChecked(tasks.get(position).isCompleted());
        holder.checkBox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (checkBox.isChecked()) {
                    task.setCompleted(true);
                    @SuppressLint("StaticFieldLeak")
                    class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
                        @Override
                        protected Void doInBackground(Void... voids) {
                           // TrashCheckListDatabase.getNoteDatabase(context)
                             //       .getTrashCheckListDAO().updateTask(task);
                            return null;
                        }
                    }
                    new SaveNoteAsyncTask().execute();
                } else {
                    task.setCompleted(false);
                    @SuppressLint("StaticFieldLeak")
                    class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
                        @Override
                        protected Void doInBackground(Void... voids) {
                          //  TrashCheckListDatabase.getNoteDatabase(context)
                            //        .getTrashCheckListDAO().updateTask(task);
                            return null;
                        }
                    }
                    new SaveNoteAsyncTask().execute();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class CheckListViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textDateTime;
        ConstraintLayout layoutNote;
        CustomCheckBox checkBox;

        public CheckListViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.check_title);
            textDateTime = itemView.findViewById(R.id.check_date_time);
            layoutNote = itemView.findViewById(R.id.layout_check);
            checkBox = itemView.findViewById(R.id.check_box);
        }

        void setNote(TrashTask task) {
            textTitle.setText(task.getTitle());
            textDateTime.setText(task.getDateTime());
            checkBox.setChecked(task.isCompleted());
        }
    }
}

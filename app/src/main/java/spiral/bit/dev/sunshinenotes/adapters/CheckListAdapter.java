package spiral.bit.dev.sunshinenotes.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.CheckListener;
import spiral.bit.dev.sunshinenotes.models.CheckItem;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.FolderViewHolder> {

    private List<CheckItem> checkItems;
    private CheckListener listener;
    private Timer timer;
    private List<CheckItem> checksSource;
    private Context context;

    public CheckListAdapter(List<CheckItem> checkItems, CheckListener listener) {
        this.checkItems = checkItems;
        this.listener = listener;
        checksSource = checkItems;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.check_item, viewGroup, false);
        context = viewGroup.getContext();
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, final int position) {
        holder.setNote(checkItems.get(position));
        holder.layoutFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCheckClicked(checkItems.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle;
        LinearLayout layoutFolder;
        RoundedImageView imageNote;
        private Context context;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textTitle = itemView.findViewById(R.id.text_folder_title);
            layoutFolder = itemView.findViewById(R.id.layout_folder);
            imageNote = itemView.findViewById(R.id.image_folder);
        }

        void setNote(CheckItem checkItem) {
            textTitle.setText(checkItem.getCheckName());
        }
    }

    public void searchFolder(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    checkItems = checksSource;
                } else {
                    ArrayList<CheckItem> tempList = new ArrayList<>();
                    for (CheckItem checkItem : checksSource) {
                        if (checkItem.getCheckName().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                            tempList.add(checkItem);
                        }
                    }
                    checkItems = tempList;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}

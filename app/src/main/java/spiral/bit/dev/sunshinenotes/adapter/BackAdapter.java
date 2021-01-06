package spiral.bit.dev.sunshinenotes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;
import java.util.Timer;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.BackListener;
import spiral.bit.dev.sunshinenotes.models.other.BackgroundItem;

public class BackAdapter extends RecyclerView.Adapter<BackAdapter.BackViewHolder> {

    private List<BackgroundItem> backgroundItems;
    private final BackListener listener;
    private Timer timer;
    private boolean isDeleteModeEnabled = false;

        public BackAdapter(List<BackgroundItem> backgroundItems, BackListener listener) {
        this.backgroundItems = backgroundItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.back_item, viewGroup, false);
        return new BackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BackViewHolder holder, final int position) {
        holder.setNote(backgroundItems.get(position));
        holder.layoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNoteClicked(backgroundItems.get(position), position);
            }
        });
        holder.layoutBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongNoteClicked(backgroundItems.get(position), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return backgroundItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class BackViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutBack;
        RoundedImageView imageBack;
        private final Context context;

        public BackViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            imageBack = itemView.findViewById(R.id.image_one_back);
            layoutBack = itemView.findViewById(R.id.layout_back);
        }

        void setNote(BackgroundItem backgroundItem) {
            imageBack.setImageResource(backgroundItem.getImageId());
        }
    }
}

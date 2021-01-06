package spiral.bit.dev.sunshinenotes.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.skydoves.doublelift.DoubleLiftLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.StatisticListener;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder> {

    private List<Statistic> statistics;
    private final StatisticListener listener;
    private Timer timer;
    private final List<Statistic> statisticsSource;

    public StatisticAdapter(List<Statistic> noteInFolders, StatisticListener listener) {
        this.statistics = noteInFolders;
        this.listener = listener;
        statisticsSource = noteInFolders;
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.statistic_item, viewGroup, false);
        return new StatisticViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StatisticViewHolder holder, final int position) {
        holder.setStatistics(statistics.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStatisticsClicked(statistics.get(position), position);
            }
        });
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onLongStatisticsClicked(statistics.get(position), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return statistics.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class StatisticViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubTitle, textDateTime, textAction, textType;
        LinearLayout layoutNote;
        RoundedImageView imageNote, imageDraw;
        CardView cardView1;
        final DoubleLiftLayout doubleLiftLayout4, doubleLiftLayout5, doubleLiftLayout6;

        public StatisticViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.item_text);
            textSubTitle = itemView.findViewById(R.id.item_sub_text);
            textDateTime = itemView.findViewById(R.id.date_text_view);
            layoutNote = itemView.findViewById(R.id.statistics_layout);
            textAction = itemView.findViewById(R.id.action_text_view);
            textType = itemView.findViewById(R.id.type_text_view);
            cardView1 = itemView.findViewById(R.id.cardView1);
            doubleLiftLayout4 = itemView.findViewById(R.id.doubleLiftLayout4);
            doubleLiftLayout5 = itemView.findViewById(R.id.doubleLiftLayout5);
            doubleLiftLayout6 = itemView.findViewById(R.id.doubleLiftLayout6);

            cardView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!doubleLiftLayout4.isExpanded()) {
                        doubleLiftLayout4.expand();
                        doubleLiftLayout5.expand();
                        doubleLiftLayout6.expand();
                    } else {
                        doubleLiftLayout4.collapse();
                        doubleLiftLayout5.collapse();
                        doubleLiftLayout6.collapse();
                    }
                }
            });

//            imageNote = itemView.findViewById(R.id.image_note);
//            imageDraw = itemView.findViewById(R.id.image_draw_note);
        }

        void setStatistics(final Statistic statistics) {
            textTitle.setText(statistics.getItemText());
            //if (statistics.getItemSubText().trim().isEmpty()) textSubTitle.setVisibility(View.GONE);
            textSubTitle.setText(statistics.getItemSubText());
            textDateTime.setText(statistics.getDateText());
            //GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            textAction.setText(statistics.getActionText());
            textType.setText(statistics.getTypeText());

//            if (simpleNote.getImagePath() != null && !simpleNote.getImagePath().isEmpty()) {
//                imageNote.setImageBitmap(BitmapFactory.decodeFile(simpleNote.getImagePath()));
//                imageNote.setVisibility(View.VISIBLE);
//                imageDraw.setVisibility(View.GONE);
//            } else {
//                imageNote.setVisibility(View.GONE);
//                if (simpleNote.getDrawPath() != null && !simpleNote.getDrawPath().isEmpty()) {
//                    InputStream is = null;
//                    try {
//                        is = context.getContentResolver().openInputStream(Uri.parse(simpleNote.getDrawPath()));
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    imageDraw.setImageBitmap(BitmapFactory.decodeStream(is));
//                    imageDraw.setVisibility(View.VISIBLE);
//                } else {
//                    imageDraw.setVisibility(View.GONE);
//                }
            // }
        }
    }

    public void searchStatistics(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    statistics = statisticsSource;
                } else {
                    ArrayList<Statistic> tempList = new ArrayList<>();
                    for (Statistic statistic : statisticsSource) {
                        if (statistic.getItemText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                statistic.getItemSubText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                statistic.getActionText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                statistic.getDateText().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                            tempList.add(statistic);
                        }
                    }
                    statistics = tempList;
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

package spiral.bit.dev.sunshinenotes.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.makeramen.roundedimageview.RoundedImageView;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.NotesListener;
import spiral.bit.dev.sunshinenotes.models.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener listener;
    private Timer timer;
    private List<Note> notesSource;
    private Context context;

    public NoteAdapter(List<Note> notes, NotesListener listener) {
        this.notes = notes;
        this.listener = listener;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item, viewGroup, false);
        context = viewGroup.getContext();
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNoteClicked(notes.get(position), position);
            }
        });
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongNoteClicked(notes.get(position), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubTitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        RoundedImageView imageDraw;
        private Context context;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textTitle = itemView.findViewById(R.id.text_title);
            textSubTitle = itemView.findViewById(R.id.text_sub_title);
            textDateTime = itemView.findViewById(R.id.text_date_time);
            layoutNote = itemView.findViewById(R.id.layout_note);
            imageNote = itemView.findViewById(R.id.image_note);
            imageDraw = itemView.findViewById(R.id.image_draw_note);
        }

        void setNote(Note note) {
            textTitle.setText(note.getTitle());
            String type = note.getFontStyle();
            String typeTextSize = note.getTextSize();
            String typeTextColor = note.getNoteColor();
            if (note.getSubTitle().trim().isEmpty()) {
                textSubTitle.setVisibility(View.GONE);
            } else {
                textSubTitle.setText(note.getSubTitle());
            }
            textDateTime.setText(note.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }


            if (type != null) {
                if (type.equals("def")) {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu_bold.ttf");
                    Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu_medium.ttf");
                    textTitle.setTypeface(typeface);
                    textSubTitle.setTypeface(typeface2);
                } else if (type.equals("comissioner")) {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/comm_black.ttf");
                    Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/comm_medium.ttf");
                    textTitle.setTypeface(typeface);
                    textSubTitle.setTypeface(typeface2);
                } else if (type.equals("roboto")) {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/robotoslab_black.ttf");
                    Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/robotoslab_regular.ttf");
                    textTitle.setTypeface(typeface);
                    textSubTitle.setTypeface(typeface2);
                } else if (type.equals("sourcecode")) {
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecode_black.ttf");
                    Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/source_regular.ttf");
                    textTitle.setTypeface(typeface);
                    textSubTitle.setTypeface(typeface2);
                }
            }

            if (typeTextSize != null) {
                if (typeTextSize.equals("def")) {
                    textTitle.setTextSize(16);
                    textSubTitle.setTextSize(13);
                } else if (typeTextSize.equals("small")) {
                    textTitle.setTextSize(12);
                    textSubTitle.setTextSize(10);
                } else if (typeTextSize.equals("medium")) {
                    textTitle.setTextSize(18);
                    textSubTitle.setTextSize(16);
                } else if (typeTextSize.equals("big")) {
                    textTitle.setTextSize(22);
                    textSubTitle.setTextSize(17);
                }
            }

            if (typeTextColor != null) {
                if (typeTextColor.equals("def")) {
                    textTitle.setTextColor(Color.WHITE);
                    textSubTitle.setTextColor(Color.WHITE);
                } else if (typeTextColor.equals("yellow")) {
                    textTitle.setTextColor(Color.YELLOW);
                    textSubTitle.setTextColor(Color.YELLOW);
                } else if (typeTextColor.equals("green")) {
                    textTitle.setTextColor(Color.GREEN);
                    textSubTitle.setTextColor(Color.GREEN);
                } else if (typeTextColor.equals("red")) {
                    textTitle.setTextColor(Color.RED);
                    textSubTitle.setTextColor(Color.RED);
                }
            }

            if (note.getImgTag() != null && !note.getImgTag().isEmpty()) {
                imageNote.setTag(note.getImgTag());
            }

            if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                imageDraw.setVisibility(View.GONE);
            } else {
                imageNote.setVisibility(View.GONE);
                if (note.getDrawPath() != null && !note.getDrawPath().isEmpty()) {
                    InputStream is = null;
                    try {
                        is = context.getContentResolver().openInputStream(Uri.parse(note.getDrawPath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    imageDraw.setImageBitmap(BitmapFactory.decodeStream(is));
                    imageDraw.setVisibility(View.VISIBLE);
                } else {
                    imageDraw.setVisibility(View.GONE);
                }
            }
        }
    }

    public void searchNote(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> tempList = new ArrayList<>();
                    for (Note note : notesSource) {
                        Log.d("YEES", "SEARCH METHOD ?!");
                        if (note.getImgTag() != null && !note.getImgTag().isEmpty()) {
                            Log.d("YEES", "BLOCK IIIIIF");
                            if (note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    note.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    note.getImgTag().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(note);
                                Log.d("YEES", "BLOCK IIIIIF COMPLETE");
                            }
                        } else {
                            if (note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    note.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(note);
                                Log.d("YEES", "BLOCK else :(");
                            }
                        }
                    }
                    notes = tempList;
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

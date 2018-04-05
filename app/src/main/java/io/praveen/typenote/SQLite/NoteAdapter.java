package io.praveen.typenote.SQLite;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.praveen.typenote.R;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> implements Filterable {

    private List<Note> notes;
    private List<Note> fullNote;
    private List<Note> filtered;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
        fullNote = notes;
    }

    public int impPos(int position){
        int pos = 0;
        int j = -1;
        for (Note i : fullNote) {
            if (i.getStar() == 1) {
                j++;
                if (j == position) return pos;
            }
            pos++;
        }
        return pos;
    }

    @NonNull
    @Override
    public Filter getFilter() {

        return new Filter() {
            @NonNull
            @Override
            protected FilterResults performFiltering(@NonNull CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.length() == 0) {
                    notes = new ArrayList<>(fullNote);
                }  else {
                    ArrayList<Note> filteredList = new ArrayList<>();
                    for (Note i : fullNote) {
                        switch (charString) {
                            case "#ALL":
                                filteredList.addAll(fullNote);
                                break;
                            case "#IMP":
                                if (i.getStar() == 1) {
                                    filteredList.add(i);
                                }
                                break;
                            default:
                                if (i.getNote().toLowerCase().contains(charString)) {
                                    filteredList.add(i);
                                } else if (i.getNote().contains(charString)) {
                                    filteredList.add(i);
                                } else if (i.getTitle().toLowerCase().contains(charString)) {
                                    filteredList.add(i);
                                } else if (i.getTitle().contains(charString)) {
                                    filteredList.add(i);
                                }
                                break;
                        }
                    }
                    notes = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, @NonNull FilterResults filterResults) {
                filtered = (ArrayList<Note>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.setIsRecyclable(false);
        holder.text.setText(note.getNote());
        holder.date.setText(note.getDate());
        if (note.getTitle().length() == 0){
            holder.title.setText("Untitled Note");
        } else {
            holder.title.setText(note.getTitle());
        }
        if (note.getStar() == 1){
            holder.imp.setBackgroundColor(Color.parseColor("#0081E9"));
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView text, date, title;
        LinearLayout imp;

        MyViewHolder(@NonNull View view) {
            super(view);
            text = view.findViewById(R.id.text_note);
            date = view.findViewById(R.id.text_date);
            title = view.findViewById(R.id.text_title);
            imp = view.findViewById(R.id.important);
        }
    }
}

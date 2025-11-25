package com.voltcheck.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.voltcheck.app.R;
import com.voltcheck.app.models.SessionEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter untuk RecyclerView di HistoryActivity
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    
    private List<SessionEntity> sessions;
    private OnSessionActionListener listener;
    
    public interface OnSessionActionListener {
        void onSessionClick(SessionEntity session);
        void onRenameClick(SessionEntity session);
        void onDeleteClick(SessionEntity session);
    }
    
    public SessionAdapter(List<SessionEntity> sessions, OnSessionActionListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionEntity session = sessions.get(position);
        holder.bind(session, listener);
    }
    
    @Override
    public int getItemCount() {
        return sessions.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionName, tvTimestamp, tvStatistics;
        ImageButton btnRename, btnDelete;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSessionName = itemView.findViewById(R.id.tvSessionName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatistics = itemView.findViewById(R.id.tvStatistics);
            btnRename = itemView.findViewById(R.id.btnRename);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        void bind(SessionEntity session, OnSessionActionListener listener) {
            tvSessionName.setText(session.getSessionName());
            
            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(new Date(session.getTimestamp())));
            
            // Format statistics
            String stats = String.format(Locale.getDefault(),
                    "Avg: %.0f mA | Max: %.0f mA | Stability: %.1f%% | Drop: %.3f V",
                    session.getAvgCurrent(),
                    session.getMaxCurrent(),
                    session.getStability(),
                    session.getVoltageDrop());
            tvStatistics.setText(stats);
            
            // Click listeners
            itemView.setOnClickListener(v -> listener.onSessionClick(session));
            btnRename.setOnClickListener(v -> listener.onRenameClick(session));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(session));
        }
    }
}

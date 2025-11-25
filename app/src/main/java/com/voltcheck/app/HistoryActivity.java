package com.voltcheck.app;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.voltcheck.app.adapters.SessionAdapter;
import com.voltcheck.app.database.SessionDatabase;
import com.voltcheck.app.models.SessionEntity;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * HistoryActivity - Menampilkan riwayat sesi pengujian
 */
public class HistoryActivity extends AppCompatActivity implements SessionAdapter.OnSessionActionListener {
    
    private static final String TAG = "HistoryActivity";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private SessionAdapter adapter;
    private List<SessionEntity> sessions = new ArrayList<>();
    private Executor executor = Executors.newSingleThreadExecutor();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("History");
        }
        
        initializeViews();
        setupRecyclerView();
        loadSessions();
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        swipeRefresh.setOnRefreshListener(this::loadSessions);
    }
    
    private void setupRecyclerView() {
        adapter = new SessionAdapter(sessions, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadSessions() {
        swipeRefresh.setRefreshing(true);
        
        executor.execute(() -> {
            try {
                SessionDatabase db = SessionDatabase.getInstance(this);
                List<SessionEntity> loadedSessions = db.sessionDao().getAllSessions();
                
                runOnUiThread(() -> {
                    sessions.clear();
                    sessions.addAll(loadedSessions);
                    adapter.notifyDataSetChanged();
                    
                    tvEmptyState.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                    swipeRefresh.setRefreshing(false);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading sessions: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading sessions", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
            }
        });
    }
    
    @Override
    public void onSessionClick(SessionEntity session) {
        // Show session detail dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle(session.getSessionName())
            .setMessage(session.getConclusion())
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void onRenameClick(SessionEntity session) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename, null);
        TextInputEditText editText = dialogView.findViewById(R.id.editSessionName);
        editText.setText(session.getSessionName());
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Rename Session")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = editText.getText().toString().trim();
                if (!newName.isEmpty()) {
                    renameSession(session, newName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onDeleteClick(SessionEntity session) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Session")
            .setMessage("Are you sure you want to delete this session?")
            .setPositiveButton("Delete", (dialog, which) -> deleteSession(session))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void renameSession(SessionEntity session, String newName) {
        executor.execute(() -> {
            try {
                session.setSessionName(newName);
                SessionDatabase db = SessionDatabase.getInstance(this);
                db.sessionDao().update(session);
                
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Session renamed", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error renaming session: " + e.getMessage());
            }
        });
    }
    
    private void deleteSession(SessionEntity session) {
        executor.execute(() -> {
            try {
                SessionDatabase db = SessionDatabase.getInstance(this);
                db.sessionDao().delete(session);
                
                runOnUiThread(() -> {
                    sessions.remove(session);
                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(this, "Session deleted", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting session: " + e.getMessage());
            }
        });
    }
    
    private void exportAllToCSV() {
        executor.execute(() -> {
            try {
                File exportDir = new File(getExternalFilesDir(null), "VoltCheck");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String fileName = "voltcheck_export_" + sdf.format(new Date()) + ".csv";
                File file = new File(exportDir, fileName);
                
                FileWriter writer = new FileWriter(file);
                writer.append("Session Name,Timestamp,Avg Current (mA),Max Current (mA),Stability (%),Voltage Drop (V)\n");
                
                for (SessionEntity session : sessions) {
                    writer.append(session.getSessionName()).append(",");
                    writer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date(session.getTimestamp()))).append(",");
                    writer.append(String.valueOf(session.getAvgCurrent())).append(",");
                    writer.append(String.valueOf(session.getMaxCurrent())).append(",");
                    writer.append(String.valueOf(session.getStability())).append(",");
                    writer.append(String.valueOf(session.getVoltageDrop())).append("\n");
                }
                
                writer.close();
                
                runOnUiThread(() -> 
                    Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
                );
                
            } catch (Exception e) {
                Log.e(TAG, "Error exporting CSV: " + e.getMessage());
                runOnUiThread(() -> 
                    Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            exportAllToCSV();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

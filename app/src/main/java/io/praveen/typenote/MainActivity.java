package io.praveen.typenote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.praveen.typenote.SQLite.ClickListener;
import io.praveen.typenote.SQLite.DatabaseHandler;
import io.praveen.typenote.SQLite.Note;
import io.praveen.typenote.SQLite.NoteAdapter;
import io.praveen.typenote.SQLite.RecyclerTouchListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NoteAdapter mAdapter;
    private List<Note> l;
    private int imp = 0, ser = 0;
    private MenuItem mi;
    private InterstitialAd interstitialAd;
    private ScheduledExecutorService scheduler;
    private boolean isVisible;
    private boolean lock = false;

    private void prepareAd(){
        interstitialAd = new InterstitialAd(MainActivity.this);
        interstitialAd.setAdUnitId("ca-app-pub-8429477298745270/2004640333");
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onStop() {
        super.onStop();
        scheduler.shutdownNow();
        scheduler = null;
        isVisible = false;
    }

    @Override
    protected void onStart(){
        super.onStart();
        isVisible = true;
        prepareAd();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int premium = preferences.getInt("premium", 0);
        if(scheduler == null && !lock){
            scheduler = Executors.newSingleThreadScheduledExecutor();
            if (premium != 1){
                scheduler.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (interstitialAd.isLoaded() && isVisible){
                                    interstitialAd.show();
                                    lock = true;
                                }
                                else Log.d("AD"," Interstitial Not Loaded");
                                prepareAd();
                            }
                        });
                    }
                }, 6, 20, TimeUnit.SECONDS);
            }
        }
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                lock = false;
                super.onAdClosed();
            }
            @Override
            public void onAdClicked() {
                lock = false;
                super.onAdClicked();
            }
            @Override
            public void onAdFailedToLoad(int i) {
                lock = false;
                super.onAdFailedToLoad(i);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/whitney.ttf").setFontAttrId(R.attr.fontPath).build());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf");
        SpannableStringBuilder SS = new SpannableStringBuilder("Notes");
        SS.setSpan(new CustomTypefaceSpan("", font2), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(SS);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        CoordinatorLayout sv = findViewById(R.id.fabView);
        populateData();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(i);
                finish();
            }
        });
        boolean fromNew;
        boolean fromEdit;
        boolean fromDelete;
        boolean fromRestore;
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().containsKey("note")) {
            fromNew = getIntent().getExtras().getBoolean("new");
            fromEdit = getIntent().getExtras().getBoolean("edit");
            fromDelete = getIntent().getExtras().getBoolean("delete");
            fromRestore = getIntent().getExtras().getBoolean("restore");
            if (fromNew) Snackbar.make(sv, "Note added successfully!", Snackbar.LENGTH_SHORT).show();
            if (fromEdit) Snackbar.make(sv, "Note edited successfully!", Snackbar.LENGTH_SHORT).show();
            if (fromDelete) Snackbar.make(sv, "Note deleted successfully!", Snackbar.LENGTH_SHORT).show();
            if (fromRestore) Snackbar.make(sv, "Note restored successfully!", Snackbar.LENGTH_SHORT).show();
            InterstitialAd interstitialAd = new InterstitialAd(MainActivity.this);
            interstitialAd.setAdUnitId("ca-app-pub-6275597090094912/5536611682");
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean shortcut = preferences.getBoolean("shortcut", true);
        if (!shortcut) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int notificationId = 1;
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = "NOTES_ADD";
                String channelName = "Notes Shortcuts";
                @SuppressLint("WrongConstant") NotificationChannel mChannel = new NotificationChannel(channelId, channelName, 3);
                if (notificationManager != null) {
                    mChannel.setSound(null, null);
                    notificationManager.createNotificationChannel(mChannel);
                }
                Intent intent = new Intent(this, NoteActivity.class);
                intent.putExtra("IS_FROM_NOTIFICATION", true);
                @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, NotificationManager.IMPORTANCE_LOW);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                builder.setContentTitle("Tap to add a note");
                builder.setContentText("Note something productive today.");
                builder.setContentIntent(pendingIntent);
                builder.setTicker("Add Notes");
                builder.setChannelId(channelId);
                builder.setOngoing(true);
                builder.setColor(getResources().getColor(R.color.colorPrimary));
                builder.setAutoCancel(true);
                builder.setSmallIcon(R.drawable.notification_white);
                builder.setPriority(NotificationManager.IMPORTANCE_LOW);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addNextIntent(intent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
                if (notificationManager != null) notificationManager.notify(notificationId, builder.build());
            } else {
                Intent intent = new Intent(this, NoteActivity.class);
                intent.putExtra("IS_FROM_NOTIFICATION", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);
                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                builder.setContentTitle("Tap to add a note");
                builder.setContentText("Note something productive today.");
                builder.setContentIntent(pendingIntent);
                builder.setTicker("Add Notes");
                builder.setOngoing(true);
                builder.setAutoCancel(true);
                builder.setColor(getResources().getColor(R.color.colorPrimary));
                builder.setSmallIcon(R.drawable.notification_white);
                builder.setPriority(Notification.PRIORITY_MAX);
                Notification notification = builder.build();
                NotificationManager notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManger != null) notificationManger.notify(1, notification);
            }
        } else {
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null) nMgr.cancelAll();
        }
    }

    public void backup() {
        try {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyHHmmss", Locale.ENGLISH);
            String fileName = "BACKUP" + formatter.format(now) + ".txt";
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            String location = "Storage/Notes/"+fileName;
            boolean b = true;
            if (!root.exists()) b = root.mkdirs();
            if (!b) {
                Toast.makeText(MainActivity.this, "Backup Failed", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(root, fileName);
            FileWriter writer = new FileWriter(file);
            for(int i = 0; i < l.size(); i++) writer.append(l.get(i).getTitle()).append("\n\n").append(l.get(i).getNote()).append("\n").append(l.get(i).getDate()).append("\n__________\n\n");
            writer.flush();
            writer.close();
            Toast.makeText(MainActivity.this, "Backup Successful!\nFind your notes at\n"+location, Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {}
    }

    public String backupString() {
        String s = "Notes Backup Error";
        try {
            StringBuilder writer = new StringBuilder();
            for(int i = 0; i < l.size(); i++) writer.append(l.get(i).getTitle()).append("\n\n").append(l.get(i).getNote()).append("\n").append(l.get(i).getDate()).append("\n__________\n\n");
            s = writer.toString();
        } catch (Exception ignored) {}
        return s;
    }

    public void populateData() {
        final DatabaseHandler db = new DatabaseHandler(this);
        l = db.getAllNotes();
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final RelativeLayout rl = findViewById(R.id.placeholder);
        if (l.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            rl.setVisibility(View.VISIBLE);
        }
        mAdapter = new NoteAdapter(l);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), new ClickListener() {

            @Override
            public void onClick(View view, final int position) {
                if (ser == 0) {
                    int pos = position;
                    if (imp == 1) pos = mAdapter.impPos(position);
                    final Note note = l.get(pos);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", note.getNote());
                    if (clipboard != null) clipboard.setPrimaryClip(clip);
                    Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                    intent.putExtra("note", note.getNote());
                    intent.putExtra("id", note.getID());
                    intent.putExtra("imp", note.getStar());
                    intent.putExtra("date", note.getDate());
                    intent.putExtra("title", note.getTitle());
                    intent.putExtra("pos", pos);
                    startActivity(intent);
                    finish();
                }
            }
        }));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mi = menu.findItem(R.id.menu_imp);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        search(searchView);
        EditText et= searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        et.setHint(MainActivity.this.getString(R.string.search));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_imp){
            if (imp == 0){
                imp = 1;
                mAdapter.getFilter().filter("#IMP");
                item.setIcon(R.drawable.ic_bookmark_white_24dp);
            } else {
                imp = 0;
                mAdapter.getFilter().filter("#ALL");
                item.setIcon(R.drawable.ic_bookmark_border_white_24dp);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            finish();
        } else if (item.getItemId() == R.id.nav_notes) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (item.getItemId() == R.id.nav_about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
            finish();
        } else if (item.getItemId() == R.id.nav_premium) {
            Intent i = new Intent(MainActivity.this, ProActivity.class);
            startActivity(i);
            finish();
        } else if (item.getItemId() == R.id.nav_backup) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
            } else {
                backupStart();
            }
        } else if (item.getItemId() == R.id.nav_bin) {
            Intent i = new Intent(MainActivity.this, BinActivity.class);
            startActivity(i);
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) Toast.makeText(MainActivity.this, "Grant the required permissions to backup your notes.", Toast.LENGTH_LONG).show();
                else backupStart();
                break;
            default:
                break;
        }
    }

    public void backupStart(){
        if (!l.isEmpty()) {
            new MaterialStyledDialog.Builder(MainActivity.this).setIcon(R.drawable.ic_unarchive)
                    .setDescription("You can backup your notes via your phone memory or sending them by email!")
                    .setPositiveText("EMAIL")
                    .setHeaderColor(R.color.colorPrimary)
                    .setTitle("Where to backup?")
                    .withIconAnimation(false)
                    .withDivider(true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            backup();
                        }
                    })
                    .setNegativeText("PHONE MEMORY")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String s = backupString();
                            Intent in = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                            in.putExtra(Intent.EXTRA_SUBJECT, "Notes Backup");
                            in.putExtra(Intent.EXTRA_TEXT, s);
                            startActivity(in);
                        }
                    }).show();
        } else Toast.makeText(MainActivity.this, "Notes are empty!", Toast.LENGTH_SHORT).show();
    }

    private void search(@NonNull SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mAdapter.getFilter().filter("");
            }
        });

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewDetachedFromWindow(View view) {
                ser = 0;
                mi.setVisible(true);
                mi.setIcon(R.drawable.ic_bookmark_border_white_24dp);
                imp = 0;
            }

            @Override
            public void onViewAttachedToWindow(View view) {
                ser = 1;
                mi.setVisible(false);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAdapter.getFilter().filter("");
                return true;
            }
        });
    }
}

/* new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) return false;
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                final Note note = l.get(position);
                db.deleteNote(note);
                mAdapter.removeItem(position);
                Snackbar.make(sv, "Note deleted!", Snackbar.LENGTH_SHORT).show();
            }}).attachToRecyclerView(recyclerView); */

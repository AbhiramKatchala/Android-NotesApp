package io.praveen.typenote;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import io.praveen.typenote.SQLite.BinDatabaseHandler;
import io.praveen.typenote.SQLite.DatabaseHandler;
import io.praveen.typenote.SQLite.Note;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ViewActivity extends AppCompatActivity {

    TextView tv, tv2, tv3;
    @Nullable
    String noteText, date;
    int imp, position, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        if (getIntent().getExtras() != null) {
            noteText = getIntent().getExtras().getString("note");
            imp = getIntent().getExtras().getInt("imp");
            date = getIntent().getExtras().getString("date");
            position = getIntent().getExtras().getInt("pos");
        }
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/whitney.ttf").setFontAttrId(R.attr.fontPath).build());
        Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf");
        SpannableStringBuilder SS = new SpannableStringBuilder("");
        SS.setSpan(new CustomTypefaceSpan("", font2), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(SS);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tv = findViewById(R.id.view_text);
        tv2 = findViewById(R.id.view_date);
        tv3 = findViewById(R.id.view_important);
        if (imp == 1){
            tv3.setVisibility(View.VISIBLE);
        }
        tv2.setText(date);
        id = getIntent().getExtras().getInt("id");
        tv.setText(noteText);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent(ViewActivity.this, MainActivity.class);
        startActivity(i);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.copy) {
            Snackbar.make(tv, "Copied!", Snackbar.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.edit) {
            Intent intent = new Intent(ViewActivity.this, EditActivity.class);
            intent.putExtra("note", noteText);
            intent.putExtra("id", id);
            intent.putExtra("imp", imp);
            startActivity(intent);
        } else if (item.getItemId() == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, noteText);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.delete) {
            final DatabaseHandler db = new DatabaseHandler(this);
            List<Note> l = db.getAllNotes();
            final Note note = l.get(position);
            db.deleteNote(note);
            BinDatabaseHandler db2 = new BinDatabaseHandler(ViewActivity.this);
            db2.addNote(new Note(note.getNote(), note.getDate(), note.getStar()));
            Intent i = new Intent(ViewActivity.this, MainActivity.class);
            i.putExtra("delete", true);
            i.putExtra("note", true);
            startActivity(i);
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ViewActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}

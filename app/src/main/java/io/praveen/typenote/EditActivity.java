package io.praveen.typenote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.praveen.typenote.SQLite.DatabaseHandler;
import io.praveen.typenote.SQLite.Note;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EditActivity extends AppCompatActivity {

    FloatingActionButton fab;
    TextInputEditText text;
    int imp = 0;
    InterstitialAd interstitialAd;
    Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_main, menu);
        if (getIntent().getExtras() != null) {
            imp = getIntent().getExtras().getInt("imp");
        }
        if (imp == 1){
            menu.findItem(R.id.menu_important).setIcon(R.drawable.ic_bookmark_white_24dp);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        interstitialAd = new InterstitialAd(EditActivity.this);
        interstitialAd.setAdUnitId("ca-app-pub-6275597090094912/5536611682");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        intent = new Intent(EditActivity.this, MainActivity.class);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/whitney.ttf").setFontAttrId(R.attr.fontPath).build());
        Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf");
        SpannableStringBuilder SS = new SpannableStringBuilder("Edit Note");
        SS.setSpan(new CustomTypefaceSpan("", font2), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(SS);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        String noteText = "";
        if (getIntent().getExtras() != null) {
            noteText = getIntent().getExtras().getString("note");
        }
        fab = findViewById(R.id.edit_fab);
        text = findViewById(R.id.edit_text);
        text.setText(noteText);
        text.setSelection(noteText != null ? noteText.length() : 0);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                String note = text.getText().toString();
                if (note.length() > 0) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.ENGLISH);
                    String formattedDate = df.format(c.getTime());
                    int id = 0;
                    if (getIntent().getExtras() != null) {
                        id = getIntent().getExtras().getInt("id");
                    }
                    DatabaseHandler db = new DatabaseHandler(EditActivity.this);
                    db.updateNote(new Note(id, note, formattedDate, imp));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("edit", true);
                    intent.putExtra("note", true);
                    if(interstitialAd.isLoaded()) {
                        interstitialAd.show();
                        interstitialAd.setAdListener(new AdListener(){
                            @Override
                            public void onAdClosed() {
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else{
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Snackbar.make(v, "Note is empty!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_important){
            if (imp == 0){
                imp = 1;
                item.setIcon(R.drawable.ic_bookmark_white_24dp);
            } else{
                imp = 0;
                item.setIcon(R.drawable.ic_bookmark_border_white_24dp);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(EditActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

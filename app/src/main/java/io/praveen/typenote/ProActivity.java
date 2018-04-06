package io.praveen.typenote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    TextView tv, tv2, tv3;
    Button b1, b2, b3;
    SharedPreferences preferences;
    BillingProcessor bp;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro);
        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApcobfuZFov1KIJgKEKrzp9PP2n1EbBpV/xf9AyhWYN47QY8/rWGPuKht/7b4DmCVnpd6PrnYJqLt/rqR5c+lifLY5XuUH1VGqnkWA33TkPXm4UkGk3q/jvVIbM5xbcdPLqNkLiEoEuBlmAYNxM6K3lf5Kz+ff1HUH1ljYjDE9M38xS0TiLnQIRPm9cfehNxaKWOF81sx5Q9K3vNB1JoNuMyaMfBFQjfMRL6llsMRF42NEf6W/4/2c5Guxvg2qLo14/gGVRLS5H0ZVwqThNZVYTtLRWWNIrgFIwMnCjcbntFkEBK/B987poGN6miDI2r1m6XALRAgLEzM/IUaPnwnWwIDAQAB", this);
        bp.loadOwnedPurchasesFromGoogle();
        tv = findViewById(R.id.pro_head);
        tv2 = findViewById(R.id.pro_text);
        b1 = findViewById(R.id.pro_upgrade);
        b2 = findViewById(R.id.pro_redeem);
        b3 = findViewById(R.id.pro_restore);
        tv3 = findViewById(R.id.pro_help);
        preferences = PreferenceManager.getDefaultSharedPreferences(ProActivity.this);
        int id = preferences.getInt("premium", 0);
        if (id == 1){
            tv.setText("You're Premium!");
            tv2.setText("Thanks for upgrading, you'll continue to receive premium features until your lifetime!");
            b1.setVisibility(View.GONE);
            b2.setVisibility(View.GONE);
            b3.setVisibility(View.GONE);
            LinearLayout temp = findViewById(R.id.pro_ll);
            temp.setVisibility(View.GONE);
            tv3.setText("For any queries,\nDon't hesitate to contact at\nhello@praveen.io or @HelloPraveenIO");
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(ProActivity.this, "notes_pro");
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(ProActivity.this, "notes_pro");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri redeemUri = Uri.parse("https://play.google.com/redeem");
                Intent redeemIntent = new Intent(Intent.ACTION_VIEW, redeemUri);
                startActivity(redeemIntent);
            }
        });
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/whitney.ttf").setFontAttrId(R.attr.fontPath).build());
        Typeface font2 = Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf");
        SpannableStringBuilder SS = new SpannableStringBuilder("Premium");
        SS.setSpan(new CustomTypefaceSpan("", font2), 0, SS.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(SS);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ProActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent(ProActivity.this, MainActivity.class);
        startActivity(i);
        finish();
        return true;
    }

    @Override
    public void onBillingInitialized() {}

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        if (productId.equals("notes_pro")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("premium", 1).apply();
            startActivity(new Intent(ProActivity.this, ProActivity.class));
            finish();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {}

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Toast.makeText(ProActivity.this, "Error in purchase!\nPlease try again or contact us for queries.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) bp.release();
        super.onDestroy();
    }
}

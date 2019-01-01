package com.example.android.sunshine;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.R;

public class DetailActivity extends AppCompatActivity {

    private TextView detail;
    private String stringDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        detail = findViewById(R.id.detail_description);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                stringDetails = intent.getStringExtra(Intent.EXTRA_TEXT);
                detail.setText(stringDetails);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.detail, menu);
        MenuItem sharer = menu.findItem(R.id.share_details);
        sharer.setIntent(this.createShareDetailsIntent());
        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setIntent(this.createSettingsIntent());
        return true;
    }

    private Intent createSettingsIntent() {
        return new Intent(this, SettingsActivity.class);
    }

    private Intent createShareDetailsIntent() {
        String mime = "text/plain";
        String title = "Share weather details: ";
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mime)
                .setText(stringDetails).createChooserIntent();
        return intent;
    }
}

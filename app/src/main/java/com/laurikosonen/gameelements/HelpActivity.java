package com.laurikosonen.gameelements;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    private TextView helpText;
    String totalElemCount;
    String[] elemCounts;
    String fundamentalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helpText = (TextView) findViewById(R.id.help_text);

        updateDeckStats();
    }

    private void updateDeckStats() {
        elemCounts = new String[13];

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            totalElemCount = extras.getString("totalElemCount");
            elemCounts[0] = extras.getString("wldElemCount");
            elemCounts[1] = extras.getString("chaElemCount");
            elemCounts[2] = extras.getString("grpElemCount");
            elemCounts[3] = extras.getString("infElemCount");
            elemCounts[4] = extras.getString("iactElemCount");
            elemCounts[5] = extras.getString("abiElemCount");
            elemCounts[6] = extras.getString("navElemCount");
            elemCounts[7] = extras.getString("comElemCount");
            elemCounts[8] = extras.getString("thiElemCount");
            elemCounts[9] = extras.getString("itmElemCount");
            elemCounts[10] = extras.getString("goElemCount");
            elemCounts[11] = extras.getString("avElemCount");
            elemCounts[12] = extras.getString("mscElemCount");
            fundamentalCount = extras.getString("fundamentalCount");
        }
        else {
            totalElemCount = "ERROR";
            for (String s : elemCounts) {
                s = "ERROR";
            }
        }

        helpText.setText(String.format(getString(R.string.help_text),
            totalElemCount,
            elemCounts[0],
            elemCounts[1],
            elemCounts[2],
            elemCounts[3],
            elemCounts[4],
            elemCounts[5],
            elemCounts[6],
            elemCounts[7],
            elemCounts[8],
            elemCounts[9],
            elemCounts[10],
            elemCounts[11],
            elemCounts[12],
            fundamentalCount));
    }
}

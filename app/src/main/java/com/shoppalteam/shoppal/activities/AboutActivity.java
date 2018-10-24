package com.shoppalteam.shoppal.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.shoppalteam.shoppal.BuildConfig;
import com.shoppalteam.shoppal.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AboutActivity extends BaseActivity {
    private static String team, teamMembers;

    AlertDialog mAlertDialog;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        createToolbar();

        String versionName = BuildConfig.VERSION_NAME;
        TextView tv = findViewById(R.id.version);
        tv.setText(getString(R.string.versionName, versionName));

        team = getResources().getString(R.string.team);
        teamMembers = getResources().getString(R.string.team_members);
    }

    @Override
    public void onResume(){
        super.onResume();
        count=0;
    }

    public void displayLicensesAlertDialog(View v) {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/licenses.html");
        mAlertDialog = new AlertDialog.Builder(this, R.style.AboutAlertDialogStyle)
                .setTitle(getString(R.string.licenses))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        count=0;
    }

    public void showTeamMembers(View v) {
        count++;
        if(count==19) { // Because of Team 19
            randomizeTeamMembers();
            mAlertDialog = new AlertDialog.Builder(this, R.style.AboutAlertDialogStyle)
                    .setTitle(team)
                    .setMessage(teamMembers)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            mAlertDialog.setCanceledOnTouchOutside(false);
            count=0;
        }
    }

    private void randomizeTeamMembers() {
        String[] teamMembersArray = teamMembers.split("\\s+");
        List<String> teamMembersList = Arrays.asList(teamMembersArray);
        Collections.shuffle(teamMembersList);
        teamMembers = String.join(" ", teamMembersList);
    }
}

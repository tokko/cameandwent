package com.tokko.cameandwent.cameandwent;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import roboguice.activity.RoboFragmentActivity;


public class MainActivity extends RoboFragmentActivity implements LogFragment.LogFragmentHost{
    private static final String MAINACTIVITY_SETTINGS_KEY = "mainactivity";
    public static final String ACTION_WEEKLY_SUMMARY = "ACTION_WEEKLY_SUMMARY";
    public static final String ACTION_MONTHLY_SUMMARY = "ACTION_MONTHLY_SUMMARY";
    private static final String HAS_SHOWN_SETTINGS = "hasshownsettings";
    private LogFragment logFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logFragment = new LogFragment();
    }


    @Override
    protected void onStart() {
        super.onStart();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, logFragment).commit();
        if(!getSharedPreferences(MAINACTIVITY_SETTINGS_KEY, MODE_PRIVATE).getBoolean(HAS_SHOWN_SETTINGS, false)) {
            showSettings();
            getSharedPreferences(MAINACTIVITY_SETTINGS_KEY, MODE_PRIVATE).edit().putBoolean(HAS_SHOWN_SETTINGS, true).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(ACTION_WEEKLY_SUMMARY)){
            SummaryFragment.newInstance().show(getSupportFragmentManager(), "summary");
        }
        if(getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(ACTION_MONTHLY_SUMMARY)){
            SummaryFragment.newMonthlyInstance().show(getSupportFragmentManager(), "summary");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showSettings(){
        //noinspection ConstantConditions
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettings();
            //getActionBar().setDisplayShowHomeEnabled(true);
            return true;
        }
        if(id == android.R.id.home){
            getFragmentManager().popBackStack();
            //noinspection ConstantConditions
            getActionBar().setDisplayHomeAsUpEnabled(false);
            return true;
        }
        if(id == R.id.show_summary){
            SummaryFragment.newInstance().show(getSupportFragmentManager(), "summary");
            return true;
        }
        if(id == R.id.show_monthly_summary){
            SummaryFragment.newMonthlyInstance().show(getSupportFragmentManager(), "monthly_summary");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else {
            //noinspection ConstantConditions
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onEditLogentry(long id) {
        LogEntryEditorFragment f = LogEntryEditorFragment.newInstance(id);
        f.show(getSupportFragmentManager(), "editorlog");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }


    }
}

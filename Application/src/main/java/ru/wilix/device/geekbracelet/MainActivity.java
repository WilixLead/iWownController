package ru.wilix.device.geekbracelet;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(getFragmentManager().getBackStackEntryCount() <= 0)
                    finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if( getFragmentManager().getBackStackEntryCount() <= 0 )
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainFragment())
                    .addToBackStack("main")
                    .commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}

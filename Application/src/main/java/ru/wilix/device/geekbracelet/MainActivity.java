package ru.wilix.device.geekbracelet;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onResume(){
        super.onResume();

        if( getFragmentManager().getBackStackEntryCount() <= 0 )
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainFragment())
                    .addToBackStack("main")
                    .commit();
    }
}

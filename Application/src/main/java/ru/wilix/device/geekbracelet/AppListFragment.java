package ru.wilix.device.geekbracelet;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import ru.wilix.device.geekbracelet.model.AppNotification;

public class AppListFragment extends ListFragment{
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private AppsAdapter listadaptor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = App.mContext.getPackageManager();
        new LoadApplications().execute();
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            applist = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            for( ApplicationInfo app : applist )
                if(AppNotification.canNotice(app.packageName) > 0)
                    app.enabled = true;
                else
                    app.enabled = false;
            listadaptor = new AppsAdapter(getActivity(), R.layout.applist_item, applist);
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
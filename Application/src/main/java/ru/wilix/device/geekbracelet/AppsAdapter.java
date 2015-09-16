package ru.wilix.device.geekbracelet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ru.wilix.device.geekbracelet.model.AppNotification;

/**
 * Created by Aloyan Dmitry on 16.09.2015
 */
public class AppsAdapter extends ArrayAdapter<ApplicationInfo> {
    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;
    private String noticeTypes[] = {
        getContext().getResources().getString(R.string.notice_type_dont_notice),
        getContext().getResources().getString(R.string.notice_type_message),
        getContext().getResources().getString(R.string.notice_type_cloud),
        getContext().getResources().getString(R.string.notice_type_error),
    };

    private AlertDialog.Builder noticeTypeDialog = new AlertDialog.Builder(getContext());
    private DialogInterface.OnClickListener dialogItemListener;
    private View.OnClickListener bodyListener;
    private ApplicationInfo selectedItem;
    private View selectedView;

    public AppsAdapter(Context context, int textViewResourceId, List<ApplicationInfo> appsList) {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        packageManager = context.getPackageManager();

        noticeTypeDialog.setTitle(R.string.choise_notice_type);

        dialogItemListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_NEGATIVE)
                    return;
                ListView lv = ((AlertDialog) dialog).getListView();
                if( lv.getCheckedItemPosition() <= 0 ) {
                    AppNotification.disableApp(selectedItem.packageName);
                    selectedItem.enabled = false;
                }else {
                    AppNotification.enableApp(selectedItem.packageName, lv.getCheckedItemPosition());
                    selectedItem.enabled = true;
                }

                selectedView.post(new Runnable() {
                    @Override
                    public void run() {
                        if( selectedItem.enabled )
                            ((ImageView) selectedView.findViewById(R.id.app_used))
                                    .setImageResource(R.drawable.abc_btn_check_to_on_mtrl_015);
                        else
                            ((ImageView) selectedView.findViewById(R.id.app_used))
                                    .setImageResource(R.drawable.abc_btn_check_to_on_mtrl_000);
                    }
                });
                dialog.dismiss();
            }
        };

        bodyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedItem = getItem((Integer)v.getTag());
                selectedView = v;
                noticeTypeDialog.setSingleChoiceItems(noticeTypes,
                        AppNotification.canNotice(selectedItem.packageName),
                        dialogItemListener);
                noticeTypeDialog.create().show();
            }
        };
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.applist_item, null);
            view.setOnClickListener(bodyListener);
        }

        ApplicationInfo data = appsList.get(position);
        if (null != data) {
            ImageView usedView = (ImageView) view.findViewById(R.id.app_used);

            ((TextView) view.findViewById(R.id.app_name)).setText(data.loadLabel(packageManager));
            ((TextView) view.findViewById(R.id.app_package)).setText(data.packageName);
            ((ImageView) view.findViewById(R.id.app_icon))
                    .setImageDrawable(data.loadIcon(packageManager));

            if( data.enabled )
                usedView.setImageResource(R.drawable.abc_btn_check_to_on_mtrl_015);
            else
                usedView.setImageResource(R.drawable.abc_btn_check_to_on_mtrl_000);
            view.setTag(position);
        }
        return view;
    }


}

package com.ingenieur.andyelderscrolls.kfdisplay;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Tes3AnimChooser {
    private ListView list;
    private Dialog dialog;

    public interface AnimSelectedListener {
        void animSelected(String anim);
    }

    public Tes3AnimChooser setAnimListener(AnimSelectedListener animListener) {
        this.animListener = animListener;
        return this;
    }

    private AnimSelectedListener animListener;


    public Tes3AnimChooser(Activity activity, String title, String[] animsToSelect) {
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String animChosen = (String) list.getItemAtPosition(which);

                if (animListener != null) {
                    animListener.animSelected(animChosen);
                }
                dialog.dismiss();

            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        // refresh the user interface
        dialog.setTitle(title);
        list.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, animsToSelect) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                view = super.getView(pos, view, parent);
                ((TextView) view).setSingleLine(true);
                return view;
            }
        });

    }


    public void showDialog() {
        dialog.show();
    }
}
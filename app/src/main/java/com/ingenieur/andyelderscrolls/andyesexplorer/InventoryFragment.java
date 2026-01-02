package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ingenieur.andyelderscrolls.R;

import androidx.fragment.app.Fragment;

public class InventoryFragment extends Fragment {
    private View rootView;
    private ScrollsExplorer scrollsExplorer;
    private View.OnKeyListener keyListener;


    public void setScrollsExplorer(ScrollsExplorer scrollsExplorer) {
        this.scrollsExplorer = scrollsExplorer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.interior_list_panel, container, false);

        // make the left and right swipers work
        ImageButton furnitureCatalogLeftSwiper = (ImageButton) rootView.findViewById(R.id.inventoryCloseButton);
        furnitureCatalogLeftSwiper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollsExplorer.esExplorerFragment.showSimpleWalk();
            }
        });

        //TODO: this doesn't work, sad :(
        keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // M sends us back
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_I) {
                    scrollsExplorer.esExplorerFragment.showSimpleWalk();
                    return true;
                }
                return false;
            }
        };

        rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // we get a key code even if we aren't displayed, so only when we have focus do we start listening
                if (hasFocus) {
                    // need to invoke later or it will catch the currently being process M key
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.setOnKeyListener(keyListener);
                        }
                    });
                } else {
                    rootView.setOnKeyListener(null);
                }
            }
        });

        return rootView;
    }
}
package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.ingenieur.andyelderscrolls.R;

import androidx.fragment.app.Fragment;

public class CharacterFragment extends Fragment {
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.character_panel, container, false);


        // make the close work
        ImageButton closeCS = (ImageButton) rootView.findViewById(R.id.closeCharacterSheet);
        closeCS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AndyESExplorerActivity) getActivity()).mViewPager.setCurrentItem(1, true);
            }
        });
        final ToggleButton freeflybutton = (ToggleButton) rootView.findViewById(R.id.freeflybutton);
        freeflybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeflybutton.setChecked(freeflybutton.isChecked());
                ((AndyESExplorerActivity) getActivity()).scrollsExplorer.simpleWalkSetup.setFreeFly(freeflybutton.isChecked());
            }
        });

        final ToggleButton showphysicsbutton = (ToggleButton) rootView.findViewById(R.id.showphysicsbutton);
        showphysicsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showphysicsbutton.setChecked(freeflybutton.isChecked());
                ((AndyESExplorerActivity) getActivity()).scrollsExplorer.simpleWalkSetup.toggleHavok();
            }
        });
        final ToggleButton showvisualsbutton = (ToggleButton) rootView.findViewById(R.id.showvisualsbutton);
        showvisualsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freeflybutton.setChecked(freeflybutton.isChecked());
                ((AndyESExplorerActivity) getActivity()).scrollsExplorer.simpleWalkSetup.toggleVisual();
            }
        });
        final Button optionsbutton = (Button) rootView.findViewById(R.id.optionsbutton);
        optionsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollsExplorer scrollsExplorer = ((AndyESExplorerActivity) getActivity()).scrollsExplorer;
                if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null) {
                    OptionsDialog od = new OptionsDialog(((AndyESExplorerActivity) getActivity()), scrollsExplorer.simpleWalkSetup);
                    od.display();
                } else {
                    OptionsDialog od = new OptionsDialog(((AndyESExplorerActivity) getActivity()), null);
                    od.display();
                }
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.es_main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        AndyESExplorerActivity andyESExplorerActivity = ((AndyESExplorerActivity) getActivity());
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.es_menu_options:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
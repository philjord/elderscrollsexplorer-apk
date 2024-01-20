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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ingenieur.andyelderscrolls.R;

import androidx.fragment.app.Fragment;

import org.jogamp.vecmath.Vector3f;

import scrollsexplorer.PropertyLoader;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import tools3d.utils.YawPitch;
import tools3d.utils.loader.PropertyCodec;

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
                showphysicsbutton.setChecked(showphysicsbutton.isChecked());
                ((AndyESExplorerActivity) getActivity()).scrollsExplorer.simpleWalkSetup.toggleHavok();

                // this will make the next cell load load up physics colors
                BethWorldVisualBranch.LOAD_PHYS_FROM_VIS = showphysicsbutton.isChecked();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Next cell load will "+(showphysicsbutton.isChecked() ? "" : "not ") + "display physics", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        final ToggleButton showvisualsbutton = (ToggleButton) rootView.findViewById(R.id.showvisualsbutton);
        showvisualsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showvisualsbutton.setChecked(showvisualsbutton.isChecked());
                ((AndyESExplorerActivity) getActivity()).scrollsExplorer.simpleWalkSetup.toggleVisual();
            }
        });

        final Button saveButton = (Button) rootView.findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollsExplorer scrollsExplorer = ((AndyESExplorerActivity) getActivity()).scrollsExplorer;
                if (scrollsExplorer != null &&scrollsExplorer.esmManager != null) {
                    PropertyLoader.properties.setProperty("YawPitch" + scrollsExplorer.esmManager.getName(),
                            new YawPitch(scrollsExplorer.simpleWalkSetup.getAvatarLocation().getTransform()).toString());
                    PropertyLoader.properties.setProperty("Trans" + scrollsExplorer.esmManager.getName(),
                            "" + PropertyCodec.vector3fIn(scrollsExplorer.simpleWalkSetup.getAvatarLocation().get(new Vector3f())));
                    PropertyLoader.properties.setProperty("CellId" + scrollsExplorer.esmManager.getName(), "" + scrollsExplorer.getSimpleBethCellManager().getCurrentCellFormId());
                    PropertyLoader.save();
                }
            }
        });

        final Button changeCellButton = (Button) rootView.findViewById(R.id.changecellbutton);
        changeCellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollsExplorer scrollsExplorer = ((AndyESExplorerActivity) getActivity()).scrollsExplorer;
                if (scrollsExplorer != null && scrollsExplorer.simpleWalkSetup != null) {
                    ((AndyESExplorerActivity) getActivity()).mViewPager.setCurrentItem(1, true);
                    scrollsExplorer.showCellPicker();
                } else {
                    System.out.println("changeCellButton failed " + scrollsExplorer + " " + scrollsExplorer.simpleWalkSetup);
                }
            }
        });
        final Button changeLocationButton = (Button) rootView.findViewById(R.id.changelocationbutton);
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollsExplorer scrollsExplorer = ((AndyESExplorerActivity) getActivity()).scrollsExplorer;
                if (scrollsExplorer != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "changeLocationButton that would be fun now, wouldn't it?", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    System.out.println("changeCellButton failed " + scrollsExplorer + " " + scrollsExplorer.simpleWalkSetup);
                }
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

        //AndyESExplorerActivity andyESExplorerActivity = ((AndyESExplorerActivity) getActivity());
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.es_menu_options:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
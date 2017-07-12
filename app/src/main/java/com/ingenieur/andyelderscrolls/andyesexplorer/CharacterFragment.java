package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import com.ingenieur.andyelderscrolls.R;

public class CharacterFragment extends Fragment
{
	private View rootView;


	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.character_panel, container, false);



		// make the right swiper work
		Button furnitureTableRightSwiper = (Button)rootView.findViewById(R.id.furnitureTableRightSwiper);
		furnitureTableRightSwiper.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((AndyESExplorerActivity)getActivity()).mViewPager.setCurrentItem(1, true);
			}
		});


		final ToggleButton freeflybutton = (ToggleButton)rootView.findViewById(R.id.freeflybutton);
		freeflybutton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				freeflybutton.setChecked(freeflybutton.isChecked());
				((AndyESExplorerActivity)getActivity()).scrollsExplorer.simpleWalkSetup.setFreeFly(freeflybutton.isChecked());
			}
		});

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.es_main_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{


		super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		AndyESExplorerActivity andyESExplorerActivity = ((AndyESExplorerActivity) getActivity());
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.es_menu_options:

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
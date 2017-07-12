package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ingenieur.andyelderscrolls.R;

public class InteriorListFragment extends Fragment
{
	private View rootView;
	private ScrollsExplorer scrollsExplorer;

	public void setScrollsExplorer(ScrollsExplorer scrollsExplorer)
	{
		this.scrollsExplorer = scrollsExplorer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.interior_list_panel, container, false);



		// make the left and right swipers work
		Button furnitureCatalogLeftSwiper = (Button)rootView.findViewById(R.id.furnitureCatalogLeftSwiper2);
		furnitureCatalogLeftSwiper.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((AndyESExplorerActivity)getActivity()).mViewPager.setCurrentItem(2, true);
			}
		});


		return rootView;
	}
}
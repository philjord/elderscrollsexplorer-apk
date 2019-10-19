package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ingenieur.andyelderscrolls.R;

import androidx.fragment.app.Fragment;

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
		ImageButton furnitureCatalogLeftSwiper = (ImageButton)rootView.findViewById(R.id.furnitureCatalogLeftSwiper2);
		furnitureCatalogLeftSwiper.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((AndyESExplorerActivity)getActivity()).mViewPager.setCurrentItem(1, true);
			}
		});


		return rootView;
	}
}
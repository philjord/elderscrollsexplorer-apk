package com.ingenieur.andyelderscrolls.andyesexplorer;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by phil on 2/6/2017.
 * Replaces everything from the parent class to get access to the fragment transaction
 * because we don't want the back stack to memory leak these fragments when we swap out for a new
 * which is simply putting in the call mCurTransaction.remove((Fragment) object);
 */
public class AndyESExplorerPagerAdapter extends FragmentPagerAdapter
{

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;

	public AndyESExplorerPagerAdapter(FragmentManager fm)
	{
		super(fm);
		this.mFragmentManager = fm;
	}


	@Override
	public Fragment getItem(int position)
	{
		if (position == 0)
		{
			return  new CharacterFragment();
		}
		else if (position == 1)
		{
			return  new AndyESExplorerFragment();
		}
		else if (position == 2)
		{
			return  new MapFragment();
		}
		else if (position == 3)
		{
			return  new InteriorListFragment();
		}

		// it is safest by far to always return something here
		return new Fragment();
	}

	@Override
	public int getCount()
	{
		return 4;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		if (mCurTransaction == null)
		{
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		final long itemId = getItemId(position);

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), itemId);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null)
		{
			mCurTransaction.attach(fragment);
		}
		else
		{
			fragment = getItem(position);
			// position might be out of range 0-3
			if (fragment != null)
			{
				mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
			}
		}


		if (fragment != mCurrentPrimaryItem)
		{
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}

		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		if(object != null)
		{
			if (mCurTransaction == null)
			{
				mCurTransaction = mFragmentManager.beginTransaction();
			}

			mCurTransaction.detach((Fragment) object);

			// they stay in mActive cos the backstack might want them, and we want to get rid of them
			// note this must be done in the same transaction as the adds etc hence the copy of the parent class
			mCurTransaction.remove((Fragment) object);
		}
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object)
	{
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem)
		{
			if (mCurrentPrimaryItem != null)
			{
				mCurrentPrimaryItem.setMenuVisibility(false);
				mCurrentPrimaryItem.setUserVisibleHint(false);
			}
			if (fragment != null)
			{
				fragment.setMenuVisibility(true);
				fragment.setUserVisibleHint(true);
			}
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(ViewGroup container)
	{
		if (mCurTransaction != null)
		{
			//attempt to solve
			//https://console.firebase.google.com/u/0/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/3ea8661b?duration=172800000
			try
			{
				mCurTransaction.commitNowAllowingStateLoss();
			}catch(NullPointerException e){}
			mCurTransaction = null;
		}
	}

	private String makeFragmentName(int viewId, long id)
	{
		return "android:switcher:" + viewId + ":" + id;
	}


	private long baseId = 0;

	//this is called when notifyDataSetChanged() is called
	@Override
	public int getItemPosition(Object object)
	{
		// refresh all fragments when data set changed
		return PagerAdapter.POSITION_NONE;
	}

	@Override
	public long getItemId(int position)
	{
		// give an ID different from position when position has been changed
		return baseId + position;
	}

	/**
	 * Notify that the position of a fragment has been changed.
	 * Create a new ID for each position to force recreation of the fragment
	 *
	 * @param n number of items which have been changed
	 */
	public void notifyChangeInPosition(int n)
	{
		// shift the ID returned by getItemId outside the range of all previous fragments
		baseId += getCount() + n;
	}
}

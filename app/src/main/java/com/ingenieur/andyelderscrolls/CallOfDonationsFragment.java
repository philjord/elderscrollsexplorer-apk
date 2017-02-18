package com.ingenieur.andyelderscrolls;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by phil on 2/16/2017.
 */

public class CallOfDonationsFragment extends DialogFragment
{
	/**
	 * Google
	 */
	public static final String GOOGLE_PUBKEY = "FIND THIS MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg8bTVFK5zIg4FGYkHKKQ/j/iGZQlXU0qkAv2BA6epOX1ihbMz78iD4SmViJlECHN8bKMHxouRNd9pkmQKxwEBHg5/xDC/PHmSCXFx/gcY/xa4etA1CSfXjcsS9i94n+j0gGYUg69rNkp+p/09nO9sgfRTAQppTxtgKaXwpfKe1A8oqmDUfOnPzsEAG6ogQL6Svo6ynYLVKIvRPPhXkq+fp6sJ5YVT5Hr356yCXlM++G56Pk8Z+tPzNjjvGSSs/MsYtgFaqhPCsnKhb55xHkc8GJ9haq8k3PSqwMSeJHnGiDq5lzdmsjdmGkWdQq2jIhKlhMZMm5VQWn0T59+xjjIIwIDAQAB";
	public static final String[] GOOGLE_CATALOG = new String[]{"corm.donation.1",
			"corm.donation.2", "corm.donation.5", "corm.donation.10", "corm.donation.15",
			"corm.donation.20"};


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_donations, null);
		View  t =  view.findViewById(R.id.donations_activity_container);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DonationsFragment donationsFragment;

		donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
				getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null,
				null, false, null, null, false, null);

		ft.replace(R.id.donations_activity_container, donationsFragment, "donationsFragment");
		ft.commit();

		return view;
	}

	/**
	 * Needed for Google Play In-app Billing. It uses startIntentSenderForResult(). The result is not propagated to
	 * the Fragment like in startActivityForResult(). Thus we need to propagate manually to our Fragment.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
		if (fragment != null)
		{
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

}


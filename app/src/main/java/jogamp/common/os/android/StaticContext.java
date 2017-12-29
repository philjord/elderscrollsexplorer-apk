package jogamp.common.os.android;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;


//PJ If 2 NewtActivities are started in the same JVM then the second one will call clear() when disposed
// however the first one will presume it still has a fine StaticContext, and will have no ability to catch the clear event.
// In order to try to address this the context is now held by WeakReference, it will often be exactly the same context
// and if not it will still hold the same resources.
// therefore clear is ignored, resetting context allowed and the weak reference is presumed to allow clean up

public class StaticContext
{
	private static WeakReference<Context> appContext = null;
	private static WeakReference<ViewGroup> contentViewGroup = null;

	private static boolean DEBUG = false;

	/**
	 * Register Android application context for static usage.
	 *
	 * @param appContext mandatory application Context
	 * @throws RuntimeException if the context is already registered.
	 */
	public static final synchronized void init(final Context appContext)
	{

		init(appContext, null);
	}

	/**
	 * Register Android application context w/ a ViewGroup for static usage.
	 *
	 * @param appContext       mandatory application Context
	 * @param contentViewGroup optional ViewGroup acting as the Window's ContentView, usually a FrameLayout instance.
	 * @throws RuntimeException if the context is already registered.
	 */
	public static final synchronized void init(final Context appContext, final ViewGroup contentViewGroup)
	{
		// re calling this method is fine
		/*if (null != StaticContext.appContext)
		{
			throw new RuntimeException("Context already set");
		}*/
		if (DEBUG)
		{
			Log.d(MD.TAG, "init(appCtx " + appContext + ", viewGroup " + contentViewGroup + ")");
		}

		StaticContext.appContext = new WeakReference(appContext);
		StaticContext.contentViewGroup = new WeakReference(contentViewGroup);

	}

	/**
	 * Unregister the Android application Context and ViewGroup
	 */
	public static final synchronized void clear()
	{
		// ignored as the context may still be wanted by other Activities
	/*	if (DEBUG)
		{
			Log.d(MD.TAG, "clear()");
		}
		appContext = null;
		contentViewGroup = null;*/
	}

	/**
	 * Return the registered Android application Context
	 *
	 * @return
	 */
	public static final synchronized Context getContext()
	{
		return appContext !=  null ? appContext.get() : null;
	}

	/**
	 * Return the registered Android ViewGroup acting as the Window's ContentView
	 *
	 * @return
	 */
	public static final synchronized ViewGroup getContentViewGroup()
	{
		return contentViewGroup != null ? contentViewGroup.get() : null;
	}
}

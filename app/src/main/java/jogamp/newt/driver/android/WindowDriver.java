//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package jogamp.newt.driver.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.KeyEvent.DispatcherState;
import android.view.SurfaceHolder.Callback2;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout.LayoutParams;
import com.jogamp.common.ExceptionUtils;
import com.jogamp.common.os.AndroidVersion;
import com.jogamp.nativewindow.AbstractGraphicsScreen;
import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.DefaultGraphicsScreen;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.VisualIDHolder.VIDType;
import com.jogamp.nativewindow.egl.EGLGraphicsDevice;
import com.jogamp.nativewindow.util.Point;
import com.jogamp.nativewindow.util.RectangleImmutable;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.Window;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.egl.EGL;
import java.nio.IntBuffer;
import jogamp.common.os.android.StaticContext;
import jogamp.newt.WindowImpl;
import jogamp.newt.driver.android.DisplayDriver;
import jogamp.newt.driver.android.event.AndroidNewtEventFactory;
import jogamp.newt.driver.android.event.AndroidNewtEventTranslator;
import jogamp.opengl.egl.EGLDisplayUtil;
import jogamp.opengl.egl.EGLGraphicsConfiguration;
import jogamp.opengl.egl.EGLGraphicsConfigurationFactory;

/** EXACT copy, except on destroy keyboardVisibleReceiver is also set to null
 *
 */
public class WindowDriver extends WindowImpl implements Callback2 {
	public static final int NATIVE_WINDOW_FORMAT_RGBA_8888 = 1;
	public static final int NATIVE_WINDOW_FORMAT_RGBX_8888 = 2;
	public static final int NATIVE_WINDOW_FORMAT_RGB_565 = 4;
	protected Activity activity = null;
	private WindowDriver.KeyboardVisibleReceiver keyboardVisibleReceiver = new WindowDriver.KeyboardVisibleReceiver();
	private boolean added2StaticViewGroup;
	private WindowDriver.MSurfaceView androidView;
	private int nativeFormat;
	private int androidFormat;
	private GLCapabilitiesImmutable capsByFormat;
	private Surface surface;
	private volatile long surfaceHandle;
	private long eglSurface;

	public static final int getSurfaceHolderFormat(CapabilitiesImmutable var0) {
		boolean var1 = false;
		byte var2;
		if(!var0.isBackgroundOpaque()) {
			var2 = -3;
		} else if(var0.getRedBits() <= 5 && var0.getGreenBits() <= 6 && var0.getBlueBits() <= 5 && var0.getAlphaBits() == 0) {
			var2 = 4;
		} else if(var0.getAlphaBits() == 0) {
			var2 = 3;
		} else {
			var2 = 1;
		}

		Log.d("JogAmp.NEWT", "getSurfaceHolderFormat: requested: " + var0);
		Log.d("JogAmp.NEWT", "getSurfaceHolderFormat:  returned: " + var2);
		return var2;
	}

	public static final int getANativeWindowFormat(int var0) {
		byte var1;
		switch(var0) {
			case 1:
			case 6:
			case 7:
				var1 = 1;
				break;
			case 2:
			case 3:
				var1 = 2;
				break;
			case 4:
			case 11:
				var1 = 4;
				break;
			case 5:
			case 8:
			case 9:
			case 10:
			default:
				var1 = 1;
		}

		Log.d("JogAmp.NEWT", "getANativeWindowFormat: android: " + var0 + " -> native " + var1);
		return var1;
	}

	public static final CapabilitiesImmutable fixCaps(boolean var0, int var1, CapabilitiesImmutable var2) {
		PixelFormat var3 = new PixelFormat();
		PixelFormat.getPixelFormatInfo(var1, var3);
		byte var5;
		byte var6;
		byte var7;
		byte var8;
		switch(var1) {
			case 1:
				var5 = 8;
				var6 = 8;
				var7 = 8;
				var8 = 8;
				break;
			case 2:
				var5 = 8;
				var6 = 8;
				var7 = 8;
				var8 = 0;
				break;
			case 3:
				var5 = 8;
				var6 = 8;
				var7 = 8;
				var8 = 0;
				break;
			case 4:
				var5 = 5;
				var6 = 6;
				var7 = 5;
				var8 = 0;
				break;
			case 5:
			case 8:
			case 9:
			case 10:
			default:
				throw new InternalError("Unhandled pixelformat: " + var1);
			case 6:
				var5 = 5;
				var6 = 5;
				var7 = 5;
				var8 = 1;
				break;
			case 7:
				var5 = 4;
				var6 = 4;
				var7 = 4;
				var8 = 4;
				break;
			case 11:
				var5 = 3;
				var6 = 3;
				var7 = 2;
				var8 = 0;
		}

		boolean var9 = var0 || var2.getRedBits() > var5 && var2.getGreenBits() > var6 && var2.getBlueBits() > var7 && var2.getAlphaBits() > var8;
		Object var4;
		if(var9) {
			Capabilities var10 = (Capabilities)var2.cloneMutable();
			var10.setRedBits(var5);
			var10.setGreenBits(var6);
			var10.setBlueBits(var7);
			var10.setAlphaBits(var8);
			var4 = var10;
		} else {
			var4 = var2;
		}

		Log.d("JogAmp.NEWT", "fixCaps:    format: " + var1);
		Log.d("JogAmp.NEWT", "fixCaps: requested: " + var2);
		Log.d("JogAmp.NEWT", "fixCaps:    chosen: " + var4);
		return (CapabilitiesImmutable)var4;
	}

	public static final boolean isAndroidFormatTransparent(int var0) {
		switch(var0) {
			case -3:
			case -2:
				return true;
			default:
				return false;
		}
	}

	public static Class<?>[] getCustomConstructorArgumentTypes() {
		return new Class[]{Context.class};
	}

	public WindowDriver() {
		this.reset();
	}

	// for when the window is super dead, no return
	public void cleanUp()
	{
		this.keyboardVisibleReceiver = null;
		this.activity = null;
		this.androidView = null;
		this.destroy(false);
	}

	public void registerActivity(Activity var1) {
		this.activity = var1;
	}

	private final void reset() {
		this.added2StaticViewGroup = false;
		this.androidView = null;
		this.nativeFormat = 0;
		this.androidFormat = 0;
		this.capsByFormat = null;
		this.surface = null;
		this.surfaceHandle = 0L;
		this.eglSurface = 0L;
		this.definePosition(0, 0);
		this.defineSize(0, 0);
		this.setBrokenFocusChange(true);
	}

	private final void setupInputListener(boolean var1) {
		Log.d("JogAmp.NEWT", "setupInputListener(enable " + var1 + ") - " + Thread.currentThread().getName());
		AndroidNewtEventTranslator var2 = var1?new AndroidNewtEventTranslator(this, this.androidView.getContext(), this.androidView.getHandler()):null;
		this.androidView.setOnTouchListener(var2);
		this.androidView.setOnKeyListener(var2);
		this.androidView.setOnFocusChangeListener(var2);
		if(AndroidVersion.SDK_INT >= 12) {
			Log.d("JogAmp.NEWT", "setupInputListener - enable GenericMotionListener - " + Thread.currentThread().getName());
			this.androidView.setOnGenericMotionListener(var2);
		}

		if(var1) {
			this.androidView.post(new Runnable() {
				public void run() {
					WindowDriver.this.androidView.setClickable(false);
					WindowDriver.this.androidView.setFocusable(true);
					WindowDriver.this.androidView.setFocusableInTouchMode(true);
				}
			});
		}

	}

	private final void setupAndroidView(Context var1) {
		this.androidView = new WindowDriver.MSurfaceView(var1);
		SurfaceHolder var2 = this.androidView.getHolder();
		var2.addCallback(this);
		var2.setFormat(getSurfaceHolderFormat(this.getRequestedCapabilities()));
	}

	public final SurfaceView getAndroidView() {
		return this.androidView;
	}

	protected final void instantiationFinishedImpl() {
		Log.d("JogAmp.NEWT", "instantiationFinishedImpl() - " + Thread.currentThread().getName());
		Context var1 = StaticContext.getContext();
		if(null == var1) {
			throw new NativeWindowException("No static [Application] Context has been set. Call StaticContext.setContext(Context) first.");
		} else {
			if(null != Looper.myLooper()) {
				this.setupAndroidView(var1);
			}

		}
	}

	protected final boolean canCreateNativeImpl() {
		Log.d("JogAmp.NEWT", "canCreateNativeImpl.0: surfaceHandle ready " + (0L != this.surfaceHandle) + " - on thread " + Thread.currentThread().getName());
		if(Window.DEBUG_IMPLEMENTATION) {
			ExceptionUtils.dumpStack(System.err);
		}

		if(this.isFullscreen()) {
			MonitorDevice var1 = this.getMainMonitor();
			RectangleImmutable var2 = var1.getViewportInWindowUnits();
			this.definePosition(var2.getX(), var2.getY());
			this.defineSize(var2.getWidth(), var2.getHeight());
		}

		boolean var7;
		if(0L == this.surfaceHandle) {
			final ViewGroup var8 = StaticContext.getContentViewGroup();
			Log.d("JogAmp.NEWT", "canCreateNativeImpl: viewGroup " + var8);
			if(null != var8 && !this.added2StaticViewGroup) {
				this.added2StaticViewGroup = true;
				var8.post(new Runnable() {
					public void run() {
						if(null == WindowDriver.this.androidView) {
							WindowDriver.this.setupAndroidView(StaticContext.getContext());
						}

						var8.addView(WindowDriver.this.androidView, new LayoutParams(WindowDriver.this.getWidth(), WindowDriver.this.getHeight(), 85));
						Log.d("JogAmp.NEWT", "canCreateNativeImpl: added to static ViewGroup - on thread " + Thread.currentThread().getName());
					}
				});

				for(long var3 = 1000L; 0L < var3 && 0L == this.surfaceHandle; var3 -= 10L) {
					try {
						Thread.sleep(10L);
					} catch (InterruptedException var6) {
						;
					}
				}

				var7 = 0L != this.surfaceHandle;
				Log.d("JogAmp.NEWT", "canCreateNativeImpl: surfaceHandle ready(2) " + var7 + " - on thread " + Thread.currentThread().getName());
			} else {
				var7 = false;
			}
		} else {
			var7 = true;
		}

		return var7;
	}

	protected final void createNativeImpl() {
		AbstractGraphicsScreen var1 = this.getScreen().getGraphicsScreen();
		EGLGraphicsDevice var2 = (EGLGraphicsDevice)var1.getDevice();
		EGLGraphicsDevice var3 = EGLDisplayUtil.eglCreateEGLGraphicsDevice(var2.getNativeDisplayID(), var2.getConnection(), var2.getUnitID());
		var3.open();
		DefaultGraphicsScreen var4 = new DefaultGraphicsScreen(var3, var1.getIndex());
		Log.d("JogAmp.NEWT", "createNativeImpl 0 - eglDevice 0x" + Integer.toHexString(var3.hashCode()) + ", " + var3 + ", surfaceHandle 0x" + Long.toHexString(this.surfaceHandle) + ", format [a " + this.androidFormat + ", n " + this.nativeFormat + "], win[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + "], pixel[" + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + "] - on thread " + Thread.currentThread().getName());
		if(0L != this.getParentWindowHandle()) {
			throw new NativeWindowException("Window parenting not supported (yet)");
		} else if(0L == this.surfaceHandle) {
			throw new InternalError("surfaceHandle null");
		} else {
			EGLGraphicsConfiguration var5 = EGLGraphicsConfigurationFactory.chooseGraphicsConfigurationStatic(this.capsByFormat, (GLCapabilitiesImmutable)this.getRequestedCapabilities(), (GLCapabilitiesChooser)this.capabilitiesChooser, var4, this.nativeFormat, isAndroidFormatTransparent(this.androidFormat));
			if(var5 == null) {
				throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: " + this);
			} else {
				int var6 = var5.getVisualID(VIDType.NATIVE);
				Log.d("JogAmp.NEWT", "nativeVisualID 0x" + Integer.toHexString(var6));
				Log.d("JogAmp.NEWT", "requestedCaps: " + var5.getRequestedCapabilities());
				Log.d("JogAmp.NEWT", "chosenCaps   : " + var5.getChosenCapabilities());
				if(0 != var6) {
					setSurfaceVisualID0(this.surfaceHandle, var6);
				}

				this.eglSurface = EGL.eglCreateWindowSurface(var3.getHandle(), var5.getNativeConfig(), this.surfaceHandle, (IntBuffer)null);
				if(0L == this.eglSurface) {
					throw new NativeWindowException("Creation of window surface failed: " + var5 + ", surfaceHandle 0x" + Long.toHexString(this.surfaceHandle) + ", error " + toHexString(EGL.eglGetError()));
				} else {
					this.setGraphicsConfiguration(var5);
					this.setWindowHandle(this.surfaceHandle);
					this.visibleChanged(false, true);
					this.focusChanged(false, true);
					this.setupInputListener(true);
					Log.d("JogAmp.NEWT", "createNativeImpl X: eglDevice 0x" + Integer.toHexString(var3.hashCode()) + ", " + var3 + ", eglSurfaceHandle 0x" + Long.toHexString(this.eglSurface));
				}
			}
		}
	}

	protected final void closeNativeImpl() {
		EGLGraphicsDevice var1 = (EGLGraphicsDevice)this.getGraphicsConfiguration().getScreen().getDevice();
		Log.d("JogAmp.NEWT", "closeNativeImpl 0 - eglDevice 0x" + Integer.toHexString(var1.hashCode()) + ", " + var1 + ", surfaceHandle 0x" + Long.toHexString(this.surfaceHandle) + ", eglSurfaceHandle 0x" + Long.toHexString(this.eglSurface) + ", format [a " + this.androidFormat + ", n " + this.nativeFormat + "], win[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + "], pixel[" + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + "]," + " - on thread " + Thread.currentThread().getName());
		if(Window.DEBUG_IMPLEMENTATION) {
			ExceptionUtils.dumpStack(System.err);
		}

		this.setupInputListener(false);
		if(0L != this.eglSurface) {
			try {
				if(!EGL.eglDestroySurface(var1.getHandle(), this.eglSurface)) {
					throw new GLException("Error destroying window surface (eglDestroySurface)");
				}
			} catch (Throwable var6) {
				Log.d("JogAmp.NEWT", "closeNativeImpl: Catch exception " + var6.getMessage());
				var6.printStackTrace();
			} finally {
				this.eglSurface = 0L;
			}
		}

		release0(this.surfaceHandle);
		var1.close();
		if(null != this.androidView && this.added2StaticViewGroup) {
			this.added2StaticViewGroup = false;
			final ViewGroup var2 = StaticContext.getContentViewGroup();
			if(null != var2) {
				var2.post(new Runnable() {
					public void run() {
						var2.removeView(WindowDriver.this.androidView);
						Log.d("JogAmp.NEWT", "closeNativeImpl: removed from static ViewGroup - on thread " + Thread.currentThread().getName());
					}
				});
			}
		}

		this.surface = null;
		this.surfaceHandle = 0L;
	}

	public final long getSurfaceHandle() {
		return this.eglSurface;
	}

	public final void focusChanged(boolean var1, boolean var2) {
		super.focusChanged(var1, var2);
	}

	protected final void requestFocusImpl(boolean var1) {
		if(null != this.androidView) {
			Log.d("JogAmp.NEWT", "requestFocusImpl: reparented " + var1);
			this.androidView.post(new Runnable() {
				public void run() {
					WindowDriver.this.androidView.requestFocus();
					WindowDriver.this.androidView.bringToFront();
				}
			});
		}

	}

	protected final int getSupportedReconfigMaskImpl() {
		return 2057;
	}

	protected final boolean reconfigureWindowImpl(int var1, int var2, int var3, int var4, int var5) {
		boolean var6 = true;
		if(0 != (2097152 & var5)) {
			Log.d("JogAmp.NEWT", "reconfigureWindowImpl.setFullscreen post creation (setContentView()) n/a");
			return false;
		} else {
			if(this.getWidth() != var3 || this.getHeight() != var4) {
				if(0L != this.getWindowHandle()) {
					Log.d("JogAmp.NEWT", "reconfigureWindowImpl.setSize n/a");
					var6 = false;
				} else {
					this.defineSize(var3, var4);
				}
			}

			if(this.getX() != var1 || this.getY() != var2) {
				if(0L != this.getWindowHandle()) {
					Log.d("JogAmp.NEWT", "reconfigureWindowImpl.setPos n/a");
					var6 = false;
				} else {
					this.definePosition(var1, var2);
				}
			}

			if(0 != (-2147483648 & var5)) {
				this.visibleChanged(false, 0 != (1 & var5));
			}

			return var6;
		}
	}

	protected final Point getLocationOnScreenImpl(int var1, int var2) {
		return new Point(var1, var2);
	}

	protected final boolean setKeyboardVisibleImpl(boolean var1) {
		if(null != this.androidView) {
			InputMethodManager var2 = (InputMethodManager)this.getAndroidView().getContext().getSystemService("input_method");
			IBinder var3 = this.getAndroidView().getWindowToken();
			boolean var4;
			if(var1) {
				var4 = var2.showSoftInput(this.androidView, 0, this.keyboardVisibleReceiver);
			} else {
				var4 = var2.hideSoftInputFromWindow(var3, 0, this.keyboardVisibleReceiver);
			}

			return var4;
		} else {
			return false;
		}
	}

	public final void surfaceCreated(SurfaceHolder var1) {
		Log.d("JogAmp.NEWT", "surfaceCreated: win[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + "], pixels[" + " " + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + "] - on thread " + Thread.currentThread().getName());
	}

	public final void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4) {
		Log.d("JogAmp.NEWT", "surfaceChanged: f " + this.nativeFormat + " -> " + var2 + ", " + var3 + "x" + var4 + ", current surfaceHandle: 0x" + Long.toHexString(this.surfaceHandle) + " - on thread " + Thread.currentThread().getName());
		if(Window.DEBUG_IMPLEMENTATION) {
			ExceptionUtils.dumpStack(System.err);
		}

		if(0L != this.surfaceHandle && this.androidFormat != var2) {
			Log.d("JogAmp.NEWT", "surfaceChanged (destroy old)");
			if(!this.windowDestroyNotify(true)) {
				this.destroy();
			}

			this.surfaceHandle = 0L;
			this.surface = null;
		}

		if(this.getScreen().isNativeValid()) {
			MonitorDevice var5 = this.getMainMonitor();
			var5.queryCurrentMode();
		}

		if(0 > this.getX() || 0 > this.getY()) {
			this.positionChanged(false, 0, 0);
		}

		if(0L == this.surfaceHandle) {
			this.androidFormat = var2;
			this.surface = var1.getSurface();
			this.surfaceHandle = getSurfaceHandle0(this.surface);
			acquire0(this.surfaceHandle);
			int var8 = getANativeWindowFormat(this.androidFormat);
			setSurfaceVisualID0(this.surfaceHandle, var8);
			this.nativeFormat = getSurfaceVisualID0(this.surfaceHandle);
			Log.d("JogAmp.NEWT", "surfaceChanged: androidFormat " + this.androidFormat + " -- (set-native " + var8 + ") --> nativeFormat " + this.nativeFormat);
			int[] var6 = new int[]{getWidth0(this.surfaceHandle), getHeight0(this.surfaceHandle)};
			int[] var7 = this.convertToWindowUnits(new int[]{var6[0], var6[1]});
			this.capsByFormat = (GLCapabilitiesImmutable)fixCaps(true, this.nativeFormat, this.getRequestedCapabilities());
			this.sizeChanged(false, var7[0], var7[1], false);
			Log.d("JogAmp.NEWT", "surfaceRealized: isValid: " + this.surface.isValid() + ", new surfaceHandle 0x" + Long.toHexString(this.surfaceHandle) + ", format [a " + this.androidFormat + "/n " + this.nativeFormat + "], win[" + this.getX() + "/" + this.getY() + " " + var7[0] + "x" + var7[1] + "], pixel[" + var6[0] + "x" + var6[1] + "], visible: " + this.isVisible());
			if(this.isVisible()) {
				this.setVisible(false, true);
			}
		}

		this.sizeChanged(false, var3, var4, false);
		this.windowRepaint(0, 0, var3, var4);
		Log.d("JogAmp.NEWT", "surfaceChanged: X");
	}

	public final void surfaceDestroyed(SurfaceHolder var1) {
		Log.d("JogAmp.NEWT", "surfaceDestroyed - on thread " + Thread.currentThread().getName());
		this.windowDestroyNotify(true);
		ExceptionUtils.dumpStack(System.err);
	}

	public final void surfaceRedrawNeeded(SurfaceHolder var1) {
		Log.d("JogAmp.NEWT", "surfaceRedrawNeeded  - on thread " + Thread.currentThread().getName());
		this.windowRepaint(0, 0, this.getSurfaceWidth(), this.getSurfaceHeight());
	}

	protected boolean handleKeyCodeBack(DispatcherState var1, KeyEvent var2) {
		if(var2.getAction() == 0 && var2.getRepeatCount() == 0) {
			Log.d("JogAmp.NEWT", "handleKeyCodeBack.0 : " + var2);
			var1.startTracking(var2, this);
		} else if(var2.getAction() == 1 && !var2.isCanceled() && var1.isTracking(var2)) {
			boolean var3 = this.setKeyboardVisibleImpl(false);
			Log.d("JogAmp.NEWT", "handleKeyCodeBack.1 : wasVisible " + var3 + ": " + var2);
			this.keyboardVisibilityChanged(false);
			if(var3) {
				this.enqueueAKey2NKeyUpDown(var2, (short)-1793);
				return true;
			}

			if(null != this.activity) {
				this.enqueueAKey2NKeyUpDown(var2, (short)27);
				return true;
			}

			Log.d("JogAmp.NEWT", "handleKeyCodeBack.X1 : " + var2);
			this.windowDestroyNotify(true);
		}

		return false;
	}

	private void enqueueAKey2NKeyUpDown(KeyEvent var1, short var2) {
		com.jogamp.newt.event.KeyEvent var3 = AndroidNewtEventFactory.createKeyEvent(var1, var2, (short)300, this);
		com.jogamp.newt.event.KeyEvent var4 = AndroidNewtEventFactory.createKeyEvent(var1, var2, (short)301, this);
		this.enqueueEvent(false, var3);
		this.enqueueEvent(false, var4);
	}

	protected void consumeKeyEvent(com.jogamp.newt.event.KeyEvent var1) {
		super.consumeKeyEvent(var1);
		if(301 == var1.getEventType() && !var1.isConsumed()) {
			if(27 == var1.getKeyCode()) {
				Log.d("JogAmp.NEWT", "handleKeyCodeBack.X2 : " + var1);
				this.activity.finish();
			} else if(2 == var1.getKeyCode()) {
				Log.d("JogAmp.NEWT", "handleKeyCodeHome.X2 : " + var1);
				this.triggerHome();
			}
		}

	}

	private void triggerHome() {
		Context var1 = StaticContext.getContext();
		if(null == var1) {
			throw new NativeWindowException("No static [Application] Context has been set. Call StaticContext.setContext(Context) first.");
		} else {
			Intent var2 = new Intent("android.intent.action.MAIN");
			var2.addCategory("android.intent.category.HOME");
			var1.startActivity(var2);
		}
	}

	protected static native boolean initIDs0();

	protected static native long getSurfaceHandle0(Surface var0);

	protected static native int getSurfaceVisualID0(long var0);

	protected static native void setSurfaceVisualID0(long var0, int var2);

	protected static native int getWidth0(long var0);

	protected static native int getHeight0(long var0);

	protected static native void acquire0(long var0);

	protected static native void release0(long var0);

	static {
		DisplayDriver.initSingleton();
	}

	class MSurfaceView extends SurfaceView {
		public MSurfaceView(Context var2) {
			super(var2);
			this.setBackgroundDrawable((Drawable)null);
		}

		public boolean onKeyPreIme(int var1, KeyEvent var2) {
			Log.d("JogAmp.NEWT", "onKeyPreIme : " + var2);
			if(var2.getKeyCode() == 4) {
				DispatcherState var3 = this.getKeyDispatcherState();
				if(var3 != null) {
					return WindowDriver.this.handleKeyCodeBack(var3, var2);
				}
			}

			return false;
		}
	}

	private class KeyboardVisibleReceiver extends ResultReceiver {
		public KeyboardVisibleReceiver() {
			super((Handler)null);
		}

		public void onReceiveResult(int var1, Bundle var2) {
			boolean var3 = false;
			switch(var1) {
				case 0:
				case 2:
					var3 = true;
					break;
				case 1:
				case 3:
					var3 = false;
			}

			Log.d("JogAmp.NEWT", "keyboardVisible: " + var3);
			WindowDriver.this.keyboardVisibilityChanged(var3);
		}
	}
}


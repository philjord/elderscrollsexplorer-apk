//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package jogamp.newt;

import com.jogamp.common.ExceptionUtils;
import com.jogamp.common.util.ArrayHashSet;
import com.jogamp.common.util.Bitfield;
import com.jogamp.common.util.Bitfield.Factory;
import com.jogamp.common.util.PropertyAccess;
import com.jogamp.common.util.ReflectionUtil;
import com.jogamp.common.util.locks.LockFactory;
import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.CapabilitiesChooser;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.NativeWindow;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.nativewindow.OffscreenLayerSurface;
import com.jogamp.nativewindow.SurfaceUpdatedListener;
import com.jogamp.nativewindow.util.DimensionImmutable;
import com.jogamp.nativewindow.util.Insets;
import com.jogamp.nativewindow.util.InsetsImmutable;
import com.jogamp.nativewindow.util.PixelRectangle;
import com.jogamp.nativewindow.util.Point;
import com.jogamp.nativewindow.util.PointImmutable;
import com.jogamp.nativewindow.util.Rectangle;
import com.jogamp.nativewindow.util.RectangleImmutable;
import com.jogamp.newt.Display;
import com.jogamp.newt.Display.PointerIcon;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.DoubleTapScrollGesture;
import com.jogamp.newt.event.GestureHandler;
import com.jogamp.newt.event.GestureHandler.GestureEvent;
import com.jogamp.newt.event.GestureHandler.GestureListener;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MonitorEvent;
import com.jogamp.newt.event.MonitorModeListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseEvent.PointerClass;
import com.jogamp.newt.event.MouseEvent.PointerType;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.NEWTEventConsumer;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jogamp.nativewindow.SurfaceScaleUtils;
import jogamp.nativewindow.SurfaceUpdatedHelper;

/**
 * EXACT copy from 2.3.2 but a few lines removed from doPointerEvent, as they are buggy
 * as well as teh NativeWindowExceptionListener system for dealing with failed creation
 */
public abstract class WindowImpl implements Window, NEWTEventConsumer
{
	public static final boolean DEBUG_TEST_REPARENT_INCOMPATIBLE;
	private static final boolean DEBUG_FREEZE_AT_VISIBILITY_FAILURE;
	protected static final ArrayList<WeakReference<WindowImpl>> windowList;
	static final long QUEUED_EVENT_TO = 1200L;
	private static final PointerType[] constMousePointerTypes;
	private volatile long windowHandle = 0L;
	private volatile int pixWidth = 128;
	private volatile int pixHeight = 128;
	private volatile int winWidth = 128;
	private volatile int winHeight = 128;
	protected final float[] minPixelScale = new float[]{1.0F, 1.0F};
	protected final float[] maxPixelScale = new float[]{1.0F, 1.0F};
	protected final float[] hasPixelScale = new float[]{1.0F, 1.0F};
	protected final float[] reqPixelScale = new float[]{0.0F, 0.0F};
	private volatile int x = 64;
	private volatile int y = 64;
	private volatile Insets insets = new Insets();
	private boolean blockInsetsChange = false;
	private final RecursiveLock windowLock = LockFactory.createRecursiveLock();
	private int surfaceLockCount = 0;
	private ScreenImpl screen;
	private boolean screenReferenceAdded = false;
	private NativeWindow parentWindow = null;
	private long parentWindowHandle = 0L;
	private AbstractGraphicsConfiguration config = null;
	protected CapabilitiesImmutable capsRequested = null;
	protected CapabilitiesChooser capabilitiesChooser = null;
	private List<MonitorDevice> fullscreenMonitors = null;
	private int nfs_width;
	private int nfs_height;
	private int nfs_x;
	private int nfs_y;
	private NativeWindow nfs_parent = null;
	private String title = "Newt Window";
	private PointerIconImpl pointerIcon = null;
	private WindowImpl.LifecycleHook lifecycleHook = null;
	protected static final int QUIRK_BIT_VISIBILITY = 0;
	protected static final Bitfield quirks;
	protected static final int STATE_BIT_COUNT_ALL_PUBLIC = 14;
	protected static final int STATE_MASK_ALL_PUBLIC = 16383;
	protected static final int STATE_BIT_FULLSCREEN_SPAN = 14;
	protected static final int STATE_BIT_COUNT_ALL_RECONFIG = 15;
	protected static final int STATE_MASK_ALL_RECONFIG = 32767;
	protected static final int STATE_MASK_ALL_PUBLIC_SUPPORTED = 16381;
	static final int PSTATE_BIT_FOCUS_CHANGE_BROKEN = 30;
	static final int PSTATE_BIT_FULLSCREEN_MAINMONITOR = 31;
	static final int STATE_MASK_FULLSCREEN_SPAN = 16384;
	static final int PSTATE_MASK_FOCUS_CHANGE_BROKEN = 1073741824;
	static final int PSTATE_MASK_FULLSCREEN_MAINMONITOR = -2147483648;
	private static final int STATE_MASK_FULLSCREEN_NFS = 1824;
	protected static final int STATE_MASK_CREATENATIVE = 2047;
	protected static final int CHANGE_MASK_VISIBILITY = -2147483648;
	protected static final int CHANGE_MASK_VISIBILITY_FAST = 1073741824;
	protected static final int CHANGE_MASK_PARENTING = 536870912;
	protected static final int CHANGE_MASK_DECORATION = 268435456;
	protected static final int CHANGE_MASK_ALWAYSONTOP = 134217728;
	protected static final int CHANGE_MASK_ALWAYSONBOTTOM = 67108864;
	protected static final int CHANGE_MASK_STICKY = 33554432;
	protected static final int CHANGE_MASK_RESIZABLE = 16777216;
	protected static final int CHANGE_MASK_MAXIMIZED_VERT = 8388608;
	protected static final int CHANGE_MASK_MAXIMIZED_HORZ = 4194304;
	protected static final int CHANGE_MASK_FULLSCREEN = 2097152;
	final Bitfield stateMask = Factory.synchronize(Factory.create(32));
	private final Bitfield stateMaskNFS = Factory.synchronize(Factory.create(32));
	protected int supportedReconfigStateMask = 0;
	protected static final int minimumReconfigStateMask = 2057;
	private Runnable windowDestroyNotifyAction = null;
	private FocusRunnable focusAction = null;
	private KeyListener keyboardFocusHandler = null;
	private final SurfaceUpdatedHelper surfaceUpdatedHelper = new SurfaceUpdatedHelper();
	private final Object childWindowsLock = new Object();
	private final ArrayList<NativeWindow> childWindows = new ArrayList();
	private ArrayList<MouseListener> mouseListeners = new ArrayList();
	private final WindowImpl.PointerState0 pState0 = new WindowImpl.PointerState0();
	private final WindowImpl.PointerState1 pState1 = new WindowImpl.PointerState1();
	private final ArrayHashSet<Integer> pName2pID = new ArrayHashSet(false, 16, 0.75F);
	private boolean defaultGestureHandlerEnabled = true;
	private DoubleTapScrollGesture gesture2PtrTouchScroll = null;
	private ArrayList<GestureHandler> pointerGestureHandler = new ArrayList();
	private ArrayList<GestureListener> gestureListeners = new ArrayList();
	private ArrayList<KeyListener> keyListeners = new ArrayList();
	private ArrayList<WindowListener> windowListeners = new ArrayList();
	private boolean repaintQueued = false;
	private final Object closingListenerLock = new Object();
	private WindowClosingMode defaultCloseOperation;
	private final Runnable destroyAction;
	private final Runnable reparentActionRecreate;
	private final int[] normPosSize;
	private final boolean[] normPosSizeStored;
	private final Runnable requestFocusAction;
	private final Runnable requestFocusActionForced;
	private final WindowImpl.FullScreenAction fullScreenAction;
	private final WindowImpl.MonitorModeListenerImpl monitorModeListenerImpl;
	private static final int keyTrackingRange = 255;
	private final Bitfield keyPressedState;
	protected boolean keyboardVisible;

	public WindowImpl() {
		this.defaultCloseOperation = WindowClosingMode.DISPOSE_ON_CLOSE;
		this.destroyAction = new Runnable() {
			public final void run() {
				boolean var1 = false;
				if(null != WindowImpl.this.lifecycleHook) {
					var1 = WindowImpl.this.lifecycleHook.pauseRenderingAction();
				}

				if(null != WindowImpl.this.lifecycleHook) {
					WindowImpl.this.lifecycleHook.destroyActionPreLock();
				}

				RuntimeException var2 = null;
				RecursiveLock var3 = WindowImpl.this.windowLock;
				var3.lock();

				try {
					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window DestroyAction() hasScreen " + (null != WindowImpl.this.screen) + ", isNativeValid " + WindowImpl.this.isNativeValid() + " - " + Display.getThreadName());
					}

					WindowImpl.this.sendWindowEvent(102);
					synchronized(WindowImpl.this.childWindowsLock) {
						if(WindowImpl.this.childWindows.size() > 0) {
							ArrayList var5 = (ArrayList)WindowImpl.this.childWindows.clone();

							while(var5.size() > 0) {
								NativeWindow var6 = (NativeWindow)var5.remove(0);
								if(var6 instanceof WindowImpl) {
									((WindowImpl)var6).windowDestroyNotify(true);
								} else {
									var6.destroy();
								}
							}
						}
					}

					if(null != WindowImpl.this.lifecycleHook) {
						try {
							WindowImpl.this.lifecycleHook.destroyActionInLock();
						} catch (RuntimeException var12) {
							var2 = var12;
						}
					}

					if(WindowImpl.this.isNativeValid()) {
						WindowImpl.this.screen.removeMonitorModeListener(WindowImpl.this.monitorModeListenerImpl);
						WindowImpl.this.closeNativeImpl();
						AbstractGraphicsDevice var4 = WindowImpl.this.config.getScreen().getDevice();
						if(var4 != WindowImpl.this.screen.getDisplay().getGraphicsDevice()) {
							var4.close();
						}

						WindowImpl.this.setGraphicsConfiguration((AbstractGraphicsConfiguration)null);
					}

					WindowImpl.this.removeScreenReference();
					Display var15 = WindowImpl.this.screen.getDisplay();
					if(null != var15) {
						var15.validateEDTStopped();
					}

					WindowImpl.this.sendWindowEvent(106);
					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window.destroy() END " + Display.getThreadName());
						if(null != var2) {
							System.err.println("Window.destroy() caught: " + var2.getMessage());
							var2.printStackTrace();
						}
					}

					if(null != var2) {
						throw var2;
					}
				} finally {
					WindowImpl.this.setWindowHandle(0L);
					WindowImpl.this.resetStateMask();
					WindowImpl.this.fullscreenMonitors = null;
					WindowImpl.this.parentWindowHandle = 0L;
					WindowImpl.this.hasPixelScale[0] = 1.0F;
					WindowImpl.this.hasPixelScale[1] = 1.0F;
					WindowImpl.this.minPixelScale[0] = 1.0F;
					WindowImpl.this.minPixelScale[1] = 1.0F;
					WindowImpl.this.maxPixelScale[0] = 1.0F;
					WindowImpl.this.maxPixelScale[1] = 1.0F;
					var3.unlock();
				}

				if(var1) {
					WindowImpl.this.lifecycleHook.resumeRenderingAction();
				}

			}
		};
		this.reparentActionRecreate = new Runnable() {
			public final void run() {
				RecursiveLock var1 = WindowImpl.this.windowLock;
				var1.lock();

				try {
					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window.reparent: ReparentActionRecreate (" + Display.getThreadName() + ") state " + WindowImpl.this.getStateMaskString() + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + ", parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + ", parentWindow " + Display.hashCodeNullSafe(WindowImpl.this.parentWindow));
					}

					WindowImpl.this.setVisibleActionImpl(true);
					WindowImpl.this.requestFocusInt(0L == WindowImpl.this.parentWindowHandle);
				} finally {
					var1.unlock();
				}

			}
		};
		this.normPosSize = new int[]{0, 0, 0, 0};
		this.normPosSizeStored = new boolean[]{false, false};
		this.requestFocusAction = new Runnable() {
			public final void run() {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window.RequestFocusAction: force 0 - (" + Display.getThreadName() + "): state " + WindowImpl.this.getStateMaskString() + " -> focus true - windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle));
				}

				WindowImpl.this.requestFocusImpl(false);
			}
		};
		this.requestFocusActionForced = new Runnable() {
			public final void run() {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window.RequestFocusAction: force 1 - (" + Display.getThreadName() + "): state " + WindowImpl.this.getStateMaskString() + " -> focus true - windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle));
				}

				WindowImpl.this.requestFocusImpl(true);
			}
		};
		this.fullScreenAction = new WindowImpl.FullScreenAction();
		this.monitorModeListenerImpl = new WindowImpl.MonitorModeListenerImpl();
		this.keyPressedState = Factory.create(256);
		this.keyboardVisible = false;
	}

	public static final void shutdownAll() {
		int var0 = windowList.size();
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.shutdownAll " + var0 + " instances, on thread " + getThreadName());
		}

		for(int var1 = 0; var1 < var0 && windowList.size() > 0; ++var1) {
			WindowImpl var2 = (WindowImpl)((WeakReference)windowList.remove(0)).get();
			if(DEBUG_IMPLEMENTATION) {
				long var3 = null != var2?var2.getWindowHandle():0L;
				System.err.println("Window.shutdownAll[" + (var1 + 1) + "/" + var0 + "]: " + toHexString(var3) + ", GCed " + (null == var2));
			}

			if(null != var2) {
				var2.shutdown();
			}
		}

	}

	private static void addWindow2List(WindowImpl var0) {
		ArrayList var1 = windowList;
		synchronized(windowList) {
			int var2 = 0;
			int var3 = 0;

			while(var2 < windowList.size()) {
				if(null == ((WeakReference)windowList.get(var2)).get()) {
					++var3;
					windowList.remove(var2);
				} else {
					++var2;
				}
			}

			windowList.add(new WeakReference(var0));
			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.addWindow2List: GCed " + var3 + ", size " + windowList.size());
			}

		}
	}

	final void resetStateMask() {
		this.stateMask.clearField(false);
		this.stateMask.put32(0, 32, 2 | (null != this.parentWindow?4:0) | 256 | 4096 | -2147483648);
		this.stateMaskNFS.clearField(false);
		this.normPosSizeStored[0] = false;
		this.normPosSizeStored[1] = false;
		this.supportedReconfigStateMask = 32767;
	}

	public final int getStatePublicBitCount() {
		return 14;
	}

	public final int getStatePublicBitmask() {
		return 16383;
	}

	public final int getStateMask() {
		return this.stateMask.get32(0, 14);
	}

	public final String getStateMaskString() {
		return appendStateBits(new StringBuilder(), this.stateMask.get32(0, 14), false).toString();
	}

	public final int getSupportedStateMask() {
		return this.supportedReconfigStateMask & 16381;
	}

	public final String getSupportedStateMaskString() {
		return appendStateBits(new StringBuilder(), this.getSupportedStateMask(), true).toString();
	}

	protected static StringBuilder appendStateBits(StringBuilder var0, int var1, boolean var2) {
		var0.append("[");
		if(var2) {
			if(0 != (-2147483648 & var1)) {
				var0.append("*");
			}

			if(0 != (1073741824 & var1)) {
				var0.append("*");
			}
		}

		var0.append(0 != (1 & var1)?"visible":"invisible");
		var0.append(", ");
		var0.append(0 != (2 & var1)?"autopos, ":"");
		if(var2) {
			if(0 != (536870912 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (4 & var1)?"child":"toplevel");
			var0.append(", ");
		} else if(0 != (4 & var1)) {
			var0.append("child");
			var0.append(", ");
		}

		var0.append(0 != (8 & var1)?"focused, ":"");
		if(var2) {
			if(0 != (268435456 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (16 & var1)?"undecor":"decor");
			var0.append(", ");
		} else if(0 != (16 & var1)) {
			var0.append("undecor");
			var0.append(", ");
		}

		if(var2) {
			if(0 != (134217728 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (32 & var1)?"aontop":"!aontop");
			var0.append(", ");
		} else if(0 != (32 & var1)) {
			var0.append("aontop");
			var0.append(", ");
		}

		if(var2) {
			if(0 != (67108864 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (64 & var1)?"aonbottom":"!aonbottom");
			var0.append(", ");
		} else if(0 != (64 & var1)) {
			var0.append("aonbottom");
			var0.append(", ");
		}

		if(var2) {
			if(0 != (33554432 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (128 & var1)?"sticky":"unsticky");
			var0.append(", ");
		} else if(0 != (128 & var1)) {
			var0.append("sticky");
			var0.append(", ");
		}

		if(var2) {
			if(0 != (16777216 & var1)) {
				var0.append("*");
			}

			var0.append(0 != (256 & var1)?"resizable":"unresizable");
			var0.append(", ");
		} else if(0 == (256 & var1)) {
			var0.append("unresizable");
			var0.append(", ");
		}

		if(var2) {
			var0.append("max[");
			if(0 != (4194304 & var1)) {
				var0.append("*");
			}

			if(0 == (1024 & var1)) {
				var0.append("!");
			}

			var0.append("h");
			var0.append(", ");
			if(0 != (8388608 & var1)) {
				var0.append("*");
			}

			if(0 == (512 & var1)) {
				var0.append("!");
			}

			var0.append("v");
			var0.append("], ");
		} else if(0 != (1536 & var1)) {
			var0.append("max[");
			if(0 != (1024 & var1)) {
				var0.append("h");
			}

			if(0 != (512 & var1)) {
				var0.append("v");
			}

			var0.append("], ");
		}

		if(var2) {
			if(0 != (2097152 & var1)) {
				var0.append("*");
			}

			var0.append("fullscreen[");
			var0.append(0 != (2048 & var1));
			var0.append(0 != (16384 & var1)?", span":"");
			var0.append("], ");
		} else if(0 != (2048 & var1)) {
			var0.append("fullscreen");
			var0.append(", ");
		}

		if(var2) {
			var0.append("pointer[");
			if(0 == (4096 & var1)) {
				var0.append("invisible");
			} else {
				var0.append("visible");
			}

			var0.append(", ");
			if(0 != (8192 & var1)) {
				var0.append("confined");
			} else {
				var0.append("free");
			}

			var0.append("]");
		} else if(0 == (4096 & var1) || 0 != (8192 & var1)) {
			var0.append("pointer[");
			if(0 == (4096 & var1)) {
				var0.append("invisible");
				var0.append(", ");
			}

			if(0 != (8192 & var1)) {
				var0.append("confined");
			}

			var0.append("]");
		}

		var0.append("]");
		return var0;
	}

	private static Class<?> getWindowClass(String var0) throws ClassNotFoundException {
		Class var1 = NewtFactory.getCustomClass(var0, "WindowDriver");
		if(null == var1) {
			throw new ClassNotFoundException("Failed to find NEWT Window Class <" + var0 + ".WindowDriver>");
		} else {
			return var1;
		}
	}

	public static WindowImpl create(NativeWindow var0, long var1, Screen var3, CapabilitiesImmutable var4) {
		try {
			Class var5;
			if(var4.isOnscreen()) {
				var5 = getWindowClass(var3.getDisplay().getType());
			} else {
				var5 = OffscreenWindow.class;
			}

			WindowImpl var6 = (WindowImpl)var5.newInstance();
			var6.parentWindow = var0;
			var6.parentWindowHandle = var1;
			var6.screen = (ScreenImpl)var3;
			var6.capsRequested = (CapabilitiesImmutable)var4.cloneMutable();
			var6.instantiationFinished();
			addWindow2List(var6);
			return var6;
		} catch (Throwable var7) {
			var7.printStackTrace();
			throw new NativeWindowException(var7);
		}
	}

	public static WindowImpl create(Object[] var0, Screen var1, CapabilitiesImmutable var2) {
		try {
			Class var3 = getWindowClass(var1.getDisplay().getType());
			Class[] var4 = getCustomConstructorArgumentTypes(var3);
			if(null == var4) {
				throw new NativeWindowException("WindowClass " + var3 + " doesn\'t support custom arguments in constructor");
			} else {
				int var5 = verifyConstructorArgumentTypes(var4, var0);
				if(var5 < var0.length) {
					throw new NativeWindowException("WindowClass " + var3 + " constructor mismatch at argument #" + var5 + "; Constructor: " + getTypeStrList(var4) + ", arguments: " + getArgsStrList(var0));
				} else {
					WindowImpl var6 = (WindowImpl) ReflectionUtil.createInstance(var3, var4, var0);
					var6.screen = (ScreenImpl)var1;
					var6.capsRequested = (CapabilitiesImmutable)var2.cloneMutable();
					var6.instantiationFinished();
					addWindow2List(var6);
					return var6;
				}
			}
		} catch (Throwable var7) {
			throw new NativeWindowException(var7);
		}
	}

	private final void shutdown() {
		if(null != this.lifecycleHook) {
			this.lifecycleHook.shutdownRenderingAction();
		}

		this.setWindowHandle(0L);
		this.resetStateMask();
		this.fullscreenMonitors = null;
		this.parentWindowHandle = 0L;
	}

	protected final void setGraphicsConfiguration(AbstractGraphicsConfiguration var1) {
		this.config = var1;
	}

	private boolean createNative() {
		long var1;
		if(DEBUG_IMPLEMENTATION) {
			var1 = System.nanoTime();
			System.err.println("Window.createNative() START (" + getThreadName() + ", " + this + ")");
		} else {
			var1 = 0L;
		}

		if(null != this.parentWindow && 1 >= this.parentWindow.lockSurface()) {
			throw new NativeWindowException("Parent surface lock: not ready: " + this.parentWindow);
		} else {
			boolean var3 = null != this.parentWindow || 0L != this.parentWindowHandle;
			if(var3 && (this.stateMask.get(1) || 0 > this.getX() || 0 > this.getY())) {
				this.definePosition(0, 0);
			}

			boolean var4 = false;

			try {
				if(this.validateParentWindowHandle()) {
					if(!this.screenReferenceAdded) {
						this.screen.addReference();
						this.screenReferenceAdded = true;
					}

					if(this.canCreateNativeImpl()) {
						int var5;
						int var6;
						boolean var7;
						if(this.stateMask.get(1)) {
							var5 = 0;
							var6 = 0;
							var7 = false;
						} else {
							var5 = this.getX();
							var6 = this.getY();
							var7 = true;
						}

						long var8 = System.currentTimeMillis();

						this.createNativeImpl();
						this.supportedReconfigStateMask = this.getSupportedReconfigMaskImpl() & 32767;
						if(DEBUG_IMPLEMENTATION) {
							boolean var10 = 2057 == (2057 & this.supportedReconfigStateMask);
							System.err.println("Supported Reconfig (minimum-ok " + var10 + "): " + appendStateBits(new StringBuilder(), this.supportedReconfigStateMask, true).toString());
						}

						this.screen.addMonitorModeListener(this.monitorModeListenerImpl);
						this.setTitleImpl(this.title);
						this.setPointerIconIntern(this.pointerIcon);
						if(!this.stateMask.get(12)) {
							if(this.isReconfigureMaskSupported(4096)) {
								this.setPointerVisibleIntern(this.stateMask.get(12));
							} else {
								this.stateMask.set(12);
							}
						}

						if(this.stateMask.get(13)) {
							if(this.isReconfigureMaskSupported(8192)) {
								this.confinePointerImpl(true);
							} else {
								this.stateMask.clear(13);
							}
						}

						this.setKeyboardVisible(this.keyboardVisible);
						long var19 = this.waitForVisible(true, false);
						if(0L <= var19) {
							if(this.stateMask.get(11) && !this.isReconfigureMaskSupported(2048)) {
								this.stateMask.clear(11);
							}

							if(this.stateMask.get(11)) {
								WindowImpl.FullScreenAction var12 = this.fullScreenAction;
								synchronized(this.fullScreenAction) {
									this.stateMask.clear(11);
									this.fullScreenAction.init(true);
									this.fullScreenAction.run();
								}
							} else if(!var3) {
								this.waitForPosition(var7, var5, var6, 1000L);
							}

							if(DEBUG_IMPLEMENTATION) {
								System.err.println("Window.createNative(): elapsed " + (System.currentTimeMillis() - var8) + " ms");
							}

							var4 = true;
						}
					}
				}
			}
			//PJPJPJPJPJPJ when the device is out of resources we allow a registered callback to deal with it
			catch(NativeWindowException e)
			{
				boolean doThrow = true;
				if(nativeWindowExceptionListener != null)
				{
					doThrow = !nativeWindowExceptionListener.handleException(e);
				}
				if(doThrow)
					throw e;
			}
			finally {
				if(null != this.parentWindow) {
					this.parentWindow.unlockSurface();
				}

			}

			if(var4) {
				this.requestFocusInt(this.isFullscreen());
				((DisplayImpl)this.screen.getDisplay()).dispatchMessagesNative();
			}

			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.createNative() END (" + getThreadName() + ", " + this + ") total " + (double)(System.nanoTime() - var1) / 1000000.0D + "ms");
			}

			return this.isNativeValid();
		}
	}

	private NativeWindowExceptionListener nativeWindowExceptionListener = null;
	public void setNativeWindowExceptionListener(NativeWindowExceptionListener nativeWindowExceptionListener)
	{
		this.nativeWindowExceptionListener = nativeWindowExceptionListener;
	}
	public interface NativeWindowExceptionListener
	{
		// return true to indicate success, false will throw the exception
		boolean handleException(NativeWindowException nwp);
	}

	private void removeScreenReference() {
		if(this.screenReferenceAdded) {
			this.screenReferenceAdded = false;
			this.screen.removeReference();
		}

	}

	private boolean validateParentWindowHandle() {
		if(null != this.parentWindow) {
			this.parentWindowHandle = getNativeWindowHandle(this.parentWindow);
			return 0L != this.parentWindowHandle;
		} else {
			return true;
		}
	}

	private static long getNativeWindowHandle(NativeWindow var0) {
		long var1 = 0L;
		if(null != var0) {
			boolean var3 = false;
			if(1 < var0.lockSurface()) {
				var3 = true;

				try {
					var1 = var0.getWindowHandle();
					if(0L == var1) {
						throw new NativeWindowException("Parent native window handle is NULL, after succesful locking: " + var0);
					}
				} catch (NativeWindowException var8) {
					if(DEBUG_IMPLEMENTATION) {
						System.err.println("Window.getNativeWindowHandle: not successful yet: " + var8);
					}
				} finally {
					var0.unlockSurface();
				}
			}

			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.getNativeWindowHandle: locked " + var3 + ", " + var0);
			}
		}

		return var1;
	}

	protected int lockSurfaceImpl() {
		return 3;
	}

	protected void unlockSurfaceImpl() {
	}

	public final WindowClosingMode getDefaultCloseOperation() {
		Object var1 = this.closingListenerLock;
		synchronized(this.closingListenerLock) {
			return this.defaultCloseOperation;
		}
	}

	public final WindowClosingMode setDefaultCloseOperation(WindowClosingMode var1) {
		Object var2 = this.closingListenerLock;
		synchronized(this.closingListenerLock) {
			WindowClosingMode var3 = this.defaultCloseOperation;
			this.defaultCloseOperation = var1;
			return var3;
		}
	}

	private final void instantiationFinished() {
		this.resetStateMask();
		this.instantiationFinishedImpl();
	}

	protected void instantiationFinishedImpl() {
	}

	protected boolean canCreateNativeImpl() {
		return true;
	}

	protected abstract void createNativeImpl();

	protected abstract void closeNativeImpl();

	protected abstract void requestFocusImpl(boolean var1);

	protected abstract int getSupportedReconfigMaskImpl();

	protected abstract boolean reconfigureWindowImpl(int var1, int var2, int var3, int var4, int var5);

	protected final boolean isReconfigureMaskSupported(int var1) {
		return var1 == (var1 & this.supportedReconfigStateMask);
	}

	protected int getReconfigureMask(int var1, boolean var2) {
		int var3 = this.stateMask.get32(0, 15);
		return var1 | var3 & -22 | (var2?1:0) | (isUndecorated(var3)?16:0) | (0L != this.getParentWindowHandle()?4:0);
	}

	protected static String getReconfigStateMaskString(int var0) {
		return appendStateBits(new StringBuilder(), var0, true).toString();
	}

	protected void setTitleImpl(String var1) {
	}

	protected abstract Point getLocationOnScreenImpl(int var1, int var2);

	protected boolean setPointerVisibleImpl(boolean var1) {
		return false;
	}

	protected boolean confinePointerImpl(boolean var1) {
		return false;
	}

	protected void warpPointerImpl(int var1, int var2) {
	}

	protected void setPointerIconImpl(PointerIconImpl var1) {
	}

	public final int lockSurface() throws NativeWindowException, RuntimeException {
		RecursiveLock var1 = this.windowLock;
		var1.lock();
		++this.surfaceLockCount;
		int var2 = 1 == this.surfaceLockCount?1:3;
		if(1 == var2) {
			try {
				if(this.isNativeValid()) {
					AbstractGraphicsDevice var3 = this.getGraphicsConfiguration().getScreen().getDevice();
					var3.lock();

					try {
						var2 = this.lockSurfaceImpl();
					} finally {
						if(1 >= var2) {
							var3.unlock();
						}

					}
				}
			} finally {
				if(1 >= var2) {
					--this.surfaceLockCount;
					var1.unlock();
				}

			}
		}

		return var2;
	}

	public final void unlockSurface() {
		RecursiveLock var1 = this.windowLock;
		var1.validateLocked();
		if(1 == this.surfaceLockCount) {
			AbstractGraphicsDevice var2 = this.getGraphicsConfiguration().getScreen().getDevice();

			try {
				this.unlockSurfaceImpl();
			} finally {
				var2.unlock();
			}
		}

		--this.surfaceLockCount;
		var1.unlock();
	}

	public final boolean isSurfaceLockedByOtherThread() {
		return this.windowLock.isLockedByOtherThread();
	}

	public final Thread getSurfaceLockOwner() {
		return this.windowLock.getOwner();
	}

	public final RecursiveLock getLock() {
		return this.windowLock;
	}

	public long getSurfaceHandle() {
		return this.windowHandle;
	}

	public boolean surfaceSwap() {
		return false;
	}

	public final void addSurfaceUpdatedListener(SurfaceUpdatedListener var1) {
		this.surfaceUpdatedHelper.addSurfaceUpdatedListener(var1);
	}

	public final void addSurfaceUpdatedListener(int var1, SurfaceUpdatedListener var2) throws IndexOutOfBoundsException {
		this.surfaceUpdatedHelper.addSurfaceUpdatedListener(var1, var2);
	}

	public final void removeSurfaceUpdatedListener(SurfaceUpdatedListener var1) {
		this.surfaceUpdatedHelper.removeSurfaceUpdatedListener(var1);
	}

	public final void surfaceUpdated(Object var1, NativeSurface var2, long var3) {
		this.surfaceUpdatedHelper.surfaceUpdated(var1, var2, var3);
	}

	public final AbstractGraphicsConfiguration getGraphicsConfiguration() {
		return this.config.getNativeGraphicsConfiguration();
	}

	public final long getDisplayHandle() {
		return this.config.getNativeGraphicsConfiguration().getScreen().getDevice().getHandle();
	}

	public final int getScreenIndex() {
		return this.screen.getIndex();
	}

	public final NativeSurface getNativeSurface() {
		return this;
	}

	public final NativeWindow getParent() {
		return this.parentWindow;
	}

	public final long getWindowHandle() {
		return this.windowHandle;
	}

	public Point getLocationOnScreen(Point var1) {
		if(this.isNativeValid()) {
			RecursiveLock var3 = this.windowLock;
			var3.lock();

			Point var2;
			try {
				var2 = this.getLocationOnScreenImpl(0, 0);
			} finally {
				var3.unlock();
			}

			if(null != var2) {
				if(null != var1) {
					var1.translate(var2.getX(), var2.getY());
					return var1;
				}

				return var2;
			}
		}

		if(null != var1) {
			var1.translate(this.getX(), this.getY());
		} else {
			var1 = new Point(this.getX(), this.getY());
		}

		if(null != this.parentWindow) {
			this.parentWindow.getLocationOnScreen(var1);
		}

		return var1;
	}

	public final boolean isNativeValid() {
		return 0L != this.windowHandle;
	}

	public final Screen getScreen() {
		return this.screen;
	}

	protected void setScreen(ScreenImpl var1) {
		this.removeScreenReference();
		this.screen = var1;
	}

	public final MonitorDevice getMainMonitor() {
		return this.screen.getMainMonitor(this.getBounds());
	}

	protected final void setVisibleImpl(boolean var1, boolean var2, int var3, int var4, int var5, int var6) {
		int var7;
		if(var2) {
			var7 = this.getReconfigureMask(-1073741824, var1);
		} else {
			var7 = this.getReconfigureMask(-2147483648, var1);
		}

		this.reconfigureWindowImpl(var3, var4, var5, var6, var7);
	}

	final void setVisibleActionImpl(boolean var1) {
		boolean var2 = false;
		int var3 = -1;
		RecursiveLock var4 = this.windowLock;
		var4.lock();

		try {
			Object var5;
			int var6;
			NativeWindow var7;
			if(!var1 && null != this.childWindows && this.childWindows.size() > 0) {
				var5 = this.childWindowsLock;
				synchronized(this.childWindowsLock) {
					for(var6 = 0; var6 < this.childWindows.size(); ++var6) {
						var7 = (NativeWindow)this.childWindows.get(var6);
						if(var7 instanceof WindowImpl) {
							((WindowImpl)var7).setVisible(false);
						}
					}
				}
			}

			if(!this.isNativeValid() && var1) {
				if(0 < this.getWidth() * this.getHeight()) {
					var2 = this.createNative();
					var3 = var2?1:-1;
				}

				this.stateMask.set(0);
			} else if(this.stateMask.get(0) != var1) {
				if(!this.isNativeValid()) {
					this.stateMask.set(0);
				} else {
					boolean var17 = quirks.get(0);
					this.setVisibleImpl(var1, var17 || this.isChildWindow(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
					if(0L > this.waitForVisible(var1, false)) {
						if(!var17) {
							quirks.set(0);
							if(DEBUG_IMPLEMENTATION) {
								System.err.println("Setting VISIBILITY QUIRK, due to setVisible(" + var1 + ") failure");
							}

							this.setVisibleImpl(var1, true, this.getX(), this.getY(), this.getWidth(), this.getHeight());
							if(0L <= this.waitForVisible(var1, false)) {
								var3 = var1?1:0;
							}
						}
					} else {
						var3 = var1?1:0;
					}
				}
			}

			if(null != this.lifecycleHook) {
				this.lifecycleHook.setVisibleActionPost(var1, var2);
			}

			if(this.isNativeValid() && var1 && null != this.childWindows && this.childWindows.size() > 0) {
				var5 = this.childWindowsLock;
				synchronized(this.childWindowsLock) {
					for(var6 = 0; var6 < this.childWindows.size(); ++var6) {
						var7 = (NativeWindow)this.childWindows.get(var6);
						if(var7 instanceof WindowImpl) {
							((WindowImpl)var7).setVisible(true);
						}
					}
				}
			}

			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window setVisible: END (" + getThreadName() + ") state " + this.getStateMaskString() + ", nativeWindowCreated: " + var2 + ", madeVisible: " + var3 + ", geom " + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + ", windowHandle " + toHexString(this.windowHandle));
			}
		} finally {
			if(null != this.lifecycleHook) {
				this.lifecycleHook.resetCounter();
			}

			var4.unlock();
		}

		if(var2 || 1 == var3) {
			this.sendWindowEvent(100);
		}

	}

	public final void setVisible(boolean var1, boolean var2) {
		if(this.isReconfigureMaskSupported(1) || !this.isNativeValid()) {
			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window setVisible: START (" + getThreadName() + ") " + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + ", windowHandle " + toHexString(this.windowHandle) + ", state " + this.getStateMaskString() + " -> visible " + var2 + ", parentWindowHandle " + toHexString(this.parentWindowHandle) + ", parentWindow " + (null != this.parentWindow));
			}

			this.runOnEDTIfAvail(var1, new WindowImpl.VisibleAction(var2));
		}
	}

	public final void setVisible(boolean var1) {
		this.setVisible(true, var1);
	}

	private void setSize(int var1, int var2, boolean var3) {
		this.runOnEDTIfAvail(true, new WindowImpl.SetSizeAction(var1, var2, var3));
	}

	public final void setSize(int var1, int var2) {
		this.runOnEDTIfAvail(true, new WindowImpl.SetSizeAction(var1, var2, false));
	}

	public final void setSurfaceSize(int var1, int var2) {
		this.setSize(SurfaceScaleUtils.scaleInv(var1, this.getPixelScaleX()), SurfaceScaleUtils.scaleInv(var2, this.getPixelScaleY()));
	}

	public final void setTopLevelSize(int var1, int var2) {
		InsetsImmutable var3 = this.getInsets();
		this.setSize(var1 - var3.getTotalWidth(), var2 - var3.getTotalHeight());
	}

	public void destroy() {
		this.stateMask.clear(0);
		this.runOnEDTIfAvail(true, this.destroyAction);
	}

	protected void destroy(boolean var1) {
		if(null != this.lifecycleHook) {
			this.lifecycleHook.preserveGLStateAtDestroy(var1);
		}

		this.destroy();
	}

	protected static boolean isOffscreenInstance(NativeWindow var0, NativeWindow var1) {
		boolean var2 = false;
		AbstractGraphicsConfiguration var3 = var0.getGraphicsConfiguration();
		if(null != var3) {
			var2 = !var3.getChosenCapabilities().isOnscreen();
		}

		if(!var2 && null != var1) {
			AbstractGraphicsConfiguration var4 = var1.getGraphicsConfiguration();
			if(null != var4) {
				var2 = !var4.getChosenCapabilities().isOnscreen();
			}
		}

		return var2;
	}

	public final ReparentOperation reparentWindow(NativeWindow var1, int var2, int var3, int var4) {
		if(!this.isReconfigureMaskSupported(4) && this.isNativeValid()) {
			return ReparentOperation.ACTION_INVALID;
		} else {
			WindowImpl.ReparentAction var5 = new WindowImpl.ReparentAction(var1, var2, var3, var4);
			this.runOnEDTIfAvail(true, var5);
			return var5.getOp();
		}
	}

	public final boolean isChildWindow() {
		return this.stateMask.get(2);
	}

	public final CapabilitiesChooser setCapabilitiesChooser(CapabilitiesChooser var1) {
		CapabilitiesChooser var2 = this.capabilitiesChooser;
		this.capabilitiesChooser = var1;
		return var2;
	}

	public final CapabilitiesImmutable getChosenCapabilities() {
		return this.getGraphicsConfiguration().getChosenCapabilities();
	}

	public final CapabilitiesImmutable getRequestedCapabilities() {
		return this.capsRequested;
	}

	public final void setUndecorated(boolean var1) {
		if(this.isNativeValid()) {
			if(!this.isReconfigureMaskSupported(16)) {
				return;
			}

			if(this.isFullscreen()) {
				this.stateMaskNFS.put(16, var1);
				return;
			}
		}

		this.runOnEDTIfAvail(true, new WindowImpl.DecorationAction(var1));
	}

	public final boolean isUndecorated() {
		return isUndecorated(this.getStateMask());
	}

	private static final boolean isUndecorated(int var0) {
		return 0 != (var0 & 2068);
	}

	public final void setAlwaysOnTop(boolean var1) {
		if(!this.isChildWindow()) {
			if(this.isNativeValid()) {
				if(!this.isReconfigureMaskSupported(32)) {
					return;
				}

				if(this.isFullscreen()) {
					if(var1 && this.isAlwaysOnBottom()) {
						this.setAlwaysOnBottom(false);
					}

					this.stateMaskNFS.put(5, var1);
					return;
				}
			}

			if(var1 && this.isAlwaysOnBottom()) {
				this.setAlwaysOnBottom(false);
			}

			this.runOnEDTIfAvail(true, new WindowImpl.AlwaysOnTopAction(var1));
		}
	}

	public final boolean isAlwaysOnTop() {
		return this.stateMask.get(5);
	}

	public final void setAlwaysOnBottom(boolean var1) {
		if(!this.isChildWindow()) {
			if(this.isReconfigureMaskSupported(64) || !this.isNativeValid()) {
				if(var1 && this.isAlwaysOnTop()) {
					this.setAlwaysOnTop(false);
				}

				this.runOnEDTIfAvail(true, new WindowImpl.AlwaysOnBottomAction(var1));
			}
		}
	}

	public final boolean isAlwaysOnBottom() {
		return this.stateMask.get(6);
	}

	public final void setResizable(boolean var1) {
		if(!this.isChildWindow()) {
			if(this.isNativeValid()) {
				if(!this.isReconfigureMaskSupported(256)) {
					return;
				}

				if(this.isFullscreen()) {
					this.stateMaskNFS.put(8, var1);
					return;
				}
			}

			this.runOnEDTIfAvail(true, new WindowImpl.ResizableAction(var1));
		}
	}

	public final boolean isResizable() {
		return this.stateMask.get(8);
	}

	public final void setSticky(boolean var1) {
		if(!this.isChildWindow()) {
			if(this.isReconfigureMaskSupported(128) || !this.isNativeValid()) {
				this.runOnEDTIfAvail(true, new WindowImpl.StickyAction(var1));
			}
		}
	}

	public final boolean isSticky() {
		return this.stateMask.get(7);
	}

	public final void setMaximized(boolean var1, boolean var2) {
		if(this.isNativeValid()) {
			if(var1 && !this.isReconfigureMaskSupported(1024)) {
				var1 = false;
			}

			if(var2 && !this.isReconfigureMaskSupported(512)) {
				var2 = false;
			}
		}

		if(!this.isChildWindow()) {
			if(this.isFullscreen()) {
				this.stateMaskNFS.put(10, var1);
				this.stateMaskNFS.put(9, var2);
			} else {
				this.runOnEDTIfAvail(true, new WindowImpl.MaximizeAction(var1, var2));
			}

		}
	}

	public final boolean isMaximizedVert() {
		return this.stateMask.get(9);
	}

	public final boolean isMaximizedHorz() {
		return this.stateMask.get(10);
	}

	protected final void maximizedChanged(boolean var1, boolean var2) {
		String var3;
		boolean var4;
		boolean var5;
		if(!this.isFullscreen()) {
			var3 = DEBUG_IMPLEMENTATION?this.getStateMaskString():null;
			var4 = this.stateMask.put(10, var1) != var1;
			var5 = this.stateMask.put(9, var2) != var2;
			if(DEBUG_IMPLEMENTATION && (var4 || var5)) {
				System.err.println("Window.maximizedChanged.accepted: " + var3 + " -> " + this.getStateMaskString());
			}
		} else if(DEBUG_IMPLEMENTATION) {
			var3 = DEBUG_IMPLEMENTATION?this.getStateMaskString():null;
			var4 = this.stateMask.get(10) != var1;
			var5 = this.stateMask.get(9) != var2;
			if(var4 || var5) {
				System.err.println("Window.maximizedChanged.ignored: " + var3 + " -> max[" + (var1?"":"!") + "h, " + (var2?"":"!") + "v]");
			}
		}

	}

	protected void reconfigMaximizedManual(int var1, int[] var2, InsetsImmutable var3) {
		MonitorMode var4 = this.getMainMonitor().getCurrentMode();
		int var5 = SurfaceScaleUtils.scaleInv(var4.getRotatedWidth(), this.getPixelScaleX());
		int var6 = SurfaceScaleUtils.scaleInv(var4.getRotatedHeight(), this.getPixelScaleY());
		if(0 != (4194304 & var1)) {
			if(0 != (1024 & var1)) {
				this.normPosSizeStored[0] = true;
				this.normPosSize[0] = var2[0];
				this.normPosSize[2] = var2[2];
				var2[0] = var3.getLeftWidth();
				var2[2] = var5 - var3.getTotalWidth();
			} else {
				this.normPosSizeStored[0] = false;
				var2[0] = this.normPosSize[0];
				var2[2] = this.normPosSize[2];
			}
		}

		if(0 != (8388608 & var1)) {
			if(0 != (512 & var1)) {
				this.normPosSizeStored[1] = true;
				this.normPosSize[1] = var2[1];
				this.normPosSize[3] = var2[3];
				var2[1] = var3.getTopHeight();
				var2[3] = var6 - var3.getTotalHeight();
			} else {
				this.normPosSizeStored[1] = false;
				var2[1] = this.normPosSize[1];
				var2[3] = this.normPosSize[3];
			}
		}

	}

	protected void resetMaximizedManual(int[] var1) {
		if(this.normPosSizeStored[0]) {
			this.normPosSizeStored[0] = false;
			var1[0] = this.normPosSize[0];
			var1[2] = this.normPosSize[2];
		}

		if(this.normPosSizeStored[1]) {
			this.normPosSizeStored[1] = false;
			var1[1] = this.normPosSize[1];
			var1[3] = this.normPosSize[3];
		}

	}

	public final String getTitle() {
		return this.title;
	}

	public final void setTitle(String var1) {
		if(var1 == null) {
			var1 = "";
		}

		this.title = var1;
		if(0L != this.getWindowHandle()) {
			this.setTitleImpl(var1);
		}

	}

	public final boolean isPointerVisible() {
		return this.stateMask.get(12);
	}

	public final void setPointerVisible(boolean var1) {
		if(this.isReconfigureMaskSupported(4096) || !this.isNativeValid()) {
			if(this.stateMask.get(12) != var1) {
				boolean var2 = 0L == this.getWindowHandle();
				if(!var2) {
					var2 = this.setPointerVisibleIntern(var1);
				}

				if(var2) {
					this.stateMask.put(12, var1);
				}
			}

		}
	}

	private boolean setPointerVisibleIntern(boolean var1) {
		boolean var2 = this.setOffscreenPointerVisible(var1, this.pointerIcon);
		return this.setPointerVisibleImpl(var1) || var2;
	}

	private boolean setOffscreenPointerVisible(boolean var1, PointerIconImpl var2) {
		if(var1) {
			return this.setOffscreenPointerIcon(var2);
		} else {
			NativeWindow var3 = this.getParent();
			if(var3 instanceof OffscreenLayerSurface) {
				OffscreenLayerSurface var4 = (OffscreenLayerSurface)var3;

				try {
					return var4.hideCursor();
				} catch (Exception var6) {
					var6.printStackTrace();
				}
			}

			return false;
		}
	}

	public final PointerIcon getPointerIcon() {
		return this.pointerIcon;
	}

	public final void setPointerIcon(PointerIcon var1) {
		final PointerIconImpl var2 = (PointerIconImpl)var1;
		if(this.pointerIcon != var2) {
			if(this.isNativeValid()) {
				this.runOnEDTIfAvail(true, new Runnable() {
					public void run() {
						WindowImpl.this.setPointerIconIntern(var2);
					}
				});
			}

			this.pointerIcon = var2;
		}

	}

	private void setPointerIconIntern(PointerIconImpl var1) {
		this.setOffscreenPointerIcon(var1);
		this.setPointerIconImpl(var1);
	}

	private boolean setOffscreenPointerIcon(PointerIconImpl var1) {
		NativeWindow var2 = this.getParent();
		if(var2 instanceof OffscreenLayerSurface) {
			OffscreenLayerSurface var3 = (OffscreenLayerSurface)var2;

			try {
				if(null != var1) {
					return var3.setCursor(var1, var1.getHotspot());
				}

				return var3.setCursor((PixelRectangle)null, (PointImmutable)null);
			} catch (Exception var5) {
				var5.printStackTrace();
			}
		}

		return false;
	}

	public final boolean isPointerConfined() {
		return this.stateMask.get(13);
	}

	public final void confinePointer(boolean var1) {
		if(this.isReconfigureMaskSupported(8192) || !this.isNativeValid()) {
			if(this.stateMask.get(13) != var1) {
				boolean var2 = 0L == this.getWindowHandle();
				if(!var2) {
					if(var1) {
						this.requestFocus();
						this.warpPointer(this.getSurfaceWidth() / 2, this.getSurfaceHeight() / 2);
					}

					var2 = this.confinePointerImpl(var1);
					if(var1) {
						try {
							Thread.sleep(3L * this.screen.getDisplay().getEDTUtil().getPollPeriod());
						} catch (InterruptedException var4) {
							;
						}
					}
				}

				if(var2) {
					this.stateMask.put(13, var1);
				}
			}

		}
	}

	public final void warpPointer(int var1, int var2) {
		if(0L != this.getWindowHandle()) {
			this.warpPointerImpl(var1, var2);
		}

	}

	public final InsetsImmutable getInsets() {
		return (InsetsImmutable)(this.isUndecorated()? Insets.getZero():this.insets);
	}

	public final int getX() {
		return this.x;
	}

	public final int getY() {
		return this.y;
	}

	public final int getWidth() {
		return this.winWidth;
	}

	public final int getHeight() {
		return this.winHeight;
	}

	public final Rectangle getBounds() {
		return new Rectangle(this.x, this.y, this.winWidth, this.winHeight);
	}

	public final int getSurfaceWidth() {
		return this.pixWidth;
	}

	public final int getSurfaceHeight() {
		return this.pixHeight;
	}

	public final int[] convertToWindowUnits(int[] var1) {
		return SurfaceScaleUtils.scaleInv(var1, var1, this.hasPixelScale);
	}

	public final int[] convertToPixelUnits(int[] var1) {
		return SurfaceScaleUtils.scale(var1, var1, this.hasPixelScale);
	}

	protected final Point convertToWindowUnits(Point var1) {
		return var1.scaleInv(this.getPixelScaleX(), this.getPixelScaleY());
	}

	protected final Point convertToPixelUnits(Point var1) {
		return var1.scale(this.getPixelScaleX(), this.getPixelScaleY());
	}

	protected final float getPixelScaleX() {
		return this.hasPixelScale[0];
	}

	protected final float getPixelScaleY() {
		return this.hasPixelScale[1];
	}

	public boolean setSurfaceScale(float[] var1) {
		System.arraycopy(var1, 0, this.reqPixelScale, 0, 2);
		return false;
	}

	public final float[] getRequestedSurfaceScale(float[] var1) {
		System.arraycopy(this.reqPixelScale, 0, var1, 0, 2);
		return var1;
	}

	public final float[] getCurrentSurfaceScale(float[] var1) {
		System.arraycopy(this.hasPixelScale, 0, var1, 0, 2);
		return var1;
	}

	public final float[] getMinimumSurfaceScale(float[] var1) {
		System.arraycopy(this.minPixelScale, 0, var1, 0, 2);
		return var1;
	}

	public final float[] getMaximumSurfaceScale(float[] var1) {
		System.arraycopy(this.maxPixelScale, 0, var1, 0, 2);
		return var1;
	}

	public final float[] getPixelsPerMM(float[] var1) {
		this.getMainMonitor().getPixelsPerMM(var1);
		var1[0] *= this.hasPixelScale[0] / this.maxPixelScale[0];
		var1[1] *= this.hasPixelScale[1] / this.maxPixelScale[1];
		return var1;
	}

	protected final boolean autoPosition() {
		return this.stateMask.get(1);
	}

	protected final void definePosition(int var1, int var2) {
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("definePosition: " + this.x + "/" + this.y + " -> " + var1 + "/" + var2);
		}

		this.stateMask.clear(1);
		this.x = var1;
		this.y = var2;
	}

	protected final void defineSize(int var1, int var2) {
		int var3 = SurfaceScaleUtils.scale(var1, this.getPixelScaleX());
		int var4 = SurfaceScaleUtils.scale(var2, this.getPixelScaleY());
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("defineSize: win[" + this.winWidth + "x" + this.winHeight + " -> " + var1 + "x" + var2 + "], pixel[" + this.pixWidth + "x" + this.pixHeight + " -> " + var3 + "x" + var4 + "]");
		}

		this.winWidth = var1;
		this.winHeight = var2;
		this.pixWidth = var3;
		this.pixHeight = var4;
	}

	public final boolean isVisible() {
		return this.stateMask.get(0);
	}

	public final boolean isFullscreen() {
		return this.stateMask.get(11);
	}

	public final Window getDelegatedWindow() {
		return this;
	}

	public boolean hasDeviceChanged() {
		return false;
	}

	public final WindowImpl.LifecycleHook getLifecycleHook() {
		return this.lifecycleHook;
	}

	public final WindowImpl.LifecycleHook setLifecycleHook(WindowImpl.LifecycleHook var1) {
		WindowImpl.LifecycleHook var2 = this.lifecycleHook;
		this.lifecycleHook = var1;
		return var2;
	}

	public NativeSurface getWrappedSurface() {
		return null;
	}

	public final void setWindowDestroyNotifyAction(Runnable var1) {
		this.windowDestroyNotifyAction = var1;
	}

	protected final long getParentWindowHandle() {
		return this.isFullscreen()?0L:this.parentWindowHandle;
	}

	public final String toString() {
		StringBuilder var1 = new StringBuilder();
		var1.append(this.getClass().getName() + "[State " + this.getStateMaskString() + ",\n " + this.screen + ",\n window[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + " wu, " + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + " pixel]" + ",\n Config " + this.config + ",\n ParentWindow " + this.parentWindow + ",\n ParentWindowHandle " + toHexString(this.parentWindowHandle) + " (" + (0L != this.getParentWindowHandle()) + ")" + ",\n WindowHandle " + toHexString(this.getWindowHandle()) + ",\n SurfaceHandle " + toHexString(this.getSurfaceHandle()) + " (lockedExt window " + this.windowLock.isLockedByOtherThread() + ", surface " + this.isSurfaceLockedByOtherThread() + ")" + ",\n WrappedSurface " + this.getWrappedSurface() + ",\n ChildWindows " + this.childWindows.size());
		var1.append(", SurfaceUpdatedListeners num " + this.surfaceUpdatedHelper.size() + " [");

		int var2;
		for(var2 = 0; var2 < this.surfaceUpdatedHelper.size(); ++var2) {
			var1.append(this.surfaceUpdatedHelper.get(var2) + ", ");
		}

		var1.append("], WindowListeners num " + this.windowListeners.size() + " [");

		for(var2 = 0; var2 < this.windowListeners.size(); ++var2) {
			var1.append(this.windowListeners.get(var2) + ", ");
		}

		var1.append("], MouseListeners num " + this.mouseListeners.size() + " [");

		for(var2 = 0; var2 < this.mouseListeners.size(); ++var2) {
			var1.append(this.mouseListeners.get(var2) + ", ");
		}

		var1.append("], PointerGestures default " + this.defaultGestureHandlerEnabled + ", custom " + this.pointerGestureHandler.size() + " [");

		for(var2 = 0; var2 < this.pointerGestureHandler.size(); ++var2) {
			var1.append(this.pointerGestureHandler.get(var2) + ", ");
		}

		var1.append("], KeyListeners num " + this.keyListeners.size() + " [");

		for(var2 = 0; var2 < this.keyListeners.size(); ++var2) {
			var1.append(this.keyListeners.get(var2) + ", ");
		}

		var1.append("], windowLock " + this.windowLock + ", surfaceLockCount " + this.surfaceLockCount + "]");
		return var1.toString();
	}

	protected final void setWindowHandle(long var1) {
		this.windowHandle = var1;
	}

	public final void runOnEDTIfAvail(boolean var1, Runnable var2) {
		if(this.windowLock.isOwner(Thread.currentThread())) {
			var2.run();
		} else {
			((DisplayImpl)this.screen.getDisplay()).runOnEDTIfAvail(var1, var2);
		}

	}

	public final boolean hasFocus() {
		return this.stateMask.get(3);
	}

	public final void requestFocus() {
		this.requestFocus(true);
	}

	public final void requestFocus(boolean var1) {
		this.requestFocus(var1, false, this.stateMask.get(30));
	}

	private void requestFocus(boolean var1, boolean var2, boolean var3) {
		if(this.isNativeValid() && (var3 || !this.hasFocus()) && (var2 || !this.focusAction())) {
			this.runOnEDTIfAvail(var1, var3?this.requestFocusActionForced:this.requestFocusAction);
		}

	}

	private void requestFocusInt(boolean var1) {
		if(var1 || !this.focusAction()) {
			if(!this.isReconfigureMaskSupported(8)) {
				return;
			}

			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.RequestFocusInt: forcing - (" + getThreadName() + "): skipFocusAction " + var1 + ", state " + this.getStateMaskString() + " -> focus true - windowHandle " + toHexString(this.windowHandle) + " parentWindowHandle " + toHexString(this.parentWindowHandle));
			}

			this.requestFocusImpl(true);
		}

	}

	public final void setFocusAction(FocusRunnable var1) {
		this.focusAction = var1;
	}

	private boolean focusAction() {
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.focusAction() START - " + getThreadName() + ", focusAction: " + this.focusAction + " - windowHandle " + toHexString(this.getWindowHandle()));
		}

		boolean var1;
		if(null != this.focusAction) {
			var1 = this.focusAction.run();
		} else {
			var1 = false;
		}

		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.focusAction() END - " + getThreadName() + ", focusAction: " + this.focusAction + " - windowHandle " + toHexString(this.getWindowHandle()) + ", res: " + var1);
		}

		return var1;
	}

	protected final void setBrokenFocusChange(boolean var1) {
		this.stateMask.put(30, var1);
	}

	public final void setKeyboardFocusHandler(KeyListener var1) {
		this.keyboardFocusHandler = var1;
	}

	public void setPosition(int var1, int var2) {
		this.stateMask.clear(1);
		this.runOnEDTIfAvail(true, new WindowImpl.SetPositionAction(var1, var2));
	}

	public final void setTopLevelPosition(int var1, int var2) {
		InsetsImmutable var3 = this.getInsets();
		this.setPosition(var1 + var3.getLeftWidth(), var2 + var3.getTopHeight());
	}

	public boolean setFullscreen(boolean var1) {
		return this.setFullscreenImpl(var1, true, (List)null);
	}

	public boolean setFullscreen(List<MonitorDevice> var1) {
		return this.setFullscreenImpl(true, false, var1);
	}

	private boolean setFullscreenImpl(boolean var1, boolean var2, List<MonitorDevice> var3) {
		WindowImpl.FullScreenAction var4 = this.fullScreenAction;
		synchronized(this.fullScreenAction) {
			this.fullscreenMonitors = var3;
			this.stateMask.put(31, var2);
			if(this.fullScreenAction.init(var1)) {
				if(this.fullScreenAction.fsOn() && isOffscreenInstance(this, this.parentWindow)) {
					if(null == this.parentWindow) {
						throw new InternalError("Offscreen instance w/o parent unhandled");
					}

					this.nfs_parent = this.parentWindow;
					this.reparentWindow((NativeWindow)null, -1, -1, 3);
				}

				this.runOnEDTIfAvail(true, this.fullScreenAction);
				if(!this.fullScreenAction.fsOn() && null != this.nfs_parent) {
					this.reparentWindow(this.nfs_parent, -1, -1, 3);
					this.nfs_parent = null;
				}
			}

			return this.stateMask.get(11);
		}
	}

	protected void monitorModeChanged(MonitorEvent var1, boolean var2) {
	}

	public final boolean removeChild(NativeWindow var1) {
		Object var2 = this.childWindowsLock;
		synchronized(this.childWindowsLock) {
			return this.childWindows.remove(var1);
		}
	}

	public final boolean addChild(NativeWindow var1) {
		if(var1 == null) {
			return false;
		} else {
			Object var2 = this.childWindowsLock;
			synchronized(this.childWindowsLock) {
				return this.childWindows.add(var1);
			}
		}
	}

	private void doEvent(boolean var1, boolean var2, NEWTEvent var3) {
		boolean var4 = false;
		if(!var1) {
			var4 = this.consumeEvent(var3);
			var2 = var4;
		}

		if(!var4) {
			this.enqueueEvent(var2, var3);
		}

	}

	public final void enqueueEvent(boolean var1, NEWTEvent var2) {
		if(this.isNativeValid()) {
			((DisplayImpl)this.screen.getDisplay()).enqueueEvent(var1, var2);
		}

	}

	public final boolean consumeEvent(NEWTEvent var1) {
		boolean var2;
		switch(var1.getEventType()) {
			case 100:
				if(this.windowLock.isLockedByOtherThread()) {
					var2 = 1200L <= System.currentTimeMillis() - var1.getWhen();
					if(DEBUG_IMPLEMENTATION) {
						System.err.println("Window.consumeEvent: RESIZED [me " + Thread.currentThread().getName() + ", owner " + this.windowLock.getOwner() + "] - queued " + var1 + ", discard-to " + var2);
					}

					return var2;
				}
				break;
			case 105:
				if(this.windowLock.isLockedByOtherThread()) {
					if(!this.repaintQueued) {
						this.repaintQueued = true;
						var2 = 1200L <= System.currentTimeMillis() - var1.getWhen();
						if(DEBUG_IMPLEMENTATION) {
							System.err.println("Window.consumeEvent: REPAINT [me " + Thread.currentThread().getName() + ", owner " + this.windowLock.getOwner() + "] - queued " + var1 + ", discard-to " + var2);
						}

						return var2;
					}

					return true;
				}

				this.repaintQueued = false;
		}

		if(var1 instanceof WindowEvent) {
			this.consumeWindowEvent((WindowEvent)var1);
		} else if(var1 instanceof KeyEvent) {
			this.consumeKeyEvent((KeyEvent)var1);
		} else {
			if(!(var1 instanceof MouseEvent)) {
				throw new NativeWindowException("Unexpected NEWTEvent type " + var1);
			}

			this.consumePointerEvent((MouseEvent)var1);
		}

		return true;
	}

	public final void sendMouseEvent(short var1, int var2, int var3, int var4, short var5, float var6) {
		this.doMouseEvent(false, false, var1, var2, var3, var4, var5, MouseEvent.getRotationXYZ(var6, var2), 1.0F);
	}

	public final void enqueueMouseEvent(boolean var1, short var2, int var3, int var4, int var5, short var6, float var7) {
		this.doMouseEvent(true, var1, var2, var3, var4, var5, var6, MouseEvent.getRotationXYZ(var7, var3), 1.0F);
	}

	protected final void doMouseEvent(boolean var1, boolean var2, short var3, int var4, int var5, int var6, short var7, float var8) {
		this.doMouseEvent(var1, var2, var3, var4, var5, var6, var7, MouseEvent.getRotationXYZ(var8, var4), 1.0F);
	}

	protected void doMouseEvent(boolean var1, boolean var2, short var3, int var4, int var5, int var6, short var7, float[] var8, float var9) {
		if(0 <= var7 && var7 <= 16) {
			this.doPointerEvent(var1, var2, constMousePointerTypes, var3, var4, 0, new short[]{(short)0}, var7, new int[]{var5}, new int[]{var6}, new float[]{0.0F}, 1.0F, var8, var9);
		} else {
			throw new NativeWindowException("Invalid mouse button number" + var7);
		}
	}

	public final void doPointerEvent(boolean var1, boolean var2, PointerType[] var3, short var4, int var5, int var6, boolean var7, int[] var8, int[] var9, int[] var10, float[] var11, float var12, float[] var13, float var14) {
		int var15 = var8.length;
		short[] var16 = new short[var15];

		for(int var17 = 0; var17 < var15; ++var17) {
			if(!var7) {
				int var18 = this.pName2pID.size();
				Integer var19 = (Integer)this.pName2pID.getOrAdd(Integer.valueOf(var8[var17]));
				short var20 = (short)this.pName2pID.indexOf(var19);
				var16[var17] = var20;
				if(DEBUG_MOUSE_EVENT) {
					int var21 = this.pName2pID.size();
					if(var18 != var21) {
						System.err.println("PointerName2ID[sz " + var21 + "]: Map " + var19 + " == " + var20);
					}
				}

				if(204 == var4) {
					this.pName2pID.remove(var19);
					if(DEBUG_MOUSE_EVENT) {
						System.err.println("PointerName2ID[sz " + this.pName2pID.size() + "]: Unmap " + var19 + " == " + var20);
					}
				}
			} else {
				var16[var17] = (short)var8[var17];
			}
		}

		short var22 = 0 < var15?(short)(var16[0] + 1):0;
		this.doPointerEvent(var1, var2, var3, var4, var5, var6, var16, var22, var9, var10, var11, var12, var13, var14);
	}

	public final void doPointerEvent(boolean var1, boolean var2, PointerType[] var3, short var4, int var5, int var6, short[] var7, short var8, int[] var9, int[] var10, float[] var11, float var12, float[] var13, float var14) {
		long var15 = System.currentTimeMillis();
		int var17 = var3.length;
		if(0 <= var6 && var6 < var17) {
			short var25;
			if(0 < var6) {
				PointerType var18 = var3[var6];
				var3[var6] = var3[0];
				var3[0] = var18;
				var25 = var7[var6];
				var7[var6] = var7[0];
				var7[0] = var25;
				int var26 = var9[var6];
				var9[var6] = var9[0];
				var9[0] = var26;
				var26 = var10[var6];
				var10[var6] = var10[0];
				var10[0] = var26;
				float var27 = var11[var6];
				var11[var6] = var11[0];
				var11[0] = var27;
			}

			if(0 <= var8 && var8 <= 16) {
				var25 = var8;
			} else {
				var25 = 1;
			}

			int var19 = var9[0];
			int var20 = var10[0];
			boolean var21 = var19 >= 0 && var20 >= 0 && var19 < this.getSurfaceWidth() && var20 < this.getSurfaceHeight();
			Point var22 = this.pState1.getMovePosition(var7[0]);
			switch(var4) {
				case 202:
					if(this.pState1.dragging) {
						if(DEBUG_MOUSE_EVENT) {
							System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(var4) + " due to dragging: " + this.pState1);
						}

						return;
					}

					if(null != var22) {
						if(var19 == -1 && var20 == -1) {
							var19 = var22.getX();
							var20 = var22.getY();
						}

						var22.set(0, 0);
					}
				case 201:
					if(var4 == 201) {
						this.pState1.insideSurface = true;
						this.pState1.exitSent = false;
					} else {
						this.pState1.insideSurface = false;
						this.pState1.exitSent = true;
					}

					this.pState1.clearButton();
					if(var3[0] != PointerType.Mouse) {
						if(DEBUG_MOUSE_EVENT) {
							System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(var4) + " due to !Mouse but " + var3[0] + ": " + this.pState1);
						}

						return;
					}

					var19 = Math.min(Math.max(var19, 0), this.getSurfaceWidth() - 1);
					var20 = Math.min(Math.max(var20, 0), this.getSurfaceHeight() - 1);
					break;
				case 205:
				case 206:
					if(null != var22) {
						//PJPJPJPJPJPJPJPJPJPJPJPJPJ!!!!! this drops events incorrectly
					/*	if(var22.getX() == var19 && var22.getY() == var20) {
							if(DEBUG_MOUSE_EVENT) {
								System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(var4) + " w/ same position: " + var22 + ", " + this.pState1);
							}

							return;
						}*/

						var22.set(var19, var20);
					}
				case 203:
				case 204:
				default:
					if(this.pState1.insideSurface != var21) {
						this.pState1.insideSurface = var21;
						if(var21) {
							this.pState1.exitSent = false;
						}

						this.pState1.clearButton();
					}
			}

			if(!this.pState1.dragging && !var21 && 202 != var4) {
				if(DEBUG_MOUSE_EVENT) {
					System.err.println("doPointerEvent: drop: " + MouseEvent.getEventTypeString(var4) + ", mod " + var5 + ", pos " + var19 + "/" + var20 + ", button " + var25 + ", lastMousePosition: " + var22 + ", insideWindow " + var21 + ", " + this.pState1);
				}

			} else {
				if(DEBUG_MOUSE_EVENT) {
					System.err.println("doPointerEvent: enqueue " + var1 + ", wait " + var2 + ", " + MouseEvent.getEventTypeString(var4) + ", mod " + var5 + ", pos " + var19 + "/" + var20 + ", button " + var25 + ", lastMousePosition: " + var22 + ", " + this.pState1);
				}

				int var23 = InputEvent.getButtonMask(var25);
				var5 |= var23;
				var5 |= this.pState1.buttonPressedMask;
				if(this.isPointerConfined()) {
					var5 |= 1073741824;
				}

				if(!this.isPointerVisible()) {
					var5 |= -2147483648;
				}

				var9[0] = var19;
				var10[0] = var20;
				MouseEvent var24;
				switch(var4) {
					case 200:
						return;
					case 203:
						if(0.0F >= var11[0]) {
							var11[0] = var12;
						}

						this.pState1.buttonPressedMask |= var23;
						if(1 == var17) {
							if(var15 - this.pState1.lastButtonPressTime < (long) MouseEvent.getClickTimeout()) {
								++this.pState1.lastButtonClickCount;
							} else {
								this.pState1.lastButtonClickCount = 1;
							}

							this.pState1.lastButtonPressTime = var15;
							this.pState1.buttonPressed = var25;
							var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, this.pState1.lastButtonClickCount, var13, var14);
						} else {
							var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, (short)1, var13, var14);
						}
						break;
					case 204:
						this.pState1.buttonPressedMask &= ~var23;
						if(1 == var17) {
							var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, this.pState1.lastButtonClickCount, var13, var14);
							if(var15 - this.pState1.lastButtonPressTime >= (long) MouseEvent.getClickTimeout()) {
								this.pState1.lastButtonClickCount = 0;
								this.pState1.lastButtonPressTime = 0L;
							}

							this.pState1.buttonPressed = 0;
							this.pState1.dragging = false;
						} else {
							var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, (short)1, var13, var14);
							if(0 == this.pState1.buttonPressedMask) {
								this.pState1.clearButton();
							}
						}

						if(null != var22) {
							var22.set(0, 0);
						}
						break;
					case 205:
						if(0 != this.pState1.buttonPressedMask) {
							var24 = new MouseEvent((short)206, this, var15, var5, var3, var7, var9, var10, var11, var12, this.pState1.buttonPressed, (short)1, var13, var14);
							this.pState1.dragging = true;
						} else {
							var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, (short)0, var13, var14);
						}
						break;
					case 206:
						if(0.0F >= var11[0]) {
							var11[0] = var12;
						}

						this.pState1.dragging = true;
					case 201:
					case 202:
					default:
						var24 = new MouseEvent(var4, this, var15, var5, var3, var7, var9, var10, var11, var12, var25, (short)0, var13, var14);
				}

				this.doEvent(var1, var2, var24);
			}
		} else {
			throw new IllegalArgumentException("actionIdx out of bounds [0.." + (var17 - 1) + "]");
		}
	}

	private static int step(int var0, int var1, int var2) {
		return var2 < var1?var0:var2;
	}

	protected void consumePointerEvent(MouseEvent var1) {
		if(DEBUG_MOUSE_EVENT) {
			System.err.println("consumePointerEvent.in: " + var1 + ", " + this.pState0 + ", pos " + var1.getX() + "/" + var1.getY() + ", win[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + "], pixel[" + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + "]");
		}

		long var2 = var1.getWhen();
		short var4 = var1.getEventType();
		boolean var6 = false;
		MouseEvent var7 = null;
		MouseEvent var8 = null;
		boolean var5;
		int var9;
		int var10;
		switch(var4) {
			case 202:
				if(this.pState0.exitSent || this.pState0.dragging) {
					if(DEBUG_MOUSE_EVENT) {
						System.err.println("consumePointerEvent: drop " + (this.pState0.exitSent?"already sent":"due to dragging") + ": " + var1 + ", " + this.pState0);
					}

					return;
				}
			case 201:
				this.pState0.clearButton();
				if(var4 == 201) {
					var5 = true;
					this.pState0.insideSurface = true;
					this.pState0.exitSent = false;
					this.pState0.dragging = false;
				} else {
					var5 = false;
					this.pState0.insideSurface = false;
					this.pState0.exitSent = true;
				}
				break;
			case 204:
			case 205:
				if(1 >= var1.getButtonDownCount()) {
					var6 = !this.pState0.exitSent;
					this.pState0.dragging = false;
				}
			case 203:
			default:
				var9 = var1.getX();
				var10 = var1.getY();
				var5 = var9 >= 0 && var10 >= 0 && var9 < this.getSurfaceWidth() && var10 < this.getSurfaceHeight();
				if(var1.getPointerType(0) == PointerType.Mouse) {
					if(!this.pState0.insideSurface && var5) {
						var7 = new MouseEvent((short)201, var1.getSource(), var1.getWhen(), var1.getModifiers(), Math.min(Math.max(var9, 0), this.getSurfaceWidth() - 1), Math.min(Math.max(var10, 0), this.getSurfaceHeight() - 1), (short)0, (short)0, var1.getRotation(), var1.getRotationScale());
						this.pState0.exitSent = false;
					} else if(!var5 && var6) {
						var8 = new MouseEvent((short)202, var1.getSource(), var1.getWhen(), var1.getModifiers(), Math.min(Math.max(var9, 0), this.getSurfaceWidth() - 1), Math.min(Math.max(var10, 0), this.getSurfaceHeight() - 1), (short)0, (short)0, var1.getRotation(), var1.getRotationScale());
						this.pState0.exitSent = true;
					}
				}

				if(this.pState0.insideSurface != var5 || null != var7 || null != var8) {
					this.pState0.clearButton();
				}

				this.pState0.insideSurface = var5;
		}

		if(null != var7) {
			if(DEBUG_MOUSE_EVENT) {
				System.err.println("consumePointerEvent.send.0: " + var7 + ", " + this.pState0);
			}

			this.dispatchMouseEvent(var7);
		} else if(DEBUG_MOUSE_EVENT && !var5) {
			System.err.println("INFO consumePointerEvent.exterior: " + this.pState0 + ", " + var1);
		}

		if(this.defaultGestureHandlerEnabled && var1.getPointerType(0).getPointerClass() == PointerClass.Onscreen) {
			if(null == this.gesture2PtrTouchScroll) {
				MonitorDevice var11 = this.getMainMonitor();
				if(null != var11) {
					DimensionImmutable var12 = var11.getSizeMM();
					float var13 = (float)var11.getCurrentMode().getRotatedWidth() / (float)var12.getWidth();
					float var14 = (float)var11.getCurrentMode().getRotatedHeight() / (float)var12.getHeight();
					float var15 = Math.min(var14, var13);
					var9 = Math.round(DoubleTapScrollGesture.SCROLL_SLOP_MM * var15);
					var10 = Math.round(DoubleTapScrollGesture.DOUBLE_TAP_SLOP_MM * var15);
					if(DEBUG_MOUSE_EVENT) {
						System.err.println("consumePointerEvent.gscroll: scrollSlop " + var9 + ", doubleTapSlop " + var10 + ", pixPerMM " + var15 + ", " + var11 + ", " + this.pState0);
					}
				} else {
					var9 = DoubleTapScrollGesture.SCROLL_SLOP_PIXEL;
					var10 = DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL;
				}

				this.gesture2PtrTouchScroll = new DoubleTapScrollGesture(step(DoubleTapScrollGesture.SCROLL_SLOP_PIXEL, DoubleTapScrollGesture.SCROLL_SLOP_PIXEL / 2, var9), step(DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL, DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL / 2, var10));
			}

			if(this.gesture2PtrTouchScroll.process(var1)) {
				var1 = (MouseEvent)this.gesture2PtrTouchScroll.getGestureEvent();
				this.gesture2PtrTouchScroll.clear(false);
				if(DEBUG_MOUSE_EVENT) {
					System.err.println("consumePointerEvent.gscroll: " + var1 + ", " + this.pState0);
				}

				this.dispatchMouseEvent(var1);
				return;
			}

			if(this.gesture2PtrTouchScroll.isWithinGesture()) {
				return;
			}
		}

		var9 = this.pointerGestureHandler.size();
		if(var9 > 0) {
			boolean var17 = false;

			for(int var18 = 0; !var1.isConsumed() && var18 < var9; ++var18) {
				GestureHandler var19 = (GestureHandler)this.pointerGestureHandler.get(var18);
				if(var19.process(var1)) {
					InputEvent var20 = var19.getGestureEvent();
					var19.clear(false);
					if(var20 instanceof MouseEvent) {
						this.dispatchMouseEvent((MouseEvent)var20);
					} else if(var20 instanceof GestureEvent) {
						GestureEvent var21 = (GestureEvent)var20;

						for(int var22 = 0; !var21.isConsumed() && var22 < this.gestureListeners.size(); ++var22) {
							((GestureListener)this.gestureListeners.get(var22)).gestureDetected(var21);
						}
					}

					return;
				}

				var17 |= var19.isWithinGesture();
			}

			if(var17) {
				return;
			}
		}

		MouseEvent var16 = null;
		switch(var4) {
			case 200:
				if(DEBUG_MOUSE_EVENT) {
					System.err.println("consumePointerEvent: drop recv\'ed (synth here) " + var1 + ", " + this.pState0);
				}

				var1 = null;
			case 201:
			case 202:
			case 205:
			default:
				break;
			case 203:
				if(1 == var1.getPointerCount()) {
					this.pState0.lastButtonPressTime = var2;
				}
				break;
			case 204:
				if(1 == var1.getPointerCount() && var2 - this.pState0.lastButtonPressTime < (long) MouseEvent.getClickTimeout()) {
					var16 = var1.createVariant((short)200);
				} else {
					this.pState0.lastButtonPressTime = 0L;
				}
				break;
			case 206:
				this.pState0.dragging = true;
		}

		if(null != var1) {
			if(DEBUG_MOUSE_EVENT) {
				System.err.println("consumePointerEvent.send.1: " + var1 + ", " + this.pState0);
			}

			this.dispatchMouseEvent(var1);
		}

		if(null != var16) {
			if(DEBUG_MOUSE_EVENT) {
				System.err.println("consumePointerEvent.send.2: " + var16 + ", " + this.pState0);
			}

			this.dispatchMouseEvent(var16);
		}

		if(null != var8) {
			if(DEBUG_MOUSE_EVENT) {
				System.err.println("consumePointerEvent.send.3: " + var8 + ", " + this.pState0);
			}

			this.dispatchMouseEvent(var8);
		}

	}

	public final void addMouseListener(MouseListener var1) {
		this.addMouseListener(-1, var1);
	}

	public final void addMouseListener(int var1, MouseListener var2) {
		if(var2 != null) {
			ArrayList var3 = (ArrayList)this.mouseListeners.clone();
			if(0 > var1) {
				var1 = var3.size();
			}

			var3.add(var1, var2);
			this.mouseListeners = var3;
		}
	}

	public final void removeMouseListener(MouseListener var1) {
		if(var1 != null) {
			ArrayList var2 = (ArrayList)this.mouseListeners.clone();
			var2.remove(var1);
			this.mouseListeners = var2;
		}
	}

	public final MouseListener getMouseListener(int var1) {
		ArrayList var2 = (ArrayList)this.mouseListeners.clone();
		if(0 > var1) {
			var1 = var2.size() - 1;
		}

		return (MouseListener)var2.get(var1);
	}

	public final MouseListener[] getMouseListeners() {
		return (MouseListener[])this.mouseListeners.toArray(new MouseListener[this.mouseListeners.size()]);
	}

	public final void setDefaultGesturesEnabled(boolean var1) {
		this.defaultGestureHandlerEnabled = var1;
	}

	public final boolean areDefaultGesturesEnabled() {
		return this.defaultGestureHandlerEnabled;
	}

	public final void addGestureHandler(GestureHandler var1) {
		this.addGestureHandler(-1, var1);
	}

	public final void addGestureHandler(int var1, GestureHandler var2) {
		if(var2 != null) {
			ArrayList var3 = (ArrayList)this.pointerGestureHandler.clone();
			if(0 > var1) {
				var1 = var3.size();
			}

			var3.add(var1, var2);
			this.pointerGestureHandler = var3;
		}
	}

	public final void removeGestureHandler(GestureHandler var1) {
		if(var1 != null) {
			ArrayList var2 = (ArrayList)this.pointerGestureHandler.clone();
			var2.remove(var1);
			this.pointerGestureHandler = var2;
		}
	}

	public final void addGestureListener(GestureListener var1) {
		this.addGestureListener(-1, var1);
	}

	public final void addGestureListener(int var1, GestureListener var2) {
		if(var2 != null) {
			ArrayList var3 = (ArrayList)this.gestureListeners.clone();
			if(0 > var1) {
				var1 = var3.size();
			}

			var3.add(var1, var2);
			this.gestureListeners = var3;
		}
	}

	public final void removeGestureListener(GestureListener var1) {
		if(var1 != null) {
			ArrayList var2 = (ArrayList)this.gestureListeners.clone();
			var2.remove(var1);
			this.gestureListeners = var2;
		}
	}

	private final void dispatchMouseEvent(MouseEvent var1) {
		for(int var2 = 0; !var1.isConsumed() && var2 < this.mouseListeners.size(); ++var2) {
			MouseListener var3 = (MouseListener)this.mouseListeners.get(var2);
			switch(var1.getEventType()) {
				case 200:
					var3.mouseClicked(var1);
					break;
				case 201:
					var3.mouseEntered(var1);
					break;
				case 202:
					var3.mouseExited(var1);
					break;
				case 203:
					var3.mousePressed(var1);
					break;
				case 204:
					var3.mouseReleased(var1);
					break;
				case 205:
					var3.mouseMoved(var1);
					break;
				case 206:
					var3.mouseDragged(var1);
					break;
				case 207:
					var3.mouseWheelMoved(var1);
					break;
				default:
					throw new NativeWindowException("Unexpected mouse event type " + var1.getEventType());
			}
		}

	}

	protected final boolean isKeyCodeTracked(short var1) {
		return ('\uffff' & var1) <= 255;
	}

	protected final boolean setKeyPressed(short var1, boolean var2) {
		int var3 = '\uffff' & var1;
		return var3 <= 255?this.keyPressedState.put(var3, var2):false;
	}

	protected final boolean isKeyPressed(short var1) {
		int var2 = '\uffff' & var1;
		return var2 <= 255?this.keyPressedState.get(var2):false;
	}

	public void sendKeyEvent(short var1, int var2, short var3, short var4, char var5) {
		this.consumeKeyEvent(KeyEvent.create(var1, this, System.currentTimeMillis(), var2 | this.pState1.buttonPressedMask, var3, var4, var5));
	}

	public void enqueueKeyEvent(boolean var1, short var2, int var3, short var4, short var5, char var6) {
		this.enqueueEvent(var1, KeyEvent.create(var2, this, System.currentTimeMillis(), var3 | this.pState1.buttonPressedMask, var4, var5, var6));
	}

	public final void setKeyboardVisible(boolean var1) {
		if(this.isNativeValid()) {
			boolean var2 = this.setKeyboardVisibleImpl(var1);
			if(DEBUG_IMPLEMENTATION || DEBUG_KEY_EVENT) {
				System.err.println("setKeyboardVisible(native): visible " + this.keyboardVisible + " -- op[visible:" + var1 + ", ok " + var2 + "] -> " + (var1 && var2));
			}

			this.keyboardVisibilityChanged(var1 && var2);
		} else {
			this.keyboardVisibilityChanged(var1);
		}

	}

	public final boolean isKeyboardVisible() {
		return this.keyboardVisible;
	}

	protected boolean setKeyboardVisibleImpl(boolean var1) {
		return false;
	}

	protected void keyboardVisibilityChanged(boolean var1) {
		if(this.keyboardVisible != var1) {
			if(DEBUG_IMPLEMENTATION || DEBUG_KEY_EVENT) {
				System.err.println("keyboardVisibilityChanged: " + this.keyboardVisible + " -> " + var1);
			}

			this.keyboardVisible = var1;
		}

	}

	public final void addKeyListener(KeyListener var1) {
		this.addKeyListener(-1, var1);
	}

	public final void addKeyListener(int var1, KeyListener var2) {
		if(var2 != null) {
			ArrayList var3 = (ArrayList)this.keyListeners.clone();
			if(0 > var1) {
				var1 = var3.size();
			}

			var3.add(var1, var2);
			this.keyListeners = var3;
		}
	}

	public final void removeKeyListener(KeyListener var1) {
		if(var1 != null) {
			ArrayList var2 = (ArrayList)this.keyListeners.clone();
			var2.remove(var1);
			this.keyListeners = var2;
		}
	}

	public final KeyListener getKeyListener(int var1) {
		ArrayList var2 = (ArrayList)this.keyListeners.clone();
		if(0 > var1) {
			var1 = var2.size() - 1;
		}

		return (KeyListener)var2.get(var1);
	}

	public final KeyListener[] getKeyListeners() {
		return (KeyListener[])this.keyListeners.toArray(new KeyListener[this.keyListeners.size()]);
	}

	private final boolean propagateKeyEvent(KeyEvent var1, KeyListener var2) {
		switch(var1.getEventType()) {
			case 300:
				var2.keyPressed(var1);
				break;
			case 301:
				var2.keyReleased(var1);
				break;
			default:
				throw new NativeWindowException("Unexpected key event type " + var1.getEventType());
		}

		return var1.isConsumed();
	}

	protected void consumeKeyEvent(KeyEvent var1) {
		boolean var2 = false;
		if(null != this.keyboardFocusHandler && !var1.isAutoRepeat()) {
			var2 = this.propagateKeyEvent(var1, this.keyboardFocusHandler);
			if(DEBUG_KEY_EVENT && var2) {
				System.err.println("consumeKeyEvent(kfh): " + var1 + ", consumed: " + var2);
			}
		}

		if(!var2) {
			for(int var3 = 0; !var2 && var3 < this.keyListeners.size(); ++var3) {
				var2 = this.propagateKeyEvent(var1, (KeyListener)this.keyListeners.get(var3));
			}

			if(DEBUG_KEY_EVENT) {
				System.err.println("consumeKeyEvent(usr): " + var1 + ", consumed: " + var2);
			}
		}

	}

	public final void sendWindowEvent(int var1) {
		this.consumeWindowEvent(new WindowEvent((short)var1, this, System.currentTimeMillis()));
	}

	public final void enqueueWindowEvent(boolean var1, int var2) {
		this.enqueueEvent(var1, new WindowEvent((short)var2, this, System.currentTimeMillis()));
	}

	public final void addWindowListener(WindowListener var1) {
		this.addWindowListener(-1, var1);
	}

	public final void addWindowListener(int var1, WindowListener var2) throws IndexOutOfBoundsException {
		if(var2 != null) {
			ArrayList var3 = (ArrayList)this.windowListeners.clone();
			if(0 > var1) {
				var1 = var3.size();
			}

			var3.add(var1, var2);
			this.windowListeners = var3;
		}
	}

	public final void removeWindowListener(WindowListener var1) {
		if(var1 != null) {
			ArrayList var2 = (ArrayList)this.windowListeners.clone();
			var2.remove(var1);
			this.windowListeners = var2;
		}
	}

	public final WindowListener getWindowListener(int var1) {
		ArrayList var2 = (ArrayList)this.windowListeners.clone();
		if(0 > var1) {
			var1 = var2.size() - 1;
		}

		return (WindowListener)var2.get(var1);
	}

	public final WindowListener[] getWindowListeners() {
		return (WindowListener[])this.windowListeners.toArray(new WindowListener[this.windowListeners.size()]);
	}

	protected void consumeWindowEvent(WindowEvent var1) {
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("consumeWindowEvent: " + var1 + ", visible " + this.isVisible() + " " + this.getX() + "/" + this.getY() + ", win[" + this.getX() + "/" + this.getY() + " " + this.getWidth() + "x" + this.getHeight() + "], pixel[" + this.getSurfaceWidth() + "x" + this.getSurfaceHeight() + "]");
		}

		for(int var2 = 0; !var1.isConsumed() && var2 < this.windowListeners.size(); ++var2) {
			WindowListener var3 = (WindowListener)this.windowListeners.get(var2);
			switch(var1.getEventType()) {
				case 100:
					var3.windowResized(var1);
					break;
				case 101:
					var3.windowMoved(var1);
					break;
				case 102:
					var3.windowDestroyNotify(var1);
					break;
				case 103:
					var3.windowGainedFocus(var1);
					break;
				case 104:
					var3.windowLostFocus(var1);
					break;
				case 105:
					var3.windowRepaint((WindowUpdateEvent)var1);
					break;
				case 106:
					var3.windowDestroyed(var1);
					break;
				default:
					throw new NativeWindowException("Unexpected window event type " + var1.getEventType());
			}
		}

	}

	protected void focusChanged(boolean var1, boolean var2) {
		if(this.stateMask.get(30) || this.stateMask.get(3) != var2) {
			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.focusChanged: (" + getThreadName() + "): (defer: " + var1 + ") state " + this.getStateMaskString() + " -> focus " + var2 + " - windowHandle " + toHexString(this.windowHandle) + " parentWindowHandle " + toHexString(this.parentWindowHandle));
			}

			this.stateMask.put(3, var2);
			int var3 = var2?103:104;
			if(!var1) {
				this.sendWindowEvent(var3);
			} else {
				this.enqueueWindowEvent(false, var3);
			}
		}

	}

	protected final void visibleChanged(boolean var1, boolean var2) {
		if(this.stateMask.put(0, var2) != var2 && DEBUG_IMPLEMENTATION) {
			System.err.println("Window.visibleChanged (" + getThreadName() + "): (defer: " + var1 + ") visible " + !var2 + " -> state " + this.getStateMaskString() + " - windowHandle " + toHexString(this.windowHandle) + " parentWindowHandle " + toHexString(this.parentWindowHandle));
		}

	}

	private long waitForVisible(boolean var1, boolean var2) {
		return this.waitForVisible(var1, var2, 1000L);
	}

	private long waitForVisible(boolean var1, boolean var2, long var3) {
		DisplayImpl var5 = (DisplayImpl)this.screen.getDisplay();
		var5.dispatchMessagesNative();
		boolean var8 = this.stateMask.get(0);

		long var6;
		for(var6 = var3; 0L < var6 && var8 != var1; var6 -= 10L) {
			try {
				Thread.sleep(10L);
			} catch (InterruptedException var12) {
				;
			}

			var5.dispatchMessagesNative();
			var8 = this.stateMask.get(0);
		}

		if(var1 != var8) {
			String var9 = "Visibility not reached as requested within " + var3 + "ms : requested " + var1 + ", is " + var8;
			if(DEBUG_FREEZE_AT_VISIBILITY_FAILURE) {
				System.err.println("XXXX: " + var9);
				System.err.println("XXXX: FREEZE");

				try {
					while(true) {
						Thread.sleep(100L);
						var5.dispatchMessagesNative();
					}
				} catch (InterruptedException var11) {
					ExceptionUtils.dumpThrowable("", var11);
					Thread.currentThread().interrupt();
					throw new NativeWindowException(var9);
				}
			} else if(var2) {
				throw new NativeWindowException(var9);
			} else {
				if(DEBUG_IMPLEMENTATION) {
					System.err.println(var9);
					ExceptionUtils.dumpStack(System.err);
				}

				return -1L;
			}
		} else {
			return 0L < var6?var6:0L;
		}
	}

	public final void pixelScaleChangeNotify(float[] var1, float[] var2, boolean var3) {
		System.arraycopy(var1, 0, this.minPixelScale, 0, 2);
		System.arraycopy(var2, 0, this.maxPixelScale, 0, 2);
		if(var3) {
			this.setSurfaceScale(this.reqPixelScale);
		}

	}

	protected void sizeChanged(boolean var1, int var2, int var3, boolean var4) {
		if(var4 || this.getWidth() != var2 || this.getHeight() != var3) {
			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.sizeChanged: (" + getThreadName() + "): (defer: " + var1 + ") force " + var4 + ", " + this.getWidth() + "x" + this.getHeight() + " -> " + var2 + "x" + var3 + ", state " + this.getStateMaskString() + " - windowHandle " + toHexString(this.windowHandle) + " parentWindowHandle " + toHexString(this.parentWindowHandle));
			}

			if(0 > var2 || 0 > var3) {
				throw new NativeWindowException("Illegal width or height " + var2 + "x" + var3 + " (must be >= 0)");
			}

			this.defineSize(var2, var3);
			if(this.isNativeValid()) {
				if(!var1) {
					this.sendWindowEvent(100);
				} else {
					this.enqueueWindowEvent(false, 100);
				}
			}
		}

	}

	private boolean waitForSize(int var1, int var2, boolean var3, long var4) {
		DisplayImpl var6 = (DisplayImpl)this.screen.getDisplay();
		var6.dispatchMessagesNative();

		long var7;
		for(var7 = var4; 0L < var7 && var1 != this.getWidth() && var2 != this.getHeight(); var7 -= 10L) {
			try {
				Thread.sleep(10L);
			} catch (InterruptedException var10) {
				;
			}

			var6.dispatchMessagesNative();
		}

		if(0L >= var7) {
			String var9 = "Size/Pos not reached as requested within " + var4 + "ms : requested " + var1 + "x" + var2 + ", is " + this.getWidth() + "x" + this.getHeight();
			if(var3) {
				throw new NativeWindowException(var9);
			} else {
				if(DEBUG_IMPLEMENTATION) {
					System.err.println(var9);
					ExceptionUtils.dumpStack(System.err);
				}

				return false;
			}
		} else {
			return true;
		}
	}

	protected final void positionChanged(boolean var1, int var2, int var3) {
		if(this.getX() == var2 && this.getY() == var3) {
			this.stateMask.clear(1);
		} else {
			if(DEBUG_IMPLEMENTATION) {
				System.err.println("Window.positionChanged: (" + getThreadName() + "): (defer: " + var1 + ") " + this.getX() + "/" + this.getY() + " -> " + var2 + "/" + var3 + " - windowHandle " + toHexString(this.windowHandle) + " parentWindowHandle " + toHexString(this.parentWindowHandle));
			}

			this.definePosition(var2, var3);
			if(!var1) {
				this.sendWindowEvent(101);
			} else {
				this.enqueueWindowEvent(false, 101);
			}
		}

	}

	private boolean waitForPosition(boolean var1, int var2, int var3, long var4) {
		DisplayImpl var6 = (DisplayImpl)this.screen.getDisplay();
		InsetsImmutable var9 = this.getInsets();
		int var7 = Math.max(64, var9.getLeftWidth() * 2);
		int var8 = Math.max(64, var9.getTopHeight() * 2);
		long var15 = var4;
		boolean var11 = false;

		boolean var12;
		do {
			if(!var1) {
				var11 = this.stateMask.get(1);
				var12 = !var11;
			} else {
				var12 = Math.abs(var2 - this.getX()) <= var7 && Math.abs(var3 - this.getY()) <= var8;
			}

			if(!var12) {
				try {
					Thread.sleep(10L);
				} catch (InterruptedException var14) {
					;
				}

				var6.dispatchMessagesNative();
				var15 -= 10L;
			}
		} while(0L < var15 && !var12);

		if(DEBUG_IMPLEMENTATION && !var12) {
			if(var1) {
				System.err.println("Custom position " + var2 + "/" + var3 + " not reached within timeout, has " + this.getX() + "/" + this.getY() + ", remaining " + var15);
			} else {
				System.err.println("Auto position not reached within timeout, has " + this.getX() + "/" + this.getY() + ", autoPosition " + var11 + ", remaining " + var15);
			}

			ExceptionUtils.dumpStack(System.err);
		}

		return var12;
	}

	protected void insetsChanged(boolean var1, int var2, int var3, int var4, int var5) {
		if(var2 >= 0 && var3 >= 0 && var4 >= 0 && var5 >= 0) {
			boolean var6 = var2 != this.insets.getLeftWidth() || var3 != this.insets.getRightWidth() || var4 != this.insets.getTopHeight() || var5 != this.insets.getBottomHeight();
			if(!this.blockInsetsChange && !this.isUndecorated()) {
				if(var6) {
					if(DEBUG_IMPLEMENTATION) {
						System.err.println("Window.insetsChanged (defer: " + var1 + "): Changed " + this.insets + " -> " + new Insets(var2, var3, var4, var5));
					}

					this.insets.set(var2, var3, var4, var5);
				}
			} else if(DEBUG_IMPLEMENTATION && var6) {
				System.err.println("Window.insetsChanged (defer: " + var1 + "): Skip insets change " + this.insets + " -> " + new Insets(var2, var3, var4, var5) + " (blocked " + this.blockInsetsChange + ", undecoration " + this.isUndecorated() + ")");
			}
		}

	}

	public final boolean windowDestroyNotify(boolean var1) {
		WindowClosingMode var2 = this.getDefaultCloseOperation();
		WindowClosingMode var3 = var1? WindowClosingMode.DISPOSE_ON_CLOSE:var2;
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.windowDestroyNotify(isNativeValid: " + this.isNativeValid() + ", force: " + var1 + ", mode " + var2 + " -> " + var3 + ") " + getThreadName() + ": " + this);
		}

		boolean var4;
		if(this.isNativeValid()) {
			if(WindowClosingMode.DISPOSE_ON_CLOSE == var3) {
				if(var1) {
					this.setDefaultCloseOperation(var3);
				}

				try {
					if(null == this.windowDestroyNotifyAction) {
						this.destroy();
					} else {
						this.windowDestroyNotifyAction.run();
					}
				} finally {
					if(var1) {
						this.setDefaultCloseOperation(var2);
					}

				}
			} else {
				this.sendWindowEvent(102);
			}

			var4 = !this.isNativeValid();
		} else {
			var4 = true;
		}

		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.windowDestroyNotify(isNativeValid: " + this.isNativeValid() + ", force: " + var1 + ", mode " + var3 + ") END " + getThreadName() + ": destroyed " + var4 + ", " + this);
		}

		return var4;
	}

	public final void windowRepaint(int var1, int var2, int var3, int var4) {
		this.windowRepaint(false, var1, var2, var3, var4);
	}

	protected final void windowRepaint(boolean var1, int var2, int var3, int var4, int var5) {
		var4 = 0 >= var4?this.getSurfaceWidth():var4;
		var5 = 0 >= var5?this.getSurfaceHeight():var5;
		if(DEBUG_IMPLEMENTATION) {
			System.err.println("Window.windowRepaint " + getThreadName() + " (defer: " + var1 + ") " + var2 + "/" + var3 + " " + var4 + "x" + var5);
		}

		if(this.isNativeValid()) {
			WindowUpdateEvent var6 = new WindowUpdateEvent((short)105, this, System.currentTimeMillis(), new Rectangle(var2, var3, var4, var5));
			this.doEvent(var1, false, var6);
		}

	}

	protected final void sendMouseEventRequestFocus(short var1, int var2, int var3, int var4, short var5, float var6) {
		this.sendMouseEvent(var1, var2, var3, var4, var5, var6);
		this.requestFocus(false);
	}

	protected final void visibleChangedSendMouseEvent(boolean var1, int var2, short var3, int var4, int var5, int var6, short var7, float var8) {
		if(0 <= var2) {
			this.visibleChanged(var1, 0 < var2);
		}

		if(0 < var3) {
			if(var1) {
				this.enqueueMouseEvent(false, var3, var4, var5, var6, var7, var8);
			} else {
				this.sendMouseEvent(var3, var4, var5, var6, var7, var8);
			}
		}

	}

	protected final void visibleChangedWindowRepaint(boolean var1, int var2, int var3, int var4, int var5, int var6) {
		if(0 <= var2) {
			this.visibleChanged(var1, 0 < var2);
		}

		this.windowRepaint(var1, var3, var4, var5, var6);
	}

	protected final void focusVisibleChanged(boolean var1, int var2, int var3) {
		if(0 <= var2) {
			this.focusChanged(var1, 0 < var2);
		}

		if(0 <= var3) {
			this.visibleChanged(var1, 0 < var3);
		}

	}

	protected final void insetsVisibleChanged(boolean var1, int var2, int var3, int var4, int var5, int var6) {
		this.insetsChanged(var1, var2, var3, var4, var5);
		if(0 <= var6) {
			this.visibleChanged(var1, 0 < var6);
		}

	}

	protected final void sizePosInsetsFocusVisibleChanged(boolean var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, boolean var12) {
		this.sizeChanged(var1, var4, var5, var12);
		this.positionChanged(var1, var2, var3);
		this.insetsChanged(var1, var6, var7, var8, var9);
		if(0 <= var10) {
			this.focusChanged(var1, 0 < var10);
		}

		if(0 <= var11) {
			this.visibleChanged(var1, 0 < var11);
		}

	}

	protected final void sizePosMaxInsetsVisibleChanged(boolean var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, boolean var13) {
		this.sizeChanged(var1, var4, var5, var13);
		this.positionChanged(var1, var2, var3);
		if(0 <= var6 && 0 <= var7) {
			this.maximizedChanged(0 < var6, 0 < var7);
		}

		this.insetsChanged(var1, var8, var9, var10, var11);
		if(0 <= var12) {
			this.visibleChanged(var1, 0 < var12);
		}

	}

	private static Class<?>[] getCustomConstructorArgumentTypes(Class<?> var0) {
		Class[] var1 = null;

		try {
			Method var2 = var0.getDeclaredMethod("getCustomConstructorArgumentTypes", new Class[0]);
			var1 = (Class[])((Class[])var2.invoke((Object)null, (Object[])null));
		} catch (Throwable var3) {
			;
		}

		return var1;
	}

	private static int verifyConstructorArgumentTypes(Class<?>[] var0, Object[] var1) {
		if(var0.length != var1.length) {
			return -1;
		} else {
			for(int var2 = 0; var2 < var1.length; ++var2) {
				if(!var0[var2].isInstance(var1[var2])) {
					return var2;
				}
			}

			return var1.length;
		}
	}

	private static String getArgsStrList(Object[] var0) {
		StringBuilder var1 = new StringBuilder();

		for(int var2 = 0; var2 < var0.length; ++var2) {
			var1.append(var0[var2].getClass());
			if(var2 < var0.length) {
				var1.append(", ");
			}
		}

		return var1.toString();
	}

	private static String getTypeStrList(Class<?>[] var0) {
		StringBuilder var1 = new StringBuilder();

		for(int var2 = 0; var2 < var0.length; ++var2) {
			var1.append(var0[var2]);
			if(var2 < var0.length) {
				var1.append(", ");
			}
		}

		return var1.toString();
	}

	public static String getThreadName() {
		return Display.getThreadName();
	}

	public static String toHexString(int var0) {
		return Display.toHexString(var0);
	}

	public static String toHexString(long var0) {
		return Display.toHexString(var0);
	}

	static {
		Debug.initSingleton();
		DEBUG_TEST_REPARENT_INCOMPATIBLE = PropertyAccess.isPropertyDefined("newt.test.Window.reparent.incompatible", true);
		DEBUG_FREEZE_AT_VISIBILITY_FAILURE = PropertyAccess.isPropertyDefined("newt.debug.Window.visibility.failure.freeze", true);
		ScreenImpl.initSingleton();
		windowList = new ArrayList();
		constMousePointerTypes = new PointerType[]{PointerType.Mouse};
		quirks = Factory.synchronize(Factory.create(32));
	}

	private class MonitorModeListenerImpl implements MonitorModeListener
	{
		boolean animatorPaused;
		boolean hidden;
		boolean hadFocus;
		boolean fullscreenPaused;
		List<MonitorDevice> _fullscreenMonitors;
		boolean _fullscreenUseMainMonitor;

		private MonitorModeListenerImpl() {
			this.animatorPaused = false;
			this.hidden = false;
			this.hadFocus = false;
			this.fullscreenPaused = false;
			this._fullscreenMonitors = null;
			this._fullscreenUseMainMonitor = true;
		}

		public void monitorModeChangeNotify(MonitorEvent var1) {
			this.hadFocus = WindowImpl.this.hasFocus();
			boolean var2 = WindowImpl.this.stateMask.get(11);
			boolean var3 = NativeWindowFactory.TYPE_MACOSX == NativeWindowFactory.getNativeWindowType(true);
			boolean var4 = var2 && WindowImpl.this.isReconfigureMaskSupported(16384);
			boolean var5 = !var4 && !var2 && WindowImpl.this.isVisible() && var3;
			if(Window.DEBUG_IMPLEMENTATION) {
				System.err.println("Window.monitorModeChangeNotify: hadFocus " + this.hadFocus + ", qFSPause " + var4 + ", qHide " + var5 + ", " + var1 + " @ " + Thread.currentThread().getName());
			}

			if(null != WindowImpl.this.lifecycleHook) {
				this.animatorPaused = WindowImpl.this.lifecycleHook.pauseRenderingAction();
			}

			if(var4) {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window.monitorModeChangeNotify: FS Pause");
				}

				this.fullscreenPaused = true;
				this._fullscreenMonitors = WindowImpl.this.fullscreenMonitors;
				this._fullscreenUseMainMonitor = WindowImpl.this.stateMask.get(31);
				WindowImpl.this.setFullscreenImpl(false, true, (List)null);
			}

			if(var5) {
				this.hidden = true;
				WindowImpl.this.setVisible(false);
			}

		}

		public void monitorModeChanged(MonitorEvent var1, boolean var2) {
			if(!this.animatorPaused && var2 && null != WindowImpl.this.lifecycleHook) {
				this.animatorPaused = WindowImpl.this.lifecycleHook.pauseRenderingAction();
			}

			boolean var3 = WindowImpl.this.stateMask.get(11);
			if(Window.DEBUG_IMPLEMENTATION) {
				System.err.println("Window.monitorModeChanged.0: success: " + var2 + ", hadFocus " + this.hadFocus + ", animPaused " + this.animatorPaused + ", hidden " + this.hidden + ", FS " + var3 + ", FS-paused " + this.fullscreenPaused + " @ " + Thread.currentThread().getName());
				System.err.println("Window.monitorModeChanged.0: " + WindowImpl.this.getScreen());
				System.err.println("Window.monitorModeChanged.0: " + var1);
			}

			WindowImpl.this.monitorModeChanged(var1, var2);
			Rectangle var5;
			if(var2 && !var3 && !this.fullscreenPaused) {
				RectangleImmutable var7 = WindowImpl.this.screen.getViewportInWindowUnits();
				if(var7.getWidth() > 0 && var7.getHeight() > 0) {
					var5 = new Rectangle(WindowImpl.this.getX(), WindowImpl.this.getY(), WindowImpl.this.getWidth(), WindowImpl.this.getHeight());
					RectangleImmutable var8 = var7.intersection(var5);
					if(WindowImpl.this.getHeight() > var8.getHeight() || WindowImpl.this.getWidth() > var8.getWidth()) {
						if(Window.DEBUG_IMPLEMENTATION) {
							System.err.println("Window.monitorModeChanged.1: Non-FS - Fit window " + var5 + " into screen viewport " + var7 + ", due to minimal intersection " + var8);
						}

						WindowImpl.this.definePosition(var7.getX(), var7.getY());
						WindowImpl.this.setSize(var7.getWidth(), var7.getHeight(), true);
					}
				}
			} else if(this.fullscreenPaused) {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window.monitorModeChanged.2: FS Restore");
				}

				WindowImpl.this.setFullscreenImpl(true, this._fullscreenUseMainMonitor, this._fullscreenMonitors);
				this.fullscreenPaused = false;
				this._fullscreenMonitors = null;
				this._fullscreenUseMainMonitor = true;
			} else if(var2 && var3 && null != WindowImpl.this.fullscreenMonitors) {
				MonitorDevice var4 = var1.getMonitor();
				if(WindowImpl.this.fullscreenMonitors.contains(var4)) {
					var5 = new Rectangle();
					MonitorDevice.unionOfViewports((Rectangle)null, var5, WindowImpl.this.fullscreenMonitors);
					if(Window.DEBUG_IMPLEMENTATION) {
						Rectangle var6 = WindowImpl.this.getBounds();
						System.err.println("Window.monitorModeChanged.3: FS Monitor Match: Fit window " + var6 + " into new viewport union " + var5 + " [window], provoked by " + var4);
					}

					WindowImpl.this.definePosition(var5.getX(), var5.getY());
					WindowImpl.this.setSize(var5.getWidth(), var5.getHeight(), true);
				}
			}

			if(this.hidden) {
				WindowImpl.this.setVisible(true);
				this.hidden = false;
			}

			WindowImpl.this.sendWindowEvent(100);
			if(this.animatorPaused) {
				WindowImpl.this.lifecycleHook.resumeRenderingAction();
			}

			if(this.hadFocus) {
				WindowImpl.this.requestFocus(true);
			}

			if(Window.DEBUG_IMPLEMENTATION) {
				System.err.println("Window.monitorModeChanged.X: @ " + Thread.currentThread().getName() + ", this: " + WindowImpl.this);
			}

		}
	}

	private class FullScreenAction implements Runnable {
		boolean _fullscreen;

		private FullScreenAction() {
		}

		private boolean init(boolean var1) {
			if(WindowImpl.this.isNativeValid()) {
				if(!WindowImpl.this.isReconfigureMaskSupported(2048)) {
					return false;
				} else {
					this._fullscreen = var1;
					return WindowImpl.this.isFullscreen() != var1;
				}
			} else {
				WindowImpl.this.stateMask.put(11, var1);
				return false;
			}
		}

		public boolean fsOn() {
			return this._fullscreen;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();
			WindowImpl.this.blockInsetsChange = true;

			try {
				int var2 = WindowImpl.this.getX();
				int var3 = WindowImpl.this.getY();
				int var4 = WindowImpl.this.getWidth();
				int var5 = WindowImpl.this.getHeight();
				RectangleImmutable var10 = WindowImpl.this.screen.getViewportInWindowUnits();
				int var6;
				int var7;
				int var8;
				int var9;
				Rectangle var11;
				boolean var12;
				boolean var13;
				if(this._fullscreen) {
					if(null == WindowImpl.this.fullscreenMonitors) {
						if(WindowImpl.this.stateMask.get(31)) {
							WindowImpl.this.fullscreenMonitors = new ArrayList();
							WindowImpl.this.fullscreenMonitors.add(WindowImpl.this.getMainMonitor());
						} else {
							WindowImpl.this.fullscreenMonitors = WindowImpl.this.getScreen().getMonitorDevices();
						}
					}

					Rectangle var34 = new Rectangle();
					MonitorDevice.unionOfViewports((Rectangle)null, var34, WindowImpl.this.fullscreenMonitors);
					var11 = var34;
					if(!WindowImpl.this.isReconfigureMaskSupported(16384) || WindowImpl.this.fullscreenMonitors.size() <= 1 && var10.compareTo(var34) <= 0) {
						WindowImpl.this.stateMask.clear(14);
					} else {
						WindowImpl.this.stateMask.set(14);
					}

					WindowImpl.this.nfs_x = var2;
					WindowImpl.this.nfs_y = var3;
					WindowImpl.this.nfs_width = var4;
					WindowImpl.this.nfs_height = var5;
					WindowImpl.this.stateMaskNFS.put32(0, 32, WindowImpl.this.stateMask.get32(0, 32) & 1824);
					var6 = var34.getX();
					var7 = var34.getY();
					var8 = var34.getWidth();
					var9 = var34.getHeight();
					WindowImpl.this.stateMask.clear(5);
					WindowImpl.this.stateMask.set(8);
					var12 = WindowImpl.this.stateMaskNFS.get(5);
					var13 = !WindowImpl.this.stateMaskNFS.get(8);
				} else {
					WindowImpl.this.stateMask.set(31);
					WindowImpl.this.fullscreenMonitors = null;
					WindowImpl.this.stateMask.clear(14);
					var11 = null;
					int var14 = WindowImpl.this.nfs_x;
					int var15 = WindowImpl.this.nfs_y;
					int var16 = WindowImpl.this.nfs_width;
					int var17 = WindowImpl.this.nfs_height;
					var12 = WindowImpl.this.stateMaskNFS.get(5) != WindowImpl.this.stateMask.get(5);
					var13 = WindowImpl.this.stateMaskNFS.get(8) != WindowImpl.this.stateMask.get(8);
					WindowImpl.this.stateMask.put32(0, 32, WindowImpl.this.stateMaskNFS.get32(0, 32) | WindowImpl.this.stateMask.get32(0, 32) & -1825);
					if(null != WindowImpl.this.parentWindow) {
						var6 = 0;
						var7 = 0;
						if(var16 > WindowImpl.this.parentWindow.getWidth()) {
							var8 = WindowImpl.this.parentWindow.getWidth();
						} else {
							var8 = var16;
						}

						if(var17 > WindowImpl.this.parentWindow.getHeight()) {
							var9 = WindowImpl.this.parentWindow.getHeight();
						} else {
							var9 = var17;
						}
					} else {
						var6 = var14;
						var7 = var15;
						var8 = var16;
						var9 = var17;
					}
				}

				DisplayImpl var35 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
				var35.dispatchMessagesNative();
				boolean var36 = WindowImpl.this.isVisible();
				boolean var37 = !this._fullscreen && var36 && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true);
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window " + var6 + "/" + var7 + " " + var8 + "x" + var9 + ", virtl-screenSize: " + var10 + " [wu], monitorsViewport " + var11 + " [wu]" + ", wasVisible " + var36 + ", tempInvisible " + var37 + ", hasParent " + (null != WindowImpl.this.parentWindow) + ", state " + WindowImpl.this.getStateMaskString() + " @ " + Thread.currentThread().getName());
				}

				if(var37) {
					WindowImpl.this.setVisibleImpl(false, true, var2, var3, var4, var5);
					WindowImpl.this.waitForVisible(false, false);

					try {
						Thread.sleep(100L);
					} catch (InterruptedException var31) {
						;
					}

					var35.dispatchMessagesNative();
				}

				NativeWindow var38;
				if(null != WindowImpl.this.parentWindow) {
					var38 = WindowImpl.this.parentWindow;
					if(1 >= var38.lockSurface()) {
						throw new NativeWindowException("Parent surface lock: not ready: " + WindowImpl.this.parentWindow);
					}
				} else {
					var38 = null;
				}

				int var18;
				try {
					int var19 = 0;
					if(var12) {
						var19 = 134217728;
					}

					if(var13) {
						var19 |= 16777216;
					}

					var18 = var19;
					if(this._fullscreen && 0 != var19) {
						WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(var19, WindowImpl.this.isVisible()));
					}

					WindowImpl.this.stateMask.put(11, this._fullscreen);
					WindowImpl.this.reconfigureWindowImpl(var6, var7, var8, var9, WindowImpl.this.getReconfigureMask((null != var38?536870912:0) | 2097152 | 268435456, WindowImpl.this.isVisible()));
				} finally {
					if(null != var38) {
						var38.unlockSurface();
					}

				}

				var35.dispatchMessagesNative();
				if(var36) {
					if(NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
						try {
							Thread.sleep(100L);
						} catch (InterruptedException var30) {
							;
						}

						var35.dispatchMessagesNative();
					}

					WindowImpl.this.setVisibleImpl(true, true, var6, var7, var8, var9);
					boolean var39 = 0L <= WindowImpl.this.waitForVisible(true, false);
					if(var39) {
						var39 = WindowImpl.this.waitForSize(var8, var9, false, 1000L);
					}

					if(var39 && !this._fullscreen && null == WindowImpl.this.parentWindow) {
						WindowImpl.this.waitForPosition(true, var6, var7, 1000L);
					}

					if(var39) {
						if(!this._fullscreen && 0 != var18) {
							WindowImpl.this.reconfigureWindowImpl(var6, var7, var8, var9, WindowImpl.this.getReconfigureMask(var18, WindowImpl.this.isVisible()));
						}

						if(WindowImpl.this.isAlwaysOnBottom()) {
							WindowImpl.this.reconfigureWindowImpl(var6, var7, var8, var9, WindowImpl.this.getReconfigureMask(67108864, WindowImpl.this.isVisible()));
						}

						if(WindowImpl.this.isSticky()) {
							WindowImpl.this.reconfigureWindowImpl(var6, var7, var8, var9, WindowImpl.this.getReconfigureMask(33554432, WindowImpl.this.isVisible()));
						}
					}

					if(var39) {
						WindowImpl.this.requestFocusInt(this._fullscreen);
						var35.dispatchMessagesNative();
					}

					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window fs done: ok " + var39 + ", " + WindowImpl.this);
					}
				}
			} finally {
				WindowImpl.this.blockInsetsChange = false;
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class SetPositionAction implements Runnable {
		int x;
		int y;

		private SetPositionAction(int var2, int var3) {
			this.x = var2;
			this.y = var3;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window setPosition: " + WindowImpl.this.getX() + "/" + WindowImpl.this.getY() + " -> " + this.x + "/" + this.y + ", fs " + WindowImpl.this.stateMask.get(11) + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle));
				}

				if(!WindowImpl.this.isFullscreen() && (WindowImpl.this.getX() != this.x || WindowImpl.this.getY() != this.y || null != WindowImpl.this.getParent())) {
					if(WindowImpl.this.isNativeValid()) {
						WindowImpl.this.reconfigureWindowImpl(this.x, this.y, WindowImpl.this.getWidth(), WindowImpl.this.getHeight(), WindowImpl.this.getReconfigureMask(0, WindowImpl.this.isVisible()));
						if(null == WindowImpl.this.parentWindow) {
							WindowImpl.this.waitForPosition(true, this.x, this.y, 1000L);
						}
					} else {
						WindowImpl.this.definePosition(this.x, this.y);
					}
				}
			} finally {
				var1.unlock();
			}

		}
	}

	private class MaximizeAction implements Runnable {
		boolean horz;
		boolean vert;

		private MaximizeAction(boolean var2, boolean var3) {
			this.horz = var2;
			this.vert = var3;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				int var2 = 0;
				if(WindowImpl.this.stateMask.put(9, this.vert) != this.vert) {
					var2 |= 8388608;
				}

				if(WindowImpl.this.stateMask.put(10, this.horz) != this.horz) {
					var2 |= 4194304;
				}

				if(0 != var2 && WindowImpl.this.isNativeValid()) {
					boolean var3 = WindowImpl.this.hasFocus();
					int var4 = WindowImpl.this.getX();
					int var5 = WindowImpl.this.getY();
					int var6 = WindowImpl.this.getWidth();
					int var7 = WindowImpl.this.getHeight();
					DisplayImpl var8 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var8.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var4, var5, var6, var7, WindowImpl.this.getReconfigureMask(var2, WindowImpl.this.isVisible()));
					var8.dispatchMessagesNative();
					if(var3) {
						WindowImpl.this.requestFocusInt(0L == WindowImpl.this.parentWindowHandle);
					}
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class StickyAction implements Runnable {
		boolean sticky;

		private StickyAction(boolean var2) {
			this.sticky = var2;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(WindowImpl.this.stateMask.put(7, this.sticky) != this.sticky && WindowImpl.this.isNativeValid()) {
					int var2 = WindowImpl.this.getX();
					int var3 = WindowImpl.this.getY();
					int var4 = WindowImpl.this.getWidth();
					int var5 = WindowImpl.this.getHeight();
					DisplayImpl var6 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var6.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(33554432, WindowImpl.this.isVisible()));
					var6.dispatchMessagesNative();
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class ResizableAction implements Runnable {
		boolean resizable;

		private ResizableAction(boolean var2) {
			this.resizable = var2;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(WindowImpl.this.stateMask.put(8, this.resizable) != this.resizable && WindowImpl.this.isNativeValid()) {
					int var2 = WindowImpl.this.getX();
					int var3 = WindowImpl.this.getY();
					int var4 = WindowImpl.this.getWidth();
					int var5 = WindowImpl.this.getHeight();
					DisplayImpl var6 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var6.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(16777216, WindowImpl.this.isVisible()));
					var6.dispatchMessagesNative();
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class AlwaysOnBottomAction implements Runnable {
		boolean alwaysOnBottom;

		private AlwaysOnBottomAction(boolean var2) {
			this.alwaysOnBottom = var2;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(WindowImpl.this.stateMask.put(6, this.alwaysOnBottom) != this.alwaysOnBottom && WindowImpl.this.isNativeValid()) {
					int var2 = WindowImpl.this.getX();
					int var3 = WindowImpl.this.getY();
					int var4 = WindowImpl.this.getWidth();
					int var5 = WindowImpl.this.getHeight();
					DisplayImpl var6 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var6.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(67108864, WindowImpl.this.isVisible()));
					var6.dispatchMessagesNative();
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class AlwaysOnTopAction implements Runnable {
		boolean alwaysOnTop;

		private AlwaysOnTopAction(boolean var2) {
			this.alwaysOnTop = var2;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(WindowImpl.this.stateMask.put(5, this.alwaysOnTop) != this.alwaysOnTop && WindowImpl.this.isNativeValid() && !WindowImpl.this.isFullscreen()) {
					int var2 = WindowImpl.this.getX();
					int var3 = WindowImpl.this.getY();
					int var4 = WindowImpl.this.getWidth();
					int var5 = WindowImpl.this.getHeight();
					DisplayImpl var6 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var6.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(134217728, WindowImpl.this.isVisible()));
					var6.dispatchMessagesNative();
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class DecorationAction implements Runnable {
		boolean undecorated;

		private DecorationAction(boolean var2) {
			this.undecorated = var2;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(WindowImpl.this.stateMask.put(4, this.undecorated) != this.undecorated && WindowImpl.this.isNativeValid() && !WindowImpl.this.isFullscreen()) {
					int var2 = WindowImpl.this.getX();
					int var3 = WindowImpl.this.getY();
					int var4 = WindowImpl.this.getWidth();
					int var5 = WindowImpl.this.getHeight();
					DisplayImpl var6 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
					var6.dispatchMessagesNative();
					WindowImpl.this.reconfigureWindowImpl(var2, var3, var4, var5, WindowImpl.this.getReconfigureMask(268435456, WindowImpl.this.isVisible()));
					var6.dispatchMessagesNative();
				}
			} finally {
				var1.unlock();
			}

			WindowImpl.this.sendWindowEvent(100);
		}
	}

	private class ReparentAction implements Runnable {
		final NativeWindow newParentWindow;
		final int topLevelX;
		final int topLevelY;
		final int hints;
		ReparentOperation operation;

		private ReparentAction(NativeWindow var2, int var3, int var4, int var5) {
			this.newParentWindow = var2;
			this.topLevelX = var3;
			this.topLevelY = var4;
			if(WindowImpl.DEBUG_TEST_REPARENT_INCOMPATIBLE) {
				var5 |= 1;
			}

			this.hints = var5;
			this.operation = ReparentOperation.ACTION_INVALID;
		}

		private ReparentOperation getOp() {
			return this.operation;
		}

		public final void run() {
			if(WindowImpl.this.isFullscreen()) {
				if(Window.DEBUG_IMPLEMENTATION) {
					System.err.println("Window.reparent: NOP (in fullscreen, " + Display.getThreadName() + ") valid " + WindowImpl.this.isNativeValid() + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + ", state " + WindowImpl.this.getStateMaskString());
				}

			} else {
				boolean var1 = false;
				if(null != WindowImpl.this.lifecycleHook) {
					var1 = WindowImpl.this.lifecycleHook.pauseRenderingAction();
				}

				this.reparent();
				if(var1) {
					WindowImpl.this.lifecycleHook.resumeRenderingAction();
				}

			}
		}

		private void reparent() {
			int var1 = WindowImpl.this.getX();
			int var2 = WindowImpl.this.getY();
			int var3 = WindowImpl.this.getWidth();
			int var4 = WindowImpl.this.getHeight();
			int var7 = var3;
			int var8 = var4;
			RecursiveLock var12 = WindowImpl.this.windowLock;
			var12.lock();

			boolean var9;
			label892: {
				try {
					boolean var13 = 0 != (1 & this.hints);
					if(WindowImpl.this.isNativeValid()) {
						var13 |= WindowImpl.isOffscreenInstance(WindowImpl.this, this.newParentWindow);
					}

					boolean var11 = var13;
					var9 = WindowImpl.this.isVisible();
					boolean var10 = var9 || 0 != (2 & this.hints);
					Window var30 = null;
					if(this.newParentWindow instanceof Window) {
						var30 = (Window)this.newParentWindow;
					}

					long var14 = 0L;
					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window.reparent: START (" + Display.getThreadName() + ") valid " + WindowImpl.this.isNativeValid() + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + ", state " + WindowImpl.this.getStateMaskString() + " -> visible " + var10 + ", forceDestroyCreate " + var11 + ", DEBUG_TEST_REPARENT_INCOMPATIBLE " + WindowImpl.DEBUG_TEST_REPARENT_INCOMPATIBLE + ", HINT_FORCE_RECREATION " + (0 != (1 & this.hints)) + ", HINT_BECOMES_VISIBLE " + (0 != (2 & this.hints)) + ", old parentWindow: " + Display.hashCodeNullSafe(WindowImpl.this.parentWindow) + ", new parentWindow: " + Display.hashCodeNullSafe(this.newParentWindow));
					}

					int var5;
					int var6;
					if(null != this.newParentWindow) {
						var5 = 0;
						var6 = 0;
						if(var7 > this.newParentWindow.getWidth()) {
							var7 = this.newParentWindow.getWidth();
						}

						if(var8 > this.newParentWindow.getHeight()) {
							var8 = this.newParentWindow.getHeight();
						}

						var14 = WindowImpl.getNativeWindowHandle(this.newParentWindow);
						if(0L == var14) {
							if(null == var30) {
								throw new NativeWindowException("Reparenting with non NEWT Window type only available after it\'s realized: " + this.newParentWindow);
							}

							WindowImpl.this.destroy(var10);
							WindowImpl.this.setScreen((ScreenImpl)var30.getScreen());
							this.operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
						} else if(this.newParentWindow != WindowImpl.this.getParent()) {
							if(!WindowImpl.this.isNativeValid()) {
								if(null != var30) {
									WindowImpl.this.setScreen((ScreenImpl)var30.getScreen());
								} else {
									Screen var31 = NewtFactory.createCompatibleScreen(this.newParentWindow, WindowImpl.this.screen);
									if(WindowImpl.this.screen != var31) {
										WindowImpl.this.setScreen((ScreenImpl)var31);
									}
								}

								if(0 < var7 && 0 < var8) {
									this.operation = ReparentOperation.ACTION_NATIVE_CREATION;
								} else {
									this.operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
								}
							} else if(!var11 && NewtFactory.isScreenCompatible(this.newParentWindow, WindowImpl.this.screen)) {
								this.operation = ReparentOperation.ACTION_NATIVE_REPARENTING;
							} else {
								WindowImpl.this.destroy(var10);
								if(null != var30) {
									WindowImpl.this.setScreen((ScreenImpl)var30.getScreen());
								} else {
									WindowImpl.this.setScreen((ScreenImpl) NewtFactory.createCompatibleScreen(this.newParentWindow, WindowImpl.this.screen));
								}

								this.operation = ReparentOperation.ACTION_NATIVE_CREATION;
							}
						} else {
							this.operation = ReparentOperation.ACTION_NOP;
						}
					} else {
						if(0 <= this.topLevelX && 0 <= this.topLevelY) {
							var5 = this.topLevelX;
							var6 = this.topLevelY;
						} else if(null != WindowImpl.this.parentWindow) {
							Point var16 = WindowImpl.this.getLocationOnScreen((Point)null);
							var5 = var16.getX();
							var6 = var16.getY();
						} else {
							var5 = var1;
							var6 = var2;
						}

						if(0L == WindowImpl.this.parentWindowHandle) {
							this.operation = ReparentOperation.ACTION_NOP;
						} else if(WindowImpl.this.isNativeValid() && !var11) {
							this.operation = ReparentOperation.ACTION_NATIVE_REPARENTING;
						} else {
							WindowImpl.this.destroy(var10);
							if(0 < var7 && 0 < var8) {
								this.operation = ReparentOperation.ACTION_NATIVE_CREATION;
							} else {
								this.operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
							}
						}
					}

					WindowImpl.this.parentWindowHandle = var14;
					if(ReparentOperation.ACTION_INVALID == this.operation) {
						throw new NativeWindowException("Internal Error: reparentAction not set");
					}

					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window.reparent: ACTION (" + Display.getThreadName() + ") windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " new parentWindowHandle " + Display.toHexString(var14) + ", reparentAction " + this.operation + ", pos/size " + var5 + "/" + var6 + " " + var7 + "x" + var8 + ", visible " + var9);
					}

					if(ReparentOperation.ACTION_NOP != this.operation) {
						if(null == this.newParentWindow) {
							WindowImpl.this.setOffscreenPointerIcon((PointerIconImpl)null);
							WindowImpl.this.setOffscreenPointerVisible(true, (PointerIconImpl)null);
						}

						if(null != WindowImpl.this.parentWindow && WindowImpl.this.parentWindow instanceof Window) {
							((Window)WindowImpl.this.parentWindow).removeChild(WindowImpl.this);
						}

						WindowImpl.this.parentWindow = this.newParentWindow;
						WindowImpl.this.stateMask.put(2, null != WindowImpl.this.parentWindow);
						if(WindowImpl.this.parentWindow instanceof Window) {
							((Window)WindowImpl.this.parentWindow).addChild(WindowImpl.this);
						}

						if(ReparentOperation.ACTION_NATIVE_REPARENTING == this.operation) {
							DisplayImpl var32 = (DisplayImpl)WindowImpl.this.screen.getDisplay();
							var32.dispatchMessagesNative();
							if(null != WindowImpl.this.parentWindow && var9 && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
								WindowImpl.this.setVisibleImpl(false, true, var1, var2, var3, var4);
								WindowImpl.this.waitForVisible(false, false);

								try {
									Thread.sleep(100L);
								} catch (InterruptedException var27) {
									;
								}

								var32.dispatchMessagesNative();
							}

							NativeWindow var17;
							if(null != WindowImpl.this.parentWindow) {
								var17 = WindowImpl.this.parentWindow;
								if(1 >= var17.lockSurface()) {
									throw new NativeWindowException("Parent surface lock: not ready: " + var17);
								}

								WindowImpl.this.parentWindowHandle = var17.getWindowHandle();
							} else {
								var17 = null;
							}

							boolean var18 = false;

							try {
								var18 = WindowImpl.this.reconfigureWindowImpl(var5, var6, var7, var8, WindowImpl.this.getReconfigureMask(805306368, WindowImpl.this.isVisible()));
							} finally {
								if(null != var17) {
									var17.unlockSurface();
								}

							}

							WindowImpl.this.definePosition(var5, var6);
							if(var18) {
								var32.dispatchMessagesNative();
								if(var9) {
									WindowImpl.this.setVisibleImpl(true, true, var5, var6, var7, var8);
									var18 = 0L <= WindowImpl.this.waitForVisible(true, false);
									if(var18) {
										if(WindowImpl.this.isAlwaysOnTop() && 0L == WindowImpl.this.parentWindowHandle && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
											WindowImpl.this.reconfigureWindowImpl(var5, var6, var7, var8, WindowImpl.this.getReconfigureMask(134217728, WindowImpl.this.isVisible()));
										}

										var18 = WindowImpl.this.waitForSize(var7, var8, false, 1000L);
									}

									if(var18) {
										if(0L == WindowImpl.this.parentWindowHandle) {
											WindowImpl.this.waitForPosition(true, var5, var6, 1000L);
										}

										WindowImpl.this.requestFocusInt(0L == WindowImpl.this.parentWindowHandle);
										var32.dispatchMessagesNative();
									}
								}
							}

							if(!var18 || !var9) {
								WindowImpl.this.definePosition(var5, var6);
								WindowImpl.this.defineSize(var7, var8);
							}

							if(!var18) {
								if(Window.DEBUG_IMPLEMENTATION) {
									System.err.println("Window.reparent: native reparenting failed (" + Display.getThreadName() + ") windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + " parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + " -> " + Display.toHexString(var14) + " - Trying recreation");
								}

								WindowImpl.this.destroy(var10);
								this.operation = ReparentOperation.ACTION_NATIVE_CREATION;
							} else if(null != WindowImpl.this.parentWindow) {
								WindowImpl.this.setOffscreenPointerIcon(WindowImpl.this.pointerIcon);
								WindowImpl.this.setOffscreenPointerVisible(WindowImpl.this.stateMask.get(12), WindowImpl.this.pointerIcon);
							}
						} else {
							WindowImpl.this.definePosition(var5, var6);
							WindowImpl.this.defineSize(var7, var8);
						}

						if(Window.DEBUG_IMPLEMENTATION) {
							System.err.println("Window.reparent: END-1 (" + Display.getThreadName() + ") state " + WindowImpl.this.getStateMaskString() + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + ", parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + ", parentWindow " + Display.hashCodeNullSafe(WindowImpl.this.parentWindow) + " " + WindowImpl.this.getX() + "/" + WindowImpl.this.getY() + " " + WindowImpl.this.getWidth() + "x" + WindowImpl.this.getHeight());
						}
						break label892;
					}
				} finally {
					if(null != WindowImpl.this.lifecycleHook) {
						WindowImpl.this.lifecycleHook.resetCounter();
					}

					var12.unlock();
				}

				return;
			}

			if(var9) {
				switch(operation) {
					case ACTION_NATIVE_REPARENTING:
						WindowImpl.this.sendWindowEvent(100);
						break;
					case ACTION_NATIVE_CREATION:
						WindowImpl.this.runOnEDTIfAvail(true, WindowImpl.this.reparentActionRecreate);
				}
			}

			if(Window.DEBUG_IMPLEMENTATION) {
				System.err.println("Window.reparent: END-X (" + Display.getThreadName() + ") state " + WindowImpl.this.getStateMaskString() + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + ", parentWindowHandle " + Display.toHexString(WindowImpl.this.parentWindowHandle) + ", parentWindow " + Display.hashCodeNullSafe(WindowImpl.this.parentWindow) + " " + WindowImpl.this.getX() + "/" + WindowImpl.this.getY() + " " + WindowImpl.this.getWidth() + "x" + WindowImpl.this.getHeight());
			}

		}
	}

	private class SetSizeAction implements Runnable {
		int width;
		int height;
		boolean force;

		private SetSizeAction(int var2, int var3, boolean var4) {
			this.width = var2;
			this.height = var3;
			this.force = var4;
		}

		public final void run() {
			RecursiveLock var1 = WindowImpl.this.windowLock;
			var1.lock();

			try {
				if(this.force || !WindowImpl.this.isFullscreen() && (WindowImpl.this.getWidth() != this.width || WindowImpl.this.getHeight() != this.height)) {
					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window setSize: START force " + this.force + ", " + WindowImpl.this.getWidth() + "x" + WindowImpl.this.getHeight() + " -> " + this.width + "x" + this.height + ", windowHandle " + Display.toHexString(WindowImpl.this.windowHandle) + ", state " + WindowImpl.this.getStateMaskString());
					}

					boolean var2 = WindowImpl.this.stateMask.get(0);
					byte var3;
					if(var2 && WindowImpl.this.isNativeValid() && (0 >= this.width || 0 >= this.height)) {
						var3 = 1;
						WindowImpl.this.defineSize(0, 0);
					} else if(var2 && !WindowImpl.this.isNativeValid() && 0 < this.width && 0 < this.height) {
						var3 = 2;
						WindowImpl.this.defineSize(this.width, this.height);
					} else if(var2 && WindowImpl.this.isNativeValid()) {
						var3 = 0;
						WindowImpl.this.reconfigureWindowImpl(WindowImpl.this.getX(), WindowImpl.this.getY(), this.width, this.height, WindowImpl.this.getReconfigureMask(0, WindowImpl.this.isVisible()));
						WindowImpl.this.waitForSize(this.width, this.height, false, 1000L);
					} else {
						var3 = 0;
						WindowImpl.this.defineSize(this.width, this.height);
					}

					if(Window.DEBUG_IMPLEMENTATION) {
						System.err.println("Window setSize: END " + WindowImpl.this.getWidth() + "x" + WindowImpl.this.getHeight() + ", visibleAction " + var3);
					}

					switch(var3) {
						case 1:
							WindowImpl.this.setVisibleActionImpl(false);
							break;
						case 2:
							WindowImpl.this.setVisibleActionImpl(true);
					}
				}
			} finally {
				var1.unlock();
			}

		}
	}

	private class VisibleAction implements Runnable {
		boolean visible;

		private VisibleAction(boolean var2) {
			this.visible = var2;
		}

		public final void run() {
			WindowImpl.this.setVisibleActionImpl(this.visible);
		}
	}

	public interface LifecycleHook {
		void resetCounter();

		void setVisibleActionPost(boolean var1, boolean var2);

		void preserveGLStateAtDestroy(boolean var1);

		void destroyActionPreLock();

		void destroyActionInLock();

		boolean pauseRenderingAction();

		void resumeRenderingAction();

		void shutdownRenderingAction();
	}

	private static class PointerState1 extends WindowImpl.PointerState0 {
		short buttonPressed;
		int buttonPressedMask;
		short lastButtonClickCount;
		final Point[] movePositions;

		private PointerState1() {
			super();
			this.buttonPressed = 0;
			this.buttonPressedMask = 0;
			this.lastButtonClickCount = 0;
			this.movePositions = new Point[]{new Point(), new Point(), new Point(), new Point(), new Point(), new Point(), new Point(), new Point()};
		}

		final void clearButton() {
			super.clearButton();
			this.lastButtonClickCount = 0;
			if(!this.dragging || 0 == this.buttonPressedMask) {
				this.buttonPressed = 0;
				this.buttonPressedMask = 0;
				this.dragging = false;
			}

		}

		final Point getMovePosition(int var1) {
			return 0 <= var1 && var1 < this.movePositions.length?this.movePositions[var1]:null;
		}

		public final String toString() {
			return "PState1[inside " + this.insideSurface + ", exitSent " + this.exitSent + ", lastPress " + this.lastButtonPressTime + ", pressed [button " + this.buttonPressed + ", mask " + this.buttonPressedMask + ", dragging " + this.dragging + ", clickCount " + this.lastButtonClickCount + "]";
		}
	}

	private static class PointerState0 {
		boolean insideSurface;
		boolean exitSent;
		long lastButtonPressTime;
		boolean dragging;

		private PointerState0() {
			this.insideSurface = false;
			this.exitSent = false;
			this.lastButtonPressTime = 0L;
			this.dragging = false;
		}

		void clearButton() {
			this.lastButtonPressTime = 0L;
		}

		public String toString() {
			return "PState0[inside " + this.insideSurface + ", exitSent " + this.exitSent + ", lastPress " + this.lastButtonPressTime + ", dragging " + this.dragging + "]";
		}
	}
}

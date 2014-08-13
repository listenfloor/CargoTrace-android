
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yfrt.cargotrace;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens.
 * Each screen contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends FrameLayout {
	protected static final double TAN30 = Math.tan(Math.toRadians(30));
	protected static final int INVALID_SCREEN = -1;

	/**
	 * The velocity at which a fling gesture will cause us to snap to the next
	 * screen
	 */
	private static final int SNAP_VELOCITY = 350;

	protected int mDefaultScreen;

	private Paint mPaint;
	private Bitmap mWallpaper;

	private int mWallpaperWidth;
	private int mWallpaperHeight;
	private float mWallpaperOffset;
	private boolean mWallpaperLoaded;

	private boolean mFirstLayout = true;

	protected int mCurrentScreen;
	protected int mDestnationScreen;
	protected int mNextScreen = INVALID_SCREEN;
	protected  Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	protected float mLastMotionX;
	protected float mLastMotionY;

	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;

	protected int mTouchState = TOUCH_STATE_REST;

	private OnLongClickListener mLongClickListener;

	private int[] mTempCell = new int[2];

	protected boolean mAllowLongPress;

	protected int mTouchSlop;
	private boolean mAlloweffect = true;

	final Rect mDrawerBounds = new Rect();
	final Rect mClipBounds = new Rect();
	int mDrawerContentHeight;
	int mDrawerContentWidth;
	
	//****************************浠ヤ笅涓烘簮鐮佸鑷繁瀹氫箟鐨勫彉閲�
	private boolean isShowInput; //杈撳叆娉曞脊鍑哄垽鏂�
	
	private Rect mTouchRect = new Rect();

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 */
	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 * @param defStyle
	 *            Unused.
	 */
	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDefaultScreen = 0;// a.getInt(R.styleable.Workspace_defaultScreen, 1);
		initWorkspace();
		setDrawingCacheEnabled(true);
		setAlwaysDrawnWithCacheEnabled(true);
	}

	/**
	 * Initializes various states for this workspace.
	 */
	private void initWorkspace() {
		mScroller = new Scroller(getContext());
		mCurrentScreen = mDefaultScreen;
		mPaint = new Paint();
		mPaint.setDither(false);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	/**
	 * Set the background's wallpaper.
	 */
	void loadWallpaper(Bitmap bitmap) {
		mWallpaper = bitmap;
		mWallpaperLoaded = true;
		requestLayout();
		invalidate();
	}

	boolean isDefaultScreenShowing() {
		return mCurrentScreen == mDefaultScreen;
	}

	public void setAlloweffect(boolean alloweffect){
		mAlloweffect = alloweffect;
	}
	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}
	public int getDesScreen(){
		return mDestnationScreen;
	}
	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen
	 */
	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = Math.max(0,
				Math.min(currentScreen, getChildCount() - 1));
		mDestnationScreen = mCurrentScreen;
		scrollTo(mCurrentScreen * getWidth(), 0);
//		invalidate();
	}

	/**
	 * Shows the default screen (defined by the firstScreen attribute in XML.)
	 */
	void showDefaultScreen() {
		setCurrentScreen(mDefaultScreen);
	}
	
	public void setDefaultScreen(int defaultScreen)
	{
		mDefaultScreen = defaultScreen;
	}

	/**
	 * Registers the specified listener on each screen contained in this
	 * workspace.
	 * 
	 * @param l
	 *            The listener used to respond to long clicks.
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mLongClickListener = l;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			final int whichScreen = mCurrentScreen;
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			mDestnationScreen = mCurrentScreen;
			mNextScreen = INVALID_SCREEN;
			clearChildrenCache();
			if ( onScreenChangeListener != null ) {
				onScreenChangeListener.onScrrenChangeEnd(mCurrentScreen);
			}
		}
	}

	// @Override
	// protected void dispatchDraw(Canvas canvas) {
	// boolean restore = false;
	//
	// // If the all apps drawer is open and the drawing region for the
	// // workspace
	// // is contained within the drawer's bounds, we skip the drawing. This
	// // requires
	// // the drawer to be fully opaque.
	// if (mLauncher.isDrawerUp()) {
	// final Rect clipBounds = mClipBounds;
	// canvas.getClipBounds(clipBounds);
	// clipBounds.offset(-mScrollX, -mScrollY);
	// if (mDrawerBounds.contains(clipBounds)) {
	// return;
	// }
	// } else if (mLauncher.isDrawerMoving()) {
	// restore = true;
	// canvas.save(Canvas.CLIP_SAVE_FLAG);
	//
	// final View view = mLauncher.getDrawerHandle();
	// final int top = view.getTop() + view.getHeight();
	//
	// canvas.clipRect(mScrollX, top, mScrollX + mDrawerContentWidth, top
	// + mDrawerContentHeight, Region.Op.DIFFERENCE);
	// }
	//
	// float x = mScrollX * mWallpaperOffset;
	// if (x + mWallpaperWidth < mRight - mLeft) {
	// x = mRight - mLeft - mWallpaperWidth;
	// }
	//
	// canvas.drawBitmap(mWallpaper, x,
	// (mBottom - mTop - mWallpaperHeight) / 2, mPaint);
	//
	// // ViewGroup.dispatchDraw() supports many features we don't need:
	// // clip to padding, layout animation, animation listener, disappearing
	// // children, etc. The following implementation attempts to fast-track
	// // the drawing dispatch by drawing only what we know needs to be drawn.
	//
	// boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING
	// && mNextScreen == INVALID_SCREEN;
	// // If we are not scrolling or flinging, draw only the current screen
	// if (fastDraw) {
	// drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
	// } else {
	// final long drawingTime = getDrawingTime();
	// // If we are flinging, draw only the current screen and the target
	// // screen
	// if (mNextScreen >= 0 && mNextScreen < getChildCount()
	// && Math.abs(mCurrentScreen - mNextScreen) == 1) {
	// drawChild(canvas, getChildAt(mCurrentScreen), drawingTime);
	// drawChild(canvas, getChildAt(mNextScreen), drawingTime);
	// } else {
	// // If we are scrolling, draw all of our children
	// final int count = getChildCount();
	// for (int i = 0; i < count; i++) {
	// drawChild(canvas, getChildAt(i), drawingTime);
	// }
	// }
	// }
	//
	// if (restore) {
	// canvas.restore();
	// }
	// }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		// if (widthMode != MeasureSpec.EXACTLY) {
		// throw new IllegalStateException(
		// "Workspace can only be used in EXACTLY mode.");
		// }
		//
		// final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		// if (heightMode != MeasureSpec.EXACTLY) {
		// throw new IllegalStateException(
		// "Workspace can only be used in EXACTLY mode.");
		// }

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		final int wallpaperWidth = mWallpaperWidth;
		mWallpaperOffset = wallpaperWidth > width ? (count * width - wallpaperWidth)
				/ ((count - 1) * (float) width)
				: 1.0f;

		if (mFirstLayout) {
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			// 濡傛灉涓嶆槸clickable鍒欎笉鑳芥甯稿洖璋僶nInterceptTouchEvent鏂规硶
//			child.setClickable(true);
//			child.setFocusableInTouchMode(true);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				if (!isShowInput || i == mCurrentScreen) {
					child.layout(childLeft, 0, childLeft + childWidth,
							child.getMeasuredHeight());
				}
				childLeft += childWidth;
			}
		}
		isShowInput = false;
		
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
			boolean immediate) {
		int screen = indexOfChild(child);
		if (screen != mCurrentScreen || !mScroller.isFinished()) {
			snapToScreen(screen);
			return true;
		}
		return false;
	}

	@Override
	protected boolean onRequestFocusInDescendants(int direction,
			Rect previouslyFocusedRect) {

		int focusableScreen;
		if (mNextScreen != INVALID_SCREEN) {
			focusableScreen = mNextScreen;
		} else {
			focusableScreen = mCurrentScreen;
		}
		if(focusableScreen>=getChildCount()) focusableScreen=getChildCount()-1;
		if(getChildAt(focusableScreen) != null)
		{
			getChildAt(focusableScreen).requestFocus(direction,
					previouslyFocusedRect);
		}
		
		return false;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (direction == View.FOCUS_LEFT) {
			if (getCurrentScreen() > 0 ) {
				snapToScreen(getCurrentScreen() - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT ) {
			if (getCurrentScreen() < getChildCount() - 1) {
				snapToScreen(getCurrentScreen() + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public void requestChildFocus(View child, View focused) {
		super.requestChildFocus(child, focused);
		// Handle the event for moving of left or right.
		Rect r = new Rect();
		focused.getDrawingRect(r);
		focused.requestRectangleOnScreen(r);
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction) {

		getChildAt(mCurrentScreen).addFocusables(views, direction);
		if (direction == View.FOCUS_LEFT) {
			if (mCurrentScreen > 0) {
				getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (mCurrentScreen < getChildCount() - 1) {
				getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
			}
		}

	}

	protected boolean find(View view, boolean isLeft, MotionEvent event) {
		if (view instanceof ViewGroup) {
			ViewGroup parent = (ViewGroup) view;
			for (int i = 0; i < parent.getChildCount(); i++) {
				View child = parent.getChildAt(i);
				// QLog.i("System.out", child.getClass());
				if (child instanceof Workspace) {
					Workspace ws = (Workspace) child;
					ws.getGlobalVisibleRect(mTouchRect);
					if (!ws.isShown() || !mTouchRect.contains((int)event.getRawX(), (int)event.getRawY())) {
						return false;
					}
					if (isLeft) {
						if (ws.getCurrentScreen() > 0
								|| getCurrentScreen() == 0) {
							return true;
						} else if (ws.mDestnationScreen != mCurrentScreen) {
							ws.setCurrentScreen(ws.mDestnationScreen);
						}
					} else {
						if (ws.getCurrentScreen() < ws.getChildCount() - 1
								|| getCurrentScreen() == getChildCount() - 1) {
							return true;
						} else if (ws.mDestnationScreen != mCurrentScreen) {
							ws.setCurrentScreen(ws.mDestnationScreen);
						}
					}
				}
				if (child instanceof ViewGroup) {
					ViewGroup cc = (ViewGroup) child;
					if (find(cc, isLeft, event)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			boolean left = (ev.getX() - mLastMotionX) > 0;
			boolean find = find(getChildAt(mDestnationScreen), left, ev);
//			QLog.i("System.out", getTag()+" find "+mDestnationScreen+" "+find+" left:"+left+" "+ev.getAction());
			if (find) {
				return false;
			}
		}
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */

		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have
			 * caught it. Check whether the user has moved far enough from his
			 * original down touch.
			 */

			/*
			 * Locally do absolute value. mLastMotionX is set to the y value of
			 * the down event.
			 */
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;
			double tan = yDiff / (double) xDiff;
			if ((xMoved || yMoved) && tan < TAN30) {// 宸﹀彸婊戝姩瑙掑害锟�锟斤拷锟�0锟�

				if (xMoved) {
					// Scroll if the user moved far enough along the X axis
					mTouchState = TOUCH_STATE_SCROLLING;
					mLastMotionX = x;
					enableChildrenCache();
				}
				// Either way, cancel any pending longpress
				if (mAllowLongPress) {
					mAllowLongPress = false;
					// Try canceling the long press. It could also have been
					// scheduled
					// by a distant descendant, so use the mAllowLongPress flag
					// to block
					// everything
					final View currentScreen = getChildAt(mCurrentScreen);
					currentScreen.cancelLongPress();
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			mLastMotionX = x;
			mLastMotionY = y;
			mAllowLongPress = true;

			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// Release the drag
			clearChildrenCache();
			mTouchState = TOUCH_STATE_REST;
			mAllowLongPress = false;
			break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		View v = getChildAt(mDestnationScreen);
		boolean ret = mTouchState != TOUCH_STATE_REST
				&& (!find(v, true, ev) && !find(v, false, ev));
		return ret;
	}

	void enableChildrenCache() {

	}

	void clearChildrenCache() {

	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		System.out.println("###########################on size changed," + "w=" + w + ",h=" + h
//				+ ",ow=" + oldw + ",oh=" + oldh);
		if (h < oldh && w == oldw) {
			isShowInput = true;
		} else {
			isShowInput = false;
		}
		if (!mScroller.isFinished()) {
			// mScroller.abortAnimation();
			mScroller.forceFinished(true);
		}
		scrollTo(w * mDestnationScreen, 0);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(this.getChildCount()<0){
			return true;
			
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				if(deltaX > 0 &&!mAlloweffect && mCurrentScreen == getChildCount()-1){
					//杩欎釜鍒ゆ柇鐢ㄤ簬瑙ｅ喅婊戝埌鏈�悗涓�紶鍥炬椂锛岃嚜鍔ㄥ氨杩涘叆涓荤▼搴忎簡
					onScreenChangeListener.onScrrenChangeEnd(getChildCount());
					return true;
				}
				mLastMotionX = x;

				if (deltaX < 0) {
					if (getScrollX() > 0) {
						scrollBy(Math.max(-getScrollX(), deltaX), 0);
					}
					else{
						if(mAlloweffect){
							scrollBy(deltaX/3, 0);
						}
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()
							- getScrollX() - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
					else
					{
						if(mAlloweffect){
							scrollBy(deltaX/3, 0);
						}
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}

		return true;
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2))
				/ screenWidth;

		snapToScreen(whichScreen);
	}
	
	public void snapToScreen(int whichScreen, int duration) {
		enableChildrenCache();

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		boolean changingScreens = whichScreen != mCurrentScreen;
		if (mDestnationScreen != whichScreen) {
			mDestnationScreen = whichScreen;
			if (onScreenChangeListener != null) {
				onScreenChangeListener.onScreenChangeStart(whichScreen);
			}
		}
		mNextScreen = whichScreen;
		View focusedChild = getFocusedChild();
		if (focusedChild != null && changingScreens
				&& focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
	}
	
	public void snapToScreen(int whichScreen) {
		enableChildrenCache();

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		boolean changingScreens = whichScreen != mCurrentScreen;
		if (mDestnationScreen != whichScreen) {
			mDestnationScreen = whichScreen;
			if (onScreenChangeListener != null) {
				onScreenChangeListener.onScreenChangeStart(whichScreen);
			}
		}
		mNextScreen = whichScreen;
		View focusedChild = getFocusedChild();
		if (focusedChild != null && changingScreens
				&& focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) );
		invalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState state = new SavedState(super.onSaveInstanceState());
		state.currentScreen = mCurrentScreen;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		if (savedState.currentScreen != -1) {
			mCurrentScreen = savedState.currentScreen;
		}
	}

	public void scrollLeft() {
		if (mNextScreen == INVALID_SCREEN && mCurrentScreen > 0
				&& mScroller.isFinished()) {
			snapToScreen(mCurrentScreen - 1);
		}
	}

	public void scrollRight() {
		if (mNextScreen == INVALID_SCREEN
				&& mCurrentScreen < getChildCount() - 1
				&& mScroller.isFinished()) {
			snapToScreen(mCurrentScreen + 1);
		}
	}

	public int getScreenForView(View v) {
		int result = -1;
		if (v != null) {
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				if (vp == getChildAt(i)) {
					return i;
				}
			}
		}
		return result;
	}

	/**
	 * @return True is long presses are still allowed for the current touch
	 */
	public boolean allowLongPress() {
		return mAllowLongPress;
	}

	/**
	 * Set true to allow long-press events to be triggered, usually checked by
	 * {@link Launcher} to accept or block dpad-initiated long-presses.
	 */
	public void setAllowLongPress(boolean allowLongPress) {
		mAllowLongPress = allowLongPress;
	}

	void removeShortcutsForPackage(String packageName) {
	}

	void updateShortcutsForPackage(String packageName) {
	}

	void moveToDefaultScreen() {
		snapToScreen(mDefaultScreen);
		getChildAt(mDefaultScreen).requestFocus();
	}

	public static class SavedState extends BaseSavedState {
		int currentScreen = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentScreen = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(currentScreen);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private OnScreenChangeListener onScreenChangeListener;

	public interface OnScreenChangeListener {
		void onScreenChangeStart(int whichScreen);
		void onScrrenChangeEnd(int whichScreen);
	}

	public void setOnScreenChangeListener(OnScreenChangeListener listener) {
		onScreenChangeListener = listener;
	}
	
	public OnScreenChangeListener getOnScreenChangeListener()
	{
		return onScreenChangeListener; 
	}
}

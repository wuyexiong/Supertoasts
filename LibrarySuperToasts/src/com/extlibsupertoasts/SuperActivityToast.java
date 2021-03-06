/**
 *  Copyright 2013 John Persano
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 * 
 */

package com.extlibsupertoasts;

import com.extlibsupertoasts.utilities.OnDismissListener;
import com.extlibsupertoasts.utilities.SuperToastConstants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * SuperActivityToasts are designed to be used inside of Activities. When the
 * Activity is destroyed the SuperActivityToast is destroyed along with it.
 * SuperActivityToasts will not linger to the next screen like standard
 * Toasts/SuperToasts.
 * 
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class SuperActivityToast {

	
	private static final String TAG = "SuperActivityToast";
	
	private static final String ERROR_CONTEXTNULL = "The Context that you passed was null! (SuperActivityToast)";
	private static final String ERROR_CONTEXTNOTACTIVITY = "The Context that you passed was not an Activity! (SuperActivityToast)";
	private static final String ERROR_VIEWORCONTAINERNULL = "Either the View or Container was null when trying to dismiss. "
			+ "Did you create and show a SuperActivityToast before trying to dismiss it?";
	
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private ViewGroup mViewGroup;
	private View toastView;
	private TextView messageTextView;
	private Handler mHandler;
	private int sdkVersion = android.os.Build.VERSION.SDK_INT;

	private CharSequence textCharSequence;
	private int textColor = Color.WHITE;
	private Typeface typeface = Typeface.DEFAULT;
	private float textSize = SuperToastConstants.TEXTSIZE_SMALL;
	private int backgroundResource = SuperToastConstants.BACKGROUND_GREYTRANSLUCENT;
	private Drawable backgroundDrawable;
	private int duration = SuperToastConstants.DURATION_SHORT;
	private boolean isIndeterminate;
	private OnClickListener mOnClickListener;
	private Animation showAnimation;
	private Animation dismissAnimation;
	private boolean touchDismiss;
	private boolean touchImmediateDismiss;
	private IconPosition mIconPosition = IconPosition.LEFT;
	private Drawable iconDrawable;
	private int iconResource;
	private OnDismissListener mOnDismissListener;

	/**
	 * This is used to specify the position of a supplied icon in the
	 * SuperActivityToast.
	 * 
	 */
	public enum IconPosition {

		/**
		 * Set the icon to the left of the text.
		 */
		LEFT,

		/**
		 * Set the icon to the right of the text.
		 */
		RIGHT,

		/**
		 * Set the icon on top of the text.
		 */
		TOP,

		/**
		 * Set the icon on the bottom of the text.
		 */
		BOTTOM;

	}

	/**
	 * Instantiates a new SuperActivityToast. You <b>MUST</b> pass an Activity
	 * as a Context. If you do not have access to an Activity Context than
	 * please use a standard SuperToast instead.
	 * 
	 * <br>
	 * 
	 * @param mContext
	 * 
	 * <br>
	 * This must be an Activity Context.
	 * <br>
	 * 
	 */
	public SuperActivityToast(Context mContext) {

		if (mContext != null) {

			if (mContext instanceof Activity) {

				this.mContext = mContext;

				mLayoutInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				final Activity mActivity = (Activity) mContext;

				mViewGroup = (ViewGroup) mActivity
						.findViewById(android.R.id.content);

				toastView = mLayoutInflater.inflate(R.layout.supertoast,
						mViewGroup, false);

			} else {

				throw new IllegalArgumentException(ERROR_CONTEXTNOTACTIVITY);

			}

		} else {

			throw new IllegalArgumentException(ERROR_CONTEXTNULL);

		}

	}
	
	

	/**
	 * This is used to show the SuperActivityToast. You should
	 * do all of your modifications to the SuperActivityToast before calling
	 * this method. 
	 */
	public void show() {

		if (!isIndeterminate) {

			mHandler = new Handler();
			mHandler.postDelayed(mHideToastRunnable, duration);

		}

		if (mOnClickListener != null) {

			toastView.setOnClickListener(mOnClickListener);

		}

		if (touchDismiss || touchImmediateDismiss) {

			if (touchDismiss) {

				toastView.setOnTouchListener(mTouchDismissListener);

			}

			else if (touchImmediateDismiss) {

				toastView.setOnTouchListener(mTouchImmediateDismissListener);

			}

		}

		messageTextView = (TextView) toastView
				.findViewById(R.id.messageTextView);

		messageTextView.setText(textCharSequence);
		messageTextView.setTypeface(typeface);
		messageTextView.setTextColor(textColor);
		messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		
		
		final FrameLayout mFrameLayout = (FrameLayout) toastView
				.findViewById(R.id.root);
		
		if (backgroundDrawable != null) {

			if (sdkVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {

				mFrameLayout.setBackgroundDrawable(backgroundDrawable);

			}

			else {

				mFrameLayout.setBackground(backgroundDrawable);

			}

		} else {

			mFrameLayout.setBackgroundResource(backgroundResource);

		}

		if (iconDrawable != null) {

			if (mIconPosition == IconPosition.BOTTOM) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(null,
						null, null, backgroundDrawable);

			} else if (mIconPosition == IconPosition.LEFT) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(
						backgroundDrawable, null, null, null);

			} else if (mIconPosition == IconPosition.RIGHT) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(null,
						null, backgroundDrawable, null);

			} else if (mIconPosition == IconPosition.TOP) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(null,
						backgroundDrawable, null, null);

			}

		} else if (iconResource > 0) {

			if (mIconPosition == IconPosition.BOTTOM) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(null,
						null, null,
						mContext.getResources().getDrawable(iconResource));

			} else if (mIconPosition == IconPosition.LEFT) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(
						mContext.getResources().getDrawable(iconResource),
						null, null, null);

			} else if (mIconPosition == IconPosition.RIGHT) {

				messageTextView
						.setCompoundDrawablesWithIntrinsicBounds(
								null, null, mContext.getResources().getDrawable(iconResource), 
								null);

			} else if (mIconPosition == IconPosition.TOP) {

				messageTextView.setCompoundDrawablesWithIntrinsicBounds(null,
						mContext.getResources().getDrawable(iconResource),
						null, null);

			}

		}

		mViewGroup.addView(toastView);
		
		if (showAnimation != null) {

			toastView.startAnimation(showAnimation);

		} else {

			toastView.startAnimation(getFadeInAnimation());

		}

	}

	
	/**
	 * This is used to set the message text of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Important note: </b>
	 * </p>
	 * 
	 * <p>
	 * This method can be called again while the SuperActivityToast is showing
	 * to modify the existing message. If your application might show two
	 * SuperActivityToasts at one time you should try to reuse the same
	 * SuperActivityToast by calling this method and {@link #resetDuration(int)}
	 * </p>
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Toasts/SuperActivityToasts are designed to display short non-essential
	 * messages such as "Message sent!" after the user sends a SMS. Generally
	 * these messages should rarely display more than one line of text.
	 * </p>
	 * 
	 * <br>
	 * 
	 * @param textCharSequence 
	 * 		
	 * <br>
	 * 
	 */
	public void setText(CharSequence textCharSequence) {

		this.textCharSequence = textCharSequence;

		if (messageTextView != null) {

			messageTextView.setText(textCharSequence);

		}

	}

	/**
	 * This is used to set the message text color of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * The text color that you choose should contrast the color of the background.
	 * Generally the colors white and black are the only colors that should be used
	 * here.
	 * </p>
	 * 
	 * <br>
	 * @param textColor 
	 * <br>
	 * Example: (Color.WHITE)
	 * <br>
	 * 
	 */
	public void setTextColor(int textColor) {

		this.textColor = textColor;

		if (messageTextView != null) {

			messageTextView.setTextColor(textColor);

		}

	}
	

	/**
	 * This is used to set the duration of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Generally short durations are preferred. 
	 * </p>
	 * 
	 * <br>
	 * @param duration
	 * <br>
	 * Example: (SuperToastConstants.DURATION_SHORT)
	 * <br>
	 * 
	 */
	public void setDuration(int duration) {

		this.duration = duration;

	}
	

	/**
	 * This is used to reset the duration of the SuperActivityToast 
	 * while it is showing.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Instead of having overlapping or sequential messages you
	 * should use this method to reuse an already showing SuperActivityToast
	 * in instances where two or more messages can be showing at the same time.
	 * </p>
	 * 
	 * <br>
	 * @param newDuration 
	 * <br>
	 * Example: (SuperToastConstants.DURATION_SHORT)
	 * <br>
	 * 
	 */
	public void resetDuration(int newDuration) {

		if (mHandler != null) {

			mHandler.removeCallbacks(mHideToastRunnable);
			mHandler = null;

		}

		mHandler = new Handler();
		mHandler.postDelayed(mHideToastRunnable, newDuration);

	}

	
	/**
	 * This is used to set an indeterminate duration of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * There are few instances where this may be necessary. Any duration set via
	 * {@link #setDuration(int)} will be ignored. 
	 * </p>
	 * 
	 * <br>
	 * @param isIndeterminate 
	 * <br>
	 * 
	 */
	public void setIndeterminate(boolean isIndeterminate) {

		this.isIndeterminate = isIndeterminate;

	}

	
	/**
	 * This is used to set an icon Drawable to the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Use {@link #setIconPosition(IconPosition)} to modify the 
	 * location of the icon.
	 * </p>
	 * 
	 * <br>
	 * @param iconDrawable 
	 * <br>
	 * 
	 */
	public void setIconDrawable(Drawable iconDrawable) {

		this.iconDrawable = iconDrawable;

	}
	
	
	/**
	 * This is used to set an icon resource to the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Use {@link #setIconPosition(IconPosition)} to modify the 
	 * location of the icon.
	 * </p>
	 * 
	 * <br>
	 * @param iconResource 
	 * <br>
	 * 
	 */
	public void setIconResource(int iconResource) {

		this.iconResource = iconResource;

	}
	
	
	/**
	 * This is used to set the position of the icon in the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * It is preferable to display the icon to the left of the text.
	 * </p>
	 * 
	 * <br>
	 * @param mIconPosition 
	 * <br>
	 * Example: IconPosition.LEFT
	 * <br>
	 * 
	 */
	public void setIconPosition(IconPosition mIconPosition) {

		this.mIconPosition = mIconPosition;

	}
	

	/**
	 * This is used to set an OnClickListener to the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Important note: </b>
	 * </p>
	 * 
	 * <p>
	 * This method is not compatible with {@link #setTouchToDismiss(boolean)} or
	 * {@link #setTouchToImmediateDismiss(boolean)}.
	 * </p>
	 * 
	 * <br>
	 * @param mOnClickListener 
	 * <br>
	 * 
	 */
	public void setOnClickListener(OnClickListener mOnClickListener) {

		this.mOnClickListener = mOnClickListener;

	}

	
	/**
	 * This is used to set the background of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * This library comes with backgrounds ready to use in your applications. 
	 * If you would like to use your own backgrounds please make sure that
	 * the background is nine-patch or XML format. 
	 * </p>
	 * 
	 * <br>
	 * @param backgroundResource 
	 * <br>
	 * Example: (SuperToastConstants.BACKGROUND_BLACK)
	 * <br>
	 * 
	 */
	public void setBackgroundResource(int backgroundResource) {

		this.backgroundResource = backgroundResource;

	}

	
	/**
	 * This is used to set the background of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * This library comes with backgrounds ready to use in your applications. 
	 * If you would like to use them please see {@link #setBackgroundResource(int)}.
	 * </p>
	 * 
	 * <br>
	 * @param backgroundDrawable 
	 * <br>
	 * 
	 */
	public void setBackgroundDrawable(Drawable backgroundDrawable) {

		this.backgroundDrawable = backgroundDrawable;

	}

	
	/**
	 * This is used to set the text size of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * Generally the text size should be around 14sp.
	 * </p>
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Important note: </b>
	 * </p>
	 * 
	 * <p>
	 * You may specify an integer value as a parameter.
	 * This method will automatically convert the integer to 
	 * scaled pixels. 
	 * </p>
	 * 
	 * <br>
	 * @param textSize 
	 * <br>
	 * Example: (SuperToastConstants.TEXTSIZE_SMALL)		
	 * <br>
	 * 
	 */
	public void setTextSize(int textSize) {

		this.textSize = textSize;

	}

	
	/**
	 * This is used to set the Typeface of the SuperActivityToast text.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Important note: </b>
	 * </p>
	 * 
	 * <p>
	 * This library comes with a link to download the Roboto font. To use the
	 * fonts see {@link #loadRobotoTypeface(String)}.
	 * </p>
	 * 
	 * <br>
	 * @param typeface 
	 * <br>
	 * 		Example: (Typeface.DEFAULT) OR (mSuperActivityToast.loadRobotoTypeface(SuperToastConstants.
	 * FONT_ROBOTO_THIN);	 * 		
	 * <br>
	 * 
	 */
	public void setTypeface(Typeface typeface) {

		this.typeface = typeface;

	}

	
	/**
	 * This is used to set the show animation of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * The Animation that you supply here should be simple and not exceed
	 * 500 milliseconds.
	 * </p>
	 * 
	 * <br>
	 * @param showAnimation 
	 * <br>
	 * 
	 */
	public void setShowAnimation(Animation showAnimation) {

		this.showAnimation = showAnimation;

	}

	
	/**
	 * This is used to set the dismiss animation of the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * The Animation that you supply here should be simple and not exceed
	 * 500 milliseconds.
	 * </p>
	 * 
	 * <br>
	 * @param dismissAnimation 
	 * <br>
	 * 
	 */
	public void setDismissAnimation(Animation dismissAnimation) {

		this.dismissAnimation = dismissAnimation;

	}
	

	/**
	 * This is used to set a private OnTouchListener to the SuperActivityToast
	 * that will dismiss the SuperActivityToast with an Animation.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * This method should be used with long running SuperActivityToasts in case
	 * the SuperActivityToast comes in between application content and the user.
	 * This method is not compatible with {@link #setOnClickListener(OnClickListener)}.
	 * </p>
	 * 
	 * <br>
	 * @param touchDismiss 
	 * <br>
	 * 
	 */
	public void setTouchToDismiss(boolean touchDismiss) {

		this.touchDismiss = touchDismiss;

	}
	

	/**
	 * This is used to set a private OnTouchListener to the SuperActivityToast
	 * that will dismiss the SuperActivityToast immediately without an Animation.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Design guide: </b>
	 * </p>
	 * 
	 * <p>
	 * This method should be used with long running SuperActivityToasts in case
	 * the SuperActivityToast comes in between application content and the user.
	 * This method is not compatible with {@link #setOnClickListener(OnClickListener)}.
	 * </p>
	 * 
	 * <br>
	 * @param touchImmediateDismiss 
	 * <br>
	 * 
	 */
	public void setTouchToImmediateDismiss(boolean touchImmediateDismiss) {

		this.touchImmediateDismiss = touchImmediateDismiss;

	}

	
	/**
	 * This is used to set an OnDismissListener to the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 * <p>
	 * <b> Important note: </b>
	 * </p>
	 * 
	 * <p>
	 * Make sure that the OnDismissListener is imported from this library.
	 * This method is not compatible with other OnDismissListeners.
	 * </p>
	 * 
	 * <br>
	 * @param mOnDismissListener 
	 * <br>
	 * 
	 */
	public void setOnDismissListener(OnDismissListener mOnDismissListener) {

		this.mOnDismissListener = mOnDismissListener;

	}
	
	
	/**
	 * This is used to dismiss the SuperActivityToast.
	 * 
	 * <br>
	 * 
	 */
	public void dismiss() {

		dismissWithAnimation();

	}
	
	
	/**
	 * This is used to dismiss the SuperActivityToast immediately without Animation.
	 * 
	 * <br>
	 * 
	 */
	public void dismissImmediately() {
		
		if(mHandler != null) { 
			
			mHandler.removeCallbacks(mHideToastRunnable);
			mHandler = null;
			
		}

		if (toastView != null && mViewGroup != null) {

			mViewGroup.removeView(toastView);
			toastView = null;

		} else {

			Log.e(TAG, ERROR_VIEWORCONTAINERNULL);

		}
		
		if(mOnDismissListener != null) {
			
			mOnDismissListener.onDismiss();
			
		}

	}
	

	// XXX: Getter methods.

	
	/**
	 * This is used to get the SuperActivityToast message TextView.
	 * 
	 * <br>
	 * @return TextView
	 * <br>
	 * 
	 */
	public TextView getTextView() {

		return messageTextView;

	}

	
	/**
	 * This is used to get the SuperActivityToast View.
	 * 
	 * <br>
	 * @return View
	 * <br>
	 * 
	 */
	public View getView() {

		return toastView;

	}
	
	
	/**
	 * Returns true of the SuperActivityToast is currently visible 
	 * to the user. 
	 * 
	 * <br>
	 * 
	 * @return boolean
	 * 
	 * <br>
	 * 
	 */
	public boolean isShowing() {

		if (toastView != null) {

			return toastView.isShown();

		}

		else {

			return false;

		}

	}
	
	
	/**
	 * This is used to get and load a Roboto font. You <b><i>MUST</i></b> put the
	 * desired font file in the assets folder of your project. The link to
	 * download the Roboto fonts is included in this library as a text file. Do
	 * not modify the names of these fonts.
	 * 
	 * <br>
	 * @param typefaceString
	 * <br>
	 * Example: (SuperToastConstants.FONT_ROBOTO_THIN)
	 * <br>
	 * @return Typeface
	 * <br>
	 * 
	 */
	public Typeface loadRobotoTypeface(String typefaceString) {

		return Typeface.createFromAsset(mContext.getAssets(), typefaceString);

	}
	

	// XXX: Private methods.
	

	private Runnable mHideToastRunnable = new Runnable() {

		public void run() {

			dismiss();

		}
	};
	
	private Runnable mHideImmediateRunnable = new Runnable() 
	{
		 
		public void run() {

			dismissImmediately();

		}
        
    };

	private Animation getFadeInAnimation() {

		AlphaAnimation mAlphaAnimation = new AlphaAnimation(0f, 1f);
		mAlphaAnimation.setDuration(500);
		mAlphaAnimation.setInterpolator(new AccelerateInterpolator());

		return mAlphaAnimation;

	}

	private Animation getFadeOutAnimation() {

		AlphaAnimation mAlphaAnimation = new AlphaAnimation(1f, 0f);
		mAlphaAnimation.setDuration(500);
		mAlphaAnimation.setInterpolator(new AccelerateInterpolator());

		return mAlphaAnimation;

	}

	private OnTouchListener mTouchDismissListener = new OnTouchListener() {

		int timesTouched;

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			/** This is a little hack to prevent the user from repeatedly 
			 *  touching the SuperProgressToast causing erratic behavior **/
			if (timesTouched == 0) {

				dismiss();

			} 
			
			timesTouched++;
			
			return false;

		}

	};

	private OnTouchListener mTouchImmediateDismissListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			dismissImmediately();

			return false;

		}

	};

	
	private void dismissWithAnimation() {
		
		
		if (mHandler != null) {

			mHandler.removeCallbacks(mHideToastRunnable);
			mHandler = null;

		}

		if (dismissAnimation != null) {

			dismissAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {

					/** Must use Handler to modify ViewGroup in onAnimationEnd() **/
					Handler mHandler = new Handler();
					mHandler.post(mHideImmediateRunnable);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

					// Not used

				}

				@Override
				public void onAnimationStart(Animation animation) {

					// Not used

				}

			});

			toastView.startAnimation(dismissAnimation);

		}

		else {

			Animation mAnimation = getFadeOutAnimation();

			mAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {

					/** Must use Handler to modify ViewGroup in onAnimationEnd() **/
					Handler mHandler = new Handler();
					mHandler.post(mHideImmediateRunnable);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

					// Not used

				}

				@Override
				public void onAnimationStart(Animation animation) {

					// Not used

				}

			});

			toastView.startAnimation(mAnimation);

		}

	}
	

	// XXX: Static methods.
	

	/**
	 * Creates a dark theme SuperActivityToast. Don't forget to call
	 * {@link #show()}.
	 * 
	 * <br>
	 * 
	 * @param context 
	 * 
	 * @param textCharSequence 
	 * 
	 * @param durationInteger 
	 * 
	 * @return SuperActivityToast
	 * 
	 */
	public static SuperActivityToast createDarkSuperActivityToast(Context context, CharSequence textCharSequence,
			 int durationInteger) {

		SuperActivityToast mSuperToast = new SuperActivityToast(context);
		mSuperToast.setText(textCharSequence);
		mSuperToast.setDuration(durationInteger);

		return mSuperToast;

	}
	

	/**
	 * Creates a light theme SuperActivityToast. Don't forget to call
	 * {@link #show()}.
	 * 
	 * <br>
	 * 
	 * @param context 
	 * 
	 * @param textCharSequence 
	 * 
	 * @param durationInteger 
	 * 
	 * @return SuperActivityToast
	 * 
	 */
	public static SuperActivityToast createLightSuperActivityToast(Context context, CharSequence textCharSequence,
			int durationInteger) {

		SuperActivityToast mSuperToast = new SuperActivityToast(context);
		mSuperToast.setText(textCharSequence);
		mSuperToast.setDuration(durationInteger);
		mSuperToast.setBackgroundResource(SuperToastConstants.BACKGROUND_WHITE);
		mSuperToast.setTextColor(Color.BLACK);

		return mSuperToast;

	}

	
	/**
	 * Creates a dark theme SuperActivityToast with an OnClickListener. Don't forget to call
	 * {@link #show()}.
	 * 
	 * <br>
	 * 
	 * @param context 
	 * 
	 * @param textCharSequence 
	 * 
	 * @param durationInteger 
	 * 
	 * @param mOnClickListener 
	 * 
	 * @return SuperActivityToast
	 * 
	 */
	public static SuperActivityToast createDarkSuperActivityToast(
			Context context, CharSequence textCharSequence,
			int durationInteger, OnClickListener mOnClickListener) {

		final SuperActivityToast mSuperToast = new SuperActivityToast(context);
		mSuperToast.setText(textCharSequence);
		mSuperToast.setDuration(durationInteger);
		mSuperToast.setOnClickListener(mOnClickListener);

		return mSuperToast;

	}

	
	/**
	 * Creates a light theme SuperActivityToast with an OnClickListener. Don't forget to call
	 * {@link #show()}.
	 * 
	 * <br>
	 * 
	 * @param context 
	 * 
	 * @param textCharSequence 
	 * 
	 * @param durationInteger 
	 * 
	 * @param mOnClickListener 
	 * 
	 * @return SuperActivityToast
	 * 
	 */
	public static SuperActivityToast createLightSuperActivityToast(
			Context context, CharSequence textCharSequence,
			int durationInteger, OnClickListener mOnClickListener) {

		final SuperActivityToast mSuperToast = new SuperActivityToast(context);
		mSuperToast.setText(textCharSequence);
		mSuperToast.setDuration(durationInteger);
		mSuperToast.setBackgroundResource(SuperToastConstants.BACKGROUND_WHITE);
		mSuperToast.setTextColor(Color.BLACK);
		mSuperToast.setOnClickListener(mOnClickListener);

		return mSuperToast;

	}

}

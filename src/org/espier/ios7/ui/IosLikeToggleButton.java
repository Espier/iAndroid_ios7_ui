/*
 * Copyright (C) 2013 FMSoft (http://www.fmsoft.cn)
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

package org.espier.ios7.ui;



import org.espier.ios7ui.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class IosLikeToggleButton extends ImageView {

    public final static int COLOR_MODE_1 = 0;
    public final static int COLOR_MODE_2 = 1;

    private final static int DURATION = 200;

    private final static int MOVE_LONG_SLOP = 10;
    private final static int MOVE_SHORT_SLOP = 1;

    private Drawable mToggle;
    private Drawable mMask;
    private Drawable mMask1;
    

    private float mLastMotionX;

    private float mOffsetX;
    private int mStartX;
    private int mCenterX;
    private int mEndX;

    private boolean mHasMotionMove;
    private boolean mIsChecked;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public static interface OnCheckedChangeListener {
        void onCheckedChanged(IosLikeToggleButton buttonView, boolean isChecked);
    }


    private Animation mAnim;
    private Transformation mTrans = new Transformation();

    public IosLikeToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setFocusable(true);
        setClickable(true);

        setMask(R.drawable.ioslike_toggle_mask_graw);
        mMask1 = this.getResources().getDrawable(R.drawable.ioslike_toggle_mask_green);
        mToggle = context.getResources().getDrawable(R.drawable.ioslike_toggle_switch);
        calcUsefulWidth(mMask, mMask1, mToggle);
    }

    public void setMask(int rId) {
    	mMask = this.getResources().getDrawable(rId);
    }
    
    public void setToggleColorMode(int colorMode) {
        switch(colorMode) {
        case COLOR_MODE_1:
            mToggle = getContext().getResources().getDrawable(
                    R.drawable.ioslike_toggle_switch);
            calcUsefulWidth(mMask,mMask1, mToggle);
            break;
        case COLOR_MODE_2:
            mToggle = getContext().getResources().getDrawable(
                    R.drawable.ioslike_toggle_switch_2);
            calcUsefulWidth(mMask,mMask1, mToggle);
            break;
        default:
            return;
        }
    }

    public void setChecked(boolean isChecked) {
            mIsChecked = isChecked;
            mOffsetX = isChecked ? mEndX : mStartX;
            invalidate();
    }

    private void setCheckedInner(boolean isChecked) {
        if (mIsChecked != isChecked) {
            setChecked(isChecked);
            if (mOnCheckedChangeListener != null)
                mOnCheckedChangeListener.onCheckedChanged(this, mIsChecked);
            invalidate();
        }
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            cancelTranslateAnimation();
            mLastMotionX = ev.getX();
            break;
        case MotionEvent.ACTION_MOVE:
            final float x = ev.getX();
            final int xDiff = (int) Math.abs(x - mLastMotionX);

            final boolean xMoved = xDiff > (mHasMotionMove ? MOVE_SHORT_SLOP : MOVE_LONG_SLOP);

            if (xMoved) {
                mOffsetX = Math.min(mEndX, Math.max(mStartX, (mOffsetX + x - mLastMotionX)));
                mLastMotionX = x;

                if (!mHasMotionMove)
                    mHasMotionMove = true;

                postInvalidate();
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mHasMotionMove) {
                mHasMotionMove = false;
                if (mOffsetX > mCenterX) {
                    if (mOffsetX != mStartX) {
                        startTranslateAnimation((int) mOffsetX, mEndX);
                        setCheckedInner(true);
                    }
                } else {
                    if (mOffsetX != mEndX) {
                        startTranslateAnimation((int) mOffsetX, mStartX);
                        setCheckedInner(false);
                    }
                }
            } else {
                if (isChecked()) {
                    startTranslateAnimation((int) mOffsetX, mStartX);
                    setCheckedInner(false);
                } else {
                    startTranslateAnimation((int) mOffsetX, mEndX);
                    setCheckedInner(true);
                }
            }
            break;
        }
        return true;
    }

    private void calcUsefulWidth(Drawable mask, Drawable mask1, Drawable toggle) {
        final int maskW = mask.getIntrinsicWidth();		
        final int maskH = mask.getIntrinsicHeight();   
        final int toggleW = toggle.getIntrinsicWidth();
        final int toggleH = toggle.getIntrinsicHeight();

        mask.setBounds(0,  0, maskW, maskH);
        mask1.setBounds(0,  0, maskW, maskH);
        toggle.setBounds(0, 0, toggleW, toggleH);

        setMinimumWidth(maskW);
        setMinimumHeight(maskH);

        mStartX = maskW - toggleW / 2;
        mEndX = maskW - mStartX;
        mCenterX = (mStartX + mEndX) / 2;
        mOffsetX = mStartX;
    }

    private void cancelTranslateAnimation() {
        if (mAnim != null) {
            mAnim.cancel();
            mAnim = null;
        }
    }

    private void startTranslateAnimation(int startX, int endX) {
        cancelTranslateAnimation();

        mAnim = new AlphaAnimation(startX, endX);
        mAnim.setDuration(DURATION);
        mAnim.start();
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mAnim != null && !mAnim.hasEnded()) {
            long currentTime = SystemClock.uptimeMillis();
            boolean more = mAnim.getTransformation(currentTime, mTrans);
            if (more) {
                mOffsetX = mTrans.getAlpha();
                postInvalidate();
            }
        }

        canvas.save();
        canvas.translate(mOffsetX - mEndX, 0);
        mToggle.draw(canvas);
        canvas.restore();
      if(isChecked()){
    	  mMask1.draw(canvas);
      }else {
    	  mMask.draw(canvas);
      }
    }

}

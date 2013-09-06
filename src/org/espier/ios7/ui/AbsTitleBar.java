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
import org.espier.ios7.ui.utils.AuxConfigs;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public abstract class AbsTitleBar extends NewStatusBarActivity {

    private ViewGroup mMainContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuxConfigs.getInstance(this).supportLandscapePortrait
                || !AuxConfigs.isPad) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.ios_abs_title_bar);

        mMainContainer = (ViewGroup) findViewById(R.id.titleBarMainContainer);
        
        setupViews();
    }

    public void setOnclickListener(int btnId, OnClickListener listener) {
        findViewById(btnId).setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuxConfigs.unInstance();//release ref
    }

    @Override
    protected void onResume() {
        super.onResume();

        for (int i = 0, count = mMainContainer.getChildCount(); i < count; ++i) {
            try {
                View v = mMainContainer.getChildAt(i);
                if (v instanceof IosLikeListContainer) {
                    final IosLikeListContainer child = (IosLikeListContainer) v;
                    child.onResume();
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    abstract protected void setupViews();

    public void setBtnTitle(int btnId, int str) {
        ((Button)findViewById(btnId)).setText(str);
    }
    
    public void enableButton(boolean enable,int btnId, Drawable drawable,String text,
            OnClickListener clickListener) {
        Button btn = (Button) findViewById(btnId);
        if (enable) {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(clickListener);
            
            if(null != drawable){
                btn.setBackgroundDrawable(drawable);
            }
            
            if (null != text) {
                btn.setText(text);
            } else {
                btn.setText("");
            }
            
            if (null != clickListener) {
                btn.setOnClickListener(clickListener);
            }
        } else {
            btn.setVisibility(View.INVISIBLE);
        }
    }

    public void addView(View child) {
        mMainContainer.addView(child);
    }

    public void addView(View child, LayoutParams lp) {
        mMainContainer.addView(child, lp);
    }
}


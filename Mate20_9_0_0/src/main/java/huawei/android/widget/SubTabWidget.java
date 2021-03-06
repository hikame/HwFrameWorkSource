package huawei.android.widget;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import huawei.android.widget.SubTabViewContainer.SlidingTabStrip;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.effect.engine.HwBlurEngine.BlurType;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class SubTabWidget extends LinearLayout {
    private static final String TAG = "SubTabWidget";
    private boolean isBlurEnable;
    private HwBlurEngine mBlurEngine;
    private boolean mClickable;
    private ImageView mFunctionView;
    private boolean mIsSetSubTab;
    private int mLastPos;
    private int mLastSubTab;
    private Typeface mMedium;
    private Typeface mRegular;
    private SubTab mSelectedSubTab;
    private SlidingTabStrip mSlidingTabStrip;
    private SubTabClickListener mSubTabClickListener;
    private SubTabViewContainer mSubTabContainer;
    public LinearLayout mSubTabContentView;
    private int mSubTabItemMargin;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int mSavedPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.mSavedPosition = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mSavedPosition);
        }
    }

    public class SubTab {
        public static final int INVALID_POSITION = -1;
        private SubTabListener mCallback;
        private int mPosition;
        private int mSubTabId;
        private Object mTag;
        private CharSequence mText;

        public SubTab(SubTabWidget this$0) {
            this(null, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text) {
            this(text, null, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, SubTabListener callback) {
            this(text, callback, null);
        }

        public SubTab(SubTabWidget this$0, CharSequence text, Object tag) {
            this(text, null, tag);
        }

        public SubTab(CharSequence text, SubTabListener callback, Object tag) {
            this.mPosition = -1;
            this.mSubTabId = -1;
            this.mText = text;
            this.mCallback = callback;
            this.mTag = tag;
        }

        public SubTabListener getCallback() {
            return this.mCallback;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public void select() {
            SubTabWidget.this.selectSubTab(this);
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public SubTab setSubTabListener(SubTabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public SubTab setText(int resId) {
            return setText(SubTabWidget.this.getContext().getResources().getText(resId));
        }

        public SubTab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                SubTabWidget.this.updateSubTab(this.mPosition);
            }
            return this;
        }

        public SubTab setTag(Object obj) {
            this.mTag = obj;
            return this;
        }

        public Object getTag() {
            return this.mTag;
        }

        public void setSubTabId(int id) {
            this.mSubTabId = id;
        }

        public int getSubTabId() {
            return this.mSubTabId;
        }
    }

    private class SubTabClickListener implements OnClickListener {
        private SubTabClickListener() {
        }

        public void onClick(View view) {
            if (SubTabWidget.this.mClickable) {
                int subTabCount = SubTabWidget.this.mSlidingTabStrip.getChildCount();
                for (int i = 0; i < subTabCount; i++) {
                    View child = SubTabWidget.this.mSlidingTabStrip.getChildAt(i);
                    child.setSelected(child == view);
                    if (child == view) {
                        int lastPos = SubTabWidget.this.mLastPos;
                        if (SubTabWidget.this.mIsSetSubTab && SubTabWidget.this.mLastSubTab != -1) {
                            lastPos = SubTabWidget.this.mLastSubTab;
                        }
                        SubTabWidget.this.mIsSetSubTab = false;
                        SubTabWidget.this.mLastPos = i;
                        SubTabWidget.this.mSubTabContainer.animateToTab(i);
                    }
                }
                if (view instanceof SubTabView) {
                    ((SubTabView) view).getSubTab().select();
                }
            }
        }
    }

    public interface SubTabListener {
        void onSubTabReselected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabSelected(SubTab subTab, FragmentTransaction fragmentTransaction);

        void onSubTabUnselected(SubTab subTab, FragmentTransaction fragmentTransaction);
    }

    private static class SubTabView extends TextView {
        private SubTab mSubTab;

        public SubTabView(Context context, SubTab subTab) {
            super(context, null, ResLoader.getInstance().getIdentifier(context, "attr", "subTabViewStyle"));
            this.mSubTab = subTab;
            update();
            setClickable(true);
            setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, ResLoader.getInstance().getIdentifier(context, "attr", "subTabViewStyle")));
        }

        public SubTab getSubTab() {
            return this.mSubTab;
        }

        public void update() {
            CharSequence text = this.mSubTab.getText();
            if (TextUtils.isEmpty(text) ^ 1) {
                setText(text);
                setVisibility(0);
            } else {
                setVisibility(8);
                setText(null);
            }
            if (this.mSubTab.getSubTabId() != -1) {
                setId(this.mSubTab.getSubTabId());
            }
        }
    }

    public SubTabWidget(Context context) {
        this(context, null);
    }

    public SubTabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, ResLoader.getInstance().getIdentifier(context, "attr", "subTabBarStyle"));
    }

    public SubTabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mClickable = true;
        this.mIsSetSubTab = false;
        this.mLastSubTab = -1;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.isBlurEnable = false;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        int layoutResId = ResLoaderUtil.getLayoutId(context, "sub_tab_content");
        if (layoutResId != 0) {
            View view = inflater.inflate(layoutResId, this, true);
            this.mSubTabContainer = (SubTabViewContainer) view.findViewById(ResLoaderUtil.getViewId(context, "subTabViewContainer"));
            this.mFunctionView = (ImageView) view.findViewById(ResLoaderUtil.getViewId(context, "function_icon"));
            SlidingTabStrip slidingTabStrip = this.mSubTabContainer.getmTabStrip();
            this.mSlidingTabStrip = slidingTabStrip;
            this.mSubTabContentView = slidingTabStrip;
        }
        setOrientation(0);
        this.mMedium = Typeface.create("HwChinese-medium", 0);
        this.mRegular = Typeface.create("sans-serif", 0);
        this.mSlidingTabStrip.setSelectedIndicatorMargin(ResLoaderUtil.getDimensionPixelSize(context, "sub_tab_indicator_margin"));
        Theme theme = ResLoader.getInstance().getTheme(context);
        if (theme != null) {
            int[] themeAttrs = ResLoader.getInstance().getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, TAG);
            if (themeAttrs != null) {
                TypedArray a = theme.obtainStyledAttributes(attrs, themeAttrs, defStyle, 0);
                this.mSlidingTabStrip.setSelectedIndicatorHeight(a.getDimensionPixelOffset(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_insetBottom"), 0));
                this.mSlidingTabStrip.setSelectedIndicatorColor(a.getColor(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_borderColor"), 0));
                this.mSubTabItemMargin = a.getDimensionPixelOffset(ResLoader.getInstance().getIdentifier(context, ResLoaderUtil.STAYLEABLE, "SubTabWidget_middleWidth"), 0);
                this.mSubTabContainer.setSubTabItemMargin(this.mSubTabItemMargin);
                a.recycle();
            }
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setBackgroundStyle(int style) {
        for (int pos = 0; pos < getSubTabCount(); pos++) {
            SubTabView subTabView = (SubTabView) this.mSlidingTabStrip.getChildAt(pos);
        }
    }

    public void addSubTab(SubTab subTab, int position, boolean setSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LayoutParams lp = new LayoutParams(-2, -1);
        lp.rightMargin = this.mSubTabItemMargin;
        lp.leftMargin = this.mSubTabItemMargin;
        subTabView.setLayoutParams(lp);
        this.mSlidingTabStrip.addView(subTabView, position, lp);
        subTab.setPosition(position);
        updateSubTabPosition(position, getSubTabCount(), true);
        if (setSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = position;
        }
    }

    public void addSubTab(SubTab subTab, boolean setSelected) {
        SubTabView subTabView = createSubTabView(subTab);
        LayoutParams lp = new LayoutParams(-2, -1);
        lp.rightMargin = this.mSubTabItemMargin;
        lp.leftMargin = this.mSubTabItemMargin;
        subTabView.setLayoutParams(lp);
        this.mSlidingTabStrip.addView(subTabView, lp);
        subTab.setPosition(getSubTabCount() - 1);
        if (setSelected) {
            subTab.select();
            subTabView.setSelected(true);
            this.mLastPos = getSubTabCount() - 1;
        }
    }

    public Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        int position = getSelectedSubTabPostion();
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mSavedPosition = position;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int position = ss.mSavedPosition;
        if (position >= 0 && position < getSubTabCount()) {
            getSubTabAt(position).select();
            ((SubTabView) this.mSlidingTabStrip.getChildAt(position)).setSelected(true);
            this.mLastPos = position;
        }
    }

    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    public SubTab getSelectedSubTab() {
        return this.mSelectedSubTab;
    }

    public int getSelectedSubTabPostion() {
        int subTabCount = getSubTabCount();
        for (int i = 0; i < subTabCount; i++) {
            if (this.mSelectedSubTab == getSubTabAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public void setSubTabSelected(int position) {
        SubTab subTab = getSubTabAt(position);
        if (!((this.mSelectedSubTab != null && this.mSelectedSubTab.getPosition() != -1) || subTab == null || subTab.getPosition() == -1)) {
            this.mSubTabContainer.setScrollPosition(subTab.getPosition(), 0.0f);
        }
        this.mSelectedSubTab = subTab;
        setSubTabSelectedInner(position);
        if (this.mLastPos != position) {
            this.mLastSubTab = this.mLastPos;
            this.mIsSetSubTab = true;
        }
        this.mLastPos = position;
    }

    public void updateSubTabPosition(int start, int end, boolean isAdd) {
        int i;
        SubTab child;
        if (isAdd) {
            for (i = start + 1; i < end; i++) {
                child = getSubTabAt(i);
                if (child != null) {
                    child.setPosition(i);
                }
            }
            return;
        }
        for (i = start; i < end; i++) {
            child = getSubTabAt(i);
            if (child != null) {
                child.setPosition(i);
            }
        }
    }

    private void setSubTabSelectedInner(int position) {
        int subTabCount = this.mSlidingTabStrip.getChildCount();
        int i = 0;
        while (i < subTabCount) {
            TextView child = (TextView) this.mSlidingTabStrip.getChildAt(i);
            boolean isSelected = i == position;
            if (i == position) {
                child.setTypeface(this.mMedium);
            } else {
                child.setTypeface(this.mRegular);
            }
            child.setSelected(isSelected);
            i++;
        }
    }

    public SubTab getSubTabAt(int position) {
        View view = this.mSlidingTabStrip.getChildAt(position);
        return view != null ? ((SubTabView) view).getSubTab() : null;
    }

    public int getSubTabCount() {
        return this.mSlidingTabStrip.getChildCount();
    }

    public SubTab newSubTab() {
        return new SubTab(this);
    }

    public SubTab newSubTab(CharSequence text) {
        return new SubTab(this, text);
    }

    public SubTab newSubTab(CharSequence text, SubTabListener callback, Object tag) {
        return new SubTab(text, callback, tag);
    }

    public void removeSubTab(SubTab subTab) {
        removeSubTabAt(subTab.getPosition());
    }

    public void removeSubTabAt(int position) {
        if (this.mSlidingTabStrip != null) {
            SubTab subTab = getSubTabAt(position);
            if (subTab != null) {
                subTab.setPosition(-1);
            }
            this.mSlidingTabStrip.removeViewAt(position);
            if (getSubTabCount() == 0) {
                this.mSelectedSubTab = null;
            }
            int i = 0;
            updateSubTabPosition(position, getSubTabCount(), false);
            if (subTab == this.mSelectedSubTab) {
                if (position - 1 > 0) {
                    i = position - 1;
                }
                selectSubTab(getSubTabAt(i));
            }
        }
    }

    public void removeAllSubTabs() {
        if (this.mSlidingTabStrip != null) {
            this.mSlidingTabStrip.removeAllViews();
            this.mSelectedSubTab = null;
        }
    }

    public void selectSubTab(SubTab subTab) {
        FragmentTransaction trans = null;
        if (this.mContext instanceof Activity) {
            trans = ((Activity) this.mContext).getFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        int i = -1;
        if (!((this.mSelectedSubTab != null && this.mSelectedSubTab.getPosition() != -1) || subTab == null || subTab.getPosition() == -1)) {
            this.mSubTabContainer.setScrollPosition(subTab.getPosition(), 0.0f);
        }
        if (this.mSelectedSubTab != subTab) {
            if (subTab != null) {
                i = subTab.getPosition();
            }
            setSubTabSelectedInner(i);
            if (this.mSelectedSubTab != null) {
                this.mSelectedSubTab.getCallback().onSubTabUnselected(this.mSelectedSubTab, trans);
            }
            this.mSelectedSubTab = subTab;
            if (this.mSelectedSubTab != null) {
                this.mSelectedSubTab.getCallback().onSubTabSelected(this.mSelectedSubTab, trans);
            }
        } else if (this.mSelectedSubTab != null) {
            this.mSelectedSubTab.getCallback().onSubTabReselected(this.mSelectedSubTab, trans);
        }
        if (trans != null && !trans.isEmpty()) {
            trans.commit();
        }
    }

    public void updateSubTab(int position) {
        SubTabView subTabView = (SubTabView) this.mSlidingTabStrip.getChildAt(position);
        if (subTabView != null) {
            subTabView.update();
        }
    }

    private SubTabView createSubTabView(SubTab subTab) {
        SubTabView subTabView = new SubTabView(getContext(), subTab);
        subTabView.setFocusable(true);
        if (this.mSubTabClickListener == null) {
            this.mSubTabClickListener = new SubTabClickListener();
        }
        subTabView.setOnClickListener(this.mSubTabClickListener);
        return subTabView;
    }

    public void setSubTabScrollingOffsets(int position, float x) {
        this.mSubTabContainer.setScrollPosition(position, x);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private int getSubTabViewCount() {
        return this.mSubTabContentView.getChildCount();
    }

    public void addFunctionMenu(int iconRes, OnClickListener clickListener) {
        if (this.mFunctionView != null) {
            this.mFunctionView.setVisibility(0);
            this.mFunctionView.setImageResource(iconRes);
            this.mFunctionView.setOnClickListener(clickListener);
        }
    }

    public void draw(Canvas canvas) {
        if (this.mBlurEngine.isShowHwBlur(this)) {
            this.mBlurEngine.draw(canvas, this);
            super.dispatchDraw(canvas);
            return;
        }
        super.draw(canvas);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            this.mBlurEngine.addBlurTargetView(this, BlurType.LightBlurWithGray);
            this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
            return;
        }
        this.mBlurEngine.removeBlurTargetView(this);
    }

    public boolean isBlurEnable() {
        return this.isBlurEnable;
    }

    public void setBlurEnable(boolean blurEnable) {
        this.isBlurEnable = blurEnable;
        this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
    }
}

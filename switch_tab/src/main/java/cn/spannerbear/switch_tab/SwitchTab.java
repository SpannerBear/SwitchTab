package cn.spannerbear.switch_tab;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by SpannerBear on 2019/3/22.
 * use to:
 */
public class SwitchTab extends View {
    
    private int mWidth;
    private int mHeight;
    private int mOR;//外圈半径
    private int mTabWidth;
    private int mTabHeight;
    private int mIR;//内圆半径
    private int mExternalMargin = 10;//外框与内框的marin
    private int mBackgroundColor = Color.BLUE;
    private int mTabColor = Color.WHITE;//tab颜色
    private int mTextColor = Color.WHITE;//普通文字颜色
    private int mSelectedTextColor = Color.BLUE;//被选中的文字颜色
    private float mTextSize = 48;
    private int mTextPadding = 7;//文字padding(只作用于纵向)
    private long mDuration = 200;
    
    private int mTextTempLocalFlag;//记录文字绘制位置的临时变量
    
    private Rect mTextRect;
    private RectF mRectF;
    private Path mPath;
    private Path mTabPath;
    
    private Paint mPaint;
    private Paint mTextPaint;
    
    private String[] mTextArray = new String[]{};
    private int mCurrentTabIndex;
    private int mTabTargetX;//tab位移目标点
    private int mCurrentTabX;//tab当前位置点
    
    private TimeInterpolator mInterpolator = new AccelerateInterpolator();
    private ValueAnimator mValueAnimator;
    private OnTabChangeListener mTCListener;
    private Animator.AnimatorListener mListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (mTCListener != null) {
                mTCListener.onChangeStart(mCurrentTabIndex);
            }
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mTCListener != null) {
                mTCListener.onChangeEnd(mCurrentTabIndex);
            }
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {
        
        }
        
        @Override
        public void onAnimationRepeat(Animator animation) {
        
        }
    };
    
    
    public SwitchTab(Context context) {
        this(context, null);
    }
    
    public SwitchTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SwitchTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initParams();
    }
    
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchTab);
        mExternalMargin = typedArray.getDimensionPixelSize(R.styleable.SwitchTab_outsideMargin, 10);
        mTextPadding = typedArray.getDimensionPixelSize(R.styleable.SwitchTab_textPadding, 7);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.SwitchTab_textSize, 48);
        mBackgroundColor = typedArray.getColor(R.styleable.SwitchTab_backgroundColor, Color.BLUE);
        mTabColor = typedArray.getColor(R.styleable.SwitchTab_tabColor, Color.WHITE);
        mTextColor = typedArray.getColor(R.styleable.SwitchTab_defaultTextColor, Color.WHITE);
        mSelectedTextColor = typedArray.getColor(R.styleable.SwitchTab_selectedTextColor, Color.WHITE);
        mCurrentTabX = mExternalMargin;
        typedArray.recycle();
    }
    
    private void initParams() {
        mTextArray = new String[]{};
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
        
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPath = new Path();
        mTabPath = new Path();
        mRectF = new RectF();
        mTextRect = new Rect();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //取出最长的字符串
        int maxLength = -1;
        String str = "";
        if (mTextArray != null) {
            for (String s : mTextArray) {
                if (maxLength < s.length()) {
                    maxLength = s.length();
                    str = s;
                }
            }
        }
        float measureText = mTextPaint.measureText(str);
        int minWidth = (int) (measureText * mTextArray.length + 2 * mTextPadding * mTextArray.length - 1 + 2 * mExternalMargin);
        mTextPaint.getTextBounds(str, 0, str.length(), mTextRect);
        int minHeight = -mTextRect.top + mTextRect.bottom + 2 * mTextPadding + 2 * mExternalMargin;
        
        mWidth = Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec));
        mHeight = minHeight;
        mOR = mHeight / 2;
        mIR = mExternalMargin == 0 ? mOR : mOR - mExternalMargin;
        mTabWidth = mWidth - mExternalMargin * 2;
        if (mTextArray.length != 0) {
            mTabWidth = mTabWidth / mTextArray.length;
        }
        mTabHeight = mHeight - mExternalMargin * 2;
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.getMode(widthMeasureSpec)));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        //描背景
        mPath.moveTo(mOR, 0);
        mPath.rLineTo(mWidth - mOR * 2, 0);
        mRectF.set(mWidth - mOR * 2, 0, mWidth, mOR * 2);
        mPath.addArc(mRectF, -90, 180);
        mPath.lineTo(mOR, mOR * 2);
        mRectF.set(0, 0, mOR * 2, mOR * 2);
        mPath.addArc(mRectF, 90, 180);
        mPath.lineTo(mWidth - mOR, 0);
        mPath.close();
        
        mTabPath.reset();
        mTabPath.moveTo(mCurrentTabX + mIR + mExternalMargin, mExternalMargin);//mCurrentTabX + mOR
        mTabPath.rLineTo(mTabWidth - mIR * 2, 0);
        mRectF.set(mCurrentTabX + mTabWidth - mIR * 2, mExternalMargin, mCurrentTabX + mTabWidth, mIR * 2 + mExternalMargin);
        mTabPath.addArc(mRectF, -90, 180);
        mTabPath.rLineTo(-(mTabWidth - mIR * 2), 0);
        mRectF.set(mCurrentTabX, mExternalMargin, mCurrentTabX + mIR * 2, mIR * 2 + mExternalMargin);
        mTabPath.addArc(mRectF, 90, 180);
        mTabPath.rLineTo(mTabWidth - mIR * 2, 0);
        mTabPath.close();
        
        mPaint.setColor(mBackgroundColor);
        //从背景减去tab
        //绘制背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPath.op(mPath, mTabPath, Path.Op.DIFFERENCE);
            canvas.drawPath(mPath, mPaint);
        } else {
            canvas.drawPath(mPath, mPaint);
        }
        //绘制tab
        mPaint.setColor(mTabColor);
        canvas.drawPath(mTabPath, mPaint);
        
        if (mTextArray.length == 0) return;
        //绘制底层默认颜色文字
        mTextPaint.setColor(mTextColor);
        mTextTempLocalFlag = mExternalMargin;
        int y = (-mTextRect.top + mTextRect.bottom) / 2 - (int) ((mTextPaint.ascent() + mTextPaint.descent()) / 2);
        int textWidth = mTabWidth;
        int halfWidth = textWidth / 2;
        for (String s : mTextArray) {
            float measureText = mTextPaint.measureText(s, 0, s.length());
            canvas.drawText(s, mTextTempLocalFlag + halfWidth - measureText / 2, mExternalMargin + mTextPadding + y, mTextPaint);
            mTextTempLocalFlag += textWidth;
        }
        
        canvas.save();//创建图层
        //绘制上层颜色文字
        mTextPaint.setColor(mSelectedTextColor);
        mTextTempLocalFlag = mExternalMargin;
        canvas.clipPath(mTabPath);
        for (String s : mTextArray) {
            float measureText = mTextPaint.measureText(s, 0, s.length());
            canvas.drawText(s, mTextTempLocalFlag + halfWidth - measureText / 2, mExternalMargin + mTextPadding + y, mTextPaint);
            mTextTempLocalFlag += textWidth;
        }
        canvas.restore();//合成图层
    }
    
    public void setTabArray(String[] strings) {
        mCurrentTabIndex = 0;
        mTextArray = strings;
        perInvalidate();
        postInvalidate();
    }
    
    public void setTabIndex(int index) {
        if (index == mCurrentTabIndex) {
            return;
        }
        notifyToTab(index);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int index = getLocationIndex(event.getX(), event.getY());
                setTabIndex(index);
                performClick();
                return true;
        }
        return true;
    }
    
    /**
     * 计算目标tab位置
     */
    private void perInvalidate() {
        mTabTargetX = mExternalMargin + mTabWidth * mCurrentTabIndex;
    }
    
    private int getLocationIndex(float x, float y) {
        Log.v("jj", "x:" + x + " y:" + y);
        if (mTextArray == null || mTextArray.length == 0) return 0;
        RectF rectF = new RectF();
        rectF.top = 0;
        rectF.bottom = mHeight;
        for (int i = 0; i < mTextArray.length; i++) {
            rectF.left = mExternalMargin + mTabWidth * i;
            rectF.right = rectF.left + mTabWidth;
            if (rectF.contains(x, y)) {
                return i;
            }
        }
        return 0;
    }
    
    private void notifyToTab(int index) {
        mCurrentTabIndex = index;
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        perInvalidate();
        mValueAnimator = ValueAnimator.ofInt(mCurrentTabX, mTabTargetX);
        mValueAnimator.setDuration(mDuration);
        mValueAnimator.setInterpolator(mInterpolator);
        mValueAnimator.addListener(mListener);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentTabX = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mValueAnimator.start();
    }
    
    public void setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
    }
    
    public void setSwitchDuration(long duration) {
        mDuration = duration;
    }
    
    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mTCListener = listener;
    }
    
    public interface OnTabChangeListener {
        void onChangeStart(int currentIndex);
        
        void onChangeEnd(int currentIndex);
    }
}

package com.lineview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * 滑动需结合Scroll+HorizontalScrollView使用实现上下左右滑动)
 * 支持响应事件
 * 暂不支持除中文以外的其他语种
 */
public class LineView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private List<String> data;
    /**
     * 圆点画笔
     */
    private Paint circlePaint;
    /**
     * 线段画笔
     */
    private Paint linePaint;
    /**
     * 数字画笔
     */
    private Paint numberPaint;
    /**
     * 文本画笔
     */
    private Paint textPaint;
    /**
     * 记录圆点路径集合
     */
    private Path[] circlePaths;
    /**
     * 记录线段路径集合
     */
    private Path[] linePaths;
    /**
     * 各个圆点中心点集合
     */
    private Point[] circlePoints;
    /**
     * 原点内圆半径
     */
    private int circleInnerRadius;

    /**
     * 线段长度(单位像素)
     */
    private int lineLength;
    /**
     * 笔触宽度(单位像素)
     */
    private int circleStokeWidth;
    /**
     * 原点半径:内圆半径+笔触宽度(单位:px)
     */
    private int circleRadius;
    /**
     * 原点顶部距离控件顶部距离(单位像素)
     */
    private int circleMarginTop;
    /**
     * 数字与圆点之间的距离(单位像素)
     */
    private int numberMarginTop;
    /**
     * 文本与数字之间的距离(单位像素)
     */
    private int textMarginTop;
    /**
     * 文本与文本之间的距离(单位像素)
     */
    private int textSpace;
    /**
     * 圆点颜色
     */
    private int circleColor;
    /**
     * 圆点选中颜色
     */
    private int circleSelectColor;
    /**
     * 线段颜色
     */
    private int lineColor;
    /**
     * 数字颜色
     */
    private int numberColor;
    /**
     * 数字选中颜色
     */
    private int numberSelectColor;
    /**
     * 文本颜色
     */
    private int textColor;
    /**
     * 文本选中颜色
     */
    private int textSelectColor;
    /**
     * 线段笔触宽度
     */
    private int lineStrokeWidth;
    /**
     * 数字字体大小
     */
    private int numberTextSize;
    /**
     * 文本字体大小
     */
    private int textSize;
    /**
     * 文本高度
     */
    private int textHeight;
    /**
     * 记录每个条目的区域集合
     */
    private Region[] textRegions;
    /**
     * 默认选中第一条
     */
    private int currSelectedItem = 0;
    /**
     * 手势监听
     */
    private GestureDetector detector;
    /**
     * 条目点击事件
     */
    private OnLineViewItemSelectedistener onLineViewItemSelectedistener;

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LineView);
        try {
            circleColor = array.getColor(R.styleable.LineView_circle_color, getResources().getColor(android.R.color.darker_gray));
            lineColor = array.getColor(R.styleable.LineView_line_color, getResources().getColor(android.R.color.darker_gray));
            numberColor = array.getColor(R.styleable.LineView_number_color, getResources().getColor(android.R.color.darker_gray));
            textColor = array.getColor(R.styleable.LineView_text_color, getResources().getColor(android.R.color.darker_gray));
            circleStokeWidth = array.getDimensionPixelSize(R.styleable.LineView_circle_stroke, 4);
            lineStrokeWidth = array.getDimensionPixelSize(R.styleable.LineView_line_stroke, 4);
            numberTextSize = (int) array.getDimension(R.styleable.LineView_number_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            textSize = (int) array.getDimension(R.styleable.LineView_text_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            textSpace = array.getDimensionPixelSize(R.styleable.LineView_text_space, 10);
            textMarginTop = array.getDimensionPixelSize(R.styleable.LineView_text_marginTop, 70);
            numberMarginTop = array.getDimensionPixelSize(R.styleable.LineView_number_marginTop, 55);
            circleMarginTop = array.getDimensionPixelSize(R.styleable.LineView_circle_marginTop, 200);
            circleInnerRadius = array.getDimensionPixelSize(R.styleable.LineView_circle_radius, 5);
            lineLength = array.getDimensionPixelSize(R.styleable.LineView_line_length, 130);
            circleSelectColor = array.getColor(R.styleable.LineView_circle_select_color, getResources().getColor(android.R.color.holo_orange_dark));
            numberSelectColor = array.getColor(R.styleable.LineView_number_select_color, getResources().getColor(android.R.color.holo_orange_dark));
            textSelectColor = array.getColor(R.styleable.LineView_text_select_color, getResources().getColor(android.R.color.holo_orange_dark));
            circleRadius = circleInnerRadius + circleStokeWidth / 2;
        } finally {
            array.recycle();
        }

        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStokeWidth);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineStrokeWidth);

        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        numberPaint.setColor(numberColor);
        numberPaint.setStyle(Paint.Style.STROKE);
        numberPaint.setTextSize(numberTextSize);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(textSize);

        setLongClickable(true);
        setOnTouchListener(this);
        detector = new GestureDetector(getContext(), this);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //此处为与HorizontalScrollView搭配使用,达到滑动目的,别的滑动方式请修改此处代码或者删除
        int mViewWidth = getPaddingLeft() + getPaddingRight() + computeMinViewWidth();
        int mViewHeight = getPaddingBottom() + getPaddingTop() + computeMinViewHeight();
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeCircleAndLinePath();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        if (textRegions == null || textRegions.length == 0) {
            return true;
        }
        for (int i = 0; i < textRegions.length; i++) {
            Region mPicArea = textRegions[i];
            if (mPicArea.contains((int) event.getX(), (int) event.getY())) {
                currSelectedItem = i;
                invalidateView();
                if (onLineViewItemSelectedistener != null) {
                    onLineViewItemSelectedistener.onSelected(i);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * 计算点圆与线轨迹
     */
    private void computeCircleAndLinePath() {
        if (data != null || data.size() != 0) {

            circlePoints = new Point[data.size()];
            circlePaths = new Path[data.size()];
            linePaths = new Path[data.size() - 1];

            //处理文字显示与圆点显示
            //绘制起始偏移量,如果文本高度一半大于半径,则取差值,否则取0
            int offsetX = textHeight / 2 > (circleRadius + circleStokeWidth / 2) ? textHeight / 2 - (circleRadius + circleStokeWidth / 2) : 0;
            //处理paddingLeft
            offsetX += getPaddingLeft();
            for (int i = 0; i < data.size(); i++) {
                Path circlePath = new Path();
                //计算每个圆点的中心点
                int mCircleCenterX = i * lineLength + circleRadius + circleStokeWidth / 2 + offsetX;
                int mCircleCenterY = circleMarginTop + circleRadius;
                circlePoints[i] = new Point(mCircleCenterX, mCircleCenterY);

                //计算圆点路径
                circlePath.addCircle(mCircleCenterX, mCircleCenterY, circleRadius, Path.Direction.CCW);
                circlePaths[i] = circlePath;
                if (i == data.size() - 1) {
                    continue;
                }
                //计算线段路径
                Path linePath = new Path();
                //计算线段起始点
                int mlineStartX = mCircleCenterX + circleRadius + circleStokeWidth / 2;
                int mlineEndX = (i + 1) * lineLength + offsetX;
                linePath.moveTo(mlineStartX, mCircleCenterY);
                linePath.lineTo(mlineEndX, mCircleCenterY);
                linePaths[i] = linePath;
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (data != null || data.size() != 0) {
            drawCircleAndLine(canvas);
            drawNumberAndItemText(canvas);
        }

    }

    /**
     * 绘制数字和文本
     */
    private void drawNumberAndItemText(Canvas canvas) {
        for (int i = 0; i < data.size(); i++) {
            //数字绘制起始点
            int mNumberStartX = circlePoints[i].x;
            int mNumberStartY = circlePoints[i].y + circleRadius + numberMarginTop;
            //测量数字宽度
            float textWidth = numberPaint.measureText(String.valueOf(i + 1), 0, String.valueOf(i + 1).length());
            //改变颜色
            if (i == currSelectedItem) {
                numberPaint.setColor(numberSelectColor);
            } else {
                numberPaint.setColor(numberColor);
            }
            canvas.drawText(String.valueOf(i + 1), mNumberStartX - textWidth / 2, mNumberStartY, numberPaint);
            drawItemText(canvas);
        }
    }

    /**
     * 绘制文本
     */
    private void drawItemText(Canvas canvas) {
        //记录每个条目区域
        textRegions = new Region[data.size()];
        for (int i = 0; i < data.size(); i++) {
            Region region = new Region();
            int mItemStartX = circlePoints[i].x;
            int mItemStartY = circlePoints[i].y + circleRadius + numberMarginTop + textMarginTop;
            String text = data.get(i);
            char[] chars = text.toCharArray();
            //每个条目区域的计算
            Rect textArea = new Rect();
            textArea.left = mItemStartX - textHeight / 2;
            textArea.right = textArea.left + textHeight;
            //减去一个文字高度,因为绘制文字是在baseline上方绘制,基线位置为drawText(text,x,y,paint)的y位置
            textArea.top = mItemStartY - textHeight;
            int lastTextHeight = 0;
            //改变颜色
            if (i == currSelectedItem) {
                textPaint.setColor(textSelectColor);
            } else {
                textPaint.setColor(textColor);
            }
            for (int j = 0; j < chars.length; j++) {
                canvas.drawText(String.valueOf(text.charAt(j)), mItemStartX - textHeight / 2, mItemStartY + lastTextHeight, textPaint);
                lastTextHeight += textHeight + textSpace;
            }
            //此处减去最后一个文字间隔
            textArea.bottom = textArea.top + lastTextHeight - textSpace;
            region.set(textArea);
            textRegions[i] = region;

        }
    }

    /**
     * 计算文本信息,包含每个条目的宽度,高度
     */
    private Rect getTextInfo(String text, Paint paint) {
        Rect mrect = new Rect();
        //获得文本的最小矩形大小,也是测量文本高度,宽度的一种方法
        paint.getTextBounds(text, 0, text.length(), mrect);
        return mrect;
    }

    /**
     * 绘制圆和线段
     */
    private void drawCircleAndLine(Canvas canvas) {
        for (int i = 0; i < circlePaths.length; i++) {
            //改变颜色与风格
            if (i == currSelectedItem) {
                circlePaint.setColor(circleSelectColor);
                circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                circlePaint.setStyle(Paint.Style.STROKE);
                circlePaint.setColor(circleColor);
            }
            canvas.drawPath(circlePaths[i], circlePaint);
        }
        for (int i = 0; i < linePaths.length; i++) {
            canvas.drawPath(linePaths[i], linePaint);
        }
    }

    /**
     * 计算View最小宽度
     */
    private int computeMinViewWidth() {
        int viewWidth = 0;
        if (data != null || data.size() != 0) {
            //计算文本高度,起始不管第一条是否为空,高度都会与其它文本保持一致
            Rect textRect = getTextInfo(data.get(0), textPaint);
            textHeight = textRect.height();
            viewWidth = (data.size() - 1) * lineLength + 2 * circleRadius + circleStokeWidth;
            if (textHeight > (2 * circleRadius + circleStokeWidth)) {
                int offsetX = textHeight / 2 - (circleRadius + circleStokeWidth / 2);
                viewWidth = viewWidth + 2 * offsetX;
            }
        }
        return viewWidth;
    }

    /**
     * 计算View最小高度
     */
    private int computeMinViewHeight() {
        int viewHeight = 0;
        if (data != null || data.size() != 0) {
            String maxText = "";
            for (int i = 0; i < data.size(); i++) {
                //计算文本宽度
                maxText = data.get(i).length() > maxText.length() ? data.get(i) : maxText;
            }
            //获得总文本间距
            int textSpaceWidth = textSpace * (maxText.length() - 1);
            Rect textRect = getTextInfo(maxText, textPaint);
            //最大文本高度
            int textMaxHeight = textRect.width() + textSpaceWidth;
            //计算ViewHeight
            viewHeight = textMaxHeight + textMarginTop + numberMarginTop + (2 * circleRadius + circleStokeWidth) + circleMarginTop;
        }
        return viewHeight;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setSelectItem(int position) {
        this.currSelectedItem = position;
        invalidateView();
    }

    public int getSeletItem() {
        return currSelectedItem;
    }

    public OnLineViewItemSelectedistener getOnLineViewItemSelectedistener() {
        return onLineViewItemSelectedistener;
    }

    public void setOnLineViewItemSelectedistener(OnLineViewItemSelectedistener onLineViewItemSelectedistener) {
        this.onLineViewItemSelectedistener = onLineViewItemSelectedistener;
    }

    interface OnLineViewItemSelectedistener {
        void onSelected(int position);
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}


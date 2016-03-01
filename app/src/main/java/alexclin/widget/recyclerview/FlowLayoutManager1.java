package alexclin.widget.recyclerview;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * FlowLayoutManager
 *
 * @author alexclin
 * @date 16/2/27 16:19
 */
public class FlowLayoutManager1 extends RecyclerView.LayoutManager {

    private static final String TAG = "FlowLayoutManager1";

    private static final boolean DEBUG = true;

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private FlowState mFlowSate;
    private int mScrollDelta = 0; //向上为正，向下为负
    private int oldDirectionValue = -1;

    private boolean mRecycle = false;
    private int orientation;
    private boolean reverseLayout;

    private FlowLayoutManager1(int orientation, boolean reverseLayout) {
        mFlowSate = new FlowState();
        this.orientation = orientation;
        this.reverseLayout = reverseLayout;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            clearLayout(recycler);
            return;
        }
        mRecycle = false;
        fillView(0, recycler, state);
    }

    private void clearLayout(RecyclerView.Recycler recycler) {
        mFlowSate.clearRect();
        removeAndRecycleAllViews(recycler);
    }

    private int getOrientation() {
        return orientation;
    }

    private boolean isReverseLayout() {
        return reverseLayout;
    }

    @Override
    public boolean canScrollVertically() {
        return getOrientation() == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return getOrientation() == HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dy, recycler, state);
    }

    private int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || delta == 0) {
            return 0;
        }
        if (isReverseLayout()) {
            if (mScrollDelta - delta < 0) {
                delta = mScrollDelta;
            }
        } else {
            if (mScrollDelta - delta > 0) {
                delta = mScrollDelta;
            }
        }
        mRecycle = true;
        delta = fillView(delta, recycler, state);
        if (getOrientation() == VERTICAL) {
            offsetChildrenVertical(-delta);
        } else {
            offsetChildrenHorizontal(-delta);
        }
        return delta;
    }

    private int fillView(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (oldDirectionValue == -1) {
            oldDirectionValue = getDirectionValue();
        } else if (oldDirectionValue != getDirectionValue()) {
            clearLayout(recycler);
        }
        int scrollDelta = mScrollDelta - delta;
        if (getChildCount() == 0) {
            int pos = findFirstVisiblePosition(state, scrollDelta);
            for (int i = pos; i < state.getItemCount(); i++) {
                if (!addChild(recycler, i, false, scrollDelta)) {
                    break;
                }
            }
            delta = limitDeltaValue(delta);
        } else {
            boolean isLoadBefore = isReverseLayout() ? (delta > 0) : (delta < 0);
            if (isLoadBefore) {//向下滚动，加载之前的Item
                recycleChildren(recycler, false);
                fillViewStart(recycler, scrollDelta);
            } else {//向上滚动，加载更多的Item
                recycleChildren(recycler, true);
                fillViewEnd(recycler, state, scrollDelta);
                delta = limitDeltaValue(delta);
            }
        }
        return delta;
    }

    private void fillViewEnd(RecyclerView.Recycler recycler, RecyclerView.State state, int scrollDelta) {
        View lastChild = getChildAt(getChildCount() - 1);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) lastChild.getLayoutParams();
        int startPos = params.getViewAdapterPosition();
        for (int i = startPos + 1; i < state.getItemCount(); i++) {
            if (!addChild(recycler, i, false, scrollDelta)) {
                break;
            }
        }
    }

    private void fillViewStart(RecyclerView.Recycler recycler, int scrollDelta) {
        View firstChild = getChildAt(0);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) firstChild.getLayoutParams();
        int startPos = params.getViewAdapterPosition();
        for (int i = startPos - 1; i >= 0; i--) {
            if (!addChild(recycler, i, true, scrollDelta)) {
                break;
            }
        }
    }

    private int limitDeltaValue(int delta) {
        int value = mFlowSate.getVertexDirectionMaxValue();
        if (value < getDirectionValue()) {
            value = getDirectionValue();
        }
        if (isReverseLayout()) {
            if (mScrollDelta + getDirectionValue() - value - delta > 0) {
                delta = mScrollDelta + getDirectionValue() - value;
            }
        } else {
            if (value + mScrollDelta - getDirectionValue() - delta < 0) {
                delta = value + mScrollDelta - getDirectionValue();
            }
        }
        return delta;
    }

    private void recycleChildren(RecyclerView.Recycler recycler, boolean fromStart) {
        if (!mRecycle) return;
        int count = getChildCount();
        List<View> recycleViews = new ArrayList<>();
        if (fromStart) {
            for (int index = 0; index < count; index++) {
                View child = getChildAt(index);
                if (!isVisibleChild(child, getScrollDelta())) {
                    recycleViews.add(child);
                } else {
                    break;
                }
            }
        } else {
            for (int index = count - 1; index >= 0; index--) {
                View child = getChildAt(index);
                if (!isVisibleChild(child, getScrollDelta())) {
                    recycleViews.add(child);
                } else {
                    break;
                }
            }
        }
        for (View child : recycleViews) {
            removeAndRecycleView(child, recycler);
        }
    }

    private boolean isVisibleChild(View child, int scrollDelta) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int pos = params.getViewAdapterPosition();
        Pair<Rect, Rect> pair = mFlowSate.getRectAt(pos);
        return pair == null || isVisibleRect(pair.second, scrollDelta);
    }

    private int findFirstVisiblePosition(RecyclerView.State state, int scrollDelta) {
        for (int i = 0; i < state.getItemCount(); i++) {
            Pair<Rect, Rect> rectPair = mFlowSate.getRectAt(i);
            if (rectPair == null||isVisibleRect(rectPair.second, scrollDelta)) {
                return i;
            }
        }
        return 0;
    }

    private boolean addChild(RecyclerView.Recycler recycler, int i, boolean first, int scrollDelta) {
        View view = recycler.getViewForPosition(i);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        Pair<Rect, Rect> rectPair = mFlowSate.getRectAt(i);
        if (rectPair == null) {
            int widthFactor = params.width;
            int heightFactor = params.height;
            rectPair = mFlowSate.addRect(widthFactor, heightFactor);
        }
        if (!isVisibleRect(rectPair.second, scrollDelta)) {
            return false;
        }

        params.width = rectPair.second.width();
        params.height = rectPair.second.height();
        if (first)
            addView(view, 0);
        else
            addView(view);
        measureChildWithMargins(view, 0, 0);
        int left, right, top, bottom;
        if (getOrientation() == VERTICAL) {
            if (isReverseLayout()) {
                left = rectPair.second.left;
                right = rectPair.second.right;
                top = rectPair.second.top + mScrollDelta + getHeight();
                bottom = rectPair.second.bottom + mScrollDelta + getHeight();
            } else {
                left = rectPair.second.left;
                right = rectPair.second.right;
                top = rectPair.second.top + mScrollDelta;
                bottom = rectPair.second.bottom + mScrollDelta;
            }
        } else {
            if (isReverseLayout()) {
                left = rectPair.second.left + mScrollDelta + getWidth();
                right = rectPair.second.right + mScrollDelta + getWidth();
                top = rectPair.second.top;
                bottom = rectPair.second.bottom;
            } else {
                left = rectPair.second.left + mScrollDelta;
                right = rectPair.second.right + mScrollDelta;
                top = rectPair.second.top;
                bottom = rectPair.second.bottom;
            }
        }
        layoutDecorated(view, left, top, right, bottom);
        return true;
    }

    private boolean isVisibleRect(Rect rect, int scrollDelta) {
        if (getOrientation() == VERTICAL) {
            if (isReverseLayout()) {
                int top = rect.top + scrollDelta + getHeight();
                int bottom = rect.bottom + scrollDelta + getHeight();
                return (top >= 0 && top <= getHeight()) || (bottom >= 0 && bottom <= getHeight());
            } else {
                int top = rect.top + scrollDelta;
                int bottom = rect.bottom + scrollDelta;
                return (top >= 0 && top <= getHeight()) || (bottom >= 0 && bottom <= getHeight());
            }
        } else {
            if (isReverseLayout()) {
                int left = rect.left + scrollDelta + getWidth();
                int right = rect.right + scrollDelta + getWidth();
                return (left >= 0 && left <= getWidth()) || (right >= 0 && right <= getWidth());
            } else {
                int left = rect.left + scrollDelta;
                int right = rect.right + scrollDelta;
                return (left >= 0 && left <= getWidth()) || (right >= 0 && right <= getWidth());
            }
        }
    }

    private int getScrollDelta() {
        return mScrollDelta;
    }

    private int getDirectionValue() {
        return getOrientation() == VERTICAL ? getHeight() : getWidth();
    }

    private int getNotDirectionValue() {
        return getOrientation() != VERTICAL ? getHeight() : getWidth();
    }

    @Override
    public void offsetChildrenVertical(int dy) {
        mScrollDelta += dy;
        super.offsetChildrenVertical(dy);
    }

    @Override
    public void offsetChildrenHorizontal(int dx) {
        mScrollDelta += dx;
        super.offsetChildrenHorizontal(dx);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        //TODO
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //TODO
    }

    @Override
    public void assertInLayoutOrScroll(String message) {
        //TODO
    }

    @Override
    public void scrollToPosition(int position) {
        //TODO
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        //TODO
    }

    @Nullable
    @Override
    public View onFocusSearchFailed(View focused, int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() > 0) return getChildAt(0);
        return null;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        super.smoothScrollToPosition(recyclerView, state, position);
    }

    public static abstract class Adapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
        private FlowLayoutManager1 layoutManager;

        public Adapter() {
            this(VERTICAL, false);
        }

        public Adapter(int orientation, boolean reverseLayout) {
            this.layoutManager = new FlowLayoutManager1(orientation,reverseLayout);
        }

        public abstract int widthForPosition(int position,int directionWidth);

        public abstract int heightForPosition(int position,int directionWidth);

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            recyclerView.setLayoutManager(layoutManager);
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            recyclerView.setLayoutManager(null);
        }

        @Override
        public final void onBindViewHolder(VH holder, int position, List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
            setItemLayoutParams(holder,position);
        }

        private void setItemLayoutParams(VH holder, int position) {
            int directionValue = layoutManager.getNotDirectionValue();
            int width = widthForPosition(position, directionValue);
            int height = heightForPosition(position,directionValue);
            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final RecyclerView.LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = new LayoutParams(width, height);
            } else {
                if (!(lp instanceof RecyclerView.LayoutParams)) {
                    if (lp instanceof ViewGroup.MarginLayoutParams) {
                        rvLayoutParams = new LayoutParams((ViewGroup.MarginLayoutParams) lp);
                    } else {
                        rvLayoutParams = new LayoutParams(lp);
                    }
                } else {
                    rvLayoutParams = (RecyclerView.LayoutParams) lp;
                }
                rvLayoutParams.width = width;
                rvLayoutParams.height = height;
            }
            Log.e("TAG",String.format("w:%d,h:%d",width,height));
            holder.itemView.setLayoutParams(rvLayoutParams);
        }
    }

    /**
     * 磁贴布局位置计算和存储
     *
     * @author xiehonglin429 on 16/2/26.
     */
    private class FlowState implements Comparator<Point> {

        private List<Point> mVertexPoints = new ArrayList<>(); //布局过程中的顶点,用于计算下一个添加的磁贴的布局位置

        private List<Pair<Rect, Rect>> mRectList = new ArrayList<>();//已经添加的磁贴的布局位置列表

        /**
         * 比较器,用于按照x坐标从小到大排列顶点列表
         */
        @Override
        public int compare(Point lhs, Point rhs) {
            return (getOrientation() == HORIZONTAL) ? (lhs.y - rhs.y) : (lhs.x - rhs.x);
        }


        public Pair<Rect, Rect> getRectAt(int index) {
            if (index < mRectList.size() && index >= 0) return mRectList.get(index);
            return null;
        }

        public void clearRect() {
            mRectList.clear();
            mVertexPoints.clear();
        }

        /**
         * 按照给定的宽高添加磁贴
         *
         * @param width  宽度
         * @param height 高度
         * @return 添加的磁贴的布局位置信息
         */
        public Pair<Rect, Rect> addRect(int width, int height) {
            Point vertex = getSmallestValueOnDirectionVertex();
            boolean isV = getOrientation() == VERTICAL;
            Rect rect = new Rect(vertex.x, vertex.y, vertex.x + width, vertex.y + height);
            boolean needAdd;
            if (mVertexPoints.size() == 0) {//当前顶点列表为空
                needAdd = true;
            } else if (valueSqDirection(mVertexPoints.get(mVertexPoints.size() - 1)) < getNotDirectionValue()) {
                //最后一个顶点的x坐标小与总宽度,代表紧贴顶部的第一行仍未布满
                boolean removeOld = isV ? (rect.bottom == mVertexPoints.get(mVertexPoints.size() - 1).y) : (rect.right == mVertexPoints.get(mVertexPoints.size() - 1).x);
                if (removeOld) {
                    //前一个顶点被填平,删除它
                    mVertexPoints.remove(mVertexPoints.size() - 1);
                }
                needAdd = true;
            } else {
                //删除被新添加的磁贴覆盖和填平的旧顶点,并添加新顶点
                needAdd = removeOldAndAddVertex(rect);
            }
            if (needAdd) {
                mVertexPoints.add(new Point(rect.right, rect.bottom));
                Collections.sort(mVertexPoints, this);
            }
            Rect realRect = convertToReal(rect);
            Pair<Rect, Rect> pair = new Pair<Rect, Rect>(rect, realRect);
            mRectList.add(pair);
            return pair;
        }

        private Rect convertToReal(Rect rect) {
            Rect realRect = new Rect(rect.left, rect.top, rect.right, rect.bottom);
            return isReverseLayout() ? reverseRect(realRect) : realRect;
        }

        /**
         * 根据方向反转矩形
         *
         * @param realRect
         * @return
         */
        private Rect reverseRect(Rect realRect) {
            if (getOrientation() == VERTICAL) {
                int rBottom = -realRect.top;
                int rTop = -realRect.bottom;
                realRect.bottom = rBottom;
                realRect.top = rTop;
            } else {
                int rLeft = -realRect.right;
                int rRight = -realRect.left;
                realRect.left = rLeft;
                realRect.right = rRight;
            }
            return realRect;
        }

        /**
         * 删除被新添加的磁贴覆盖和填平的旧顶点,并判断是否要添加新顶点
         *
         * @param rect 新增加的磁贴的布局位置
         * @return 是否需要添加新顶点
         */
        private boolean removeOldAndAddVertex(Rect rect) {
            Iterator<Point> iterator = mVertexPoints.iterator();
            boolean needAdd = true;
            boolean isFirst = true;
            boolean isV = getOrientation() == VERTICAL;
            while (iterator.hasNext()) {
                Point vertex = iterator.next();
                boolean remove;
                if (isV) {
                    remove = (vertex.x > rect.left && vertex.x <= rect.right) || (vertex.y == rect.bottom && vertex.x == rect.left);
                } else {
                    remove = (vertex.y > rect.top && vertex.y <= rect.bottom) || (vertex.x == rect.right && vertex.y == rect.top);
                }
                if (remove) {
                    iterator.remove();
                }
                boolean bool = isV ? (isFirst && vertex.x > rect.right) : (isFirst && vertex.y > rect.bottom);
                if (bool) {
                    if (isV ? (vertex.y == rect.bottom) : (vertex.x == rect.right))
                        needAdd = false;
                    isFirst = false;
                }
            }
            return needAdd;
        }

        /**
         * 获取当前顶点中的y坐标最小顶点
         *
         * @return y坐标最小的顶点
         */
        private Point getMinimumHeightVertexPoint() {
            int index = 0;
            Point point = mVertexPoints.get(index);
            if (mVertexPoints.size() > 1) {
                for (int i = 1; i < mVertexPoints.size(); i++) {
                    Point p = mVertexPoints.get(i);
                    if (compareDirection(p, point) < 0) {
                        if (i > 0) {
                            Point prevP = mVertexPoints.get(i - 1);
                            if (valueSqDirection(prevP) > getNotDirectionValue()) {
                                continue;
                            }
                        }
                        point = p;
                        index = i;
                    }
                }
            }
            boolean v = getOrientation() == VERTICAL;
            int value = valueDirection(point);
            if (index == 0) {
                return new Point(v ? 0 : value, v ? point.y : 0);
            } else {
                Point prevP = mVertexPoints.get(index - 1);
                int pValue = valueSqDirection(prevP);
                return new Point(v ? pValue : value, v ? value : pValue);
            }
        }

        private Point getSmallestValueOnDirectionVertex() {
            if (mVertexPoints.size() == 0) return new Point(0, 0);
            Point vertex = mVertexPoints.get(mVertexPoints.size() - 1);
            if (valueSqDirection(vertex) < getNotDirectionValue()) {
                //最后一个顶点的x坐标小与总宽度,代表紧贴顶部的第一行仍未布满
                boolean v = (getOrientation() == VERTICAL);
                int sqValue = valueSqDirection(vertex);
                return new Point(v ? sqValue : 0, v ? 0 : sqValue);
            } else {
                return getMinimumHeightVertexPoint();
            }
        }

        public int getVertexDirectionMaxValue() {
            Point point = mVertexPoints.get(0);
            if (mVertexPoints.size() > 1) {
                for (int i = 1; i < mVertexPoints.size(); i++) {
                    Point p = mVertexPoints.get(i);
                    if (compareDirection(p, point) > 0) {
                        point = p;
                    }
                }
            }
            return valueDirection(point);
        }

        /**
         * 当前滑动方向上的坐标值
         */
        private int valueDirection(Point point) {
            return (getOrientation() == HORIZONTAL) ? point.x : point.y;
        }

        /**
         * 垂直于滑动方向的坐标值
         *
         * @param point 点
         * @return
         */
        private int valueSqDirection(Point point) {
            return (getOrientation() == VERTICAL) ? point.x : point.y;
        }

        /**
         * 比较两个点在滑动坐标方向上坐标的大小
         *
         * @param lhs 左点
         * @param rhs 右点
         * @return
         */
        private int compareDirection(Point lhs, Point rhs) {
            return  (getOrientation() == HORIZONTAL) ? (lhs.x - rhs.x) : (lhs.y - rhs.y);
        }
    }
}

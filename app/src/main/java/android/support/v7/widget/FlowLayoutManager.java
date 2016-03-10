package android.support.v7.widget;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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
public class FlowLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "FlowLayoutManager";

    private static final boolean DEBUG = true;

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private FlowState mFlowSate;
    private Adapter mFlowSource;
    private int mScrollDelta = 0; //向上为正，向下为负
    private int oldDirectionValue = -1;

    private boolean mRecycle = false;

    public FlowLayoutManager(Adapter flowSource) {
        this.mFlowSate = new FlowState();
        this.mFlowSource = flowSource;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private int getOrientation(){
        return mFlowSource.orientation();
    }

    private boolean isReverseLayout(){
        return mFlowSource.reverseLayout();
    }

    @Override
    public boolean canScrollVertically() {
        return getOrientation()==VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return getOrientation()==HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dx,recycler,state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dy,recycler,state);
    }

    private int scrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || delta == 0) {
            return 0;
        }
        if(isReverseLayout()){
            if(mScrollDelta - delta<0){
                delta = mScrollDelta;
            }
        }else{
            if(mScrollDelta - delta>0){
                delta = mScrollDelta;
            }
        }
        mRecycle = true;
        delta = fillView(delta, recycler, state);
        offsetContent(delta);
        return delta;
    }

    private void offsetContent(int newDelta) {
        if(getOrientation()==VERTICAL){
            offsetChildrenVertical(-newDelta);
        }else{
            offsetChildrenHorizontal(-newDelta);
        }
    }

    private void addFooter(RecyclerView.Recycler recycler,RecyclerView.State state) {
        View footer = recycler.getViewForPosition(state.getItemCount()-1);
        measureAndLayoutChild(footer,getFooterBaseRect(),false,mFlowSource.getHeaderOffset());
    }

    private boolean isFooter(int i, RecyclerView.State state) {
        return mFlowSource.hasFooter()&&i==state.getItemCount()-1;
    }

    private Rect getFooterBaseRect() {
        checkFlowStateIndex(mFlowSource.getFlowCount()-1);
        int maxValue = mFlowSate.getVertexDirectionMaxValue()+mFlowSource.getHeaderOffset();
        boolean isV = getOrientation()==VERTICAL;
        int top = isV?maxValue:0;
        int left = isV?0:maxValue;
        int bottom = isV?top+mFlowSource.getFooterOffset():getNotDirectionValue();
        int right = isV?getNotDirectionValue():left+mFlowSource.getFooterOffset();
        Rect rect = new Rect(left,top,right,bottom);
        return isReverseLayout()?reverseRect(rect):rect;
    }

    private void checkFlowStateIndex(int flowIndex) {
        if(flowIndex<0) return;
        if(flowIndex>mFlowSource.getFlowCount()-1) flowIndex = mFlowSource.getFlowCount()-1;
        if(mFlowSate.getRectAt(flowIndex)!=null) return;
        int start = mFlowSate.getRectSize()>0?mFlowSate.getRectSize()-1:0;
        for(int i=start;i<mFlowSource.getFlowCount();i++){
            mFlowSate.addRect(mFlowSource.widthFactorAt(i),mFlowSource.heightFactorAt(i));
        }
    }

    private void addHeader(RecyclerView.Recycler recycler) {
        View header = recycler.getViewForPosition(0);
        measureAndLayoutChild(header, getHeaderBaseRect(),true, 0);
    }

    private boolean isHeader(int i) {
        return mFlowSource.hasHeader()&&i==0;
    }

    private Rect getHeaderBaseRect(){
        boolean isV = getOrientation()==VERTICAL;
        Rect headerRect = new Rect(0,0,isV?getNotDirectionValue():mFlowSource.getHeaderOffset(),isV?mFlowSource.getHeaderOffset():getNotDirectionValue());
        return isReverseLayout()?reverseRect(headerRect):headerRect;
    }

    private Rect getLayoutRect(Rect realRect,int delta){
        int left,right,top,bottom;
        if(getOrientation()==VERTICAL){
            if(isReverseLayout()){
                left = realRect.left+getPaddingLeft();
                right = realRect.right+getPaddingLeft();
                top = realRect.top+ mScrollDelta+getDirectionValue()-delta+getPaddingTop();
                bottom = realRect.bottom+ mScrollDelta+getDirectionValue()-delta+getPaddingTop();
            }else{
                left = realRect.left+getPaddingLeft();
                right = realRect.right+getPaddingLeft();
                top = realRect.top+ mScrollDelta+delta+getPaddingTop();
                bottom = realRect.bottom+ mScrollDelta+delta+getPaddingTop();
            }
        }else{
            if(isReverseLayout()){
                left = realRect.left+ mScrollDelta+getDirectionValue()-delta+getPaddingLeft();
                right = realRect.right+ mScrollDelta+getDirectionValue()-delta+getPaddingLeft();
                top = realRect.top+getPaddingTop();
                bottom = realRect.bottom+getPaddingTop();
            }else{
                left = realRect.left+ mScrollDelta+delta+getPaddingLeft();
                right = realRect.right+ mScrollDelta+delta+getPaddingLeft();
                top = realRect.top+getPaddingTop();
                bottom = realRect.bottom+getPaddingTop();
            }
        }
        return new Rect(left,top,right,bottom);
    }

    private int fillView(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(oldDirectionValue ==-1){
            oldDirectionValue = getDirectionValue();
        }else if(oldDirectionValue !=getDirectionValue()){
            clearLayout(recycler);
            oldDirectionValue = getDirectionValue();
        }
        int scrollDelta = mScrollDelta-delta;
        if(getChildCount()==0){
            int pos = findFirstVisiblePosition(state, scrollDelta);
            if(pos==-1){
                int max = getLayoutDirectionTotalValue();
                scrollDelta = getDirectionValue()-max;
                if(scrollDelta>0) scrollDelta = 0;
                if(mScrollDelta<scrollDelta){
                    int newDelta = mScrollDelta-scrollDelta;
                    if(delta==0){
                        offsetContent(newDelta);
                    }else
                        delta = newDelta;
                }
                fillViewStart(recycler,state,scrollDelta);
                return delta;
            }
            for(int i=pos;i<state.getItemCount();i++){
                if(isHeader(i)){
                    addHeader(recycler);
                    continue;
                }
                if(isFooter(i, state)){
                    addFooter(recycler,state);
                    continue;
                }
                if (!addFlowChild(recycler, i, false, scrollDelta,true)){
                    break;
                }
            }
            delta = limitDeltaValue(delta);
        }else{
            boolean isLoadBefore = isReverseLayout()?(delta>0):(delta<0);
            if(isLoadBefore){//向下滚动，加载之前的Item
                recycleChildren(recycler,state, false);
                fillViewStart(recycler,state, scrollDelta);
            }else{//向上滚动，加载更多的Item
                recycleChildren(recycler,state, true);
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
            if (isHeader(i)) {
                addHeader(recycler);
                continue;
            }
            if (isFooter(i, state)) {
                addFooter(recycler, state);
                continue;
            }
            if (!addFlowChild(recycler, i, false, scrollDelta,true)) {
                break;
            }
        }
    }

    private void fillViewStart(RecyclerView.Recycler recycler,RecyclerView.State state, int scrollDelta) {
        int startPos;
        if(getChildCount()>0){
            View firstChild = getChildAt(0);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) firstChild.getLayoutParams();
            startPos = params.getViewAdapterPosition();
        }else{
            startPos = state.getItemCount();
        }
        int endPos = findFirstVisiblePosition(state,scrollDelta);
        for(int i=startPos-1;i>=endPos;i--){
            if (isHeader(i)){
                addHeader(recycler);
                continue;
            }else if(isFooter(i, state)){
                addFooter(recycler,state);
                continue;
            }
            addFlowChild(recycler, i, true, scrollDelta,false);
        }
    }

    private int limitDeltaValue(int delta) {
        int value = getLayoutDirectionTotalValue();
        if(isReverseLayout()){
            if(mScrollDelta+getDirectionValue()-value-delta>0){
                delta = mScrollDelta+getDirectionValue() - value;
            }
        }else{
            if(value+ mScrollDelta - getDirectionValue()-delta<0){
                delta = value+ mScrollDelta - getDirectionValue();
            }
        }
        return delta;
    }

    private int getLayoutDirectionTotalValue() {
        int value = mFlowSate.getVertexDirectionMaxValue()+mFlowSource.getHeaderOffset()+mFlowSource.getFooterOffset();
        if(value<getDirectionValue()){
            value = getDirectionValue();
        }
        return value;
    }

    private void recycleChildren(RecyclerView.Recycler recycler,RecyclerView.State state,boolean fromStart){
        if(!mRecycle) return;
        int count = getChildCount();
        List<View> recycleViews = new ArrayList<>();
        if(fromStart){
            for(int index =0;index<count;index++){
                View child = getChildAt(index);
                if(!isVisibleChild(child,state,getScrollDelta())){
                    recycleViews.add(child);
                }else {
                    break;
                }
            }
        }else{
            for(int index =count-1;index>=0;index--){
                View child = getChildAt(index);
                if(!isVisibleChild(child,state,getScrollDelta())){
                    recycleViews.add(child);
                }else {
                    break;
                }
            }
        }
        for(View child:recycleViews){
            removeAndRecycleView(child, recycler);
        }
    }



    private int findFirstVisiblePosition(RecyclerView.State state, int scrollDelta) {
        for(int i=0;i<state.getItemCount();i++){
            if(isHeader(i)){
                if(isVisibleRect(getHeaderBaseRect(),scrollDelta))
                    return 0;
                else
                    continue;
            }else if(isFooter(i,state)){
                if(isVisibleRect(getFooterBaseRect(),scrollDelta)){
                    return i;
                }else{
                    return -1;
                }
            }
            int index = itemIndexToFlowIndex(i);
            Pair<Rect,Rect> rectPair = mFlowSate.getRectAt(index);
            if(rectPair==null){
                int widthFactor = mFlowSource.widthFactorAt(index);
                int heightFactor = mFlowSource.heightFactorAt(index);
                rectPair = mFlowSate.addRect(widthFactor,heightFactor);
            }
            if(isVisibleRect(rectPair.second, scrollDelta)){
                return i;
            }
        }
        return -1;
    }

    private int itemIndexToFlowIndex(int index){
        return mFlowSource.hasHeader()?index-1:index;
    }

    private boolean addFlowChild(RecyclerView.Recycler recycler, int i, boolean first, int scrollDelta,boolean breakUnvisible) {
        int index = itemIndexToFlowIndex(i);
        Pair<Rect,Rect> rectPair = mFlowSate.getRectAt(index);
        if(rectPair==null){
            if(mFlowSource ==null) return false;
            int widthFactor = mFlowSource.widthFactorAt(index);
            int heightFactor = mFlowSource.heightFactorAt(index);
            rectPair = mFlowSate.addRect(widthFactor,heightFactor);
        }
        if(breakUnvisible&&!isVisibleRect(rectPair.second,scrollDelta)){
            return false;
        }
        View view = recycler.getViewForPosition(i);
        measureAndLayoutChild(view, rectPair.second, first, mFlowSource.getHeaderOffset());
        return true;
    }

    private void measureAndLayoutChild(View view, Rect rect, boolean addFirst, int offset){
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        params.width = rect.width()-params.leftMargin-params.rightMargin;
        params.height = rect.height()-params.topMargin-params.bottomMargin;
        if(addFirst)
            addView(view,0);
        else
            addView(view);
        measureChildWithMargins(view, params.leftMargin+params.bottomMargin, params.topMargin+params.bottomMargin);
        Rect layoutRect = getLayoutRect(rect, offset);
        layoutDecorated(view, layoutRect.left + params.leftMargin, layoutRect.top + params.topMargin,
                layoutRect.right - params.rightMargin, layoutRect.bottom - params.bottomMargin);
    }

    private boolean isVisibleChild(View child,RecyclerView.State state,int scrollDelta) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int pos = params.getViewAdapterPosition();
        if(isHeader(pos))
            return isVisibleRect(getHeaderBaseRect(),scrollDelta);
        if(isFooter(pos,state))
            return isVisibleRect(getFooterBaseRect(),scrollDelta);
        Pair<Rect,Rect> pair = mFlowSate.getRectAt(itemIndexToFlowIndex(pos));
        return pair==null||isVisibleRect(pair.second,scrollDelta);
    }

    private boolean isVisibleRect(Rect rect,int scrollDelta){
        if(getOrientation()==VERTICAL){
            int height = getDirectionValue();
            if(isReverseLayout()){
                int top = rect.top+scrollDelta+height-mFlowSource.getHeaderOffset();
                int bottom = rect.bottom+scrollDelta+height-mFlowSource.getHeaderOffset();
                return (top>-1&&top<=height)||(bottom>-1&&bottom<=height);
            }else{
                int top = rect.top+scrollDelta+mFlowSource.getHeaderOffset();
                int bottom = rect.bottom+scrollDelta+mFlowSource.getHeaderOffset();
                return (top>-1&&top<=height)||(bottom>-1&&bottom<=height);
            }
        }else{
            int width = getDirectionValue();
            if(isReverseLayout()){
                int left = rect.left+scrollDelta+width-mFlowSource.getHeaderOffset();
                int right = rect.right+scrollDelta+width-mFlowSource.getHeaderOffset();
                return (left>-1&&left<=width)||(right>-1&&right<=width);
            }else{
                int left = rect.left+scrollDelta+mFlowSource.getHeaderOffset();
                int right = rect.right+scrollDelta+mFlowSource.getHeaderOffset();
                return (left>-1&&left<=width)||(right>-1&&right<=width);
            }
        }
    }

    private int getScrollDelta(){
        return mScrollDelta;
    }

    private int getDirectionValue(){
        return getOrientation()==VERTICAL?getHeight()-getPaddingTop()-getPaddingBottom():getWidth()-getPaddingLeft() -getPaddingRight();
    }

    private int getNotDirectionValue() {
        return getOrientation() != VERTICAL ? getHeight()-getPaddingTop()-getPaddingBottom() : getWidth()-getPaddingLeft()-getPaddingRight();
    }
    /**
    * 根据方向反转矩形
    * @param realRect
    * @return
            */
    private Rect reverseRect(Rect realRect) {
        if(getOrientation()==VERTICAL){
            int rBottom = -realRect.top;
            int rTop = -realRect.bottom;
            realRect.bottom = rBottom;
            realRect.top = rTop;
        }else{
            int rLeft = -realRect.right;
            int rRight = -realRect.left;
            realRect.left = rLeft;
            realRect.right = rRight;
        }
        return realRect;
    }

    @Override
    public void offsetChildrenVertical(int dy) {
        mScrollDelta +=dy;
        super.offsetChildrenVertical(dy);
    }

    @Override
    public void offsetChildrenHorizontal(int dx) {
        mScrollDelta +=dx;
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
    public void scrollToPosition(int position) {
        offsetContent(calculateScrollDelta(position));
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return FlowLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }
                };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    private PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos != isReverseLayout() ? -1 : 1;
        if (getOrientation() == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    private int calculateScrollDelta(int position){
        int delta = 0;
        if(position>=0&&position<getItemCount()){
            if(position==0){
                delta = mScrollDelta;
            }else{
                Rect posRect;
                if(mFlowSource.hasFooter()&&position==getItemCount()-1){
                    posRect = getLayoutRect(getFooterBaseRect(),mFlowSource.getHeaderOffset());
                }else{
                    int index = itemIndexToFlowIndex(position);
                    checkFlowStateIndex(index);
                    Pair<Rect,Rect> pair = mFlowSate.getRectAt(index);
                    if(pair!=null){
                        posRect = getLayoutRect(pair.second,mFlowSource.getHeaderOffset());
                    }else{
                        return 0;
                    }
                }
                if(getOrientation()==VERTICAL){
                    if(isReverseLayout()){
                        delta = mScrollDelta+posRect.top;
                    }else{
                        delta = getDirectionValue()-posRect.bottom-mScrollDelta;
                    }
                }else{
                    if (isReverseLayout())
                        delta = getDirectionValue()-posRect.right-mScrollDelta;
                    else
                        delta = mScrollDelta + posRect.left;
                }
            }
        }
        return delta;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        //ignore
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        clearLayout(recyclerView.mRecycler);
        recyclerView.postInvalidate();
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        onItemsChanged(recyclerView);
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        onItemsChanged(recyclerView);
    }

    @Override
    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        onItemsChanged(recyclerView);
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        onItemsChanged(recyclerView);
    }

    @Nullable
    @Override
    public View onFocusSearchFailed(View focused, int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(getChildCount()>0) return getChildAt(0);
        return null;
    }

    private static class InnerHolder extends RecyclerView.ViewHolder{
        private int type;

        public InnerHolder(View itemView,int type) {
            super(itemView);
            this.type = type;
        }
    }

    public static abstract class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int orientation = VERTICAL;
        private boolean reverseLayout = false;
        private View mHeaderView;
        private View mFooterView;
        private int mHeaderValue;
        private int mFooterValue;

        private FlowLayoutManager layoutManager;

        private static final int TYPE_HEADER = -20160301;
        private static final int TYPE_FOOTER = -20160302;

        public Adapter() {
            this(VERTICAL,false);
        }

        public Adapter(int orientation, boolean reverseLayout) {
            this.orientation = orientation;
            this.reverseLayout = reverseLayout;
        }

        public abstract int totalFactor();
        public abstract int widthFactorAt(int position);
        public abstract int heightFactorAt(int position);

        public int orientation() {
            return orientation;
        }

        public boolean reverseLayout(){
            return reverseLayout;
        }

        protected int getUnitValue(){
            return layoutManager.getNotDirectionValue()/totalFactor();
        }

        @Override
        public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType==TYPE_HEADER||viewType==TYPE_FOOTER){
                return new InnerHolder(viewType == TYPE_HEADER ? mHeaderView : mFooterView,viewType);
            }
            return onCreateFlowViewHolder(parent,viewType);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            layoutManager = new FlowLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            recyclerView.setLayoutManager(null);
        }

        @Override
        public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
            if(holder instanceof InnerHolder){
                setHeaderFooterParams((InnerHolder) holder);
            }else{
                onBindViewHolder(holder,realPosition(position));
            }
        }

        private int realPosition(int position){
            return hasHeader()?position-1:position;
        }

        private void setHeaderFooterParams(InnerHolder holder) {
            int value = holder.type==TYPE_HEADER?mHeaderValue:mFooterValue;
            int notDirectionValue = layoutManager.getNotDirectionValue();
            int width = orientation==VERTICAL?notDirectionValue:value;
            int height = orientation==VERTICAL?value:notDirectionValue;
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
            holder.itemView.setLayoutParams(rvLayoutParams);
        }

        @Override
        public final int getItemViewType(int position) {
            if(mHeaderView!=null&&position==0){
                return TYPE_HEADER;
            }else if(mFooterView!=null&&position==getItemCount()-1){
                return TYPE_FOOTER;
            }else{
                return getFlowViewType(realPosition(position));
            }
        }

        @Override
        public final int getItemCount() {
            int count = getFlowCount();
            if(mHeaderView!=null) count++;
            if(mFooterView!=null) count++;
            return count;
        }

        private int dip2px(View view,float dpValue) {
            final float scale = view.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        public final void setHeaderDip(View header,float headHeightDip){
            setHeader(header,header!=null?dip2px(header,headHeightDip):0);
        }

        public final void setFooterDip(View footer,float footerHeightDip){
            setFooter(footer,footer!=null?dip2px(footer,footerHeightDip):0);
        }

        public final void setHeader(View header,int headerHeight){
            mHeaderView = header;
            mHeaderValue = headerHeight;
            notifyDataSetChanged();
        }

        public final void setFooter(View footer,int footerHeight){
            mFooterView = footer;
            mFooterValue = footerHeight;
            notifyDataSetChanged();
        }

        public int getHeaderOffset(){
            return hasHeader()?mHeaderValue:0;
        }

        public int getFooterOffset(){
            return hasFooter()?mFooterValue:0;
        }

        public abstract int getFlowCount();

        public int getFlowViewType(int position){
            return 0;
        }

        public boolean hasHeader(){
            return mHeaderView!=null;
        }

        public boolean hasFooter(){
            return mFooterView!=null;
        }

        public abstract RecyclerView.ViewHolder onCreateFlowViewHolder(ViewGroup parent, int viewType);
    }


    /**
     * 磁贴布局位置计算和存储
     * @author xiehonglin429 on 16/2/26.
     */
    private class FlowState implements Comparator<Point>{

        private List<Point> mVertexPoints = new ArrayList<>(); //布局过程中的顶点,用于计算下一个添加的磁贴的布局位置

        private List<Pair<Rect,Rect>> mRectList = new ArrayList<>();//已经添加的磁贴的布局位置列表

        /**
         * 比较器,用于按照x坐标从小到大排列顶点列表
         */
        @Override
        public int compare(Point lhs, Point rhs) {
            return (getOrientation()==HORIZONTAL)?(lhs.y - rhs.y):(lhs.x - rhs.x);
        }


        public Pair<Rect,Rect> getRectAt(int index){
            if(index<mRectList.size()&& index>=0) return mRectList.get(index);
            return null;
        }

        public void clearRect(){
            mRectList.clear();
            mVertexPoints.clear();
        }

        public int getRectSize(){
            return mRectList.size();
        }

        /**
         *
         * 按照给定的宽高添加磁贴
         * @param width 宽度
         * @param height 高度
         * @return 添加的磁贴的布局位置信息
         */
        public Pair<Rect, Rect> addRect(int width,int height){
            Point vertex = getSmallestValueOnDirectionVertex();
            boolean isV = getOrientation() == VERTICAL;
            Rect rect = new Rect(vertex.x,vertex.y,vertex.x+width,vertex.y+height);
            boolean needAdd;
            if(mVertexPoints.size()==0){//当前顶点列表为空
                needAdd = true;
            } else if (valueSqDirection(mVertexPoints.get(mVertexPoints.size()-1))< mFlowSource.totalFactor()) {
                //最后一个顶点的x坐标小与总宽度,代表紧贴顶部的第一行仍未布满
                boolean removeOld = isV?(rect.bottom == mVertexPoints.get(mVertexPoints.size()-1).y):(rect.right == mVertexPoints.get(mVertexPoints.size()-1).x);
                if (removeOld) {
                    //前一个顶点被填平,删除它
                    mVertexPoints.remove(mVertexPoints.size()-1);
                }
                needAdd = true;
            } else {
                //删除被新添加的磁贴覆盖和填平的旧顶点,并添加新顶点
                needAdd = removeOldAndAddVertex(rect);
            }
            if(needAdd) {
                mVertexPoints.add(new Point(rect.right,rect.bottom));
                Collections.sort(mVertexPoints, this);
            }
            Rect realRect = convertToReal(rect);
            Pair<Rect, Rect> pair = new Pair<Rect, Rect>(rect,realRect);
            mRectList.add(pair);
            return pair;
        }

        private Rect convertToReal(Rect rect) {
            int realTotalValue = getNotDirectionValue();
            int top = rect.top*realTotalValue/mFlowSource.totalFactor();
            int left = rect.left*realTotalValue/mFlowSource.totalFactor();
            int bottom = rect.bottom*realTotalValue/mFlowSource.totalFactor();
            int right = rect.right*realTotalValue/mFlowSource.totalFactor();
            Rect realRect = new Rect(left,top,right,bottom);
            return isReverseLayout()?reverseRect(realRect):realRect;
        }

        /**
         * 删除被新添加的磁贴覆盖和填平的旧顶点,并判断是否要添加新顶点
         * @param rect 新增加的磁贴的布局位置
         * @return 是否需要添加新顶点
         */
        private boolean removeOldAndAddVertex(Rect rect) {
            Iterator<Point> iterator = mVertexPoints.iterator();
            boolean needAdd = true;
            boolean isFirst = true;
            boolean isV = getOrientation() == VERTICAL;
            while (iterator.hasNext()){
                Point vertex = iterator.next();
                boolean remove;
                if(isV){
                    remove = (vertex.x>rect.left&&vertex.x<=rect.right)||(vertex.y==rect.bottom&&vertex.x==rect.left);
                }else{
                    remove = (vertex.y>rect.top&&vertex.y<=rect.bottom)||(vertex.x==rect.right&&vertex.y==rect.top);
                }
                if(remove){
                    iterator.remove();
                }
                boolean bool = isV?(isFirst&&vertex.x>rect.right):(isFirst&&vertex.y>rect.bottom);
                if(bool){
                    if(isV?(vertex.y==rect.bottom):(vertex.x==rect.right))
                        needAdd = false;
                    isFirst = false;
                }
            }
            return needAdd;
        }

        /**
         * 获取当前顶点中的y坐标最小顶点
         * @return y坐标最小的顶点
         */
        private Point getMinimumHeightVertexPoint() {
            int index = 0;
            Point point = mVertexPoints.get(index);
            if(mVertexPoints.size()>1){
                for(int i=1;i<mVertexPoints.size();i++){
                    Point p = mVertexPoints.get(i);
                    if(compareDirection(p,point)<0){
                        if(i>0){
                            Point prevP = mVertexPoints.get(i-1);
                            if(valueSqDirection(prevP)> mFlowSource.totalFactor()){
                                continue;
                            }
                        }
                        point = p;
                        index = i;
                    }
                }
            }
            boolean v = getOrientation()==VERTICAL;
            int value = valueDirection(point);
            if(index==0){
                return new Point(v?0:value,v?point.y:0);
            }else{
                Point prevP = mVertexPoints.get(index-1);
                int pValue = valueSqDirection(prevP);
                return new Point(v?pValue:value,v?value:pValue);
            }
        }

        private Point getSmallestValueOnDirectionVertex(){
            if(mVertexPoints.size()==0) return new Point(0,0);
            Point vertex = mVertexPoints.get(mVertexPoints.size()-1);
            if(valueSqDirection(vertex)< mFlowSource.totalFactor()){
                //最后一个顶点的x坐标小与总宽度,代表紧贴顶部的第一行仍未布满
                boolean v = (getOrientation()==VERTICAL);
                int sqValue = valueSqDirection(vertex);
                return new Point(v?sqValue:0,v?0:sqValue);
            }else{
                return getMinimumHeightVertexPoint();
            }
        }

        public int getVertexDirectionMaxValue(){
            if(mVertexPoints.isEmpty()) return 0;
            Point point = mVertexPoints.get(0);
            if(mVertexPoints.size()>1){
                for(int i=1;i<mVertexPoints.size();i++){
                    Point p = mVertexPoints.get(i);
                    if(compareDirection(p, point)>0){
                        point = p;
                    }
                }
            }
            int realTotalValue =getNotDirectionValue();
            return valueDirection(point)*realTotalValue/mFlowSource.totalFactor();
        }

        /**
         * 当前滑动方向上的坐标值
         */
        private int valueDirection(Point point){
            return (getOrientation()==HORIZONTAL)?point.x:point.y;
        }

        /**
         * 垂直于滑动方向的坐标值
         * @param point 点
         * @return
         */
        private int valueSqDirection(Point point){
            return (getOrientation()==VERTICAL)?point.x:point.y;
        }

        /**
         * 比较两个点在滑动坐标方向上坐标的大小
         * @param lhs 左点
         * @param rhs 右点
         * @return
         */
        private int compareDirection(Point lhs, Point rhs){
            return (getOrientation()==HORIZONTAL)?(lhs.x-rhs.x):(lhs.y-rhs.y);
        }
    }
}

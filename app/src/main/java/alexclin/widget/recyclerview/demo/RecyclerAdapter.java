package alexclin.widget.recyclerview.demo;


import android.content.res.Resources;
import android.support.v7.widget.FlowLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * @author alexclin on 16/2/26.
 */
public class RecyclerAdapter extends FlowLayoutManager.Adapter{
    private List<Item> mList;

    public RecyclerAdapter(List<Item> mList,int orientation,boolean reverse) {
        super(orientation, reverse);
        this.mList = mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFlowViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(),R.layout.item_view,null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        Item item = mList.get(position);
        Resources resources = vh.mTv.getResources();
        vh.mTv.setText((position+1)+"");
        vh.mTv.setBackgroundColor(resources.getColor(item.getTextColor()));
        vh.mTv.setTextColor(resources.getColor(item.getBackgroundColor()));
    }

    @Override
    public int totalFactor() {
        return 12;
    }

    @Override
    public int widthFactorAt(int position) {
        return getItem(position).getWidth();
    }

    @Override
    public int heightFactorAt(int position) {
        return getItem(position).getHeight();
    }

    @Override
    public int getFlowCount() {
        return mList==null?0:mList.size();
    }

    private Item getItem(int position){
        return mList.get(position);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mTv = (TextView) itemView.findViewById(R.id.item_tv);
        }
    }
}

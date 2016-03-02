package alexclin.widget.recyclerview.demo;


import android.content.res.Resources;
import android.support.v7.widget.FlowLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * @author alexclin on 16/2/26.
 */
public class RecyclerPaddingAdapter1 extends FlowLayoutManager.Adapter {
    private List<Item> mList;

    public RecyclerPaddingAdapter1() {
        this.mList = Item.paddingList();
    }

    private Item getItem(int position){
        return mList.get(position);
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
        return mList!=null?mList.size():0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFlowViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(parent.getContext(),R.layout.item_view_padding,null);
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
        vh.mBtn.setVisibility((position == 0||position==3||position==12||position==2) ? View.VISIBLE : View.GONE);
    }

    public void updateList(List<Item> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    private static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTv;
        private View mBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mTv = (TextView) itemView.findViewById(R.id.item_tv);
            mBtn = itemView.findViewById(R.id.item_btn);
        }
    }
}

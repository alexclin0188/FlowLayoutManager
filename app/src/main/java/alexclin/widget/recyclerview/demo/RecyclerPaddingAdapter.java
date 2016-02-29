package alexclin.widget.recyclerview.demo;


import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import alexclin.widget.recyclerview.FlowLayoutManager;

/**
 * @author alexclin on 16/2/26.
 */
public class RecyclerPaddingAdapter extends FlowLayoutManager.Adapter<RecyclerView.ViewHolder> {
    private List<Item> mList;

    public RecyclerPaddingAdapter() {
        super();
        this.mList = Item.paddingList();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    }

    @Override
    public int getItemCount() {
        return mList==null?0:mList.size();
    }

    @Override
    public int totalFactor() {
        return 12;
    }

    @Override
    public int widthFactorAt(int position) {
        return mList.get(position).getWidth();
    }

    @Override
    public int heightFactorAt(int position) {
        return mList.get(position).getHeight();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mTv = (TextView) itemView.findViewById(R.id.item_tv);
        }
    }
}

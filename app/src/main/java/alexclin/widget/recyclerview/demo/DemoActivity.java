package alexclin.widget.recyclerview.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.FlowLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * DemoActivity
 *
 * @author alexclin
 * @date 16/2/27 23:43
 */
public class DemoActivity extends Activity {
    public static final String ORIENTATION = "orientation";
    public static final String REVERSE_LAYOUT = "reverse";

    private RecyclerAdapter paddingAdapter;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        int orentaion = getIntent().getIntExtra(ORIENTATION, FlowLayoutManager.VERTICAL);
        boolean reverse = getIntent().getBooleanExtra(REVERSE_LAYOUT, false);
        List<Item> list = Item.createList(orentaion);
        paddingAdapter = orentaion==-100?new RecyclerPaddingAdapter():new RecyclerAdapter(list,orentaion,reverse);
        TextView tv = new TextView(this);

        tv.setText("HEAD");
        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setGravity(Gravity.CENTER);
        if(reverse) {
            paddingAdapter.setFooter(tv, 300);
            tv.setBackgroundColor(getResources().getColor(R.color.black));
        }else {
            paddingAdapter.setHeader(tv, 300);
            tv.setBackgroundColor(getResources().getColor(R.color.red));
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.act_main_view_recycler);
        if(orentaion== FlowLayoutManager.HORIZONTAL){
            ViewGroup.LayoutParams vlp = mRecyclerView.getLayoutParams();
            vlp.height = getDisplayMetrics(this).heightPixels/2;
            mRecyclerView.setLayoutParams(vlp);
        }
        mRecyclerView.setAdapter(paddingAdapter);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        textView = (TextView) findViewById(R.id.tv);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            int index = 0;
            @Override
            public void onRefresh() {
                textView.setVisibility(View.VISIBLE);
                textView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setVisibility(View.GONE);
                        refreshLayout.setRefreshing(false);
                        index++;
                        if(index%2==0)
                            paddingAdapter.updateList(Item.createList(FlowLayoutManager.VERTICAL));
                        else
                            paddingAdapter.updateList(Item.paddingList());
                    }
                },3000);
            }
        });
    }

    private DisplayMetrics getDisplayMetrics(Context ctx){
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        return dm;
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn1:
                paddingAdapter.updateList(Item.createList(FlowLayoutManager.VERTICAL));
                Toast.makeText(this,"LongList:"+paddingAdapter.getFlowCount(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                paddingAdapter.updateList(Item.paddingList());
                Toast.makeText(this,"ShortList:"+paddingAdapter.getFlowCount(),Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn3:
                int position = 1;
                mRecyclerView.smoothScrollToPosition(position);
                Toast.makeText(this,"SmoothScrollTo:"+position,Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

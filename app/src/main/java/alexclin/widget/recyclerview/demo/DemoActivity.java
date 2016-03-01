package alexclin.widget.recyclerview.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.FlowLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
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

    private RecyclerPaddingAdapter1 paddingAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        int orentaion = getIntent().getIntExtra(ORIENTATION, FlowLayoutManager.VERTICAL);
        boolean reverse = getIntent().getBooleanExtra(REVERSE_LAYOUT, false);
        List<Item> list = Item.createList(orentaion);
        paddingAdapter = new RecyclerPaddingAdapter1();
        TextView tv = new TextView(this);
        tv.setBackgroundColor(getResources().getColor(R.color.red));
        paddingAdapter.setHeader(tv,500);
        RecyclerView.Adapter adapter = orentaion==-100?paddingAdapter:new RecyclerAdapter(list,orentaion,reverse);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.act_main_view_recycler);
        if(orentaion== FlowLayoutManager.HORIZONTAL){
            RelativeLayout.LayoutParams vlp = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            vlp.height = getDisplayMetrics(this).heightPixels/2;
            vlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            recyclerView.setLayoutParams(vlp);
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setClipChildren(false);

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
                Toast.makeText(this,"1111111",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn2:
                paddingAdapter.updateList(Item.paddingList());
                Toast.makeText(this,"2222222",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

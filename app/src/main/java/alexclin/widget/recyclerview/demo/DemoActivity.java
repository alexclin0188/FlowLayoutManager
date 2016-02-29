package alexclin.widget.recyclerview.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import alexclin.widget.recyclerview.FlowLayoutManager;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        int orentaion = getIntent().getIntExtra(ORIENTATION, FlowLayoutManager.VERTICAL);
        boolean reverse = getIntent().getBooleanExtra(REVERSE_LAYOUT, false);
        List<Item> list = Item.createList(orentaion);
        FlowLayoutManager.Adapter adapter = orentaion==-100?new RecyclerPaddingAdapter():new RecyclerAdapter(list,orentaion,reverse);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.act_main_view_recycler);
        if(orentaion==FlowLayoutManager.HORIZONTAL){
            RelativeLayout.LayoutParams vlp = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            vlp.height = getDisplayMetrics(this).heightPixels/2;
            vlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            recyclerView.setLayoutParams(vlp);
        }
        recyclerView.setAdapter(adapter);
    }

    private DisplayMetrics getDisplayMetrics(Context ctx){
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        return dm;
    }
}

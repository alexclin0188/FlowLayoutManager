package alexclin.widget.recyclerview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import android.support.v7.widget.FlowLayoutManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e("", "D:" + getResources().getDisplayMetrics().density);
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        switch (view.getId()) {
            case R.id.btn_bottom_top:
                intent.putExtra(DemoActivity.ORIENTATION, FlowLayoutManager.VERTICAL);
                intent.putExtra(DemoActivity.REVERSE_LAYOUT, true);
                break;
            case R.id.btn_left_right:
                intent.putExtra(DemoActivity.ORIENTATION, FlowLayoutManager.HORIZONTAL);
                intent.putExtra(DemoActivity.REVERSE_LAYOUT, false);
                break;
            case R.id.btn_right_left:
                intent.putExtra(DemoActivity.ORIENTATION, FlowLayoutManager.HORIZONTAL);
                intent.putExtra(DemoActivity.REVERSE_LAYOUT, true);
                break;
            case R.id.btn_test:
                intent.putExtra(DemoActivity.ORIENTATION, -100);
                break;
            default:
                intent.putExtra(DemoActivity.ORIENTATION, FlowLayoutManager.VERTICAL);
                intent.putExtra(DemoActivity.REVERSE_LAYOUT, false);
                break;
        }
        startActivity(intent);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.e("1234", "D:" + dm.density+",sd:"+dm.scaledDensity);
    }
}

package com.lineview.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LineView.OnLineViewItemSelectedistener {
    private LineView lineView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineView = (LineView) (findViewById(R.id.lineview));
        lineView.setData(new ArrayList<String>() {
            {
                add("北京市东城区永外彭庄");
                add("北京市丰台区玉林东路玉林东里三区首都医科大学");
                add("北京市东城区培新街");
                add("北京市东城区夕照寺街");
                add("北京市崇文区崇文门夕照寺大街绿景馨园");
                add("北京市朝阳区垂杨柳西里");
                add("北京市东三环垂杨柳南街");
                add("北京市朝阳区劲松桥农光东里");
                add("北京市朝阳区西大望路");
            }
        });
        lineView.setOnLineViewItemSelectedistener(this);
    }

    @Override
    public void onSelected(int position) {
        List<String> data = lineView.getData();
        Toast.makeText(this, "positon="+position+", data="+data.get(position), Toast.LENGTH_SHORT).show();
    }
}

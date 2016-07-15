package com.wisen.wisenapp.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.ui.expandablelistview.ExpandableListViewTestActivity;

public class UIMainActivity extends AppCompatActivity {

    private Button btn_expand_view = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uimain);

        btn_expand_view = (Button)findViewById(R.id.btn_expand_view);
        btn_expand_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UIMainActivity.this, ExpandableListViewTestActivity.class);
                startActivity(intent);
            }
        });
    }
}

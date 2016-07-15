package com.wisen.wisenapp.ui.expandablelistview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wisen.wisenapp.R;

public class ExpandableListViewTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable_list_view_test);

        final ExpandableListAdapter exAdapter = new BaseExpandableListAdapter() {
            int[] logos = new int[] {R.drawable.wei,R.drawable.shu,R.drawable.wu};
            private String[] infos = new String[] {"魏","蜀","吴"};
            int[][] sub_logos = new int[][] {
                    {R.drawable.machao,R.drawable.zhangfei},
                    {R.drawable.liubei,R.drawable.zhugeliang,R.drawable.zhaoyun},
                    {R.drawable.lvmeng,R.drawable.luxun,R.drawable.sunquan}
            };
            private String[][] sub_infos = new String[][] {
                    {"马超","张飞"},
                    {"刘备","诸葛亮","赵云"},
                    {"吕蒙","陆逊","孙权"}
            };

            @Override
            public int getGroupCount() {
                return infos.length;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return sub_infos[groupPosition].length;
            }

            @Override
            public Object getGroup(int groupPosition) {
                return infos[groupPosition];
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return sub_infos[groupPosition][childPosition];
            }

            @Override
            public long getGroupId(int groupPosition) {
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            TextView getTextView() {
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                TextView textView = new TextView(
                        ExpandableListViewTestActivity.this);
                textView.setLayoutParams(lp);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setPadding(36, 0, 0, 0);
                textView.setTextSize(20);
                textView.setTextColor(Color.BLACK);
                return textView;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                LinearLayout ll = new LinearLayout(ExpandableListViewTestActivity.this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ImageView imageView = new ImageView(ExpandableListViewTestActivity.this);
                imageView.setImageResource(logos[groupPosition]);
                imageView.setPadding(50, 0, 0, 0);
                ll.addView(imageView);
                TextView textView = getTextView();
                textView.setTextColor(Color.BLACK);
                textView.setText(getGroup(groupPosition).toString());
                ll.addView(textView);
                return ll;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                LinearLayout ll = new LinearLayout(
                        ExpandableListViewTestActivity.this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ImageView imageView = new ImageView(
                        ExpandableListViewTestActivity.this);
                imageView.setImageResource(sub_logos[groupPosition][childPosition]);
                ll.addView(imageView);
                TextView textView = getTextView();
                textView.setText(getChild(groupPosition, childPosition)
                        .toString());
                ll.addView(textView);
                return ll;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };

        ExpandableListView ex_View = (ExpandableListView)findViewById(R.id.expandable_listview);
        ex_View.setAdapter(exAdapter);

        ex_View.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        ExpandableListViewTestActivity.this,
                        "你点击了" + exAdapter.getChild(groupPosition, childPosition),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}

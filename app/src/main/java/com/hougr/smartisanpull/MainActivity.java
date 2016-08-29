package com.hougr.smartisanpull;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private SmartisanRefreshableLayout mSmartisanRefreshableLayout;
    private ListView mListView;

    private int mRefreshCount =0;

    private LinkedList<String> mStringList;
    private ViewHolderAdapter mViewHolderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mSmartisanRefreshableLayout = (SmartisanRefreshableLayout) findViewById(R.id.refreshable_view);
        mSmartisanRefreshableLayout.setOnRefreshListener(new SmartisanRefreshableLayout.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRefreshFinished() {
                mSmartisanRefreshableLayout.finishRefreshing();
                mViewHolderAdapter.addToListHead((mRefreshCount++)+" 喜欢的话，可以在github上赏我一颗star");
            }
        });

        mStringList = new LinkedList<>();
        mStringList.add("“锤子下拉”，东半球最优雅的下拉控件");
        mStringList.add("https://github.com/hougr/SmartisanPull");
        mStringList.add("模仿“锤子阅读”的下拉效果");
        mStringList.add("仅供学习交流，请勿用于商业用途。");
        mStringList.add("----------分隔线----------");
        mStringList.add("下面是我做的过程中觉得有意思的地方：");
        mStringList.add("（下面把“可刷新”的最小距离叫刷新距离）");
        mStringList.add("1、下拉时先把item0上面的分隔线滚动出来，");
        mStringList.add("   该分隔线在下拉过程中一直显示，");
        mStringList.add("   直到header完全消失，它才重新藏起来");
        mStringList.add("2、到达刷新距离前，提示语逐渐清晰");
        mStringList.add("3、到达刷新距离时，两圆弧刚好各转半圈，");
        mStringList.add("   两圆弧间的两个缺口处于同一水平线");
        mStringList.add("4、刷新距离以下，摩擦系数越来越大");
        mStringList.add("5、如果手指向上返回，动画逐渐回到原始状态");
        mStringList.add("6、两圆弧的旋转始终是平滑的，只有速度变化");
        mStringList.add("7、最后，两圆弧逐渐过渡成线段，消失在两端");
        mStringList.add("喜欢的话，可以在github上赏我一颗star");

        mListView = (ListView) findViewById(R.id.list_view);
        mViewHolderAdapter = new ViewHolderAdapter(getApplicationContext(),mStringList);
        mListView.setAdapter(mViewHolderAdapter);
    }

    private class ViewHolderAdapter extends BaseAdapter {
        public List<String> mItemStringList;
        public LayoutInflater mLayoutInflater;

        ViewHolderAdapter(Context context, List<String> stringList){
            mItemStringList =stringList;
            mLayoutInflater = LayoutInflater.from(context);
        }

        public void addToListHead(String newItemString){
            mItemStringList.add(0,newItemString);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(mItemStringList ==null){
                return 0;
            }
            return mItemStringList.size();
        }

        @Override
        public Object getItem(int i) {
            return mItemStringList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final String itemString= mItemStringList.get(i);
            ViewHolder viewHolder;
            if(view == null){
                viewHolder=new ViewHolder();
                view = mLayoutInflater.inflate(R.layout.listview_item,null);
                viewHolder.mItemView = view;
                viewHolder.mTextView =(TextView)view.findViewById(R.id.textView);
                view.setTag(viewHolder);
            }else {
                viewHolder=(ViewHolder) view.getTag();
            }
            viewHolder.mTextView.setText(itemString);
            viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),"点击了："+itemString,Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }

        public final class  ViewHolder{
            View mItemView;
            TextView mTextView;
        }
    }
}

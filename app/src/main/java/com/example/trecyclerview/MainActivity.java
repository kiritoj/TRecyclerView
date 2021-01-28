package com.example.trecyclerview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.MessageQueue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trecyclerview.base.BaseViewHolder;
import com.example.trecyclerview.base.MyBaseAdapter;
import com.example.trecyclerview.recycler.RefreshHeader;
import com.example.trecyclerview.recycler.RefreshListener;
import com.example.trecyclerview.recycler.TRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Handler handler = new Handler();
        final List<String> strings = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            strings.add("item-" + i);
        }
        final MyBaseAdapter<String> adapter = new MyBaseAdapter(strings, R.layout.item) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                holder.setText(R.id.text_view, (String) getmData().get(position));
            }
        };
        final TRecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setRefreshListener(new RefreshListener() {
            @Override
            public void onOnLoad() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        strings.add("taoke");
//                        strings.add("xiaojin");
//                        recyclerView.loadComplete();
//                        adapter.notifyItemRangeInserted(strings.size() -2, 2);
                        recyclerView.loadNoMore();

                    }
                },2000);

            }

            @Override
            public void onRefreshTimeOut() {
                Toast.makeText(MainActivity.this,"刷新超时",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadMoreTimeOut() {
                Toast.makeText(MainActivity.this,"加载超时",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //insert
                        strings.add(0,"taoke");
                        strings.add(1,"xiaojin");
                        strings.add(2,"shaocong");
                        strings.add(3,"xxx");
                        adapter.notifyItemRangeInserted(0,4);
                        recyclerView.refreshComplete();
                        Toast.makeText(MainActivity.this,"刷新成功",Toast.LENGTH_SHORT).show();
                    }
                },2000);
            }

        });
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

//        final RecyclerView recyclerView = findViewById(R.id.normal_recycler);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                strings.set(0,"taoke");
//                strings.set(1, "xiaojin");
//                adapter.notifyItemRangeChanged(0, 2);
//            }
//        });

    }
}

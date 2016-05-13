package cn.edu.jxnu.awesome_campus.ui.jxnugo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.squareup.okhttp.Headers;
import com.tendcloud.tenddata.TCAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cn.edu.jxnu.awesome_campus.InitApp;
import cn.edu.jxnu.awesome_campus.R;
import cn.edu.jxnu.awesome_campus.api.JxnuGoApi;
import cn.edu.jxnu.awesome_campus.event.EVENT;
import cn.edu.jxnu.awesome_campus.event.EventModel;
import cn.edu.jxnu.awesome_campus.model.jxnugo.CommentBean;
import cn.edu.jxnu.awesome_campus.model.jxnugo.CommentModel;
import cn.edu.jxnu.awesome_campus.support.adapter.BaseListAdapter;
import cn.edu.jxnu.awesome_campus.support.adapter.jxnugo.CommentListAdapter;
import cn.edu.jxnu.awesome_campus.support.adapter.jxnugo.GoodsListAdapter;
import cn.edu.jxnu.awesome_campus.support.utils.net.NetManageUtil;
import cn.edu.jxnu.awesome_campus.support.utils.net.callback.JsonEntityCallback;
import cn.edu.jxnu.awesome_campus.ui.base.BaseToolbarActivity;

/**
 * Created by KevinWu on 16-5-13.
 */
public class GoodsCommentActivity extends BaseToolbarActivity {
    public static final String TAG="GoodsCommentActivity";
    private EditText commentEditText;
    private String title="评论区";//测试用，正式版用string
    private int postID;
    private ProgressBar progressBar;
    private CommentModel model;
    private  BaseListAdapter adapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_comment);
        TCAgent.onPageStart(InitApp.AppContext, TAG);
        EventBus.getDefault().register(this);
        postID=getIntent().getIntExtra("id",-1);
        initView();
        bindAdapter();
        initToolbar();
        setToolbarTitle(title);
        onDataRefresh();

    }

    private void initView() {
        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void displayLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    //获取网络数据
    private void onDataRefresh() {
        if(postID!=-1){
            final Handler handler = new Handler(Looper.getMainLooper());
        NetManageUtil.get(JxnuGoApi.BaseCommentListUrl+postID)
        .addTag(TAG)
        .enqueue(new JsonEntityCallback<CommentBean>() {
            @Override
            public void onFailure(IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new EventModel<CommentModel>(EVENT.GOODS_COMMENT_REFRESH_FAILURE));
                    }
                });
            }

            @Override
            public void onSuccess(CommentBean entity, Headers headers) {
                if(entity!=null){
                    Log.d("评论条数:","--"+entity.getComments().length);
                    final List<CommentModel> list = Arrays.asList(entity.getComments());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new EventModel<CommentModel>(EVENT.GOODS_COMMENT_REFRESH_SUCCESS,list));

                        }
                    });
                }else{
                    EventBus.getDefault().post(new EventModel<CommentModel>(EVENT.GOODS_COMMENT_REFRESH_FAILURE));
                }
            }
        });}
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        TCAgent.onPageEnd(InitApp.AppContext, TAG);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void onEventMainThread(EventModel eventModel) {
        switch (eventModel.getEventCode()){
            case EVENT.GOODS_COMMENT_REFRESH_FAILURE:
                break;
            case EVENT.GOODS_COMMENT_REFRESH_SUCCESS:
                Log.d(TAG,"成功");
                List list = eventModel.getDataList();
                adapter.newList(list);
                hideLoading();
                break;
        }
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);

//        if(swipeRefreshLayout != null){
//            swipeRefreshLayout.setRefreshing(false);
//        }
    }

    private void bindAdapter() {
        model=new CommentModel();
        adapter=new CommentListAdapter(this,model);
        recyclerView.setAdapter(adapter);
        displayLoading();
    }
}
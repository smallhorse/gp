package com.ubtechinc.goldenpig.personal.interlocution;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.ToastUtils;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.actionbar.SecondTitleBarViewImg;
import com.ubtechinc.goldenpig.base.BaseNewActivity;
import com.ubtechinc.goldenpig.comm.widget.LoadingDialog;
import com.ubtechinc.goldenpig.eventbus.modle.Event;
import com.ubtechinc.goldenpig.model.InterlocutionItemModel;
import com.ubtechinc.goldenpig.model.JsonCallback;
import com.ubtechinc.goldenpig.route.ActivityRoute;
import com.ubtechinc.goldenpig.view.Divider;
import com.ubtechinc.goldenpig.view.StateView;
import com.yanzhenjie.recyclerview.swipe.SwipeItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.ubtechinc.goldenpig.eventbus.EventBusUtil.ADD_INTERLO_SUCCESS;

public class InterlocutionActivity extends BaseNewActivity implements SwipeItemClickListener {
    @BindView(R.id.rl_titlebar)
    SecondTitleBarViewImg rl_titlebar;
    @BindView(R.id.recycler)
    SwipeMenuRecyclerView recycler;
    InterlocutionAdapter adapter;
    private ArrayList<InterlocutionItemModel> mList;
    /**
     *
     */
    private Boolean hasLoadMsg = false;
    InterlocutionModel requestModel;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_interlocution;
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStateView(true);
        mStateView.setOnRetryClickListener(new StateView.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                onRefresh();
            }
        });
        rl_titlebar.setTitleText(getString(R.string.ubt_custom_answer));
        rl_titlebar.setLeftOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rl_titlebar.setIvRight(R.drawable.ic_add);
        rl_titlebar.getIvRight().setVisibility(View.GONE);
        rl_titlebar.setRightOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityRoute.toAnotherActivity(InterlocutionActivity.this, AddInterlocutionActivity
                        .class, false);
            }
        });
        mList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setCustomBackgroundSize(getResources().getDimensionPixelSize(R.dimen.dp_94) + 1);
        recycler.setLayoutManager(linearLayoutManager);
        recycler.setHasFixedSize(true);
        Divider divider = new Divider(new ColorDrawable(getResources().getColor(R.color
                .ubt_main_bg_color)),
                OrientationHelper.VERTICAL);
        divider.setHeight((int) getResources().getDimension(R.dimen.dp_10));
        recycler.addItemDecoration(divider);
        recycler.setSwipeMenuCreator(swipeMenuCreator);
        recycler.setSwipeItemClickListener(this);
        recycler.setSwipeMenuItemClickListener(mMenuItemClickListener);
        adapter = new InterlocutionAdapter(this, mList);
        recycler.setAdapter(adapter);
        requestModel = new InterlocutionModel();
        onRefresh();
    }

    public void onRefresh() {
        LoadingDialog.getInstance(this).show();
        Type type = new TypeToken<List<InterlocutionItemModel>>() {
        }.getType();
        requestModel.getInterlocutionRequest(new JsonCallback<List<InterlocutionItemModel>>(type) {
            @Override
            public void onError(String e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShortToast(e);
                        LoadingDialog.getInstance(InterlocutionActivity.this).dismiss();
                    }
                });

            }

            @Override
            public void onSuccess(List<InterlocutionItemModel> reponse) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        LoadingDialog.getInstance(InterlocutionActivity.this).dismiss();
                        mList.clear();
                        if (reponse == null || reponse.size() == 0) {
                            InterlocutionItemModel model = new InterlocutionItemModel();
                            model.type = 1;
                            mList.add(model);
                            rl_titlebar.getIvRight().setVisibility(View.GONE);
                        } else {
                            rl_titlebar.getIvRight().setVisibility(View.VISIBLE);
                        }
                        mList.addAll(reponse);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * 菜单创建器，在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            if (viewType == 1) {
                return;
            }
            int width = getResources().getDimensionPixelSize(R.dimen.dp_65);
            // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
            // 2. 指定具体的高，比如80;
            // 3. WRAP_CONTENT，自身高度，不推荐;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            {
                SwipeMenuItem deleteItem = new SwipeMenuItem(InterlocutionActivity.this)
                        .setBackgroundColor(getResources().getColor(R.color
                                .ubt_dialog_btn_txt_color))
                        .setText("删除")
                        .setTextColor(Color.WHITE)
                        .setTextSize(16)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。

            }
        }
    };

    /**
     * RecyclerView的Item的Menu点击监听。
     */
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                if (menuPosition == 0) {
                    requestModel.deleteInterlocRequest(mList.get(adapterPosition).strDocId, new
                            JsonCallback<String>(String.class) {
                                @Override
                                public void onSuccess(String reponse) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtils.showShortToast("删除成功");
                                            LoadingDialog.getInstance(InterlocutionActivity.this)
                                                    .dismiss();
                                            mList.remove(adapterPosition);
                                            if (mList.size() == 0) {
                                                InterlocutionItemModel model = new
                                                        InterlocutionItemModel();
                                                model.type = 1;
                                                mList.add(model);
                                                rl_titlebar.getIvRight().setVisibility(View.GONE);
                                            } else {
                                                rl_titlebar.getIvRight().setVisibility(View
                                                        .VISIBLE);
                                            }
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onError(String str) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            LoadingDialog.getInstance(InterlocutionActivity.this)
                                                    .dismiss();
                                            ToastUtils.showShortToast(str);
                                        }
                                    });
                                }
                            });
                    LoadingDialog.getInstance(InterlocutionActivity.this).setTimeout(20)
                            .setShowToast(true).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestModel.release();
    }

    @Override
    protected void onReceiveEvent(Event event) {
        super.onReceiveEvent(event);
        if (event.getCode() == ADD_INTERLO_SUCCESS) {
            onRefresh();
        }
    }

    @Override
    public void onItemClick(View itemView, int position) {
        LogUtils.d("hdf", "position:" + position);
        if (mList.get(position).type == 1) {
            Intent it = new Intent(InterlocutionActivity.this, AddInterlocutionActivity.class);
            startActivity(it);
        } else {
            Intent it = new Intent(InterlocutionActivity.this, AddInterlocutionActivity.class);
            it.putExtra("item", mList.get(position));
            startActivity(it);
        }
    }
}
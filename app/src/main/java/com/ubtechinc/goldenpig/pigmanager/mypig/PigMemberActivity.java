package com.ubtechinc.goldenpig.pigmanager.mypig;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.TIMMessage;
import com.ubt.imlibv2.bean.ContactsProtoBuilder;
import com.ubt.imlibv2.bean.UbtTIMManager;
import com.ubtechinc.commlib.utils.ToastUtils;
import com.ubtechinc.commlib.view.SpaceItemDecoration;
import com.ubtechinc.goldenpig.BuildConfig;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.base.BaseToolBarActivity;
import com.ubtechinc.goldenpig.comm.net.CookieInterceptor;
import com.ubtechinc.goldenpig.comm.view.WrapContentLinearLayoutManager;
import com.ubtechinc.goldenpig.comm.widget.LoadingDialog;
import com.ubtechinc.goldenpig.comm.widget.UBTBaseDialog;
import com.ubtechinc.goldenpig.comm.widget.UBTSubTitleDialog;
import com.ubtechinc.goldenpig.eventbus.EventBusUtil;
import com.ubtechinc.goldenpig.eventbus.modle.Event;
import com.ubtechinc.goldenpig.login.observable.AuthLive;
import com.ubtechinc.goldenpig.net.CheckBindRobotModule;
import com.ubtechinc.goldenpig.pigmanager.adpater.PigMemberAdapter;
import com.ubtechinc.goldenpig.pigmanager.bean.PigInfo;
import com.ubtechinc.goldenpig.pigmanager.register.CheckUserRepository;
import com.ubtechinc.goldenpig.pigmanager.register.GetPigListHttpProxy;
import com.ubtechinc.goldenpig.pigmanager.register.TransferAdminHttpProxy;
import com.ubtechinc.goldenpig.push.PushHttpProxy;
import com.ubtechinc.goldenpig.route.ActivityRoute;
import com.ubtechinc.goldenpig.utils.PigUtils;
import com.ubtechinc.nets.http.ThrowableWrapper;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.ubtechinc.goldenpig.eventbus.EventBusUtil.USER_PIG_UPDATE;

/**
 * @author :hqt
 * @email :qiangta.huang@ubtrobot.com
 * @description :成员组管理
 * @time :2018/9/19 21:11
 * @change :
 * @changetime :2018/9/19 21:11
 */
public class PigMemberActivity extends BaseToolBarActivity implements View.OnClickListener {
    private SwipeMenuRecyclerView mMemberRcy;
    private PigMemberAdapter adapter;
    private Button mUnbindBtn;
    private PigInfo mPig;
    private ArrayList<CheckBindRobotModule.User> mUsertList = new ArrayList<>();
    private boolean isDownloadedUserList;
    private UnbindPigProxy.UnBindPigCallback unBindPigCallback;

    @Override
    protected int getConentView() {
        return R.layout.activity_pigmember;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        EventBusUtil.register(this);
        setTitleBack(true);
        setToolBarTitle(getString(R.string.ubt_menber_group));
        initViews();
        initData();
    }

    private void initData() {
        unBindPigCallback = new UnbindPigProxy.UnBindPigCallback() {

            @Override
            public void onError(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShortToast(PigMemberActivity.this, msg);
                    }
                });
            }

            @Override
            public void onSuccess() {
                imSyncRelationShip();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isDownloadedUserList = false;
                        updatePigList();
                        getMember("1");
                    }
                });
            }
        };
    }

    private void imSyncRelationShip() {
        //TODO 给自己的猪发
        if (AuthLive.getInstance().getCurrentPig().isAdmin) {
            TIMMessage selfMessage = ContactsProtoBuilder.createTIMMsg(ContactsProtoBuilder.syncPairInfo(3));
            UbtTIMManager.getInstance().sendTIM(selfMessage);
        }
    }

    private void initViews() {
        mMemberRcy = findViewById(R.id.ubt_rcy_member);
        mUnbindBtn = findViewById(R.id.ubt_btn_unbind_member);
        mUnbindBtn.setOnClickListener(this);

        mToolbarRightBtn = findViewById(R.id.ubt_imgbtn_add);
        mToolbarRightBtn.setOnClickListener(this);

        mPig = AuthLive.getInstance().getCurrentPig();

        adapter = new PigMemberAdapter(this, mUsertList);
        mMemberRcy.setLayoutManager(new WrapContentLinearLayoutManager(this));
        mMemberRcy.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.dp_5), false));
        mMemberRcy.setSwipeMenuCreator(swipeMenuCreator);
        mMemberRcy.setSwipeMenuItemClickListener(mMenuItemClickListener);
        mMemberRcy.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        setAddBtnEnable(isCurrentAdmin());

    }

    private boolean isCurrentAdmin() {
        if (mUsertList != null) {
            for (CheckBindRobotModule.User user : mUsertList) {
                if (user.getIsAdmin() == 1 && user.getUserId() == Integer.valueOf(AuthLive.getInstance().getCurrentUser().getUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDownloadedUserList = false;
        getMember("1");
    }

    private synchronized void getMember(String admin) {
        if (isDownloadedUserList) {
            setAddBtnEnable(isCurrentAdmin());
            if (adapter != null) {
                Set<CheckBindRobotModule.User> set = new TreeSet<>((o1, o2) -> String.valueOf(o1.getUserId()).compareTo(String.valueOf(o2.getUserId())));
                set.addAll(mUsertList);
                mUsertList.clear();
                mUsertList.addAll(set);
                String currUId = AuthLive.getInstance().getUserId();
                if (!mUsertList.isEmpty()) {
                    boolean contain = false;
                    for (CheckBindRobotModule.User user : mUsertList) {
                        int uid = user.getUserId();
                        if (currUId.equals(String.valueOf(uid))) {
                            contain = true;
                            break;
                        }
                    }
                    if (contain) {
                        Collections.sort(mUsertList, (o1, o2) -> o2.getIsAdmin() - o1.getIsAdmin());
                        adapter.notifyDataSetChanged();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
            return;
        }
        if ("0".equals(admin)) {
            isDownloadedUserList = true;
        }
        if (mPig == null) {
            ToastUtils.showShortToast(this, getString(R.string.ubt_no_pigs));
            return;
        }
        if (mUsertList == null) {
            mUsertList = new ArrayList<>();
        } else if ("1".equals(admin) && mUsertList != null) {
            mUsertList.clear();
        }
        CheckUserRepository repository = new CheckUserRepository();
        repository.getRobotBindUsers(mPig.getRobotName(), CookieInterceptor.get().getToken(), BuildConfig.APP_ID, admin, new CheckUserRepository.ICheckBindStateCallBack() {
            @Override
            public void onError(ThrowableWrapper e) {
                ToastUtils.showShortToast(PigMemberActivity.this, "获取成员列表失败");
            }

            @Override
            public void onSuccess(CheckBindRobotModule.Response response) {
                ToastUtils.showShortToast(PigMemberActivity.this, "获取成员列表成功");
            }

            @Override
            public void onSuccessWithJson(String jsonStr) {
                LoadingDialog.getInstance(PigMemberActivity.this).dismiss();
                final List<CheckBindRobotModule.User> bindUsers = jsonToUserList(jsonStr);
                if (mUsertList != null) {
                    mUsertList.addAll(bindUsers);
                }
                getMember("0");
            }
        });
    }

    private void setAddBtnEnable(boolean isEnable) {
        if (isEnable) {
            mToolbarRightBtn = findViewById(R.id.ubt_imgbtn_add);
            mToolbarRightBtn.setVisibility(View.VISIBLE);
            mToolbarRightBtn.setOnClickListener(this);
        } else {
            findViewById(R.id.ubt_imgbtn_add).setVisibility(View.GONE);
        }
    }

    private void doUnbind(final String userId) {
        if (mPig == null) {
            return;
        }
        ///操作用户是唯一或只是一般成员可好直接弹框点击确认退出
        //否则要跳转到权限转让界面操作
        if (mUsertList.size() > 1 && isCurrentAdmin()) {
//            HashMap<String, ArrayList<CheckBindRobotModule.User>> param = new HashMap<>();
//            param.put("users", mUsertList);
//            ActivityRoute.toAnotherActivity(this, TransferAdminActivity.class, param, false);

            UBTSubTitleDialog dialog = new UBTSubTitleDialog(this);
            dialog.setRightBtnColor(ResourcesCompat.getColor(getResources(), R.color.ubt_tab_btn_txt_checked_color, null));
            dialog.setTips(getString(R.string.ubt_exit_group_tips));
            dialog.setLeftButtonTxt(getString(R.string.ubt_cancel));
            dialog.setRightButtonTxt(getString(R.string.ubt_enter));
            dialog.setSubTips(getString(R.string.ubt_transfer_tips));
            dialog.setOnUbtDialogClickLinsenter(new UBTSubTitleDialog.OnUbtDialogClickLinsenter() {
                @Override
                public void onLeftButtonClick(View view) {

                }

                @Override
                public void onRightButtonClick(View view) {
                    ActivityRoute.toAnotherActivity(PigMemberActivity.this, TransferAdminActivity.class,
                            null, 0x01, false);
                }
            });
            dialog.show();

        } else {
            UBTBaseDialog dialog = new UBTBaseDialog(this);
            dialog.setRightButtonTxt(getString(R.string.ubt_enter));
            dialog.setLeftButtonTxt(getString(R.string.ubt_cancel));
            dialog.setTips(getString(R.string.ubt_drop_up_tips));

            dialog.setRightBtnColor(ResourcesCompat.getColor(getResources(), R.color.ubt_tab_btn_txt_checked_color, null));
            dialog.setOnUbtDialogClickLinsenter(new UBTBaseDialog.OnUbtDialogClickLinsenter() {
                @Override
                public void onLeftButtonClick(View view) {

                }

                @Override
                public void onRightButtonClick(View view) {
                    UnbindPigProxy pigProxy = new UnbindPigProxy();
                    final String serialNo = AuthLive.getInstance().getCurrentPig().getRobotName();

                    final String token = CookieInterceptor.get().getToken();
                    pigProxy.unbindPig(serialNo, userId, token, BuildConfig.APP_ID, unBindPigCallback);
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x01:
                if (resultCode == RESULT_OK) {
                    //TODO 先转让管理员再退出群组
                    doExitGroup();
                }
                break;
        }
    }

    private void doExitGroup() {
        UnbindPigProxy pigProxy = new UnbindPigProxy();
        final String serialNo = AuthLive.getInstance().getCurrentPig().getRobotName();

        final String token = CookieInterceptor.get().getToken();
        pigProxy.unbindPig(serialNo, AuthLive.getInstance().getUserId(), token, BuildConfig.APP_ID, unBindPigCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ubt_imgbtn_add:
                HashMap<String, Boolean> param = new HashMap<>();
                param.put("isPair", false);
                ActivityRoute.toAnotherActivity(this, QRCodeActivity.class, param, false);
                break;
            case R.id.ubt_btn_unbind_member:
                doUnbind(AuthLive.getInstance().getUserId());
                break;
            default:
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregister(this);
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
                SwipeMenuItem transferItem = new SwipeMenuItem(PigMemberActivity.this)
                        .setBackgroundColor(getResources().getColor(R.color
                                .ubt_tab_btn_txt_checked_color))
                        .setText(R.string.ubt_trans_admin)
                        .setTextColor(Color.WHITE)
                        .setTextSize(15)
                        .setWidth(getResources().getDimensionPixelSize(R.dimen.dp_88))
                        .setHeight(height);
                swipeRightMenu.addMenuItem(transferItem); // 添加菜单到右侧。
                SwipeMenuItem deleteItem = new SwipeMenuItem(PigMemberActivity.this)
                        .setBackground(getResources().getDrawable(R.drawable.shape_ubt_member_menu_bg))
                        .setText(R.string.ubt_delete)
                        .setTextColor(Color.WHITE)
                        .setTextSize(15)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。

            }
        }
    };
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                if (mUsertList != null && adapterPosition > -1 && adapterPosition < mUsertList.size()) {
                    if (menuPosition == 1) {
                        showDeleteMember(String.valueOf(mUsertList.get(adapterPosition).getUserId()));
                    } else if (menuPosition == 0) {
                        showTransferAdminDialog(String.valueOf(mUsertList.get(adapterPosition).getUserId()));
                    }
                }
            }
        }
    };

    /**
     * 显示删除成员确定框
     *
     * @param userId 用户ID
     */
    private void showDeleteMember(final String userId) {
        //doUnbind(String.valueOf(userId);
        UBTBaseDialog dialog = new UBTBaseDialog(this);
        dialog.setRightBtnColor(ResourcesCompat.getColor(getResources(), R.color.ubt_tab_btn_txt_checked_color, null));
        dialog.setTips(getString(R.string.ubt_delte_member_tips));
        dialog.setLeftButtonTxt(getString(R.string.ubt_cancel));
        dialog.setRightButtonTxt(getString(R.string.ubt_enter));
        dialog.setOnUbtDialogClickLinsenter(new UBTBaseDialog.OnUbtDialogClickLinsenter() {
            @Override
            public void onLeftButtonClick(View view) {

            }

            @Override
            public void onRightButtonClick(View view) {
                UnbindPigProxy pigProxy = new UnbindPigProxy();
                final String serialNo = AuthLive.getInstance().getCurrentPig().getRobotName();

                final String token = CookieInterceptor.get().getToken();
                pigProxy.unbindPig(serialNo, userId, token, BuildConfig.APP_ID, new UnbindPigProxy.UnBindPigCallback() {

                    @Override
                    public void onError(String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShortToast(PigMemberActivity.this, msg);
                            }
                        });

                    }

                    @Override
                    public void onSuccess() {
                        doPushDelMemberMsg(userId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isDownloadedUserList = false;
                                updatePigList();
                                getMember("1");
                            }
                        });
                    }
                });
            }
        });
        dialog.show();
    }

    private void doPushDelMemberMsg(String userId) {
        //TODO 给删除成员推送
        PushHttpProxy pushHttpProxy = new PushHttpProxy();
        Map map = new HashMap();
        map.put("app_category", 1);
        pushHttpProxy.pushToken("", "你已被管理员移除成员组", userId, map, 1);
    }

    /**
     * 显示转让权限确认对话框
     */
    private void showTransferAdminDialog(final String userId) {
        UBTSubTitleDialog dialog = new UBTSubTitleDialog(this);
        dialog.setRightBtnColor(ResourcesCompat.getColor(getResources(), R.color.ubt_tab_btn_txt_checked_color, null));
        dialog.setTips(getString(R.string.ubt_trandfer_admin_tips));
        dialog.setLeftButtonTxt(getString(R.string.ubt_cancel));
        dialog.setRightButtonTxt(getString(R.string.ubt_enter));
        dialog.setSubTips(getString(R.string.ubt_transfer_tips));
        dialog.setOnUbtDialogClickLinsenter(new UBTSubTitleDialog.OnUbtDialogClickLinsenter() {
            @Override
            public void onLeftButtonClick(View view) {

            }

            @Override
            public void onRightButtonClick(View view) {
                //TODO do管理员权限转让
                doTransferAdmin(userId);
            }
        });
        dialog.show();
    }

    /**
     * 执行转让权限操作
     */
    private void doTransferAdmin(String userId) {
        LoadingDialog.getInstance(this).show();
        TransferAdminHttpProxy httpProxy = new TransferAdminHttpProxy();
        httpProxy.transferAdmin(this, CookieInterceptor.get().getToken(), AuthLive.getInstance().getCurrentPig().getRobotName(), userId, new TransferAdminHttpProxy.TransferCallback() {
            @Override
            public void onError(String error) {
                LoadingDialog.getInstance(PigMemberActivity.this).dismiss();
                com.ubtech.utilcode.utils.ToastUtils.showShortToast(error);
            }

            @Override
            public void onException(Exception e) {
                LoadingDialog.getInstance(PigMemberActivity.this).dismiss();
//                com.ubtech.utilcode.utils.ToastUtils.showShortToast("转让失败");
            }

            @Override
            public void onSuccess(String msg) {
                com.ubtech.utilcode.utils.ToastUtils.showShortToast("转让成功");
                imSyncRelationShip();
                doPushTransferMsg(userId);
                isDownloadedUserList = false;
                updatePigList();
                getMember("1");
            }
        });
    }

    private void doPushTransferMsg(String userId) {
        //TODO 给新管理员推送
        try {
            PushHttpProxy pushHttpProxy = new PushHttpProxy();
            Map map = new HashMap();
            map.put("app_category", 1);
            pushHttpProxy.pushToken("", "你已成为新的八戒管理员", userId, map, 1);
        } catch (Exception e) {
            //TODO
        }

    }

    private void updatePigList() {
        if (AuthLive.getInstance().getCurrentPigList() != null) {
            AuthLive.getInstance().getCurrentPigList().clear();
        }
        new GetPigListHttpProxy().getUserPigs(CookieInterceptor.get().getToken(), BuildConfig.APP_ID, "", new GetPigListHttpProxy.OnGetPigListLitener() {
            @Override
            public void onError(ThrowableWrapper e) {
                Log.e("getPigList", e.getMessage());
            }

            @Override
            public void onException(Exception e) {
                Log.e("getPigList", e.getMessage());
            }

            @Override
            public void onSuccess(String response) {
                Log.e("getPigList", response);
                PigUtils.getPigList(response, AuthLive.getInstance().getUserId(), AuthLive.getInstance().getCurrentPigList());
                ArrayList<PigInfo> list = AuthLive.getInstance().getCurrentPigList();
                if (list == null || list.isEmpty()) {
                    finish();
                }
            }
        });
    }

    private List<CheckBindRobotModule.User> jsonToUserList(String jsonStr) {
        List<CheckBindRobotModule.User> result = null;
        Gson gson = new Gson();
        try {
            result = gson.fromJson(jsonStr, new TypeToken<List<CheckBindRobotModule.User>>() {
            }.getType());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event) {
        if (event == null) return;
        int code = event.getCode();
        switch (code) {
            case USER_PIG_UPDATE:
                isDownloadedUserList = false;
                getMember("1");
                break;
        }
    }
}

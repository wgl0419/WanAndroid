package com.wangyz.wanandroid.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.wangyz.wanandroid.R;
import com.wangyz.wanandroid.base.BaseActivity;
import com.wangyz.wanandroid.bean.event.Event;
import com.wangyz.wanandroid.contract.Contract;
import com.wangyz.wanandroid.custom.BottomNavigationViewHelper;
import com.wangyz.wanandroid.presenter.MainActivityPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * MainActivity
 *
 * @author wangyz
 */
@RuntimePermissions
public class MainActivity extends BaseActivity<Contract.MainActivityView, MainActivityPresenter> implements Contract.MainActivityView {

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.menu)
    ImageView mMenu;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.search)
    ImageView mSearch;

    @BindView(R.id.bottom)
    BottomNavigationView mBottomNavigationView;

    private FragmentManager mFragmentManager;

    private FragmentTransaction mTransaction;

    private Fragment mCurrentFragment;

    private MainFragment mMainFragment;

    private TreeFragment mTreeFragment;

    private TreeArticleFragment mTreeArticleFragment;

    private ProjectFragment mProjectFragment;

    private WxFragment mWxFragment;

    private MenuFragment mMenuFragment;

    private int mTreeType;

    private boolean childFragment;

    private Context mContext;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        mContext = getApplicationContext();

        EventBus.getDefault().register(this);

        MainActivityPermissionsDispatcher.requestPermissionWithPermissionCheck(this);

        BottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);

        mBottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_bottom_home:
                    mTitle.setText(getString(R.string.home));
                    if (mMainFragment == null) {
                        mMainFragment = new MainFragment();
                    }
                    switchFragment(R.id.layout_main, mMainFragment);
                    return true;
                case R.id.menu_bottom_system:
                    mTitle.setText(getString(R.string.system));
                    if (mCurrentFragment instanceof TreeArticleFragment) {
                        return true;
                    } else {
                        if (childFragment) {
                            showTreeArticle();
                            return true;
                        } else {
                            if (mTreeFragment == null) {
                                mTreeFragment = new TreeFragment();
                            }
                            childFragment = false;
                            switchFragment(R.id.layout_main, mTreeFragment);
                        }

                    }
                    return true;
                case R.id.menu_bottom_project:
                    mTitle.setText(getString(R.string.project));
                    if (mProjectFragment == null) {
                        mProjectFragment = new ProjectFragment();
                    }
                    switchFragment(R.id.layout_main, mProjectFragment);
                    return true;
                case R.id.menu_bottom_wx:
                    mTitle.setText(getString(R.string.wx));
                    if (mWxFragment == null) {
                        mWxFragment = new WxFragment();
                    }
                    switchFragment(R.id.layout_main, mWxFragment);
                    return true;
                default:
                    break;
            }
            return false;
        });

        mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();

        mMainFragment = new MainFragment();
        mTransaction.add(R.id.layout_main, mMainFragment);
        mCurrentFragment = mMainFragment;

        mMenuFragment = new MenuFragment();
        mTransaction.add(R.id.layout_menu, mMenuFragment);

        mTransaction.commit();

    }

    @Override
    protected MainActivityPresenter createPresenter() {
        return new MainActivityPresenter();
    }


    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void requestPermission() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onLoading() {
        LogUtils.i();
    }

    @Override
    public void onLoadSuccess() {
        LogUtils.i();
    }

    @Override
    public void onLoadFailed() {
        LogUtils.i();
    }

    @OnClick(R.id.menu)
    public void onToggleMenu() {
        if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.openDrawer(Gravity.START);
        } else {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    @OnClick(R.id.search)
    public void search() {
        Intent intent = new Intent(mContext, SearchActivity.class);
        mContext.startActivity(intent);
    }

    private void switchFragment(int containerId, Fragment fragment) {
        if (mCurrentFragment != fragment) {
            mTransaction = mFragmentManager.beginTransaction();
            if (fragment.isAdded()) {
                mTransaction.hide(mCurrentFragment).show(fragment).commit();
            } else {
                mTransaction.hide(mCurrentFragment).add(containerId, fragment).commit();
            }
            mCurrentFragment = fragment;
        }
    }

    private void showTreeArticle() {
        mTransaction = mFragmentManager.beginTransaction();
        if (mTreeArticleFragment.isAdded()) {
            mTransaction.hide(mCurrentFragment).show(mTreeArticleFragment).commit();
        } else {
            mTransaction.hide(mCurrentFragment).add(R.id.layout_main, mTreeArticleFragment).addToBackStack("tree").commit();
        }
        mCurrentFragment = mTreeArticleFragment;
        childFragment = true;
    }

    private void removeTreeArticle() {
        mTransaction = mFragmentManager.beginTransaction();
        if (mTreeArticleFragment.isAdded()) {
            mTransaction.remove(mTreeArticleFragment);
            mTransaction.commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (event.target == Event.TARGET_MAIN) {
            if (event.type == Event.TYPE_TREE_ARTICLE_FRAGMENT) {
                if (mTreeArticleFragment != null) {
                    removeTreeArticle();
                }
                mTreeType = Integer.valueOf(event.data);
                mTreeArticleFragment = new TreeArticleFragment();
                mTreeArticleFragment.setTreeType(mTreeType);
                showTreeArticle();
            } else if (event.type == Event.TYPE_CHANGE_DAY_NIGHT_MODE) {
                recreate();
            }
        }
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyevent) {
        if (keyevent != null && keyevent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mCurrentFragment instanceof TreeArticleFragment) {
                switchFragment(R.id.layout_main, mTreeFragment);
                childFragment = false;
                return true;
            } else if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.closeDrawer(Gravity.START);
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(i, keyevent);
    }
}

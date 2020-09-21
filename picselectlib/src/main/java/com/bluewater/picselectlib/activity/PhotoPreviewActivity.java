package com.bluewater.picselectlib.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bluewater.picselectlib.adapter.PhotoPagerAdapter;
import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.widget.ViewPagerFixed;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * 本地照片预览
 */
public class PhotoPreviewActivity extends AppCompatActivity implements PhotoPagerAdapter.PhotoViewClickListener
{
    /*  Intent传参   */
    public static final String EXTRA_PHOTOS = "extra_photos";                   //所有预览的照片，ArrayList<String>类型
    public static final String EXTRA_CURRENT_ITEM = "extra_current_item";       //当前图片下标，int类型
    public static final String EXTRA_RESULT = "preview_result";                 //选择结果，返回为ArrayList<String>图片路径集合

    public static final int REQUEST_PREVIEW = 99;       //预览请求状态码

    private static final String ADD_PIC_TAG = "AddPic";     //添加图片标志

    private Context mContext;
    private Activity mActivity;

    private ViewPagerFixed mViewPager;          //图片预览展示控件ViewPager
    private PhotoPagerAdapter mPagerAdapter;    //adapter

    private ArrayList<String> paths;    //已选图片路径

    private int currentItem = 0;        //当前图片下标，默认为0

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        mContext = this;
        mActivity = this;

        initViews();
        initData();
        initEvents();

        updateActionBarTitle();     //更新标题
    }

    /**
     * 初始化视图
     */
    private void initViews()
    {
        //----------------工具栏设置
        Toolbar mToolbar = findViewById(R.id.pickerToolbar);
        setSupportActionBar(mToolbar);                              // 将ToolBar设置成ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);      // 给左上角图标的左边加上一个返回的图标

        mViewPager = findViewById(R.id.vp_preview_photos);
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        paths = new ArrayList<>();

        ArrayList<String> pathArr = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);      //获取已选图片
        currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);                       //获取当前预览图片的下标

        if (pathArr != null)
        {
            //去除ADD_PIC_TAG
            if (pathArr.contains(ADD_PIC_TAG))
            {
                pathArr.remove(ADD_PIC_TAG);
            }

            paths.addAll(pathArr);
        }

        mPagerAdapter = new PhotoPagerAdapter(mContext, paths);
        mPagerAdapter.setPhotoViewClickListener(this);              //设置预览图片点击事件
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setCurrentItem(currentItem);     //跳转到指定页面
        mViewPager.setOffscreenPageLimit(5);        //设置预加载页面数量
    }

    /**
     * 初始化事件
     */
    private void initEvents()
    {
        //手指滑动翻页事件
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                updateActionBarTitle();     //更新标题
            }

            @Override
            public void onPageSelected(int position)
            {

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    /**
     * 更新标题
     */
    public void updateActionBarTitle()
    {
        getSupportActionBar().setTitle(getString(R.string.image_index, mViewPager.getCurrentItem() + 1, paths.size()));
    }

    /**
     * 预览图片点击事件
     * @param view
     * @param v
     * @param v1
     */
    @Override
    public void OnPhotoTapListener(View view, float v, float v1)
    {
        onBackPressed();
    }

    /**
     * 初始化菜单
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    /**
     * 菜单项的监听方法
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)   //ToolBar左上角返回按钮
        {
            onBackPressed();
            return true;
        }

        // 删除当前照片
        if (item.getItemId() == R.id.action_discard)    //菜单“删除”按钮
        {
            final int index = mViewPager.getCurrentItem();  //当前预览的照片索引
            final String deletedPath = paths.get(index);    //当前预览的照片路径

            Snackbar snackbar = Snackbar.make(
                    getWindow().getDecorView().findViewById(android.R.id.content),
                    R.string.deleted_a_photo,
                    Snackbar.LENGTH_LONG);

            //最后一张照片弹出删除提示对话框，否则只弹出Snackbar
            if (paths.size() <= 1)
            {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_to_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                dialogInterface.dismiss();

                                paths.remove(index);
                                onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
            else
            {
                snackbar.show();

                paths.remove(index);
                mPagerAdapter.notifyDataSetChanged();
            }

            snackbar.setAction(R.string.undo, new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (paths.size() > 0)
                    {
                        paths.add(index, deletedPath);
                    }
                    else
                    {
                        paths.add(deletedPath);
                    }
                    mPagerAdapter.notifyDataSetChanged();
                    mViewPager.setCurrentItem(index, true);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, paths);   //返回已选的图片
        setResult(RESULT_OK, intent);
        finish();

        super.onBackPressed();
    }
}

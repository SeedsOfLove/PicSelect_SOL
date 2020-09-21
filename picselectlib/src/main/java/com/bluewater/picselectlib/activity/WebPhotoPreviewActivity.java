package com.bluewater.picselectlib.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.SaveNetPhotoUtils;
import com.bluewater.picselectlib.adapter.WebPhotoPagerAdapter;
import com.bluewater.picselectlib.widget.ViewPagerFixed;

import java.util.ArrayList;

/**
 * 网络照片预览
 */
public class WebPhotoPreviewActivity extends AppCompatActivity implements WebPhotoPagerAdapter.PhotoViewClickListener
{
    public static final String EXTRA_PHOTOS = "extra_photos";                   //所有预览的照片，ArrayList<String>类型
    public static final String EXTRA_CURRENT_ITEM = "extra_current_item";       //当前图片下标，int类型

    private Context mContext;
    private Activity mActivity;

    private ViewPagerFixed mViewPager;              //图片预览展示控件ViewPager
    private WebPhotoPagerAdapter mPagerAdapter;     //adapter

    private ImageView ivDownLoad;

    private ArrayList<String> imgUrls;      //图片地址

    private int currentItem = 0;        //当前图片下标，默认为0

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_photo_preview);

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

        mViewPager = findViewById(R.id.vp_web_preview_photos);
        ivDownLoad = findViewById(R.id.iv_download);
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        imgUrls = new ArrayList<>();

        ArrayList<String> urlArr = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);      //获取已选图片
        currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);                       //获取当前预览图片的下标

        if (urlArr != null)
        {
            imgUrls.addAll(urlArr);
        }

        mPagerAdapter = new WebPhotoPagerAdapter(mContext, imgUrls);
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

        //下载按钮
        ivDownLoad.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String url = imgUrls.get(mViewPager.getCurrentItem());
                SaveNetPhotoUtils.savePhoto(mContext, url);

                Log.i("网络图片保存", "地址：" + url);
            }
        });
    }

    /**
     * 更新标题
     */
    public void updateActionBarTitle()
    {
        getSupportActionBar().setTitle(getString(R.string.image_index, mViewPager.getCurrentItem() + 1, imgUrls.size()));
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
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)   //ToolBar左上角返回按钮
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

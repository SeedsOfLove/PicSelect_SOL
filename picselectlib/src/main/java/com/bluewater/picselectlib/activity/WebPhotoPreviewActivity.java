package com.bluewater.picselectlib.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.SaveNetPhotoUtils;
import com.bluewater.picselectlib.adapter.WebPhotoPagerAdapter;
import com.bluewater.picselectlib.widget.ViewPagerFixed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 网络照片预览
 */
public class WebPhotoPreviewActivity extends AppCompatActivity
        implements WebPhotoPagerAdapter.PhotoViewClickListener, WebPhotoPagerAdapter.PhotoViewLongClickListener
{
    public static final String EXTRA_PHOTOS = "extra_photos";                   //所有预览的照片，ArrayList<String>类型
    public static final String EXTRA_CURRENT_ITEM = "extra_current_item";       //当前图片下标，int类型

    private Context mContext;
    private Activity mActivity;

    private ViewPagerFixed mViewPager;              //图片预览展示控件ViewPager
    private WebPhotoPagerAdapter mPagerAdapter;     //adapter

    private ImageView ivShare;
    private ImageView ivDownLoad;

    private ArrayList<String> imgUrls;      //图片地址

    private int currentItem = 0;        //当前图片下标，默认为0

    //长按消息弹出删除框，根据activity的touchEvent计算,x y 轴坐标，删除框在点击的坐标显示
    private int x = 0;
    private int y = 0;

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
        ivShare = findViewById(R.id.iv_web_photo_preview_share);
        ivDownLoad = findViewById(R.id.iv_web_photo_preview_download);
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
        mPagerAdapter.setPhotoViewLongClickListener(this);          //设置预览图片长按事件
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

        //分享图片
        ivShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                shareWebImg();
            }
        });

        //下载按钮
        ivDownLoad.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                saveWebImg();
            }
        });
    }

    /**
     * 保存网络图片
     */
    public void saveWebImg()
    {
        String url = imgUrls.get(mViewPager.getCurrentItem());
        SaveNetPhotoUtils.savePhoto(mContext, url);

        Log.i("网络图片保存", "地址：" + url);
    }

    /**
     * 分享网络图片
     */
    public void shareWebImg()
    {
        String url = imgUrls.get(mViewPager.getCurrentItem());
        Uri uri = Uri.parse(url);               //网络图片的URI

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(intent, "分享图片"));
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
    public void OnPhotoLongTapListener(View view)
    {
        PopupMenu popupMenu = new PopupMenu(mContext, view);     //定义PopupMenu对象
        popupMenu.getMenuInflater().inflate(R.menu.menu_web_preview, popupMenu.getMenu());  //设置PopupMenu对象的布局

        //通过反射，显示在点击的位置
        try
        {
            Field mPopup = popupMenu.getClass().getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            Object o = mPopup.get(popupMenu);
            Method show = o.getClass().getMethod("show", int.class, int.class);

            int[] position = new int[2];
            view.getLocationInWindow(position);     //获取view在屏幕上的坐标
            x = (x - position[0]);
            y = (y - position[1] - view.getHeight());

            show.invoke(o, x, y);

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }finally
        {
            //出错时调用普通show方法。未出错时此方法也不会影响正常显示
            popupMenu.show();
        }

        //设置PopupMenu的点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                String choice = item.getTitle().toString();

                switch (choice)
                {
                    case "保存图片":
                        saveWebImg();
                        break;
                    case "分享图片":
                        shareWebImg();
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            x = (int) ev.getRawX();
            y = (int) ev.getRawY();
        }

        return super.dispatchTouchEvent(ev);
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

package com.bluewater.pic_select_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bluewater.picselectlib.intent.WebPhotoPreviewIntent;

import java.util.ArrayList;

public class WebImagePreviewActivity extends AppCompatActivity
{
    private Context mContext;
    private Activity mActivity;

    private GridView gridView;
    private GridAdapter gridAdapter;

    private ArrayList<String> imageUrls;       // 已选中的照片地址， 用于回显选中状态

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_image_preview);

        mContext = this;
        mActivity = this;

        gridView = findViewById(R.id.gridView_web_img);

        imageUrls = new ArrayList<>();
        imageUrls.add("https://img1.baidu.com/it/u=4269599166,108953845&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333");
        imageUrls.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fup.enterdesk.com%2Fphoto%2F2009-2-11%2F200902061824471888.jpg&refer=http%3A%2F%2Fup.enterdesk.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1669778229&t=e0c19d180eb81fb0f415eac381304bdf");
        imageUrls.add("https://pics3.baidu.com/feed/8435e5dde71190efa21876ceada11f10fcfa6005.jpeg?token=34296e70d2b35d79c557abb1dc6f60c6");
        imageUrls.add("https://img1.baidu.com/it/u=8735185,775478860&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500");
        imageUrls.add("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.2008php.com%2F2015_Website_appreciate%2F2015-03-26%2F20150326005538vOVzfvOVzf.jpg&refer=http%3A%2F%2Fwww.2008php.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1669778229&t=a89c26358a0c791a3d8a5448e2c73ebf");

        initGridView();
    }

    /**
     * 初始化GridView
     */
    private void initGridView()
    {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l)
            {
                WebPhotoPreviewIntent intent = new WebPhotoPreviewIntent(mContext);
                intent.setCurrentItem(position);                            //跳转到指定预览的照片
                intent.setPhotoPaths(imageUrls);                           //若没有等于最大选择数，此imagePaths就会包含ADD_PIC_TAG
                startActivity(intent);
            }
        });

        gridAdapter = new GridAdapter(imageUrls, mContext, mActivity, imageUrls.size());
        gridView.setAdapter(gridAdapter);
    }





}
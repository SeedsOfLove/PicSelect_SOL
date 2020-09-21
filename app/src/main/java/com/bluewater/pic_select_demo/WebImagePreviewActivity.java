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
        imageUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600668847206&di=7d552a62264b6d7d386a41200a15da23&imgtype=0&src=http%3A%2F%2Fm.360buyimg.com%2Fpop%2Fjfs%2Ft25168%2F33%2F166786854%2F85434%2F1f2b7808%2F5b681c1fn4f745897.jpg");
        imageUrls.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3222531102,37124484&fm=26&gp=0.jpg");
        imageUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600668901375&di=0483f9815b3c34289e1a12fee837747a&imgtype=0&src=http%3A%2F%2Fimages.china.cn%2Fattachement%2Fjpg%2Fsite1000%2F20150818%2F7427ea210951173d703e2b.jpg");
        imageUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600668915220&di=800764cfe7a605c36070238b4b855f90&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201511%2F28%2F20151128201007_8xjPf.jpeg");
        imageUrls.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600668929162&di=aa0ac3c08483f9470c3bd9ac55906ae4&imgtype=0&src=http%3A%2F%2Ff.hiphotos.baidu.com%2Fzhidao%2Fwh%253D450%252C600%2Fsign%3D3c89fa31544e9258a6618eeaa9b2fd6e%2Fb7003af33a87e9500080e13912385343faf2b4cb.jpg");

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
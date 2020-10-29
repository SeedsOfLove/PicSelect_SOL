package com.bluewater.pic_select_demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bluewater.picselectlib.activity.PhotoPickerActivity;
import com.bluewater.picselectlib.activity.PhotoPreviewActivity;
import com.bluewater.picselectlib.SelectModel;
import com.bluewater.picselectlib.intent.PhotoPickerIntent;
import com.bluewater.picselectlib.intent.PhotoPreviewIntent;

import java.util.ArrayList;

/**
 * Android之仿微信发朋友圈图片选择功能
 */

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final String ADD_PIC_TAG = "AddPic";     //添加图片标志

    private static final int REQUEST_CAMERA_CODE = 10;
    private static final int REQUEST_PREVIEW_CODE = 20;

    private static final int REQUEST_PERMISSION_CODE = 101;

    private static final int MAX_PIC = 9;     //支持的照片最大选择数

    private Context mContext;
    private Activity mActivity;

    private GridView gridView;
    private GridAdapter gridAdapter;

    private Button btn_PreviewWebImg;

    private ArrayList<String> imagePaths;       // 已选中的照片地址， 用于回显选中状态

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mActivity = this;

        gridView = findViewById(R.id.gridView);

        imagePaths = new ArrayList<>();
        imagePaths.add(ADD_PIC_TAG);

        checkPermissions();

        btn_PreviewWebImg = findViewById(R.id.btn_web_img);
        btn_PreviewWebImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, WebImagePreviewActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
     */
    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            //验证是否许可权限
            for (String str : permissions)
            {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, str) != PackageManager.PERMISSION_GRANTED)
                {
                    //申请权限
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSION_CODE);
                    return;
                }
            }
        }
        else
        {
            initGridView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    initGridView();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "拒绝", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    /**
     * 初始化GridView
     */
    private void initGridView()
    {
        Log.i(TAG, "initView: 运行");

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String strImgPath = (String) parent.getItemAtPosition(position);

                if (strImgPath.equals(ADD_PIC_TAG))//进入图片选择
                {
                    PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                    intent.setSelectModel(SelectModel.MULTI);                   // 单选、多选
                    intent.isCameraShow(true);                                  // 是否显示拍照
                    intent.setMaxTotal(MAX_PIC);                                // 最多选择照片数量，默认为9
                    intent.setSelectedPaths(imagePaths);
                    startActivityForResult(intent, REQUEST_CAMERA_CODE);
                }
                else//进入图片预览
                {
                    Log.i(TAG, "选择了图片" + (position + 1));

                    PhotoPreviewIntent intent = new PhotoPreviewIntent(mContext);
                    intent.setCurrentItem(position);                            //跳转到指定预览的照片
                    intent.setPhotoPaths(imagePaths);                           //若没有等于最大选择数，此imagePaths就会包含ADD_PIC_TAG
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }
        });

        gridAdapter = new GridAdapter(imagePaths, mContext, mActivity, MAX_PIC);
        gridView.setAdapter(gridAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                // 选择照片回调
                case REQUEST_CAMERA_CODE:
                    ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);    //获取返回的已选图片路径
                    for (String temp: list)
                    {
                        Log.i(TAG, "照片地址：" + temp);
                    }
                    loadAdapter(list);      //加载图片
                    break;
                // 预览照片回调
                case REQUEST_PREVIEW_CODE:
                    ArrayList<String> ListExtra = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
                    loadAdapter(ListExtra);
                    break;
            }
        }
    }

    /**
     * 加载图片
     * @param paths 图片路径
     */
    private void loadAdapter(ArrayList<String> paths)
    {
        //先清空list
        if (imagePaths != null && imagePaths.size() > 0)
        {
            imagePaths.clear();
        }

        //去除添加图片标志
        if (paths.contains(ADD_PIC_TAG))
        {
            paths.remove(ADD_PIC_TAG);
        }

        if (paths.size() < MAX_PIC )
        {
            paths.add(ADD_PIC_TAG); //在末尾处添加“添加图片”标志
        }

        imagePaths.addAll(paths);   //list赋值

        //更新GridAdapter的数据
//        gridAdapter.notifyDataSetChanged();   //引用此句，在第一次拍照时，添加图片的背景图会变成照片图
        gridAdapter = new GridAdapter(imagePaths, mContext, mActivity, MAX_PIC);
        gridView.setAdapter(gridAdapter);
    }

}


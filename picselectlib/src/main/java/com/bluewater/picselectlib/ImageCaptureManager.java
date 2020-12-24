package com.bluewater.picselectlib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 图像捕获管理器
 */
public class ImageCaptureManager
{
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    public static final int REQUEST_TAKE_PHOTO = 1;

    private Context mContext;

    private String mCurrentPhotoPath;      //当前拍照的照片路径

    public ImageCaptureManager(Context mContext)
    {
        this.mContext = mContext;
    }

    /**
     * 调用系统相机拍照
     * @return
     */
    public Intent dispatchTakePictureIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(mContext.getPackageManager()) != null)
        {
            File photoFile = createImageFile();     //创建图片文件

            if (photoFile != null)
            {
                //判断版本号，N(API24)以上拍照调用方式不一样
                int currentApiVersion = Build.VERSION.SDK_INT;

                if (currentApiVersion < Build.VERSION_CODES.N)
                {
                    Uri uri = Uri.fromFile(photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }
                else
                {
                    //访问媒体库的内容提供器接口
                    ContentResolver resolver = mContext.getContentResolver();

                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, photoFile.getAbsolutePath());

                    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues); //插入数据

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }
            }
            else
            {
                Toast.makeText(mContext, R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(mContext, R.string.mis_msg_no_camera, Toast.LENGTH_SHORT).show();
        }

        return intent;
    }

    /**
     *  启动系统MediaScanner服务扫描指定的文件或目录，从而更新Android媒体库
     */
    public void galleryAddPic()
    {
        File file = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(file);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    /**
     * 获取当前拍照照片的路径
     * @return
     */
    public String getCurrentPhotoPath()
    {
        return mCurrentPhotoPath;
    }

    /**
     * 创建图片文件
     * （文件名：IMG_年月日_时分秒.jpg）
     *
     * @return
     * @throws IOException
     */
    private File createImageFile()
    {
        //文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        File sdcardDir = null;
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //推断SDCard是否存在
        if(sdcardExist)
        {
            sdcardDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");  //图片存储路径
        }
        else
        {
            Toast.makeText(mContext, "无法存储图片，SD卡不存在", Toast.LENGTH_SHORT).show();
        }

        File image = new File(sdcardDir, imageFileName + ".jpg");   //新建文件

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null && mCurrentPhotoPath != null)
        {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY))
        {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }

}

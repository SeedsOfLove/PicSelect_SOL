package com.bluewater.picselectlib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.content.FileProvider;

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
     */
    public Intent dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = createImageFile();

            if (photoFile != null) {
                // 使用 FileProvider 生成 Uri
                // 注意：第二个参数必须和 AndroidManifest 中的 authorities 一致
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        mContext.getPackageName() + ".fileprovider", // 这里会自动获取主项目的包名
                        photoFile);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // 授予临时权限（关键）
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                Toast.makeText(mContext, R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show();
            }
        } else {
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

        // 使用 getExternalFilesDir，这属于应用私有空间，API 34 也能直接用 File 访问
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = new File(storageDir, imageFileName);

        // 保存绝对路径供 Glide 使用
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

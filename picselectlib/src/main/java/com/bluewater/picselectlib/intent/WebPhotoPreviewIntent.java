package com.bluewater.picselectlib.intent;

import android.content.Context;
import android.content.Intent;

import com.bluewater.picselectlib.activity.WebPhotoPreviewActivity;

import java.util.ArrayList;

/**
 * 网络照片预览意图
 */
public class WebPhotoPreviewIntent extends Intent
{

    public WebPhotoPreviewIntent(Context packageContext)
    {
        super(packageContext, WebPhotoPreviewActivity.class);
    }

    /**
     * 照片地址
     *
     * @param paths
     */
    public void setPhotoPaths(ArrayList<String> paths)
    {
        this.putStringArrayListExtra(WebPhotoPreviewActivity.EXTRA_PHOTOS, paths);
    }

    /**
     * 当前照片的下标
     *
     * @param currentItem
     */
    public void setCurrentItem(int currentItem)
    {
        this.putExtra(WebPhotoPreviewActivity.EXTRA_CURRENT_ITEM, currentItem);
    }

    /**
     * 是否显示底部工具栏
     *
     * @param flag
     */
    public void showToolBar(boolean flag)
    {
        this.putExtra(WebPhotoPreviewActivity.EXTRA_SHOW_TOOLBAR, flag);
    }
}
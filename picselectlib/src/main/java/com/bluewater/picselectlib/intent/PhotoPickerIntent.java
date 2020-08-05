package com.bluewater.picselectlib.intent;

import android.content.Context;
import android.content.Intent;

import com.bluewater.picselectlib.ImageConfig;
import com.bluewater.picselectlib.activity.PhotoPickerActivity;
import com.bluewater.picselectlib.SelectModel;

import java.util.ArrayList;

/**
 * 照片选择意图
 */
public class PhotoPickerIntent extends Intent
{

    public PhotoPickerIntent(Context packageContext)
    {
        super(packageContext, PhotoPickerActivity.class);
    }

    /**
     * 是否显示拍照
     * @param bool
     */
    public void isCameraShow(boolean bool)
    {
        this.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, bool);
    }

    /**
     * 最多选择照片数量，默认为9
     * @param total
     */
    public void setMaxTotal(int total)
    {
        this.putExtra(PhotoPickerActivity.EXTRA_SELECT_COUNT, total);
    }

    /**
     * 选择模式
     *
     * @param model     SelectModel.SINGLE 单选    SelectModel.MULTI 多选
     */
    public void setSelectModel(SelectModel model)
    {
        this.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, Integer.parseInt(model.toString()));
    }

    /**
     * 已选择的照片地址
     *
     * @param imagePathis
     */
    public void setSelectedPaths(ArrayList<String> imagePathis)
    {
        this.putStringArrayListExtra(PhotoPickerActivity.EXTRA_DEFAULT_SELECTED_LIST, imagePathis);
    }

    /**
     * 显示相册图片的属性
     *
     * @param config
     */
    public void setImageConfig(ImageConfig config)
    {
        this.putExtra(PhotoPickerActivity.EXTRA_IMAGE_CONFIG, config);
    }
}

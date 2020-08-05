package com.bluewater.picselectlib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.bean.Image;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片展示和选择的Adapter
 */
public class ImageGridAdapter extends BaseAdapter
{
    private static final int TYPE_CAMERA = 0;       //照相机
    private static final int TYPE_NORMAL = 1;       //普通图片

    private Context mContext;
    private LayoutInflater mInflater;

    private boolean mShowCamera;                    //是否显示照相机
    private boolean mShowSelectIndicator = true;    //是否显示选择指示器（即图片右上角的勾），默认显示

    private List<Image> mImages = new ArrayList<>();            //展示的所有照片
    private List<Image> mSelectedImages = new ArrayList<>();    //已选择的照片list

    private int mItemSize;      //Item图片的尺寸
    private GridView.LayoutParams mItemLayoutParams;

    public ImageGridAdapter(Context context, boolean showCamera, int itemSize)
    {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mShowCamera = showCamera;
        this.mItemSize = itemSize;
        this.mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);   //正方形
    }

    public class ViewHolder
    {
        ImageView image;        //照片
        ImageView indicator;    //右上角打勾的选择指示器
        View mask;              //灰色透明的选中标记

        public ViewHolder(View view)
        {
            image = view.findViewById(R.id.iv_item_select_img_image);
            indicator = view.findViewById(R.id.iv_item_select_img_checkmark);
            mask = view.findViewById(R.id.view_item_select_img_mask);
            view.setTag(this);
        }

        /**
         * 绑定数据
         * @param data
         */
        private void bindData(final Image data)
        {
            if (data == null)
                return;

            // 处理单选和多选状态
            if (mShowSelectIndicator)   //多选
            {
                indicator.setVisibility(View.VISIBLE);

                if (mSelectedImages.contains(data))     // 设置选中状态
                {
                    indicator.setImageResource(R.mipmap.btn_selected);
                    mask.setVisibility(View.VISIBLE);
                }
                else    // 未选择
                {
                    indicator.setImageResource(R.mipmap.btn_unselected);
                    mask.setVisibility(View.GONE);
                }
            }
            else    //单选
            {
                indicator.setVisibility(View.GONE);
            }

            File imageFile = new File(data.path);   //获取文件实例

            if (mItemSize > 0)
            {
                DrawableCrossFadeFactory drawableCrossFadeFactory =
                        new DrawableCrossFadeFactory
                                .Builder()
                                .setCrossFadeEnabled(true)
                                .build();

                // 显示图片
                Glide.with(mContext)
                        .load(imageFile)
                        .placeholder(R.mipmap.default_error)
                        .error(R.mipmap.default_error)
                        .override(mItemSize, mItemSize)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                        .into(image);
            }
        }
    }

    /**
     * 设置所有展示图片的数据集
     * @param images
     */
    public void setData(List<Image> images)
    {
        mSelectedImages.clear();

        if (images != null && images.size() > 0)
        {
            mImages = images;
        }
        else
        {
            mImages.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList)
    {
        mSelectedImages.clear();

        for (String path : resultList)
        {
            Image image = getImageByPath(path);

            if (image != null)
            {
                mSelectedImages.add(image);
            }
        }

        notifyDataSetChanged();
    }

    /**
     * 选择某个图片，改变选择状态
     * @param image
     */
    public void changeSelectState(Image image)
    {
        if (mSelectedImages.contains(image))
        {
            mSelectedImages.remove(image);
        }
        else
        {
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置选择指示器的显示（即图片右上角的勾）
     * @param isShow 单选可设为false，多选设为true
     */
    public void setSelectIndicatorShow(boolean isShow)
    {
        mShowSelectIndicator = isShow;
    }

    /**
     * 设置照相机图标的显示
     * @param isShow
     */
    public void setCameraShow(boolean isShow)
    {
        if (mShowCamera == isShow)
            return;

        mShowCamera = isShow;
        notifyDataSetChanged();
    }

    /**
     * 是否显示照相机
     * @return
     */
    public boolean isShowCamera()
    {
        return mShowCamera;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        int type = getItemViewType(position);   //获取Item类型

        if (type == TYPE_CAMERA)    //照相机
        {
            view = mInflater.inflate(R.layout.item_camera, viewGroup, false);
            view.setTag(null);
        }
        else if (type == TYPE_NORMAL)   //普通图片
        {
            ViewHolder holder;

            if (view == null)
            {
                view = mInflater.inflate(R.layout.item_select_image, viewGroup, false);
                holder = new ViewHolder(view);
            }
            else
            {
                holder = (ViewHolder) view.getTag();

                if (holder == null)
                {
                    view = mInflater.inflate(R.layout.item_select_image, viewGroup, false);
                    holder = new ViewHolder(view);
                }
            }

            if (holder != null)
            {
                holder.bindData(getItem(position));
            }
        }

        /** Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
        if (lp.height != mItemSize)
        {
            view.setLayoutParams(mItemLayoutParams);
        }

        return view;
    }

    /**
     * 设置2种类型的Item
     * @return
     */
    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    /**
     * 获取Item的类型
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position)
    {
        if (mShowCamera)    //若显示照相机
        {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;   //则首个Item类型为照相机
        }

        return TYPE_NORMAL;
    }

    /**
     * 重置每个Column的Size（正方形）
     * @param columnWidth
     */
    public void setItemSize(int columnWidth)
    {
        if (mItemSize == columnWidth)
        {
            return;
        }
        mItemSize = columnWidth;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
        notifyDataSetChanged();
    }

    /**
     * 得到要绑定条目总数
     * @return
     */
    @Override
    public int getCount()
    {
        return mShowCamera ? mImages.size() + 1 : mImages.size();   //若含摄像机，则数目+1
    }

    /**
     * 给定索引值，得到索引值对应的Image对象
     * @param position
     * @return
     */
    @Override
    public Image getItem(int position)
    {
        if (mShowCamera)
        {
            if (position == 0)
            {
                return null;
            }

            return mImages.get(position - 1);
        }
        else
        {
            return mImages.get(position);
        }
    }

    /**
     * 获取条目的ID
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * 根据路径获取Image实例
     * @param path
     * @return
     */
    private Image getImageByPath(String path)
    {
        if (mImages != null && mImages.size() > 0)
        {
            for (Image image : mImages)
            {
                if (image.path.equalsIgnoreCase(path))
                {
                    return image;
                }
            }
        }
        return null;
    }
}


package com.bluewater.picselectlib.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bluewater.picselectlib.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.File;
import java.util.List;

/**
 * 预览照片的Adapter
 */
public class PhotoPagerAdapter extends PagerAdapter
{
    private Context mContext;

    private List<String> paths;                 //所有预览照片的路径
    private LayoutInflater mLayoutInflater;

    public PhotoViewClickListener listener; //点击事件监听

    public PhotoPagerAdapter(Context mContext, List<String> paths)
    {
        this.mContext = mContext;
        this.paths = paths;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        View itemView = mLayoutInflater.inflate(R.layout.item_preview, container, false);
        PhotoView imageView = itemView.findViewById(R.id.pv_preview_pager);     //照片显示控件

        String path = paths.get(position);          //当前预览照片的路径
        Uri uri = Uri.fromFile(new File(path));     //本地图片的URI

        //淡入淡出动画效果
        DrawableCrossFadeFactory drawableCrossFadeFactory =
                new DrawableCrossFadeFactory
                        .Builder()
                        .setCrossFadeEnabled(true)
                        .build();

        //显示图片
        Glide.with(mContext)
                .load(uri)
                .error(R.mipmap.default_error)
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(imageView);

        //图片点击事件
        imageView.setOnPhotoTapListener(new OnPhotoTapListener()
        {
            @Override
            public void onPhotoTap(ImageView view, float v, float v1)
            {
                if (listener != null)
                {
                    listener.OnPhotoTapListener(view, v, v1);
                }
            }
        });

        container.addView(itemView);

        return itemView;
    }


    @Override
    public int getCount()
    {
        return paths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    /**
     * 设置预览图片点击事件
     * @param listener
     */
    public void setPhotoViewClickListener(PhotoViewClickListener listener)
    {
        this.listener = listener;
    }

    /**
     * 照片点击事件接口
     */
    public interface PhotoViewClickListener
    {
        void OnPhotoTapListener(View view, float v, float v1);
    }

}

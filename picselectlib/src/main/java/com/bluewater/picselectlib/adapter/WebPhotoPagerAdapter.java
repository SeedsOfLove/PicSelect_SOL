package com.bluewater.picselectlib.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bluewater.picselectlib.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 预览网络照片的Adapter
 */
public class WebPhotoPagerAdapter extends PagerAdapter
{
    private Context mContext;

    private List<String> mImgUrls;                 //所有预览照片地址
    private LayoutInflater mLayoutInflater;

    public PhotoViewClickListener mListener;                //点击事件监听
    public PhotoViewLongClickListener mListenerLong;        //长按事件监听

    public WebPhotoPagerAdapter(Context mContext, List<String> imgUrls)
    {
        this.mContext = mContext;
        this.mImgUrls = imgUrls;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position)
    {
        View itemView = mLayoutInflater.inflate(R.layout.item_preview, container, false);
        PhotoView imageView = itemView.findViewById(R.id.pv_preview_pager);     //照片显示控件

        String url = mImgUrls.get(position);    //当前预览照片的网络地址
        Uri uri = Uri.parse(url);               //网络图片的URI

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
        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener()
        {
            @Override
            public void onPhotoTap(View view, float x, float y)
            {
                if (mListener != null)
                {
                    mListener.OnPhotoTapListener(view, x, y);
                }
            }
        });

        //图片长按事件
        imageView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if (mListenerLong != null)
                {
                    mListenerLong.OnPhotoLongTapListener(view);
                }

                return false;
            }
        });

        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getCount()
    {
        return mImgUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
    {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
    {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object)
    {
        return POSITION_NONE;
    }

    /**
     * 设置预览图片点击事件
     * @param listener
     */
    public void setPhotoViewClickListener(PhotoViewClickListener listener)
    {
        this.mListener = listener;
    }

    /**
     * 设置预览图片长按事件
     * @param listener
     */
    public void setPhotoViewLongClickListener(PhotoViewLongClickListener listener)
    {
        this.mListenerLong = listener;
    }

    /**
     * 照片点击事件接口
     */
    public interface PhotoViewClickListener
    {
        void OnPhotoTapListener(View view, float v, float v1);
    }

    /**
     * 照片点击事件接口
     */
    public interface PhotoViewLongClickListener
    {
        void OnPhotoLongTapListener(View view);
    }
}

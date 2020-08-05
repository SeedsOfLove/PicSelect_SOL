package com.bluewater.pic_select_demo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter
{
    private static final String ADD_PIC_TAG = "AddPic";     //添加图片标志

    private Context mContext;
    private Activity mActivity;

    private ArrayList<String> mListPaths;
    private LayoutInflater inflater;

    public GridAdapter(ArrayList<String> listPaths, Context context, Activity activity, int maxSelect)
    {
        this.mContext = context;
        this.mActivity = activity;

        this.mListPaths = listPaths;
        if (mListPaths.size() == (maxSelect + 1))    //等于最大选择数时，去掉ADD_PIC_TAG
        {
            mListPaths.remove(mListPaths.size() - 1);
        }

        inflater = LayoutInflater.from(mContext);
    }

    public int getCount()
    {
        return mListPaths.size();
    }

    @Override
    public String getItem(int position)
    {
        return mListPaths.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        //convertView缓存布局，避免重复加载布局
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item, parent, false);

            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);     //将ViewHolder存储在View中
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();     //重获ViewHolder
        }

        final String path = mListPaths.get(position);
        if (path.equals(ADD_PIC_TAG))
        {
            holder.image.setImageResource(R.mipmap.find_add_img);
        }
        else
        {
            //淡入淡出动画效果
            DrawableCrossFadeFactory drawableCrossFadeFactory =
                    new DrawableCrossFadeFactory
                            .Builder()
                            .setCrossFadeEnabled(true)
                            .build();

            Glide.with(mActivity)
                    .load(path)
                    .placeholder(R.mipmap.default_error)
                    .error(R.mipmap.default_error)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.image);
        }
        return convertView;
    }

    class ViewHolder
    {
        ImageView image;
    }
}

package com.bluewater.picselectlib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.bean.Folder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片分类文件夹（相册）Adapter
 */
public class FolderAdapter extends BaseAdapter
{
    private Context mContext;
    private LayoutInflater mInflater;

    private List<Folder> mFolders = new ArrayList<>();      //相册list(不含“所有图片”)

    int mImageSize;             //图片尺寸
    int lastSelected = 0;       //最新选择的相册索引（默认第一个）

    public FolderAdapter(Context context)
    {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }

    public class ViewHolder
    {
        ImageView ivCover;            //相册Item展示图
        TextView tvName;              //相册名称
        TextView tvSize;              //相册照片个数
        ImageView ivIndicator;        //选择指示器

        public ViewHolder(View view)
        {
            ivCover = view.findViewById(R.id.iv_folder_item_cover);
            tvName = view.findViewById(R.id.tv_folder_item_name);
            tvSize = view.findViewById(R.id.tv_folder_item_size);
            ivIndicator = view.findViewById(R.id.iv_folder_item_indicator);
            view.setTag(this);
        }

        /**
         * 绑定数据
         * @param data
         */
        private void bindData(Folder data)
        {
            tvName.setText(data.folderName);
            tvSize.setText(data.folserImagesList.size() + "张");

            //淡入淡出动画效果
            DrawableCrossFadeFactory drawableCrossFadeFactory =
                    new DrawableCrossFadeFactory
                            .Builder()
                            .setCrossFadeEnabled(true)
                            .build();

            // 显示展示图
            Glide.with(mContext)
                    .load(new File(data.folderCover.path))          //展示图的路径
                    .placeholder(R.mipmap.default_error)            //占位图
                    .error(R.mipmap.default_error)                  //错图
                    .override(mImageSize, mImageSize)               //重写图片大小
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .centerCrop()
                    .into(ivCover);
        }
    }

    /**
     * 设置数据集
     * @param folders
     */
    public void setData(List<Folder> folders)
    {
        if (folders != null && folders.size() > 0)
        {
            mFolders = folders;
        }
        else
        {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 设置当前选择的索引
     * @param position
     */
    public void setSelectIndex(int position)
    {
        if (lastSelected == position)
            return;

        lastSelected = position;

        notifyDataSetChanged();
    }

    /**
     * 获取当前选择的相册索引
     * @return
     */
    public int getSelectIndex()
    {
        return lastSelected;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        ViewHolder holder;
        if (view == null)
        {
            view = mInflater.inflate(R.layout.item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        }
        else
        {
            holder = (ViewHolder) view.getTag();
        }

        if (holder != null)
        {
            if (position == 0)      //“所有图片”相册
            {
                holder.tvName.setText(mContext.getResources().getString(R.string.all_image));
                holder.tvSize.setText(getTotalImageSize() + "张");
                if (mFolders.size() > 0)
                {
                    Folder f = mFolders.get(0);

                    //淡入淡出动画效果
                    DrawableCrossFadeFactory drawableCrossFadeFactory =
                            new DrawableCrossFadeFactory
                                    .Builder()
                                    .setCrossFadeEnabled(true)
                                    .build();

                    Glide.with(mContext)
                            .load(new File(f.folderCover.path))
                            .error(R.mipmap.default_error)
                            .override(mImageSize, mImageSize)
                            .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                            .centerCrop()
                            .into(holder.ivCover);
                }
            }
            else    //其他相册
            {
                holder.bindData(getItem(position));     //此处 position > 0
            }

            //选择指示器的显示
            if (lastSelected == position)
            {
                holder.ivIndicator.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.ivIndicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    /**
     * 获取手机中所有图片总数
     * @return
     */
    private int getTotalImageSize()
    {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0)
        {
            for (Folder f : mFolders)
            {
                result += f.folserImagesList.size();
            }
        }
        return result;
    }

    /**
     * 得到要绑定条目总数
     * @return
     */
    @Override
    public int getCount()
    {
        return mFolders.size() + 1;     //前面多了一个“所有图片”的对象，不包含在mFolders中
    }

    /**
     * 给定索引值，得到索引值对应的对象
     * @param position
     * @return
     */
    @Override
    public Folder getItem(int position)
    {
        if (position == 0)
            return null;

        return mFolders.get(position - 1);  //前面多了一个“所有图片”的对象，不包含在mFolders中
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


}

package com.bluewater.picselectlib.bean;

import java.util.List;

/**
 * 文件夹（相册）
 */
public class Folder
{
    public Image folderCover;                   //展示图
    public String folderName;                   //相册名称
    public String folderPath;                   //相册路径
    public List<Image> folserImagesList;        //相册中的所有图片实例

    @Override
    public boolean equals(Object o)
    {
        try
        {
            Folder other = (Folder) o;

            return this.folderPath.equalsIgnoreCase(other.folderPath);

        } catch (ClassCastException e)
        {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}


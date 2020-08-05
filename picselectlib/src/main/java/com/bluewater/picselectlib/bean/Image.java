package com.bluewater.picselectlib.bean;

/**
 * 图片实体
 */
public class Image
{
    public String path;     //图片路径
    public String name;     //图片名称
    public long time;       //图片时间

    public Image(String path, String name, long time)
    {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o)
    {
        try
        {
            Image other = (Image) o;

            return this.path.equalsIgnoreCase(other.path);

        } catch (ClassCastException e)
        {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
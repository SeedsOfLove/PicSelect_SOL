package com.bluewater.picselectlib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 读取手机照片的限制参数
 */
public class ImageConfig implements Parcelable
{
    public int minWidth;            // 图片最小宽度
    public int minHeight;           // 图片最小高度
    public long minSize;             // 图片大小，单位字节
    public String[] mimeType;       // 照片类型: 例如 { image/jpeg, image/png, ... }

    protected ImageConfig(Parcel in)
    {
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.minSize = in.readLong();
        this.mimeType = in.createStringArray();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.minWidth);
        dest.writeInt(this.minHeight);
        dest.writeLong(this.minSize);
        dest.writeStringArray(this.mimeType);
    }

    public static final Creator<ImageConfig> CREATOR = new Creator<ImageConfig>()
    {
        public ImageConfig createFromParcel(Parcel source)
        {
            return new ImageConfig(source);
        }

        public ImageConfig[] newArray(int size)
        {
            return new ImageConfig[size];
        }
    };
}

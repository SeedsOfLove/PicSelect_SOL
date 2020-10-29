package com.bluewater.picselectlib.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluewater.picselectlib.bean.Folder;
import com.bluewater.picselectlib.adapter.FolderAdapter;
import com.bluewater.picselectlib.bean.Image;
import com.bluewater.picselectlib.ImageCaptureManager;
import com.bluewater.picselectlib.ImageConfig;
import com.bluewater.picselectlib.adapter.ImageGridAdapter;
import com.bluewater.picselectlib.R;
import com.bluewater.picselectlib.intent.PhotoPreviewIntent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 照片选择
 */
public class PhotoPickerActivity extends AppCompatActivity
{
    public static final String TAG = "PhotoPickerActivity";

    private static final String ADD_PIC_TAG = "AddPic";     //添加图片标志

    /*  Intent传参   */
    public static final String EXTRA_SHOW_CAMERA = "is_camera_show";                //是否显示相机，boolean类型
    public static final String EXTRA_SELECT_COUNT = "max_select_count";             //最多选择照片数量，int类型
    public static final String EXTRA_SELECT_MODE = "select_count_mode";             //图片选择模式，int类型
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_result";      //默认选择的数据集，ArrayList<String>类型
    public static final String EXTRA_IMAGE_CONFIG = "image_config";                 //筛选照片配置信息，ImageConfig类型
    public static final String EXTRA_RESULT = "select_result";                      //选择结果，返回为 ArrayList<String> 图片路径集合

    public static final int MODE_SINGLE = 0;                //单选
    public static final int MODE_MULTI = 1;                 //多选

    public static final int DEFAULT_MAX_TOTAL = 9;          //默认最大照片数量

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    private Context mContext;

    private MenuItem menuDoneItem;          //菜单“完成”选项

    private GridView mGridView;             //照片展示列表GridView
    private ImageGridAdapter mImageAdapter; //GridView的Adapter

    private RelativeLayout mPopupAnchorView;//底部操作栏
    private Button btnAlbum;                //所有图片（相册）按钮
    private Button btnPreview;              //预览按钮

    private ArrayList<String> imagePathList;        // 存储已选图片路径数据

    private ListPopupWindow mFolderPopupWindow;     // 相册文件夹列表弹出窗口
    private FolderAdapter mFolderAdapter;           // 图片分类文件夹（相册）的Adapter
    private ArrayList<Folder> mResultFoldersList;   // 相册文件夹list

    private ImageCaptureManager captureManager;     //图像捕获管理器

    private boolean mIsShowCamera = false;      // 是否显示照相机
    private int mDesireImageCount;              // 最大照片数量
    private int mSelectmode;                    // 图片选择模式
    private ImageConfig mImageConfig;           // 筛选照片配置信息

    private boolean hasFolderGened = false;     //是否生成相册文件夹

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        mContext = this;

        getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);         // 首次加载所有图片

        initData();
        initViews();
        initEvents();
    }

    /**
     * 初始化数据
     */
    private void initData()
    {
        imagePathList = new ArrayList<>();

        mResultFoldersList = new ArrayList<>();
        mFolderAdapter = new FolderAdapter(mContext);

        captureManager = new ImageCaptureManager(mContext);

        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);                  // 是否显示照相机
        mDesireImageCount = getIntent().getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_MAX_TOTAL);     // 选择图片数量
        mSelectmode = getIntent().getExtras().getInt(EXTRA_SELECT_MODE, MODE_SINGLE);           // 图片选择模式
        mImageConfig = getIntent().getParcelableExtra(EXTRA_IMAGE_CONFIG);                      // 筛选照片配置信息

        // 默认已选择的照片
        if (mSelectmode == MODE_MULTI)
        {
            ArrayList<String> pathTmp = getIntent().getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);   //获取已经选择的照片路径
            if (pathTmp != null && pathTmp.size() > 0)
            {
                 imagePathList.addAll(pathTmp);
            }
        }
    }

    /**
     * 初始化视图
     */
    private void initViews()
    {
        //----------------工具栏设置
        Toolbar toolbar = findViewById(R.id.pickerToolbar);
        setSupportActionBar(toolbar);                                               // 将ToolBar设置成ActionBar
        getSupportActionBar().setTitle(getResources().getString(R.string.image));                                     // 设置标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);                      // 给左上角图标的左边加上一个返回的图标

        //----------------GridView
        mGridView = findViewById(R.id.grid);
        mGridView.setNumColumns(getNumColumns());   //设置GridView显示列数

        mImageAdapter = new ImageGridAdapter(mContext, mIsShowCamera, getItemImageWidth());
        mImageAdapter.setSelectIndicatorShow(mSelectmode == MODE_MULTI);               // 是否显示选择指示器（即图片右上角的勾）
        mGridView.setAdapter(mImageAdapter);

        //----------------底部操作栏
        mPopupAnchorView = findViewById(R.id.photo_picker_footer);
        btnAlbum = findViewById(R.id.btnAlbum);
        btnPreview = findViewById(R.id.btnPreview);
    }

    /**
     * 初始化事件
     */
    private void initEvents()
    {
        //GridView Item点击事件
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (mImageAdapter.isShowCamera())   //显示照相机
                {
                    if (i == 0)     //GridView的第一个Item为照相机，点击执行拍照处理
                    {
                        if (mSelectmode == MODE_MULTI)
                        {
                            // 判断选择数量问题
                            if (mDesireImageCount == imagePathList.size())
                            {
                                Toast.makeText(mContext, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        showCameraAction();     //拍照
                    }
                    else    //选择照片操作
                    {
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mSelectmode);
                    }
                }
                else    //选择照片操作
                {
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mSelectmode);
                }
            }
        });

        // 打开相册列表(所有图片)
        btnAlbum.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mFolderPopupWindow == null)
                {
                    createPopupFolderList();    //创建弹出的相册文件夹列表
                }

                if (mFolderPopupWindow.isShowing())     //弹出窗口是否正在显示
                {
                    mFolderPopupWindow.dismiss();
                }
                else
                {
                    mFolderPopupWindow.show();

                    //方便定位选择的相册位置，使其在视野内
                    int index = mFolderAdapter.getSelectIndex();        //当前选择相册的索引
                    index = index == 0 ? index : index - 1;
                    mFolderPopupWindow.getListView().setSelection(index);   //设置当前选中的项
                }
            }
        });

        //预览按钮
        btnPreview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PhotoPreviewIntent intent = new PhotoPreviewIntent(mContext);
                intent.setCurrentItem(0);                       //当前图片下标，0代表预览的第一张图
                intent.setPhotoPaths(imagePathList);            //已选图片
                startActivityForResult(intent, PhotoPreviewActivity.REQUEST_PREVIEW);
            }
        });
    }

    /**
     * 拍照
     */
    private void showCameraAction()
    {
        Intent intent = captureManager.dispatchTakePictureIntent();
        startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode)
    {
        if (image != null)
        {
            if (mode == MODE_MULTI)         // 多选模式
            {
                if (imagePathList.contains(image.path))     //若已选了该图片，则从已选中移除，并刷新界面
                {
                    imagePathList.remove(image.path);
                    refreshActionStatus();
                }
                else
                {
                    if (mDesireImageCount == imagePathList.size())  //选择数量限制判断
                    {
                        Toast.makeText(mContext, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    imagePathList.add(image.path);      //添加图片
                    refreshActionStatus();
                }
                mImageAdapter.changeSelectState(image);
            }
            else if (mode == MODE_SINGLE)   // 单选模式
            {
                imagePathList.add(image.path);
                complete();                     //返回已选择的单张图片数据
            }
        }
    }

    /**
     * 刷新操作按钮状态
     */
    private void refreshActionStatus()
    {
        if (imagePathList.contains(ADD_PIC_TAG))    //去除“添加图片”的图标image
        {
            imagePathList.remove(ADD_PIC_TAG);
        }

        boolean hasSelected = imagePathList.size() > 0;     //是否已有选择图片

        //设置Menu选择情况文本显示
        String setDoneWithCount = getString(R.string.done_with_count, imagePathList.size(), mDesireImageCount);
        menuDoneItem.setTitle(setDoneWithCount);
        menuDoneItem.setVisible(hasSelected);               //显示menu

        //设置“预览”选择情况文本显示
        if (hasSelected)
        {
            btnPreview.setText(getResources().getString(R.string.preview) + "(" + (imagePathList.size()) + ")");
        }
        else
        {
            btnPreview.setText(getResources().getString(R.string.preview));
        }
        btnPreview.setEnabled(hasSelected);                 //预览按钮使能设置
    }

    /**
     * 创建弹出的相册文件夹列表窗口
     */
    private void createPopupFolderList()
    {
        mFolderPopupWindow = new ListPopupWindow(mContext);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));     //设置背景色
        mFolderPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);                          //设置宽度
        mFolderPopupWindow.setAdapter(mFolderAdapter);                                      //设置adapter

        //----------------ListPopupWindow高度设置
        // 计算ListPopupWindow内容的高度
        int folderItemViewHeight =
                getResources().getDimensionPixelOffset(R.dimen.folder_cover_size)                   // 图片高度
                        + getResources().getDimensionPixelOffset(R.dimen.folder_padding)            // Padding Top
                        + getResources().getDimensionPixelOffset(R.dimen.folder_padding);           // Padding Bottom
        int folderViewHeight = mFolderAdapter.getCount() * folderItemViewHeight;        // 计算ListPopupWindow的高度     (内容个数 * 内容高度)
        int screenHeigh = getResources().getDisplayMetrics().heightPixels;              //屏幕高度
        if (folderViewHeight >= screenHeigh)
        {
            mFolderPopupWindow.setHeight(Math.round(screenHeigh * 0.6f));
        }
        else
        {
            mFolderPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        }

        mFolderPopupWindow.setAnchorView(mPopupAnchorView);                             //设置锚点
        mFolderPopupWindow.setModal(true);                                              //模态
        mFolderPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);   //动画

        //Item点击事件
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                mFolderAdapter.setSelectIndex(position);

                final int index = position;
                final AdapterView adapterView = parent;

                //延迟0.1s刷新显示
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mFolderPopupWindow.dismiss();       //窗口消失

                        if (index == 0)//相册中的“所有图片”
                        {
                            getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback); //加载所有图片
                            btnAlbum.setText(R.string.all_image);
                            mImageAdapter.setCameraShow(mIsShowCamera);     //显示照相机
                        }
                        else//其他相册
                        {
                            Folder folder = (Folder) adapterView.getAdapter().getItem(index);   //获取所选Folder实例

                            if (folder != null)
                            {
                                btnAlbum.setText(folder.folderName);
                                mImageAdapter.setData(folder.folserImagesList);

                                // 设定该默认选择
                                if (imagePathList != null && imagePathList.size() > 0)
                                {
                                    mImageAdapter.setDefaultSelected(imagePathList);
                                }
                            }
                            mImageAdapter.setCameraShow(false);     //隐藏照相机
                        }

                        mGridView.smoothScrollToPosition(0);     // GridView滑到顶端
                    }
                }, 100);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                // 相机拍照完成后，返回图片路径
                case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                    if (captureManager.getCurrentPhotoPath() != null)
                    {
                        captureManager.galleryAddPic();
                        imagePathList.add(captureManager.getCurrentPhotoPath());
                    }
                    complete();
                    break;

                // 预览照片
                case PhotoPreviewActivity.REQUEST_PREVIEW:

                    ArrayList<String> pathArr = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);    //获取返回的数据

                    // 刷新页面
                    if (pathArr != null && pathArr.size() != imagePathList.size())
                    {
                        imagePathList = pathArr;
                        refreshActionStatus();
                        mImageAdapter.setDefaultSelected(imagePathList);
                    }
                    break;
            }
        }
    }

    /**
     *  初始化菜单
     * @param menu  菜单实例
     * @return      true 显示菜单，false 不显示菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.action_picker_done);
        menuDoneItem.setVisible(false);                         //初始化不显示“完成”
        refreshActionStatus();                                  //刷新操作按钮状态
        return true;
    }

    /**
     * 菜单项的监听方法
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (item.getItemId() == android.R.id.home)          //ToolBar左上角返回按钮
        {
            Log.d(TAG, "onOptionsItemSelected: 返回");
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_picker_done)    //菜单“完成”选项
        {
            complete();     // 返回已选择的图片数据
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 返回已选择的图片数据
     */
    private void complete()
    {
        Intent data = new Intent();
        data.putStringArrayListExtra(EXTRA_RESULT, imagePathList);      //返回所有图片路径
        setResult(RESULT_OK, data);                                     //回传数据
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d(TAG, "onConfigurationChanged");

        // 重置列数
        mGridView.setNumColumns(getNumColumns());
        // 重置Item宽度
        mImageAdapter.setItemSize(getItemImageWidth());

        if (mFolderPopupWindow != null)
        {
            if (mFolderPopupWindow.isShowing())
            {
                mFolderPopupWindow.dismiss();
            }

            // 重置PopupWindow高度
            int screenHeigh = getResources().getDisplayMetrics().heightPixels;
            mFolderPopupWindow.setHeight(Math.round(screenHeigh * 0.6f));
        }

        super.onConfigurationChanged(newConfig);
    }

    /**
     * 获取GridView Item的宽度
     * @return
     */
    private int getItemImageWidth()
    {
        int cols = getNumColumns();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    /**
     * 根据屏幕宽度与密度计算GridView显示的列数
     * 最少为三列
     * @return
     */
    private int getNumColumns()
    {
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        return cols < 3 ? 3 : cols;
    }

    /*
    * Loader就是加载器，简单来说，Loader做了2件事：
    *       （1）在单独的线程中读取数据，不会阻塞UI线程(异步加载)
    *       （2）监视数据的更新
    *
    * LoaderManager就是加载器的管理器，
    * 一个LoaderManager可以管理一个或多个Loader,
    * 一个Activity或者Fragment只能有一个LoadManager。
    * LoaderManager管理Loader的初始化，重启和销毁操作。
    *
    *
    * onCreateLoader()相当于一个被观察者，
    * onLoadFinished()相当于一个观察者，
    * 只要被观察者的数据有改变，那么观察者就能得到通知，并进行相应的响应。
    *
    * */
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>()
    {
        /*
        *   MediaStore是android系统提供的一个多媒体数据库，专门用于存放多媒体信息的
        *   MediaStore.Files: 共享的文件,包括多媒体和非多媒体信息
        *   MediaStore.Audio: 存放音频信息
        *   MediaStore.Image: 存放图片信息
        *   MediaStore.Vedio: 存放视频信息
        * */
        //读取图片文件的参数
        private final String[] IMAGE_PROJECTION =
                {
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID
                };

        //实例化并返回一个新创建给指定ID的Loader对象，第一次创建时回调
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            // ---------根据图片设置参数新增验证条件
            StringBuilder selectionArgs = new StringBuilder();
            if (mImageConfig != null)
            {
                if (mImageConfig.minWidth != 0)
                {
                    selectionArgs.append(MediaStore.Images.Media.WIDTH + " >= " + mImageConfig.minWidth);
                }

                if (mImageConfig.minHeight != 0)
                {
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.HEIGHT + " >= " + mImageConfig.minHeight);
                }

                if (mImageConfig.minSize != 0f)
                {
                    selectionArgs.append("".equals(selectionArgs.toString()) ? "" : " and ");
                    selectionArgs.append(MediaStore.Images.Media.SIZE + " >= " + mImageConfig.minSize);
                }

                if (mImageConfig.mimeType != null)
                {
                    selectionArgs.append(" and (");
                    for (int i = 0, len = mImageConfig.mimeType.length; i < len; i++)
                    {
                        if (i != 0)
                        {
                            selectionArgs.append(" or ");
                        }
                        selectionArgs.append(MediaStore.Images.Media.MIME_TYPE + " = '" + mImageConfig.mimeType[i] + "'");
                    }
                    selectionArgs.append(")");
                }
            }

            if (id == LOADER_ALL)           //全部加载
            {
                /*
                *   CursorLoader构造方法的参数说明
                *
                *   context ： 上下文
                *   uri : 要访问数据库的 uri地址
                *   projection ： 对应于数据库语句里的某列， 如果只需要访问某几列， 则传入这几列的名字即可，
                *                 如果不传， 则默认访问全部数据。例如 学生对象， 有：name，number，age，sex 等，
                *                 如果只需要查询姓名和学号， 则传入[“name”，“number”]即可。
                *   selection ：一些特殊的筛选条件，比如要求年龄大于10， 则传入 “age > ?”
                *   selectionArgs: 传入具体的参数， 会替换上述 selection中的“?”
                *   sortOrder： 排序规则， 可以为空
                *
                *   （可以理解为对数据库操作，增删改查）
                *
                * */
                CursorLoader cursorLoader = new CursorLoader(mContext,
                                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                            IMAGE_PROJECTION,
                                                            selectionArgs.toString(),
                                                            null,
                                                            IMAGE_PROJECTION[2] + " DESC");

                return cursorLoader;
            }
            else if (id == LOADER_CATEGORY) //按类别加载
            {
                String selectionStr = selectionArgs.toString();

                if (!selectionStr.equals(""))
                {
                    selectionStr += " and" + selectionStr;
                }

                CursorLoader cursorLoader = new CursorLoader(mContext,
                                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                            IMAGE_PROJECTION,
                                                            IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'" + selectionStr,
                                                            null,
                                                            IMAGE_PROJECTION[2] + " DESC");

                return cursorLoader;
            }

            return null;
        }

        //数据load完成之后回调此方法
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            if (data != null)
            {
                List<Image> images = new ArrayList<>();

                int count = data.getCount();    //所有照片
                if (count > 0)
                {
                    data.moveToFirst();
                    do
                    {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        Image image = new Image(path, name, dateTime);

                        images.add(image);

                        //生成相册文件夹
                        if (!hasFolderGened)
                        {

                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();    // 获取文件夹

                            //生成Folder相册实例
                            Folder folder = new Folder();
                            folder.folderName = folderFile.getName();
                            folder.folderPath = folderFile.getAbsolutePath();
                            folder.folderCover = image;

                            if (!mResultFoldersList.contains(folder))    // 若相册文件夹list中不包含此Folder
                            {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);                       //加入首张Image
                                folder.folserImagesList = imageList;

                                mResultFoldersList.add(folder); //list中添加此Folder
                            }
                            else    //若已存在此Folder，则更新Folder里的数据
                            {
                                Folder f = mResultFoldersList.get(mResultFoldersList.indexOf(folder));
                                f.folserImagesList.add(image);
                            }
                        }

                    } while (data.moveToNext());    //下一张照片

                    mFolderAdapter.setData(mResultFoldersList); //加载相册列表数据
                    hasFolderGened = true;

                    mImageAdapter.setData(images);  //默认展示所有图片

                    // 设定默认选择
                    if (imagePathList != null && imagePathList.size() > 0)
                    {
                        mImageAdapter.setDefaultSelected(imagePathList);
                    }
                }
            }
        }

        //当创建好的Loader被reset时调用此方法，重新清楚绑定好的数据，重新加载数据
        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {

        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }
}

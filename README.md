# PicSelect_SOL
图片选择器

>此工具是仿照微信发布朋友圈中的图片选择功能

> 可拍照或选择本地相册

> 预览本地照片

> 预览网络图片（保存本地、分享）


**效果展示**

![1.gif](./img/1.gif)


## 使用方式
### Step 1. Add the JitPack repository to your build file
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
### Step 2. Add the dependency
```
dependencies {
        implementation 'com.github.SeedsOfLove:PicSelect_SOL:1.0.3'
	}
```

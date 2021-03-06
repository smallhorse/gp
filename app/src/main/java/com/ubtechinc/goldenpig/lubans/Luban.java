package com.ubtechinc.goldenpig.lubans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtechinc.goldenpig.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.ubtechinc.goldenpig.lubans.Preconditions.checkNotNull;

public class Luban {

    public static final int FIRST_GEAR = 1;

    public static final int THIRD_GEAR = 3;

    private static final String TAG = "Luban";

    public static String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static volatile Luban INSTANCE;

    private final File mCacheDir;

    private OnCompressListener compressListener;

    private File mFile;

    private int gear = THIRD_GEAR;

    Luban(File cacheDir) {
        mCacheDir = cacheDir;
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context A context.
     * @see #getPhotoCacheDir(Context, String)
     */
    public static File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, Luban.DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store
     * retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getPhotoCacheDir(Context)
     */
    public static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    public static Luban get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Luban(Luban.getPhotoCacheDir(context));
        }
        return INSTANCE;
    }

    public Luban launch() {
        checkNotNull(mFile,
                "the image file cannot be null, please call .load() before this method!");

        if (compressListener != null) {
            compressListener.onStart();
        }

        if (gear == Luban.FIRST_GEAR) {
            Observable.just(mFile).map(new Function<File, File>() {
                @Override
                public File apply(File file) throws Exception {
                    return firstCompress(file);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (compressListener != null) {
                                compressListener.onError(throwable);
                            }
                        }
                    })
                    .onErrorResumeNext(Observable.<File>empty())
                    /*.filter(new Function<File, Boolean>() {
                        @Override
                        public Boolean apply(File file) throws Exception {
                            return file != null;
                        }
                    })*/
                    .filter(new Predicate<File>() {
                        @Override
                        public boolean test(File file) throws Exception {
                            return file != null;
                        }
                    })
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) throws Exception {
                            if (compressListener != null) {
                                compressListener.onSuccess(file);
                            }
                        }
                    });
        } else if (gear == Luban.THIRD_GEAR) {
            Observable.just("").map(new Function<String, File>() {
                @Override
                public File apply(String s) throws Exception {
                    return thirdCompress(mFile.getAbsolutePath());
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (compressListener != null) {
                                compressListener.onError(throwable);
                            }
                        }
                    })
                    .onErrorResumeNext(Observable.<File>empty())
                    /*.filter(new Function<File, Boolean>() {
                        @Override
                        public Boolean apply(File file) throws Exception {
                            return file != null;
                        }
                    })*/
                    .filter(new Predicate<File>() {
                        @Override
                        public boolean test(File file) throws Exception {
                            return file != null;
                        }
                    })
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void accept(File file) throws Exception {
                            if (compressListener != null) {
                                compressListener.onSuccess(file);
                            }
                        }
                    });
        }

        return this;
    }

    public Luban load(File file) {
        mFile = file;
        return this;
    }

    public Luban setCompressListener(OnCompressListener listener) {
        compressListener = listener;
        return this;
    }

    public Luban putGear(int gear) {
        this.gear = gear;
        return this;
    }

    public Observable<File> asObservable() {
        if (gear == FIRST_GEAR) {
            //return Observable.just(firstCompress(mFile));
            return Observable.just(mFile).map(new Function<File, File>() {
                @Override
                public File apply(File file) throws Exception {
                    return firstCompress(file);
                }
            });
        } else if (gear == THIRD_GEAR) {
            return Observable.just(mFile).map(new Function<File, File>() {
                @Override
                public File apply(File file) throws Exception {
                    return thirdCompress(file.getAbsolutePath());
                }
            });
            //return Observable.just(thirdCompress(mFile.getAbsolutePath()));
        } else {
            return Observable.empty();
        }
    }

    public Observable<File> asObservableList(List<String> list) {
        if (gear == FIRST_GEAR) {
            //return Observable.just(firstCompress(mFile));
            return Observable.fromIterable(list).concatMap(new Function<String, ObservableSource<? extends File>>() {
                @Override
                public ObservableSource<? extends File> apply(String s) throws Exception {
                    return Observable.just(firstCompress(new File(s)));
                }
            });
        } else if (gear == THIRD_GEAR) {
            return Observable.just(mFile).map(new Function<File, File>() {
                @Override
                public File apply(File file) throws Exception {
                    return thirdCompress(file.getAbsolutePath());
                }
            });
            //return Observable.just(thirdCompress(mFile.getAbsolutePath()));
        } else {
            return Observable.empty();
        }

    }

//    public void aaa(List<String> list) {
//        Observable.just(list.get(0)).concatMap(new Func1<String, Observable<File>>() {
//            @Override
//            public Observable<File> call(String s) {
//                return null;
//            }
//        }).subscribe(new Action1<File>() {
//            @Override
//            public void call(File file) {
//
//            }
//        });
//
//        Observable.from(list).concatMap(new Func1<String, Observable<String>>() {
//            @Override
//            public Observable<String> call(String s) {
//                return Observable.just(s + "q");
//            }
//        }).subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//
//            }
//        });
//    }

    private File thirdCompress(@NonNull String filePath) {
        ViewRootImpl();
        String thumb = mCacheDir.getAbsolutePath() + "/" + System.currentTimeMillis();

        double size;

        int angle = getImageSpinAngle(filePath);
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;

        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;

        double scale = ((double) width / height);

        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                size = (width * height) / Math.pow(1664, 2) * 150;
                size = size < 60 ? 60 : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                size = size < 60 ? 60 : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 200;
            size = size < 100 ? 100 : size;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            size = size < 100 ? 100 : size;
        }

        return compress(filePath, thumb, thumbW, thumbH, angle, (long) size);
    }

    private File firstCompress(@NonNull File file) {
        int minSize = 300;//60
        int longSide = 1920;//720
        int shortSide = 1440;//1280

        String filePath = file.getAbsolutePath();
        String thumbFilePath = mCacheDir.getAbsolutePath() + "/" + System.currentTimeMillis();
        long size = 300;
        long maxSize = file.length() / 5 / 1024 > 500 ? file.length() / 5 / 1024
                : 500;//kB

        int angle = getImageSpinAngle(filePath);
        int[] imgSize = getImageSize(filePath);
        int width = 0, height = 0;
        if (imgSize[0] <= imgSize[1]) {//竖图
            double scale = (double) imgSize[0] / (double) imgSize[1];
            if (scale <= 1.0 && scale > 0.5625) {//短竖图
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = minSize;
            } else if (scale <= 0.5625) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = maxSize;
            }
        } else {
            double scale = (double) imgSize[1] / (double) imgSize[0];
            if (scale <= 1.0 && scale > 0.5625) {//短横图
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = minSize;
            } else if (scale <= 0.5625) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = maxSize;
            }
        }

        return compress(filePath, thumbFilePath, width, height, angle, size);
    }

    /**
     * obtain the image's width and height
     *
     * @param imagePath the path of image
     */
    public int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }

    /**
     * obtain the thumbnail that specify the size
     *
     * @param imagePath the target image path
     * @param width     the width of thumbnail
     * @param height    the height of thumbnail
     * @return {@link Bitmap}
     */
    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;

            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * obtain the image rotation angle
     *
     * @param path path of target image
     */
    private int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 指定参数压缩图片
     * create the thumbnail with the true rotate angle
     *
     * @param largeImagePath the big image path
     * @param thumbFilePath  the thumbnail path
     * @param width          width of thumbnail
     * @param height         height of thumbnail
     * @param angle          rotation angle of thumbnail
     * @param size           the file size of image
     */
    private File compress(String largeImagePath, String thumbFilePath, int width, int height,
                          int angle, long size) {
        Bitmap thbBitmap = compress(largeImagePath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);

        return saveImage(thumbFilePath, thbBitmap, size);
    }

    /**
     * 旋转图片
     * rotate the image with specified angle
     *
     * @param angle  the angle will be rotating 旋转的角度
     * @param bitmap target image               目标图片
     */
    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap
                .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 保存图片到指定路径
     * Save image with specified size
     *
     * @param filePath the image file save path 储存路径
     * @param bitmap   the image what be save   目标图片
     * @param size     the file size of image   期望大小
     */
    private File saveImage(String filePath, Bitmap bitmap, long size) {
        checkNotNull(bitmap, TAG + "bitmap cannot be null");

        //File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        File result = new File(ImageUtils.getCameraPath(),
                ImageUtils.generateFileName("cut.jpg"));
        /*if (!result.exists() && !result.mkdirs()) {
            return null;
        }*/
        if (result.exists()) {
            result.delete();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);

        //int i = (int) (size / stream.toByteArray().length / 1024.0 * 100);

        while (stream.toByteArray().length / 1024 > size) {
            stream.reset();
            options -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
            if (options < 60) {
                break;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(result);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void ViewRootImpl() {
        Log.d("hdf", "Luban:" + android.os.Process.myPid() + " Thread: " + android.os.Process
                .myTid() + " name " + Thread.currentThread().getName());
    }
}
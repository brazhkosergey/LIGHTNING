package ui.camera;

import entity.MainVideoCreator;
import org.apache.log4j.Logger;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher {
    private static Logger log = Logger.getLogger(VideoCatcher.class);

    private int whitePercent = -1;
    private int percentDiffWhite = 100;
    private int numberGRB;
    private boolean programLightCatchWork;

    private int fps;
    private int countTimesToHaveNotBytesToRead;
    private int maxWidth;
    private int maxHeight;
    private boolean isFullSize;

//    private int countDoNotShowImages;
//    private int settingCountDoNotShowImages;
    //    private Map<Long, byte[]> bufferBytes;
//    private Map<Long, byte[]> mapBytes;
//    private Map<Integer, Boolean> eventsFramesNumber;
//    private Deque<Long> timeDeque;


    private Deque<byte[]> imageDeque;
    private VideoCreator videoCreator;

    private URL url;
    private HttpURLConnection connection = null;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private ByteArrayOutputStream temporaryStream = null;

    private CameraPanel panel;

    private boolean restart;
    private boolean catchVideo;
    private boolean changeURL;

    private Thread FpsCountThread;
    private Thread UpdateDataThread;
    private Thread MainThread;
    private boolean showImage = true;


//    private int fpsNotZero;

//    private int totalSecondAlreadySaved;
//    private int stopSaveVideoInt;
//    private int sizeVideoSecond;
//    private int partVideoSize;


    //    private boolean startSaveVideo;
//    private boolean saveVideoOnePartOfVideo;
//    private boolean delBytes;
//    private int countPartsOfVideo;

//    private List<Integer> fpsList;

//    private boolean saveBuffToFile;

    public VideoCatcher(CameraPanel panel, VideoCreator videoCreatorForBouth) {
        log.info("Создаем наблюдатель для камеры номер " + panel.getCameraNumber());
//        fpsList = new ArrayList<>();
        FpsCountThread = new Thread(() -> {
            while (true) {
                if (catchVideo) {
//                    totalSecondAlreadySaved++;
//                    if (!startSaveVideo) {
//                        if (totalSecondAlreadySaved >= sizeVideoSecond) {
//                            totalSecondAlreadySaved = sizeVideoSecond;
//                        }
//                    } else {
//                        stopSaveVideoInt++;
//                        partVideoSize++;
//                    }
                    if (fps != 0) {
//                        fpsList.add(fps);
//                        int a=0;
//                        for(Integer integer:fpsList){
//                            a= a+integer;
//                        }
//                        fpsNotZero = a/fpsList.size();
//
//                        if(fpsList.size()>50){
//                            fpsList.remove(0);
//                        }

//                        panel.getTitle().setTitle(stopSaveVideoInt +":" + totalSecondAlreadySaved +
//                                ":FPS = " + fpsNotZero + ". WHITE: " + whitePercent);
                        panel.getTitle().setTitle(
//                                videoCreator.getStopSaveVideoInt() + ":" + videoCreator.getTotalSecondAlreadySaved() +
                                "FPS = " + fps + ". WHITE: " + whitePercent);
                        panel.repaint();
                        fps = 0;
                    } else {
                        if (!restart) {
                            countTimesToHaveNotBytesToRead++;
                            if (countTimesToHaveNotBytesToRead > 10) {
                                restart = true;
                                bufferedInputStream = null;
                                countTimesToHaveNotBytesToRead = 0;
                                createInputStream();
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        UpdateDataThread = new Thread(() -> {
            int checkData = 0;
            while (true) {
                if (catchVideo) {
                    if (checkData == 10) {
                        numberGRB = MainFrame.getColorRGBNumber();
                        percentDiffWhite = MainFrame.getPercentDiffWhite();
//                            settingCountDoNotShowImages = MainFrame.getDoNotShowImages();
//                            showImage = countDoNotShowImages >= settingCountDoNotShowImages;
//                            programLightCatchWork = MainFrame.isProgramLightCatchWork();

//                            int size = timeDeque.size();

//                            if (!startSaveVideo) {
//                                if (MainVideoCreator.isSaveVideo()) {
//                                    int frameCount = timeDeque.size();
//                                    eventsFramesNumber.put(frameCount, MainVideoCreator.isProgramingLightCatch());
//                                    startSaveVideo = true;
//                                    log.info("Начинаем сохранять поток. Камера номер " + panel.getCameraNumber());
//                                }
//
//                                sizeVideoSecond = MainFrame.getTimeToSave();
//                                delBytes = size > sizeVideoSecond * fpsNotZero;
//                                if (delBytes) {
//                                    panel.getTitle().setTitleColor(new Color(46, 139, 87));
//                                } else {
//                                    panel.getTitle().setTitleColor(Color.red);
//                                }
//
//                                while (true) {
//                                    if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
//                                        Long aLong = timeDeque.pollLast();
//                                        if(aLong!=null){
//                                            bufferBytes.remove(aLong);
//                                        }
//                                    } else {
//                                        break;
//                                    }
//                                }
////                                if(totalSecondAlreadySaved>20){
////
////                                    for (int i = 0; i < size; i++) {
////                                        Long timeLong = timeDeque.pollLast();
////                                        if(timeLong!=null){
////                                            mapBytes.put(timeLong, bufferBytes.get(timeLong));
////                                            bufferBytes.remove(timeLong);
////                                        }
////                                    }
////
////                                    int num;
////                                    if (panel.getCameraNumber() % 2 == 0) {
////                                        num = 2;
////                                    } else {
////                                        num = 1;
////                                    }
////
////                                    System.out.println("Сохраняем файла размером кадров - " + mapBytes.size() +
////                                            ". Number - " + panel.getCameraNumber());
////
////                                    videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber, 0);
////                                    mapBytes = new HashMap<>();
////
////                                    totalSecondAlreadySaved = 0;
////                                }
//                            }
//                            if (startSaveVideo) {
//                                if(!saveBuffToFile){
//                                    Thread.sleep(3000);
//                                    size = timeDeque.size();
//                                    for (int i = 0; i < size; i++) {
//                                        Long timeLong = timeDeque.pollLast();
//                                        if(timeLong!=null){
//                                            mapBytes.put(timeLong, bufferBytes.get(timeLong));
//                                            bufferBytes.remove(timeLong);
//                                        }
//                                    }
//
//                                    int num;
//                                    if (panel.getCameraNumber() % 2 == 0) {
//                                        num = 2;
//                                    } else {
//                                        num = 1;
//                                    }
//
//                                    System.out.println("Сохраняем файла размером кадров - " + mapBytes.size() +
//                                            ". Number - " + panel.getCameraNumber());
//
//                                    videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber, countPartsOfVideo);
//                                    mapBytes = new HashMap<>();
//                                    saveBuffToFile= true;
//                                }
//
//                                int k = 1;
//                                if(stopSaveVideoInt >= sizeVideoSecond){
//                                    k=2;
//                                }
//
//                                if (partVideoSize>20||
////                                        saveVideoOnePartOfVideo ||
//                                        stopSaveVideoInt >= sizeVideoSecond) {
//
//                                    for (int i = 0; i < size; i++) {
//                                        Long timeLong = timeDeque.pollLast();
//                                        if(timeLong!=null){
//                                            mapBytes.put(timeLong, bufferBytes.get(timeLong));
//                                            bufferBytes.remove(timeLong);
//                                        }
//                                    }
//
//                                    int num;
//                                    if (panel.getCameraNumber() % 2 == 0) {
//                                        num = 2;
//                                    } else {
//                                        num = 1;
//                                    }
//
//                                    System.out.println("Сохраняем файл размером кадров - " + mapBytes.size() +
//                                            ". Number - " + panel.getCameraNumber());
//
//                                    videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber, k);
////                                    videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber, ++countPartsOfVideo);
//                                    mapBytes = new HashMap<>();
////                                    eventsFramesNumber = new HashMap<>();
////                                    totalSecondAlreadySaved = 0;
//                                    saveVideoOnePartOfVideo = false;
//                                    partVideoSize = 0;
//                                    if (stopSaveVideoInt >= sizeVideoSecond) {
//                                        log.info("Закончили сохранять поток. Камера номер - "+panel.getCameraNumber());
//                                        MainVideoCreator.stopCatchVideo();
//
//                                        eventsFramesNumber = new HashMap<>();
//
//
//                                        countPartsOfVideo = 0;
//                                        stopSaveVideoInt = 0;
//                                        startSaveVideo = false;
//                                        saveBuffToFile = false;
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        checkData = 0;
                    } else {
//                                Long first = timeDeque.peekFirst();

//                                if (first != null) {
//                                    if (bufferBytes != null && bufferBytes.containsKey(first)) {
//                                        bytes = bufferBytes.get(first);
//                                    }
//                                }

                        byte[] bytes = null;

                        if (imageDeque.size() > 0) {
                            bytes = imageDeque.pollLast();
                            if (bytes != null) {
                                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                                try {
                                    BufferedImage image = ImageIO.read(inputStream);
                                    inputStream.close();
                                    panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
                                    panel.repaint();
                                } catch (Exception ignored) {
                                }
                                showImage = true;
                            }
                        }


                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        checkData++;
                    }
                } else {
                    panel.getTitle().setTitle("Камера вимкнена");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        UpdateDataThread.setPriority(Thread.MIN_PRIORITY);

        MainThread = new Thread(() -> {
            UpdateDataThread.start();
            FpsCountThread.start();
            log.info("Запускаем наблюдатель для камеры номер " + panel.getCameraNumber());

            int x = 0;
            int t;

            while (true) {
                while (catchVideo) {
                    try {
                        if (bufferedInputStream == null) {
                            if (temporaryStream != null) {
                                temporaryStream.close();
                            }
                            temporaryStream = new ByteArrayOutputStream(35535);
                            createInputStream();
                        } else {
                            t = x;
                            try {
                                x = bufferedInputStream.read();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            temporaryStream.write(x);
                            if (x == 216 && t == 255) {// начало изображения
                                temporaryStream.reset();

                                temporaryStream.write(t);
                                temporaryStream.write(x);
                            } else if (x == 217 && t == 255) {//конец изображения
                                long l = System.currentTimeMillis();
                                byte[] bytes = temporaryStream.toByteArray();

                                if(showImage){
                                    imageDeque.addFirst(bytes);
                                    showImage = false;
                                }

                                videoCreator.addImageBytes(l, bytes);
                                fps++;
//                                timeDeque.addFirst(l);
//                                bufferBytes.put(l, temporaryStream.toByteArray());//TODO java.lang.OutOfMemoryError: Java heap space
//
//
//                                if (!startSaveVideo) {
//                                    if (delBytes) {
//                                        Long aLong = timeDeque.pollLast();
//                                        if(aLong!=null){
//                                            bufferBytes.remove(aLong);
//                                        }
//                                    }
//                                }
//                                else {
//                                    if (stopSaveVideo) {
////                                        MainVideoCreator.stopCatchVideo();
////                                        int size = timeDeque.size();
////                                        for (int i = 0; i < size; i++) {
////                                            Long timeLong = timeDeque.pollLast();
////                                            mapBytes.put(timeLong, bufferBytes.get(timeLong));
////                                            bufferBytes.remove(timeLong);
////                                        }
////
////                                        int num;
////                                        if (panel.getCameraNumber() % 2 == 0) {
////                                            num = 2;
////                                        } else {
////                                            num = 1;
////                                        }
////
////                                        System.out.println("Сохраняем файла размером кадров - " + mapBytes.size());
////                                        System.out.println("Новый размер буффера: " + bufferBytes.size());
////                                        System.out.println("Размер очереди: " + timeDeque.size());
////
////                                        Thread saveBytesThread = new Thread(()->{
////                                            videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber, ++countPartsOfVideo);
////                                        });
////
//                                        saveBytesThread.setName("SaveBytesThread. Camera "+panel.getCameraNumber());
//                                        saveBytesThread.start();
//                                        mapBytes = new HashMap<>();
//                                        eventsFramesNumber = new HashMap<>();
//                                        stopSaveVideoInt = 0;
//                                        totalSecondAlreadySaved = 0;
//                                        countPartsOfVideo = 0;
//                                        stopSaveVideo = false;
//                                startSaveVideo = false;
//                                    }
//                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error error) {
                        log.error(error.getLocalizedMessage());
                        error.printStackTrace();
                    }
                }

                if (!catchVideo) {
                    try {
                        if (temporaryStream != null) {
                            temporaryStream.close();
                            temporaryStream = null;
                        }

                        if (bufferedInputStream != null) {
                            bufferedInputStream.close();
                            bufferedInputStream = null;
                        }

                        if (inputStream != null) {
                            inputStream.close();
                            inputStream = null;
                        }

                        if (connection != null) {
                            connection.disconnect();
                            connection = null;
                        }
//                        mapBytes = null;
//                        bufferBytes = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (changeURL) {
                    catchVideo = true;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        MainThread.setPriority(Thread.MAX_PRIORITY);
//        timeDeque = new ConcurrentLinkedDeque<>();
//        eventsFramesNumber = new HashMap<>();
        imageDeque = new ConcurrentLinkedDeque<>();
        this.panel = panel;
        setWidthAndHeight(270, 260);
        panel.repaint();
        this.videoCreator = videoCreatorForBouth;
        videoCreator.addVideoCatcher(this);
    }

    public void startCatchVideo(URL urlMainStream) {
        if (urlMainStream != null) {
            if (this.url != null) {
                changeURL = true;
                catchVideo = false;
            } else {
                catchVideo = true;
                panel.startShowVideo();
            }
            this.url = urlMainStream;
        } else {
            stopCatchVideo();
        }
    }

    private void createInputStream() {
        if (url != null) {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

                connection = (HttpURLConnection) url.openConnection();
                inputStream = connection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);
                restart = false;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (!restart&&catchVideo) {
                    createInputStream();
                } else {
                    panel.getTitle().setTitle("Відновлюемо зв'язок");
                    panel.repaint();
                }
            }
        } else {
            System.out.println("URL = NULL");
        }
    }

    public void start() {
        MainThread.setName("Save Stream Thread. Camera " + panel.getCameraNumber());
        UpdateDataThread.setName("Update Data Thread. Camera " + panel.getCameraNumber());
        FpsCountThread.setName("FPS CountThread. Camera " + panel.getCameraNumber());

        MainThread.start();
    }

    private BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = maxWidth - bi.getWidth();
        int hh = maxHeight - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = maxWidth;
                size = bi.getWidth();
            } else {
                max = maxHeight;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BICUBIC);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        }

        if (bi2 != null) {

            if (programLightCatchWork && maxWidth < 280) {
                int[] rgb1 = bi2.getRGB(0, 0, bi2.getWidth(), bi2.getHeight(), null, 0, 1024);
//                int[] rgb1 = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, 2048);
                int countWhite = 0;
                int allPixels = rgb1.length;

                int k = 10;

                for (int i = 0; i < allPixels; i = i + k) {
                    if (rgb1[i] > numberGRB && rgb1[i] < -1) {
                        countWhite++;
                    }
                }

                if (countWhite != 0) {
                    countWhite = countWhite * k;
                }

                double percent = (double) countWhite / allPixels;
                int percentInt = (int) (percent * 100);

                if (whitePercent != -1) {
                    int differentWhitePercent = Math.abs(percentInt - whitePercent);
                    if (differentWhitePercent > percentDiffWhite) {
                        MainVideoCreator.startCatchVideo(true);
                        whitePercent = -1;
                    } else {
                        if (percentInt != whitePercent) {
                            whitePercent = percentInt;
                        }
                    }
                } else {
                    whitePercent = percentInt;
                }
            }
            return bi2;
        } else {
            return bi;
        }
    }

    public void setBorderColor(Color color){
        panel.getTitle().setTitleColor(color);
    }
    VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public void setWidthAndHeight(int width, int height) {
        isFullSize = width > 270;
        panel.setWindowSize(width, height);
        panel.setPreferredSize(new Dimension(width + 5, height + 5));
        this.maxWidth = width;
        this.maxHeight = height;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public void stopCatchVideo() {
        panel.stopShowVideo();
        url = null;
        catchVideo = false;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }

    //    public void run() {
//        UpdateDataThread.start();
//        int x = 0;
//        int t1 = 0;
//        int t2 = 0;
//        int t3 = 0;
//
//        BufferedImage image;
//        while (true) {
//            while (catchVideo) {
//                try {
//                    if (bufferedInputStream == null) {
//                        if (temporaryStream != null) {
//                            temporaryStream.close();
//                        }
//                        temporaryStream = new ByteArrayOutputStream(65535);
//                        createInputStream();
//                    } else {
////                        t3 = t2;
////                        t2 = t1;
////                        t1 = x;
////                        ============
////                        t1 = x;
//                        try {
//                            x = bufferedInputStream.read();
////                        } catch (IOException e) {
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        temporaryStream.write(x);
//                        if (t3 == 255 &&t2 == 216 && t1 == 255 && x == 254 ) {// 255,216 + 255,254 начало изображения   - FF D8 FF FE  - 255 - 216 - - 255 - 254
////                        if (x == 216 && t1 == 255) {// 255,216 + 255,254 начало изображения   - FF D8 FF FE  - 255 - 216 - - 255 - 254
//                            temporaryStream.reset();
//
//                            temporaryStream.write(t3);
//                            temporaryStream.write(t2);
////                            ========================
//                            temporaryStream.write(t1);
//                            temporaryStream.write(x);
//                        } else if (x == 217 && t1 == 255) {//конец изображения  - FF D9
//                            byte[] imageBytes = temporaryStream.toByteArray();
//                            if (countDoNotShowImages >= settingCountDoNotShowImages) {
//                                System.out.println("Размер файла - " + imageBytes.length);
//                                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//                                try {
//                                    image = ImageIO.read(inputStream);
//                                    //javax.imageio.IIOException: Bogus Huffman table definition
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readImageHeader(Native Method)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readNativeHeader(JPEGImageReader.java:620)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.checkTablesOnly(JPEGImageReader.java:347)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.gotoImage(JPEGImageReader.java:492)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readHeader(JPEGImageReader.java:613)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readInternal(JPEGImageReader.java:1070)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.read(JPEGImageReader.java:1050)
////                                        at javax.imageio.ImageIO.read(ImageIO.java:1448)
////                                        at javax.imageio.ImageIO.read(ImageIO.java:1352)
////                                        at ui.camera.VideoCatcher.run(VideoCatcher.java:237)
////                                        at java.lang.Thread.run(Thread.java:748)
//
//
////                                        javax.imageio.IIOException: Invalid JPEG file structure: missing SOS marker
////                                        Размер файла - 340
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readImageHeader(Native Method)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readNativeHeader(JPEGImageReader.java:620)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.checkTablesOnly(JPEGImageReader.java:347)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.gotoImage(JPEGImageReader.java:492)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readHeader(JPEGImageReader.java:613)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.readInternal(JPEGImageReader.java:1070)
////                                        at com.sun.imageio.plugins.jpeg.JPEGImageReader.read(JPEGImageReader.java:1050)
////                                        at javax.imageio.ImageIO.read(ImageIO.java:1448)
////                                        at javax.imageio.ImageIO.read(ImageIO.java:1352)
////                                        at ui.camera.VideoCatcher.run(VideoCatcher.java:237)
////                                        at java.lang.Thread.run(Thread.java:748)
//
//                                    inputStream.close();
//                                    if (image != null) {
//                                        panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
////                                panel.setBufferedImage(processImageNew(image, maxWidth, maxHeight));
//                                        panel.repaint();
//                                    }
//                                } catch (Exception e) {
//                                    System.out.println("Размер файла - " + imageBytes.length);
//                                    e.printStackTrace();
//                                }
//                                countDoNotShowImages = 0;
//                            } else {
//                                countDoNotShowImages++;
//                            }
//
//                            long l = System.currentTimeMillis();
//                            timeDeque.addFirst(l);
//                            bufferBytes.put(l, imageBytes);
//                            fps++;
//
//                            if (!startSaveVideo) {
//                                if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
//                                    bufferBytes.remove(timeDeque.pollLast());
//                                }
//                            }
//
//                            if (stopSaveVideo) {
//                                MainVideoCreator.stopCatchVideo();
//                                int size = timeDeque.size();
//                                for (int i = 0; i < size; i++) {
//                                    Long timeLong = timeDeque.pollLast();
//                                    mapBytes.put(timeLong, bufferBytes.get(timeLong));
//                                    bufferBytes.remove(timeLong);
//                                }
//
//                                int num;
//                                if (panel.getCameraNumber() % 2 == 0) {
//                                    num = 2;
//                                } else {
//                                    num = 1;
//                                }
//
//                                videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber);
//                                mapBytes = new HashMap<>();
//                                eventsFramesNumber = new HashMap<>();
//                                stopSaveVideoInt = 0;
//                                stopSaveVideo = false;
//                                startSaveVideo = false;
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (!catchVideo) {
//                try {
//                    if (temporaryStream != null) {
//                        temporaryStream.close();
//                        temporaryStream = null;
//                    }
//
//                    if (bufferedInputStream != null) {
//                        bufferedInputStream.close();
//                        bufferedInputStream = null;
//                    }
//
//                    if (inputStream != null) {
//                        inputStream.close();
//                        inputStream = null;
//                    }
//
//                    if (connection != null) {
//                        connection.getInputStream().close();
//                        connection.disconnect();
//                        connection = null;
//                    }
//                    mapBytes = null;
//                    bufferBytes = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (changeURL) {
//                catchVideo = true;
//            }
//
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}

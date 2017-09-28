package ui.camera;

import entity.MainVideoCreator;
import ui.main.MainFrame;
import ui.setting.Setting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher implements Runnable {

    private int whitePercent = -1;
    private int numberGRB;
    private boolean programLightCatchWork;

    private int fps;
    private int countTimesToHaveNotBytesToRead;
    private int maxWidth;
    private int maxHeight;
    private boolean isFullSize;

    private int countDoNotShowImages;
    private int settingCountDoNotShowImages;


    private Map<Long, byte[]> bufferBytes;
    private Map<Long, byte[]> mapBytes;
    private Map<Integer, Boolean> eventsFramesNumber;
    private Deque<Long> timeDeque;

    private VideoCreator videoCreator;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private ByteArrayOutputStream temporaryStream = null;
    private CameraPanel panel;
    private URL url;
    private HttpURLConnection connection = null;

    private boolean restart;
    private boolean catchVideo;
    private boolean changeURL;

    private Thread fpsThread;
    private int fpsNotZero;

    private int totalSecondAlreadySaved;
    private int stopSaveVideoInt;
    private int sizeVideoSecond;

    private boolean stopSaveVideo;
    private boolean startSaveVideo;
    private boolean saveVideoOnePartOfVideo;
    private int countPartsOfVideo;

    public VideoCatcher(CameraPanel panel, VideoCreator videoCreator) {
        startSaveVideo = false;

        fpsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (catchVideo) {
                        numberGRB = MainFrame.getColorRGBNumber();
                        settingCountDoNotShowImages = MainFrame.getDoNotShowImages();
                        programLightCatchWork = MainFrame.isProgramLightCatchWork();

                        if (fps != 0) {
                            fpsNotZero = fps;
                            countTimesToHaveNotBytesToRead = 0;

                            panel.getTitle().setTitle("FPS = " + fpsNotZero + ". WHITE: " + whitePercent);
                            panel.repaint();
                            fps = 0;
                        } else {
                            if (!restart) {
                                countTimesToHaveNotBytesToRead++;
                                    System.out.println("===============================");
                                    System.out.println(countTimesToHaveNotBytesToRead);
                                    if (countTimesToHaveNotBytesToRead > 10) {
                                        restart = true;
                                        bufferedInputStream = null;
                                        countTimesToHaveNotBytesToRead = 0;
                                        createInputStream();
                                    }

                            }
                        }

                        if (!startSaveVideo) {
                            if (MainVideoCreator.isSaveVideo()) {
                                int frameCount = timeDeque.size();
                                eventsFramesNumber.put(frameCount, MainVideoCreator.isProgramingLightCatch());
                                startSaveVideo = true;
                            }

                            if (timeDeque.size() + fpsNotZero > sizeVideoSecond * fpsNotZero) {
                                panel.getTitle().setTitleColor(new Color(46, 139, 87));
                            } else {
                                panel.getTitle().setTitleColor(Color.red);
                            }

                            if (sizeVideoSecond != MainFrame.getTimeToSave()) {
                                sizeVideoSecond = MainFrame.getTimeToSave();
                                while (true) {
                                    if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
                                        bufferBytes.remove(timeDeque.pollLast());
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }

                        if (startSaveVideo) {
                            if(saveVideoOnePartOfVideo){
                                Thread.sleep(1000);
                                stopSaveVideoInt++;
                                int size = timeDeque.size();
                                System.out.println("Размер буффера: "+bufferBytes.size());
                                for (int i = 0; i < size; i++) {
                                    Long timeLong = timeDeque.pollLast();
                                    mapBytes.put(timeLong, bufferBytes.get(timeLong));
                                    bufferBytes.remove(timeLong);
                                }

                                int num;
                                if (panel.getCameraNumber() % 2 == 0) {
                                    num = 2;
                                } else {
                                    num = 1;
                                }

                                System.out.println("Сохраняем файла размером кадров - "+mapBytes.size());
                                System.out.println("Новый размер буффера: "+bufferBytes.size());
                                System.out.println("Размер очереди: " + timeDeque.size());

                                videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber,++countPartsOfVideo);
                                mapBytes = new HashMap<>();
                                eventsFramesNumber = new HashMap<>();
                                saveVideoOnePartOfVideo = false;
                                totalSecondAlreadySaved =0;
                            }

                            totalSecondAlreadySaved++;
                            stopSaveVideoInt++;
                            if (stopSaveVideoInt == sizeVideoSecond) {
                                stopSaveVideo = true;
                            }
                        }
                    } else {
                        panel.getTitle().setTitle("Камера вимкнена");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        timeDeque = new ConcurrentLinkedDeque<>();
        eventsFramesNumber = new HashMap<>();
        this.videoCreator = videoCreator;
        this.panel = panel;
        setWidthAndHeight(270, 260);
        panel.repaint();
    }

    public void startCatchVideo(URL url) {
        if (this.url != null) {
            changeURL = true;
            this.url = url;
            catchVideo = false;
        } else {
            this.url = url;
            catchVideo = true;
            panel.startShowVideo();
        }
    }

    private void createInputStream() {
        System.out.println("Пробуем создать BufferedInputStream");
        if (url != null) {
            try {
                if(connection!=null){
                    connection.disconnect();
                }

                if(bufferedInputStream!=null){
                    bufferedInputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                if (!restart) {
                    bufferBytes = new HashMap<>();
                    mapBytes = new HashMap<>();
                }

                System.out.println("пробуем открыть соединение");
                connection = (HttpURLConnection) url.openConnection();
                System.out.println("Ok");
                System.out.println("пробуем открыть InputStream");
                inputStream = connection.getInputStream();
                System.out.println("ok");
                System.out.println("пробуем открыть bufferedInputStream");
                bufferedInputStream = new BufferedInputStream(inputStream);
                System.out.println("ok");
                restart = false;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if(!restart){
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

    @Override
    public void run() {
        fpsThread.start();
        int x = 0;
        int t = 0;

        BufferedImage image;
        while (true) {
            while (catchVideo) {
                try {
                    if (bufferedInputStream == null) {
                        if (temporaryStream != null) {
                            temporaryStream.close();
                        }
                        temporaryStream = new ByteArrayOutputStream(65535);
                        createInputStream();
                    } else {
                        t = x;
                        try {
                            x = bufferedInputStream.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        temporaryStream.write(x);
                        if (x == 216 && t == 255) {// начало изображения
                            temporaryStream.reset();

                            temporaryStream.write(t);
                            temporaryStream.write(x);
                        } else if (x == 217 && t == 255) {//конец изображения
                            byte[] imageBytes = temporaryStream.toByteArray();
                            long l = System.currentTimeMillis();
                            timeDeque.addFirst(l);
                            bufferBytes.put(l, imageBytes);
                            fps++;

                            if (countDoNotShowImages >= settingCountDoNotShowImages) {
                                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                                try {
                                    image = ImageIO.read(inputStream);
                                    inputStream.close();
                                    panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
//                                panel.setBufferedImage(processImageNew(image, maxWidth, maxHeight));
                                    panel.repaint();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                countDoNotShowImages = 0;
                            } else {
                                countDoNotShowImages++;
                            }

                            if (!startSaveVideo) {
                                if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
                                    bufferBytes.remove(timeDeque.pollLast());
                                }
                            }

                            if (stopSaveVideo) {
                                MainVideoCreator.stopCatchVideo();
                                int size = timeDeque.size();
                                for (int i = 0; i < size; i++) {
                                    Long timeLong = timeDeque.pollLast();
                                    mapBytes.put(timeLong, bufferBytes.get(timeLong));
                                    bufferBytes.remove(timeLong);
                                }

                                int num;
                                if (panel.getCameraNumber() % 2 == 0) {
                                    num = 2;
                                } else {
                                    num = 1;
                                }

                                videoCreator.addMapByte(num, mapBytes, fpsNotZero, eventsFramesNumber,++countPartsOfVideo);
                                mapBytes = new HashMap<>();
                                eventsFramesNumber = new HashMap<>();
                                stopSaveVideoInt = 0;
                                totalSecondAlreadySaved = 0;
                                countPartsOfVideo = 0;
                                stopSaveVideo = false;
                                startSaveVideo = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                    mapBytes = null;
                    bufferBytes = null;
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
    }

    public void continueSaveVideo() {
        int frameCount = timeDeque.size();
        eventsFramesNumber.put(frameCount, MainVideoCreator.isProgramingLightCatch());
        stopSaveVideoInt = 0;
// TODO       if(totalSecondAlreadySaved>299){
        if(totalSecondAlreadySaved > 50){
            saveVideoOnePartOfVideo = true;
            System.out.println("Сохраняем часть видео " + saveVideoOnePartOfVideo);
        }
    }

    private BufferedImage processImageNew(BufferedImage bi, int maxWidth, int maxHeight) {
        if (bi.getWidth() < maxWidth || bi.getHeight() < maxHeight) {
            return bi;
        } else {
            BufferedImage smallImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = smallImage.createGraphics();
            Image scaled = bi.getScaledInstance(maxWidth,
                    maxHeight, Image.SCALE_SMOOTH);
            g.drawImage(scaled, 0, 0, null);
//            g.drawImage(bi, 0, 0, maxWidth, maxHeight, null);
            g.dispose();
            return smallImage;
        }
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
//                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BICUBIC);
//                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        }

        if (programLightCatchWork) {
            int[] rgb1 = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, 2048);
            int countWhite = 0;
            int allPixels = rgb1.length;

            int k = 30 - (settingCountDoNotShowImages * 2);

            if (k < 10) {
                k = 10;
            }

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
                if (differentWhitePercent > MainFrame.getPercentDiffWhite()) {
                    VideoCreator.startSaveVideoProgram();
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

        if (bi2 != null) {
            return bi2;
        } else {
            return bi;
        }
    }

    VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public void setWidthAndHeight(int width, int height) {
        if (width > 270) {
            isFullSize = true;
        } else {
            isFullSize = false;
        }

        panel.setWindowSize(width, height);
        panel.setPreferredSize(new Dimension(width + 5, height + 5));
        this.maxWidth = width;
        this.maxHeight = height;
    }

    public void stopCatchVideo() {
        panel.stopShowVideo();
        url = null;
        catchVideo = false;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }

    //    public void run() {
//        fpsThread.start();
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

package ui.camera;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher implements Runnable {
    int fps;
    int maxWidth;
    int maxHeight;
    boolean isFullSize;

    private Map<Long, byte[]> bufferBytes;
    private Map<Long, byte[]> mapBytes;
    private Deque<Long> timeDeque;

    private VideoCreator videoCreator;
    private BufferedInputStream inputStream;
    CameraPanel panel;
    private URL url;
    HttpURLConnection connection = null;

    boolean catchVideo;
    Thread fpsThread;
    int fpsNotZero;
    boolean mainCamera;

    int stopSaveVideoInt;
    int sizeVideoSecond;

    boolean stopSaveVideo;
    boolean startSaveVideo;
    boolean continueSaveVideo;

    public VideoCatcher(CameraPanel panel, VideoCreator videoCreator) {
        startSaveVideo = false;
        fpsThread = new Thread(() -> {
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (catchVideo) {
                    panel.getTitle().setTitle("FPS = " + fps);
                    if (fps != 0) {
                        fpsNotZero = fps;
                    }

                    fps = 0;

                    if (sizeVideoSecond != MainFrame.timeToSave) {
                        sizeVideoSecond = MainFrame.timeToSave;
                        while (true) {
                            if (timeDeque.size() > sizeVideoSecond * fpsNotZero) {
                                bufferBytes.remove(timeDeque.pollLast());
                            } else {
                                break;
                            }
                        }
                    }

                    if (!startSaveVideo) {
                        if (MainVideoCreator.isSaveVideo()) {
                            startSaveVideo = true;
                        }
                    }

                    if (startSaveVideo) {

                        if(continueSaveVideo){
                            if(!MainVideoCreator.isContinueSaveVideo()){
                                stopSaveVideo = true;
                            }
                            if(mainCamera){
                                MainFrame.showInformMassage("Продовжуемо",true);
                            }
                        } else {
                            if(MainVideoCreator.isContinueSaveVideo()){
                                continueSaveVideo = true;
                                System.out.println("Сохраняем видео дальше, еще одна молния.");
                            }

                            stopSaveVideoInt++;
                            if (stopSaveVideoInt == sizeVideoSecond) {
                                stopSaveVideo = true;
                                if(mainCamera){
                                    MainFrame.showInformMassage("Залишилось секунд "+(sizeVideoSecond-stopSaveVideoInt),true);
                                }
                            }
                        }
                    }
                }
            }
        });
        timeDeque = new ConcurrentLinkedDeque<>();
        bufferBytes = new HashMap<>();
        mapBytes = new HashMap<>();
        this.videoCreator = videoCreator;
        this.panel = panel;
        setWidthAndHeight(240, 220);
        panel.repaint();
    }

    private void createInputStream() throws IOException {
        MainFrame.getMainFrame().showMessage("Пробуем открыть соединение...");
        connection = (HttpURLConnection) url.openConnection();
        MainFrame.getMainFrame().showMessage("Пробуем открыть поток....");
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            inputStream = new BufferedInputStream(connection.getInputStream());
        }
        MainFrame.getMainFrame().showMessage("Все ок.");
    }

    @Override
    public void run() {
        fpsThread.start();
        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
        int x = 0;
        int t = 0;

        BufferedImage image;
        while (true) {
            while (catchVideo) {
                if (inputStream == null) {
                    try {
                        createInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    t = x;
                    try {
                        x = inputStream.read();
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
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                        try {
                            image = ImageIO.read(inputStream);
                            panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
                            fps++;
                            panel.repaint();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        long l = System.currentTimeMillis();


                        timeDeque.addFirst(l);
                        bufferBytes.put(l, imageBytes);


                        if (timeDeque.size() > sizeVideoSecond * fpsNotZero && !startSaveVideo) {
                            bufferBytes.remove(timeDeque.pollLast());
                        }

                        if (stopSaveVideo) {
                            System.out.println("Стоп сохранять видео.");
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
                            videoCreator.addMapByte(num, mapBytes, fpsNotZero);
                            mapBytes = new HashMap<>();
                            stopSaveVideoInt = 0;
                            stopSaveVideo = false;
                            startSaveVideo = false;
                            continueSaveVideo = false;
                        }
                    }
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
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
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        } else {
            return bi;
        }
        return bi2;
    }

    public VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public void setWidthAndHeight(int width, int height) {

        if (width > 246) {
            isFullSize = true;
        } else {
            isFullSize = false;
        }

        panel.setWindowSize(width, height);
        this.maxWidth = width;
        this.maxHeight = height;
    }

    public void startCatchVideo(URL url) {
        this.url = url;
        catchVideo = true;
        panel.startShowVideo();
        System.out.println("Начинаем схватывать видео - " + catchVideo);
    }

    public void stopCatchVideo() {
        panel.stopShowVideo();
        catchVideo = false;
    }

    public void setMainCamera(boolean mainCamera) {
        this.mainCamera = mainCamera;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }
}

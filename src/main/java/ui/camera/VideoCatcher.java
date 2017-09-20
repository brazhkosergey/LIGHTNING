package ui.camera;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher implements Runnable {
    int fps;
    int maxWidth ;
    int maxHeight;
    boolean isFullSize;
    Deque<BufferedImage> deque;
//    Deque<byte[]> dequeBytes;
    VideoCreator videoCreator;

    List<BufferedImage> list;
//    List<byte[]> listBytes;
    int centerEventFrameNumber;


    CameraPanel panel;
    private URL url;
    HttpURLConnection connection = null;

    private BufferedInputStream inputStream;
    boolean catchVideo;
    Thread fpsThread;
    int fpsNotZero;

    public VideoCatcher(CameraPanel panel, VideoCreator videoCreator) {

        fpsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                panel.getTitle().setTitle("FPS = " + fps);
                if (fps != 0) {
                    fpsNotZero = fps;
                }
                fps = 0;
            }
        });

        deque = new ConcurrentLinkedDeque<>();
//        dequeBytes = new ConcurrentLinkedDeque<>();
        list = new ArrayList<>();
//        listBytes = new ArrayList<>();

        this.videoCreator = videoCreator;
        this.panel = panel;
        setWidthAndHeight(245, 220);
        panel.repaint();
    }

    private void createInputStream() throws IOException {
        MainFrame.getMainFrame().showMessage("Пробуем открыть соединение...");
        connection = (HttpURLConnection) url.openConnection();
        MainFrame.getMainFrame().showMessage("Пробуем открыть поток....");
        try{
            inputStream = new BufferedInputStream(connection.getInputStream());
        }catch (Exception e){
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

        BufferedImage image = null;
        while (true) {
            while (catchVideo) {
                if (inputStream == null) {
                    try {
                        createInputStream();
                    } catch (IOException e) {
                        MainFrame.getMainFrame().showMessage(e.getMessage());
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

//                        dequeBytes.addFirst(imageBytes);
                        deque.addFirst(image);

                        if (MainVideoCreator.isSaveVideo() || deque.size() > MainFrame.timeToSave * fpsNotZero) {

                            if (MainVideoCreator.isSaveVideo()) {
                                if(centerEventFrameNumber == 0){
//                                    centerEventFrameNumber = dequeBytes.size();
                                    centerEventFrameNumber = deque.size();
                                }

//                                listBytes.add(dequeBytes.pollFirst());
                                list.add(deque.pollFirst());
                                System.out.println("Номер панели - "+panel.getCameraNumber());
                                System.out.println("Размер очереди изображений  - "+deque.size());
                                System.out.println("Размер листа изображений  - "+list.size());

//                              if(listBytes.size()>MainFrame.timeToSave*fpsNotZero*2||list.size()>MainFrame.timeToSave*fpsNotZero*2){
                                if(list.size()>MainFrame.timeToSave*fpsNotZero*2){
                                    System.out.println("Стоп сохранять видео.");
                                    MainVideoCreator.stopCatchVideo();
                                    int size = deque.size();
                                    for(int i=0;i<size;i++){
                                        list.add(deque.pollLast());
                                    }
                                }
                            } else {
                                if (list.size() > 0) {
                                    int size = deque.size();
                                    for(int i=0;i<size;i++){
                                        list.add(deque.pollLast());
                                    }

                                    deque.pollFirst();
                                }
//                                dequeBytes.pollFirst();
                                deque.pollFirst();
                            }
                        } else {
//                            if (listBytes.size() > 0) {
//                                int size = dequeBytes.size();
//                                for(int i=0;i<size;i++){
//                                    listBytes.add(dequeBytes.pollFirst());
//                                }
//                                int num;
//                                if (panel.getCameraNumber() % 2 == 0) {
//                                    num = 2;
//                                } else {
//                                    num = 1;
//                                }
//
//                                videoCreator.addListByte(num, listBytes, centerEventFrameNumber);
//                                listBytes = new ArrayList<>();
//                                centerEventFrameNumber = 0;
//
                            if (list.size() > 0) {
                                System.out.println("Сохраняем видео с листа изображений ");
                                System.out.println("Размер очереди изображений  - "+deque.size());
                                System.out.println("Размер листа изображений  - "+list.size());

                                int num;
                                if (panel.getCameraNumber() % 2 == 0) {
                                    num = 2;
                                } else {
                                    num = 1;
                                }

                                videoCreator.addListImage(num, list, centerEventFrameNumber);
                                list = new ArrayList<>();
                                centerEventFrameNumber = 0;
                            }
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
        System.out.println("Начинаем схватывать видео - "+catchVideo);
    }

    public void stopCatchVideo() {
        catchVideo = false;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }
}

package ui.camera;

import entity.MainVideoCreator;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import ui.video.VideoPanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher implements Runnable {
    int fps = 0;
    int maxWidth = 0;
    int maxHeight = 0;
    boolean isFullSize = false;
    Deque<BufferedImage> deque;
    VideoCreator videoCreator;


    List<BufferedImage> list;

    CameraPanel panel;
    private URL url;
    private URLConnection connection;
    private BufferedInputStream inputStream;
    boolean catchVideo = false;
    Thread fpsThread;


    public VideoCatcher(CameraPanel panel,VideoCreator videoCreator) {

        fpsThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                panel.getTitle().setTitle("FPS = " + fps);
                fps = 0;
            }
        });

        deque = new ConcurrentLinkedDeque<>();
        list = new ArrayList<>();

        this.videoCreator = videoCreator;
        this.panel = panel;
        setWidthAndHeight(245, 220);
        panel.repaint();
    }

    private void createInputStream() throws IOException {
        connection = url.openConnection();
//        connection.addRequestProperty("");
        inputStream = new BufferedInputStream(connection.getInputStream());
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
                            deque.addFirst(image);
                            System.out.println("Размер очереди "+deque.size());
                            if (MainVideoCreator.isSaveVideo() || deque.size() > 30 * 10) {
                                if (MainVideoCreator.isSaveVideo()) {
                                    list.add(deque.pollFirst());
                                    System.out.println("Размер листа - "+list.size());
                                    System.out.println("Размер очереди после добавления в лист "+deque.size());
                                    System.out.println("===============================================");
                                } else {
                                    deque.pollFirst();
                                }
                            } else {

                                if(list.size()>0) {
                                    list.addAll(deque);
                                    deque.clear();
                                    int num;
                                    if(panel.getCameraNumber()%2==0){
                                        num = 2;
                                    }else {
                                        num = 1;
                                    }

                                    videoCreator.addList(num,list);
                                    list = new ArrayList<>();
                                }
//                                if (saverThread == null&&list.size()>0) {
//                                    saverThread = new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            System.out.println("Начинаем создавать файл");
//                                            File file = new File(path);
//
//                                            if(!file.exists()){
//                                                try {
//                                                    file.createNewFile();
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//
//                                            list.addAll(deque);
//                                            deque.clear();
//                                            SeekableByteChannel out = null;
//                                            try {
//                                                out = NIOUtils.writableFileChannel(path);
//                                                // for Android use: AndroidSequenceEncoder
//                                                AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(10, 1));
//
////                                                for(BufferedImage bufferedImage:list){
//                                                for(int i=0;i<list.size();i++){
//                                                    System.out.println("Добавляем кадр "+i);
//                                                    encoder.encodeImage(list.get(i));
//                                                }
//                                                // Finalize the encoding, i.e. clear the buffers, write the header, etc.
//                                                encoder.finish();
//                                            }catch (Exception e){
//                                                e.printStackTrace();
//                                            }finally {
//                                                NIOUtils.closeQuietly(out);
//                                            }
//                                            list.clear();
//                                            System.out.println("Файл создан. Закрываем поток.");
//                                        }
//                                    });
//                                    saverThread.start();
//                                }
                            }

                            panel.setBufferedImage(processImage(image, maxWidth, maxHeight));
//                            panel.setBufferedImage(image);
                            fps++;
                            panel.repaint();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            try {
                Thread.sleep(1000);
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

//
//    public void setSaveVideo(boolean saveVideo) {
//        this.saveVideo = saveVideo;
//    }
}

package ui.camera;

import entity.MainVideoCreator;
import ui.video.VideoPlayer;
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
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher {
    private static Logger log = Logger.getLogger(VideoCatcher.class);

    private int whitePercent = -1;
    private int percentDiffWhite = 100;
    private int numberGRB;
    private boolean programLightCatchWork;

    private int fps;
    private int countTimesToHaveNotBytesToRead;

    private int countDoNotShowImages = 1;

    private Deque<byte[]> imageDeque;
    private VideoCreator videoCreator;

    private URL url;
    private HttpURLConnection connection = null;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private ByteArrayOutputStream temporaryStream = null;

    private CameraPanel cameraPanel;

    private boolean restart;
    private boolean catchVideo;
    private boolean changeURL;

    private Thread FpsCountThread;
    private Thread UpdateDataThread;
    private Thread MainThread;
    private boolean showImage = true;

    public VideoCatcher(CameraPanel cameraPanel, VideoCreator videoCreatorForBouth) {
        log.info("Создаем наблюдатель для камеры номер " + cameraPanel.getCameraNumber());
        FpsCountThread = new Thread(() -> {
            while (true) {
                if (catchVideo) {
                    if (fps != 0) {
                        cameraPanel.getTitle().setTitle(
                                "FPS = " + fps);
                        cameraPanel.repaint();
                        fps = 0;
                        countTimesToHaveNotBytesToRead = 0;
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
                        programLightCatchWork = MainFrame.isProgramLightCatchWork();
                        countDoNotShowImages = MainFrame.getShowImagePerSecond();
                        checkData = 0;
                    } else {
                        byte[] bytes;
                        if (imageDeque.size() > 0 && !VideoPlayer.isShowVideoPlayer()) {
                            bytes = imageDeque.pollLast();
                            if (bytes != null) {
                                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                                try {
                                    ImageIO.setUseCache(false);
                                    BufferedImage image = ImageIO.read(inputStream);
                                    inputStream.close();
                                    cameraPanel.setBufferedImage(findProgramEvent(processImage(image, cameraPanel.getWidth(),
                                            cameraPanel.getHeight())));
                                    cameraPanel.repaint();
                                } catch (Exception ignored) {
                                }
                                showImage = true;
                            }
                        }
                        checkData++;
                    }
                    try {
                        Thread.sleep((1000 / countDoNotShowImages));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    cameraPanel.getTitle().setTitle(MainFrame.getBundle().getString("cameradoesnotwork"));
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
            log.info("Запускаем наблюдатель для камеры номер " + cameraPanel.getCameraNumber());

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

                                if (showImage) {
                                    imageDeque.addFirst(bytes);
                                    showImage = false;
                                }

                                videoCreator.addImageBytes(l, bytes);
                                fps++;
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (changeURL) {
                    catchVideo = true;
                    changeURL = false;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        MainThread.setPriority(Thread.MAX_PRIORITY);
        imageDeque = new ConcurrentLinkedDeque<>();
        this.cameraPanel = cameraPanel;
        cameraPanel.repaint();
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
                cameraPanel.startShowVideo();
            }
            this.url = urlMainStream;
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
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (!restart && catchVideo) {
                    createInputStream();
                } else {
                    cameraPanel.getTitle().setTitle(MainFrame.getBundle().getString("restoreconnection"));
                    cameraPanel.repaint();
                }
            }
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(catchVideo + " URL = NULL " + Thread.currentThread().getName() + "- " + changeURL);
            catchVideo = false;
        }
    }

    public void start() {
        MainThread.setName("Save Stream Thread. Camera " + cameraPanel.getCameraNumber());
        UpdateDataThread.setName("Update Data Thread. Camera " + cameraPanel.getCameraNumber());
        FpsCountThread.setName("FPS CountThread. Camera " + cameraPanel.getCameraNumber());
        MainThread.start();
    }

    public static BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        int width;
        int height;

        if (maxWidth / 1.77 > maxHeight) {
            height = maxHeight;
            width = (int) (height * 1.77);
        } else {

            width = maxWidth;
            height = (int) (width / 1.77);
        }

        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = width - bi.getWidth();
        int hh = height - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = width;
                size = bi.getWidth();
            } else {
                max = height;
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
            return bi2;
        } else {
            return bi;
        }
    }

    private BufferedImage findProgramEvent(BufferedImage bi2) {
        if (programLightCatchWork && !cameraPanel.isFullSize()) {
            int[] rgb1 = bi2.getRGB(0, 0, bi2.getWidth(), bi2.getHeight(), null, 0, 1024);
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
    }

    void setBorderColor(Color color) {
        cameraPanel.getTitle().setTitleColor(color);
    }

    VideoCreator getVideoCreator() {
        return videoCreator;
    }

    public void stopCatchVideo() {
        catchVideo = false;
        cameraPanel.stopShowVideo();
        url = null;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }

    CameraPanel getCameraPanel() {
        return cameraPanel;
    }
}

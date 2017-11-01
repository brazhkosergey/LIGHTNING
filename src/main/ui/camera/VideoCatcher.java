package ui.camera;

import entity.MainVideoCreator;
import org.apache.log4j.Logger;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCatcher {
    private static Logger log = Logger.getLogger(VideoCatcher.class);
    private int fps;
    private int countTimesToHaveNotBytesToRead;
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

    private Set<Integer> set;
    private Deque<Integer> whiteDeque;
    int percentWhiteDiff = 0;

    public VideoCatcher(CameraPanel cameraPanel, VideoCreator videoCreatorForBouth) {
        set = MainFrame.getMainFrame().getColorRGBNumberSet();
        whiteDeque = new ConcurrentLinkedDeque<>();
        log.info("Создаем наблюдатель для камеры номер " + cameraPanel.getCameraNumber());
        FpsCountThread = new Thread(() -> {
            while (true) {
                if (catchVideo) {
                    if (fps != 0) {
                        cameraPanel.getTitle().setTitle("FPS = " + fps);
                        cameraPanel.repaint();
                        fps = 0;
                        countTimesToHaveNotBytesToRead = 0;
                    } else {
                        if (!restart) {
                            countTimesToHaveNotBytesToRead++;
                            if (countTimesToHaveNotBytesToRead > 10) {
                                restart = true;
                                cameraPanel.stopShowVideo();
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
            while (true) {
                if (catchVideo) {
                    byte[] bytes;
                    if (imageDeque.size() > 0) {
                        bytes = imageDeque.pollLast();
                        if (bytes != null) {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                            try {
                                ImageIO.setUseCache(false);
                                BufferedImage image = ImageIO.read(inputStream);
                                inputStream.close();
                                cameraPanel.setBufferedImage(CameraPanel.processImage(findProgramEvent(image), cameraPanel.getWidth(), cameraPanel.getHeight()));
                                cameraPanel.repaint();
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
                cameraPanel.startShowVideo();
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

    private BufferedImage findProgramEvent(BufferedImage bi) {
        if (MainFrame.isProgramLightCatchWork()) {
            long l = System.currentTimeMillis();
            int[] rgb1 = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, 2048);
            int countWhite = 0;
            int allPixels = rgb1.length;
            for (int i = 0; i < allPixels; i++) {
                if (set.contains(rgb1[i])) {
                    countWhite++;
                }
            }

            System.out.println("Времени затрачено на поиск белых пикселей - " + (System.currentTimeMillis() - l));

            whiteDeque.addFirst(countWhite);
            if (whiteDeque.size() > 10) {
                int total = 0;
                for (Integer integer : whiteDeque) {
                    total += integer;
                }
                int average = total / whiteDeque.size();
                if (countWhite != 0) {

                    int differentWhitePixelsAverage = Math.abs(average - countWhite);
                    if (differentWhitePixelsAverage != 0) {
                        if (average != 0) {
                            int diffPercent = differentWhitePixelsAverage * 100 / average;
                            int abs = Math.abs(diffPercent);
                            int percentDiffWhiteFromSetting = MainFrame.getPercentDiffWhite();
                            if (percentWhiteDiff != percentDiffWhiteFromSetting) {
                                percentWhiteDiff = percentDiffWhiteFromSetting;
                            } else {
                                if (abs > percentWhiteDiff * 50) {
                                    System.out.println(cameraPanel.getCameraNumber() + " - Белых пикселей - " + countWhite);
                                    System.out.println("Среднее - " + average);
                                    System.out.println("Разница пикселей - " + differentWhitePixelsAverage);
                                    System.out.println("Разница процентов - " + diffPercent);
                                    System.out.println("Сработка, номер камеры - " + cameraPanel.getCameraNumber());
                                    System.out.println("=========================================");

                                    MainVideoCreator.startCatchVideo(true);
                                    whiteDeque.clear();
                                }
                            }
                        }
                    }
                } else {
                    if (average != 0) {
                        whiteDeque.clear();
                    }
                }
                whiteDeque.pollLast();
            }
        }
        return bi;
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

    CameraPanel.CameraWindow getCameraPanelWindow() {
        return cameraPanel.getCameraWindow();
    }
}

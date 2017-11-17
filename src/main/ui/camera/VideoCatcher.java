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

/**
 * class to read bytes from camera
 */
public class VideoCatcher {
    private static Logger log = Logger.getLogger(VideoCatcher.class);

    /**
     * for counting fps from camera
     */
    private int fps;
    /**
     * for counting fps of showed images to camera panel
     */
    private int fpsShow = 0;
    /**
     * used for check connections to camera, and reconnect
     */
    private int countSecondsToHaveNotImage;

    /**
     * deque whit bytes from camera
     */
    private Deque<byte[]> bytesForImagesToShowDeque;
    /**
     * save bytes from two or one camera to temporary files
     */
    private VideoBytesSaver videoBytesSaver;

    /**
     * camera url
     */
    private URL url;
    private HttpURLConnection connection = null;
    private BufferedInputStream bufferedInputStream;
    private InputStream inputStream;
    private ByteArrayOutputStream temporaryStream = null;

    /**
     * panel to show images
     */
    private CameraPanel cameraPanel;

    private boolean restart;
    private boolean catchVideo;
    private boolean changeURL;

    /**
     * Thread to calculate FPS and show it to camera panel title, once in second
     */
    private Thread fpsCountThread;
    /**
     * Thread to scan images for white count, and showing it to camera panel
     */
    private Thread showImageToCameraPanelThread;
    /**
     * thread to read bytes from camera and send it video creator
     */
    private Thread readBytesThread;

    /**
     * mark that program finish to scan and showing image,
     * and possible to start to scan next image, after it main thread will add one image to "bytesForImagesToShowDeque"
     */
    private boolean showImage = true;

    /**
     * used for checking the count of white pixels of image
     */
    private Set<Integer> setOfColorsRGBNumbers;
    /**
     * counts of white percent of last 10 images
     */
    private Deque<Integer> whiteDeque;
    /**
     * used for programming catch lightning
     */
    private int percentWhiteDiff = 0;

    /**
     * @param cameraPanel         - camera panel for show video form this catcher camera
     * @param videoBytesSaverForBoth - video creator for saving bytes from this cather camera
     */
    public VideoCatcher(CameraPanel cameraPanel, VideoBytesSaver videoBytesSaverForBoth) {
        setOfColorsRGBNumbers = MainFrame.getMainFrame().getColorRGBNumberSet();
        whiteDeque = new ConcurrentLinkedDeque<>();
        log.info("Создаем наблюдатель для камеры номер " + cameraPanel.getCameraNumber());
        fpsCountThread = new Thread(() -> {
            while (true) {
                if (catchVideo) {
                    if (fps != 0) {
                        cameraPanel.getTitle().setTitle("FPS = " + fps + " : " + fpsShow);
                        cameraPanel.repaint();
                        fps = 0;
                        fpsShow = 0;
                        countSecondsToHaveNotImage = 0;
                    } else {
                        if (!restart) {
                            countSecondsToHaveNotImage++;
                            if (countSecondsToHaveNotImage > 10) {
                                restart = true;
                                cameraPanel.stopShowVideo();
                                bufferedInputStream = null;
                                countSecondsToHaveNotImage = 0;
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

        showImageToCameraPanelThread = new Thread(() -> {
            while (true) {
                if (catchVideo) {
                    byte[] bytes;
                    int totalMillisecondsToShowFrame = 0;
                    if (bytesForImagesToShowDeque.size() > 0) {
                        bytes = bytesForImagesToShowDeque.pollLast();
                        if (bytes != null) {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                            try {
                                long startTime = System.currentTimeMillis();
                                ImageIO.setUseCache(false);
                                BufferedImage image = ImageIO.read(inputStream);
                                inputStream.close();
                                cameraPanel.setBufferedImage(CameraPanel.processImage(scanCountOfWhitePixelsPercent(image), cameraPanel.getWidth(), cameraPanel.getHeight()));
                                cameraPanel.repaint();
                                fpsShow++;
                                totalMillisecondsToShowFrame = (int) (System.currentTimeMillis() - startTime);
                            } catch (Exception ignored) {
                            }
                            showImage = true;
                        }

                        int timeToSleep = 1000 / MainFrame.getShowFramesPercent();
                        int diff = timeToSleep - totalMillisecondsToShowFrame;
                        if (diff > 0) {
                            try {
                                Thread.sleep(diff);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
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
        showImageToCameraPanelThread.setPriority(Thread.MIN_PRIORITY);

        readBytesThread = new Thread(() -> {
            showImageToCameraPanelThread.start();
            fpsCountThread.start();
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
                                    bytesForImagesToShowDeque.addFirst(bytes);
                                    showImage = false;
                                }

                                videoBytesSaver.addImageBytes(l, bytes);
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
        readBytesThread.setPriority(Thread.MAX_PRIORITY);
        bytesForImagesToShowDeque = new ConcurrentLinkedDeque<>();
        this.cameraPanel = cameraPanel;
        cameraPanel.repaint();
        this.videoBytesSaver = videoBytesSaverForBoth;
        videoBytesSaver.addVideoCatcher(this);
    }

    /**
     * used when the ip address was set for this number of camera
     *
     * @param urlMainStream = url
     */
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

    /**
     * used when program was started
     */
    public void start() {
        readBytesThread.setName("Save Stream Thread. Camera " + cameraPanel.getCameraNumber());
        showImageToCameraPanelThread.setName("Update Data Thread. Camera " + cameraPanel.getCameraNumber());
        fpsCountThread.setName("FPS CountThread. Camera " + cameraPanel.getCameraNumber());
        readBytesThread.start();
    }


    /**
     * scan count of white and catch lightning if it is
     *
     * @param bi - frame
     * @return - the save frame after scanning
     */
    private BufferedImage scanCountOfWhitePixelsPercent(BufferedImage bi) {
        if (MainFrame.isProgramLightCatchEnable()) {
            int countWhite = 0;
            for (int y = 0; y < bi.getHeight(); y += 2) {
                for (int x = 0; x < bi.getWidth(); x += 2) {
                    if (setOfColorsRGBNumbers.contains(bi.getRGB(x, y))) {
                        countWhite++;
                    }
                }
            }
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

    /**
     * used green color when have saved as many second as set in setting, use red color when already not saved enough
     * @param color = color
     */
    void setBorderColor(Color color) {
        cameraPanel.getTitle().setTitleColor(color);
    }

    VideoBytesSaver getVideoBytesSaver() {
        return videoBytesSaver;
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

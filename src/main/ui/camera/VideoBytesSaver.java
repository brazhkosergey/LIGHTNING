package ui.camera;

import entity.MainVideoCreator;
import org.apache.log4j.Logger;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * class for saving bytes from camera group (two cameras)
 */
public class VideoBytesSaver {
    private static Logger log = Logger.getLogger(VideoBytesSaver.class);
    /**
     * camera group number from 1 to 4
     */
    private int cameraGroupNumber;

    /**
     * background for both cameras
     */
    private BufferedImage backGroundImage;

    /**
     * list with links to video catchers
     */
    private List<VideoCatcher> catcherList;

    /**
     * deque to save time, when image bytes was read to RAM
     */
    private Deque<Long> dequeImagesTime;

    /**
     * map to save byte array and time, when it was read to RAM
     */
    private Map<Long, byte[]> buffMapImages;


    /**
     * deque of links to temporary files with bytes from cameras on disk
     */
    private Deque<File> fileDeque;

    /**
     * counts of frames, in each temporary file
     */
    private Map<File, Integer> countsOfFramesInEachFile;

    /**
     * total FPS from both cameras
     */
    private int totalFPS = 0;

    /**
     * deque with total fps count for as many last second as program should save
     */
    private Deque<Integer> fpsDeque;

    /**
     * list with total fps count for as many last second as program should save
     */
    private List<Integer> fpsList;


    /**
     * mark if almost one of catchers is worked
     */
    private boolean creatorWork;

    /**
     * mark one second gone
     */
    private boolean oneSecond = false;
    /**
     * total count of frames in temporary files and RAM
     */
    private int totalCountFrames;
    /**
     * seconds to save
     */
    private int secondsToSave;

    /**
     * map with number of frame, when was lightning, and type of event (program - true or sensor - false)
     */
    private Map<Integer, Boolean> eventsFramesNumber;

    /**
     * mark that bytes from camera should be saved
     */
    private boolean enableSaveVideo;
    /**
     * mark count of already saved second after lightning
     */
    private int stopSaveVideoInt;

    /**
     * Thread to mark one second, and update data from setting
     */
    private Thread timerThread;

    /**
     * Thread to save image bytes to files
     */
    private Thread saveBytesThread;

    /**
     * date when lightning was
     */
    private Date date;

    private boolean containProgramCatchLightning;

    /**
     * @param cameraGroupNumber - number of camera group (1-4)
     */
    public VideoBytesSaver(int cameraGroupNumber) {
        catcherList = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;

        fileDeque = new ConcurrentLinkedDeque<>();
        countsOfFramesInEachFile = new HashMap<>();
        fpsDeque = new ConcurrentLinkedDeque<>();

        fpsList = new ArrayList<>();

        dequeImagesTime = new ConcurrentLinkedDeque<>();
        buffMapImages = new HashMap<>();
        eventsFramesNumber = new HashMap<>();

        timerThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    creatorWork = false;
                    for (VideoCatcher catcher : catcherList) {
                        if (!creatorWork) {
                            creatorWork = catcher.isCatchVideo();
                        }

                        if (fileDeque.size() >= secondsToSave) {
                            catcher.setBorderColor(new Color(70, 193, 84));
                        } else {
                            catcher.setBorderColor(Color.RED);
                        }
                    }

                    if (creatorWork) {
                        fpsList.add(totalFPS);
                        fpsDeque.addFirst(totalFPS);
                        totalFPS = 0;
                        oneSecond = true;
                        secondsToSave = MainFrame.getSecondsToSave();

                        if (enableSaveVideo) {
                            stopSaveVideoInt++;
                        }
                    }

                    if (!creatorWork) {
                        while (fileDeque.size() > 0) {
                            try {
                                File fileToDel = fileDeque.pollLast();
                                Integer remove = countsOfFramesInEachFile.remove(fileToDel);
                                totalCountFrames -= remove;
                                fileToDel.delete();
                                fpsList.remove(0);
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        }
                        stopSaveVideoInt = 0;
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.setName("VideoCreatorTimer Thread " + cameraGroupNumber);

        saveBytesThread = new Thread(() -> {
            while (true) {
                if (oneSecond) {
                    try {
                        Thread saveFileThread = new Thread(() -> {
                            if (dequeImagesTime.size() > 0) {
                                int size = fpsDeque.size();
                                File temporaryFile = new File(MainFrame.getDefaultPath() + "\\buff\\" + cameraGroupNumber + "\\" + System.currentTimeMillis() + ".tmp");
                                int countImagesInFile = 0;
                                try {
                                    if (temporaryFile.createNewFile()) {
                                        FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile);
                                        for (int i = 0; i < size; i++) {
                                            Integer integer = fpsDeque.pollLast();
                                            if (integer != null) {
                                                for (int j = 0; j < integer; j++) {
                                                    Long aLong = dequeImagesTime.pollLast();
                                                    if (aLong != null) {
                                                        byte[] remove = buffMapImages.remove(aLong);
                                                        if (remove != null) {
                                                            try {
                                                                countImagesInFile++;
                                                                fileOutputStream.write(remove);
                                                            } catch (Exception e) {
                                                                log.error(e.getMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        try {
                                            fileOutputStream.flush();
                                            fileOutputStream.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                    e.printStackTrace();
                                }
                                File file = new File(MainFrame.getDefaultPath() + "\\buff\\" + cameraGroupNumber + "\\"
                                        + System.currentTimeMillis() + "-" + countImagesInFile + ".tmp");
                                if (temporaryFile.renameTo(file)) {
                                    fileDeque.addFirst(file);
                                    countsOfFramesInEachFile.put(file, countImagesInFile);
                                }
                            }

                            if (enableSaveVideo) {
                                if (stopSaveVideoInt >= secondsToSave && totalCountFrames > 0) {
                                    stopSaveVideoInt = 0;
                                    MainVideoCreator.stopCatchVideo(containProgramCatchLightning);

                                    containProgramCatchLightning = false;

                                    log.info("Сохраняем данные. Группа номер - " + cameraGroupNumber);
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("[");
                                    int iCount = 0;
                                    int currentTotalCountImage = totalCountFrames;

                                    for (Integer integer : eventsFramesNumber.keySet()) {
                                        iCount++;
                                        if (eventsFramesNumber.get(integer)) {
                                            stringBuilder.append("(").append(integer).append(")");
                                        } else {
                                            stringBuilder.append(integer);
                                        }
                                        if (iCount != eventsFramesNumber.size()) {
                                            stringBuilder.append(",");
                                        }
                                    }

                                    eventsFramesNumber.clear();
                                    stringBuilder.append("]");

                                    int totalFPSForFile = 0;
                                    int sizeFps = fpsList.size();
                                    for (int i = 0; i < sizeFps; i++) {
                                        Integer integer = fpsList.get(i);
                                        totalFPSForFile += integer;
                                    }

                                    for (int i = 0; i < sizeFps; i++) {
                                        fpsList.remove(0);
                                    }
                                    double d = (double) totalFPSForFile / sizeFps;
                                    totalFPSForFile = (int) (d + 0.5);

                                    String eventPercent = stringBuilder.toString();
                                    String path = MainFrame.getPath() + "\\bytes\\" + date.getTime() +
                                            "-" + cameraGroupNumber + "(" + totalFPSForFile + ")"
                                            + eventPercent + ".tmp";

                                    File destFolder = new File(path);
                                    int size = fileDeque.size();
                                    int secondsCount = 0;

                                    if (destFolder.mkdirs()) {
                                        for (int i = 0; i < size; i++) {
                                            try {
                                                File fileToSave = fileDeque.pollLast();
                                                if (fileToSave != null) {
                                                    Integer remove = countsOfFramesInEachFile.remove(fileToSave);
                                                    totalCountFrames -= remove;//Не здесь
                                                    secondsCount++;
                                                    boolean reSave = fileToSave.renameTo(new File(destFolder, fileToSave.getName()));
                                                    if (!reSave) {
                                                        fileToSave.delete();
                                                    }
                                                }
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }

                                    BufferedImage image = MainFrame.imagesForBlock.get(cameraGroupNumber);
                                    if (image != null) {
                                        File imageFile = new File(MainFrame.getPath() + "\\bytes\\" + date.getTime() +
                                                "-" + cameraGroupNumber + "(" + totalFPSForFile + ")"
                                                + eventPercent + ".jpg");
                                        try {
                                            if (imageFile.createNewFile()) {
                                                ImageIO.write(image, "jpg", imageFile);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    log.info("Сохранили файл. Группа - " + cameraGroupNumber + ". " +
                                            "Кадров - " + currentTotalCountImage + ". " +
                                            "Файлов в буфере " + size + ". " +
                                            "Сохранили секунд " + secondsCount);


                                    System.out.println("Сохранили файл. Группа - " + cameraGroupNumber + ". " +
                                            "Кадров - " + currentTotalCountImage + ". " +
                                            "Файлов в буфере " + size + ". " +
                                            "Сохранили секунд " + secondsCount);
                                    System.out.println("Путь к файлу - " + path);


                                    enableSaveVideo = false;
                                } else {
                                    System.out.println(" Мало секунд " + stopSaveVideoInt);
                                }
                            } else {
                                System.out.println("Банально сохраняем секунду");
                                int i = secondsToSave;
                                while (fileDeque.size() > i) {
                                    try {
                                        if (!enableSaveVideo) {
                                            File fileToDel = fileDeque.pollLast();
                                            if (fileToDel != null) {
                                                Integer remove = countsOfFramesInEachFile.remove(fileToDel);
                                                totalCountFrames -= remove;
                                                int framesBeforeCalculating = totalCountFrames;
                                                int b = 0;
                                                for (File file : countsOfFramesInEachFile.keySet()) {
                                                    b += countsOfFramesInEachFile.get(file);
                                                }

                                                if (eventsFramesNumber.size() != 0) {
                                                    Map<Integer, Boolean> temporaryMap = new HashMap<>();
                                                    for (Integer integer : eventsFramesNumber.keySet()) {
                                                        temporaryMap.put(integer - remove, eventsFramesNumber.get(integer));
                                                    }
                                                    eventsFramesNumber.clear();
                                                    for (Integer integer : temporaryMap.keySet()) {
                                                        eventsFramesNumber.put(integer, temporaryMap.get(integer));
                                                    }
                                                }
                                                fpsList.remove(0);
                                                fileToDel.delete();
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error(e.getMessage());
                                    }
                                }
                            }
                        });

                        saveFileThread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    oneSecond = false;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        saveBytesThread.setName("Video Creator SaveBytesThread " + cameraGroupNumber);
    }

    /**
     * add bytes array from both cameras to common collection
     *
     * @param time  = time, when image was saved to RAM (milliseconds)
     * @param bytes = image bytes array
     */
    void addImageBytes(long time, byte[] bytes) {
        while (dequeImagesTime.contains(time)) {
            time++;
        }
        if (!dequeImagesTime.contains(time)) {
            dequeImagesTime.addFirst(time);
            buffMapImages.put(time, bytes);
            totalFPS++;
            totalCountFrames++;
        }
    }

    /**
     * method to start event (save video)
     *
     * @param programEventDetection = type of detection (program - true, sensor - false)
     * @param date                  = date, when was event (lightning)
     */
    public void startSaveVideo(boolean programEventDetection, Date date) {
        if (!containProgramCatchLightning) {
            containProgramCatchLightning = programEventDetection;
        }
        int imageNumber = totalCountFrames;
        boolean work = false;
        for (VideoCatcher catcher : catcherList) {
            work = catcher.isCatchVideo();
            if (work) {
                break;
            }
        }

        if (work) {
            eventsFramesNumber.put(imageNumber, programEventDetection);
//            if (dequeImagesTime.size() > 0) {
                if (!enableSaveVideo) {
                    log.info("Начинаем запись. Группа " + cameraGroupNumber + ". Кадр номер - " + imageNumber + ". Время - " + System.currentTimeMillis());
                    enableSaveVideo = true;
                    this.date = date;
                } else {
                    log.info("Продлжаем запись. Группа " + cameraGroupNumber + ". Кадр номер - " + imageNumber + ". Время - " + System.currentTimeMillis());
                    stopSaveVideoInt = 0;
                }
//            }
            System.out.println(cameraGroupNumber + " - Начинаем сохранять, Время - " + System.currentTimeMillis() + ". Кадр номер - " + imageNumber);
        }
    }

    /**
     * add ip camera to saver
     *
     * @param videoCatcher - video catcher
     */
    void addVideoCatcher(VideoCatcher videoCatcher) {
        if (!timerThread.isAlive()) {
            timerThread.start();
            saveBytesThread.start();
        }
        catcherList.add(videoCatcher);
    }

    BufferedImage getBackGroundImage() {
        return backGroundImage;
    }

    public void setBackGroundImage(BufferedImage backGroundImage) {
        this.backGroundImage = backGroundImage;
    }
}
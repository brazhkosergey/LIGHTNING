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

public class VideoCreator {
    private static Logger log = Logger.getLogger(VideoCreator.class);

    private int cameraGroupNumber;
    private BufferedImage bufferedImageBack;
    private List<VideoCatcher> catcherList;

    private Deque<Long> dequeImagesTime;
    private Deque<File> fileDeque;
    private Deque<Integer> fpsDeque;
    private List<Integer> fpsList;

    private Map<Long, byte[]> buffMapImages;
    private Map<File, Integer> buffFilesSizeImagesCount;

    private int totalFPS = 0;

    private boolean oneSecond = false;

    private int totalCountImages;
    private int timeToSave;
    private Map<Integer, Boolean> eventsFramesNumber;

    private boolean startSaveVideo;
    private int stopSaveVideoInt;

    private Thread timerThread;
    private Date date;

    public VideoCreator(int cameraGroupNumber) {
        catcherList = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;

        fileDeque = new ConcurrentLinkedDeque<>();
        buffFilesSizeImagesCount = new HashMap<>();
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
                    fpsList.add(totalFPS);
                    fpsDeque.addFirst(totalFPS);
                    totalFPS = 0;
                    oneSecond = true;
                    timeToSave = MainFrame.getTimeToSave();
                    boolean creatorWork = false;
                    for (VideoCatcher catcher : catcherList) {
                        if (!creatorWork) {
                            creatorWork = catcher.isCatchVideo();
                        }

                        if (fileDeque.size() >= timeToSave) {
                            catcher.setBorderColor(new Color(70, 193, 84));
                        } else {
                            catcher.setBorderColor(Color.RED);
                        }
                    }

                    if (!creatorWork) {
                        while (fileDeque.size() > 0) {
                            File fileToDel = fileDeque.pollLast();
                            fileToDel.delete();
                            Integer remove = buffFilesSizeImagesCount.remove(fileToDel);
                            totalCountImages -= remove;
                        }
                    }

                    if (startSaveVideo) {
                        stopSaveVideoInt++;
                    }

                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.setName("VideoCreatorTimer Thread " + cameraGroupNumber);

        Thread saveBytesThread = new Thread(() -> {
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
                                            for (int j = 0; j < integer; j++) {
                                                Long aLong = dequeImagesTime.pollLast();
                                                byte[] remove = buffMapImages.remove(aLong);
                                                if (remove != null) {
                                                    fileOutputStream.write(remove);
                                                    countImagesInFile++;
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
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                File file = new File(MainFrame.getDefaultPath() + "\\buff\\" + cameraGroupNumber + "\\"
                                        + System.currentTimeMillis() + "-" + countImagesInFile + ".tmp");
                                if (temporaryFile.renameTo(file)) {
                                    fileDeque.addFirst(file);
                                    System.out.println("Добавили файл в буфер. Размер буфера: "+fileDeque.size());
                                    buffFilesSizeImagesCount.put(file, countImagesInFile);
                                }
                            }

                            if (startSaveVideo) {
                                if (stopSaveVideoInt >= timeToSave && totalCountImages > 0) {
                                    stopSaveVideoInt = 0;
                                    MainVideoCreator.stopCatchVideo();
                                    log.info("Сохраняем данные. Группа номер - " + cameraGroupNumber);
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("[");
                                    int iCount = 0;
                                    int currentTotalCountImage = totalCountImages;

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
                                        totalFPSForFile += fpsList.get(i);
                                    }

                                    totalFPSForFile = totalFPSForFile / sizeFps;

                                    String eventPercent = stringBuilder.toString();
                                    String path = MainFrame.getPath() + "\\bytes\\" + date.getTime() +
                                            "-" + cameraGroupNumber + "(" + totalFPSForFile + ")"
                                            + eventPercent + ".tmp";//"\\";

                                    File destFolder = new File(path);
                                    int size = fileDeque.size();
                                    int secondsCount = 0;

                                    System.out.println("Размер буфера, на момент начала сохранения - "+size);
                                    if (destFolder.mkdirs()) {
                                        for (int i = 0; i < size; i++) {
                                            File fileToSave = fileDeque.pollLast();
                                            if(fileToSave!=null){
                                                System.out.println("========================================");
                                                System.out.println("Сохранили файл - "+i+1);
                                                System.out.println("Размер буфера - "+fileDeque.size());
                                                System.out.println("========================================");
                                                boolean reSave = fileToSave.renameTo(new File(destFolder, fileToSave.getName()));

                                                if(reSave){

                                                }

                                                Integer remove = buffFilesSizeImagesCount.remove(fileToSave);
                                                totalCountImages -= remove;
                                                secondsCount++;
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
                                    startSaveVideo = false;
                                }
                            } else {
                                int i = timeToSave;
                                while (fileDeque.size() > i) {
                                    if(!startSaveVideo){
                                        File fileToDel = fileDeque.pollLast();
                                        fileToDel.delete();
                                        Integer remove = buffFilesSizeImagesCount.remove(fileToDel);
                                        totalCountImages -= remove;
                                        fpsList.remove(0);
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
        saveBytesThread.start();
    }

    void addImageBytes(long time, byte[] bytes) {
        while (dequeImagesTime.contains(time)) {
            time++;
        }

        if (!dequeImagesTime.contains(time)) {
            dequeImagesTime.addFirst(time);
            buffMapImages.put(time, bytes);
            totalFPS++;
            totalCountImages++;
        } else {
            System.out.println("Однаковое время");
        }
    }

    public void startSaveVideo(boolean programSave, Date date) {
        boolean work = false;
        for (VideoCatcher catcher : catcherList) {
            work = catcher.isCatchVideo();
            if (work) {
                break;
            }
        }

        if (work) {
            int imageNumber = totalCountImages;
            System.out.println("Сработка. Кадр номер - " + imageNumber);
            eventsFramesNumber.put(imageNumber, programSave);
            if (!startSaveVideo) {
                log.info("Начинаем запись. Группа " + cameraGroupNumber + ". Кадр номер - " + imageNumber);
                startSaveVideo = true;
                this.date = date;
            } else {
                log.info("Продлжаем запись. Группа " + cameraGroupNumber + ". Кадр номер - " + imageNumber);
                stopSaveVideoInt = 0;
            }
        }
    }

    BufferedImage getBufferedImageBack() {
        return bufferedImageBack;
    }

    void addVideoCatcher(VideoCatcher videoCatcher) {
        if (!timerThread.isAlive()) {
            timerThread.start();
        }
        catcherList.add(videoCatcher);
    }

    public void setBufferedImageBack(BufferedImage bufferedImageBack) {
        this.bufferedImageBack = bufferedImageBack;
    }
}
package ui.camera;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoCreator {

    private int cameraGroupNumber;
    private int videoNumber;
    private Map<Integer, Map<Long, byte[]>> mapOfMapByte;
    private List<byte[]> list;
    private Map<Integer, Boolean> percentEventOfFrames;
    private BufferedImage bufferedImageBack;
    private int[] arr = new int[3];
    private int[] countOfVideoNumber = new int[3];
    private List<Map<Integer, Boolean>> listArr;
    private List<VideoCatcher> catcherList;


    private Deque<Long> dequeImagesTime;
    private Deque<File> fileDeque;
    private Deque<Integer> fpsDeque;
    private Map<Long, byte[]> buffMapImages;
    private Map<File, Integer> buffFilesSize;

    private int time = 0;
    private int totalFPS = 0;
    private int totalFPSForFile = 0;

    private int totalCountImages;
    private int timeToSave;
    private Map<Integer, Boolean> eventsFramesNumber;


    private boolean startSaveVideo;
    private int stopSaveVideoInt;

    private Thread timerThread;
    private Date date;

    public VideoCreator(int cameraGroupNumber) {
        listArr = new ArrayList();
        mapOfMapByte = new HashMap<>();
        catcherList = new ArrayList<>();
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;

        fileDeque = new ConcurrentLinkedDeque<>();
        buffFilesSize = new HashMap<>();
        fpsDeque = new ConcurrentLinkedDeque<>();
        dequeImagesTime = new ConcurrentLinkedDeque<>();
        buffMapImages = new HashMap<>();
        eventsFramesNumber = new HashMap<>();

        timerThread = new Thread(() -> {
            while (true) {
                try {
                    long l = System.currentTimeMillis();
                    fpsDeque.addFirst(totalFPS);
                    for (Integer integer : fpsDeque) {
                        totalFPSForFile += integer;
                    }

                    totalFPSForFile = totalFPSForFile / fpsDeque.size();
                    totalFPS = 0;
                    time++;

                    boolean creatorWork = false;
                    for (VideoCatcher catcher : catcherList) {
                        if(!creatorWork){
                            creatorWork = catcher.isCatchVideo();
                        }

                        int i = (timeToSave / 5);
                        if (fileDeque.size() > i) {
                            catcher.setBorderColor(new Color(46, 139, 87));
                        } else {
                            catcher.setBorderColor(Color.RED);
                        }
                    }

                    if(!creatorWork){
                        while (fileDeque.size() > 0) {
                            File fileToDel = fileDeque.pollLast();
                            fileToDel.delete();
                            Integer remove = buffFilesSize.remove(fileToDel);
                            totalCountImages -= remove;
                        }
                    }

                    timeToSave = MainFrame.getTimeToSave();
                    while (timeToSave < fpsDeque.size()) {
                        Integer integer = fpsDeque.pollLast();
                        for (int i = 0; i < integer; i++) {
                            Long aLong = dequeImagesTime.pollLast();
                            buffMapImages.remove(aLong);
                            totalCountImages--;
                        }
                    }

                    if (startSaveVideo) {
                        stopSaveVideoInt++;
                    }

                    long l1 = System.currentTimeMillis();
//                System.out.println("Времени затрачено " + (l1 - l) + ". Размер очереди - " + fpsDeque.size() + ". Всего изображений - " + totalCountImages);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.setName("VideoCreatorTimer Thread "+cameraGroupNumber);

        Thread saveBytesThread = new Thread(() -> {
            while (true) {
                try {
                    if (time >= 5) {
                        if(dequeImagesTime.size()>0){
                            File file = new File(MainFrame.getPath() + "\\buff\\" + cameraGroupNumber + "\\" + System.currentTimeMillis() + ".tmp");
                            int countImagesInFile = 0;
                            try {
                                if (file.createNewFile()) {
                                    file.deleteOnExit();
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    int size = fpsDeque.size();
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

                                    System.out.println(+cameraGroupNumber + "Файл добавлен. Изображений "
                                            + countImagesInFile + ". Всего файлов - " + fileDeque.size()
                                            + ". Всего изображений в буфере " + totalCountImages);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            fileDeque.addFirst(file);
                            buffFilesSize.put(file, countImagesInFile);
                        }

                        if (startSaveVideo) {
                            if (stopSaveVideoInt >= timeToSave && totalCountImages > 0) {
                                startSaveVideo = false;
                                stopSaveVideoInt = 0;

                                MainVideoCreator.stopCatchVideo();
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("[");
                                int iCount = 0;
                                int currentTotalCountImage = totalCountImages;

                                for (Integer integer : eventsFramesNumber.keySet()) {
                                    iCount++;
                                    int percent = (integer * 100) / currentTotalCountImage;
                                    System.out.println(integer + "кадр, это " + percent + " - процентов. Из - " + currentTotalCountImage);

                                    if (eventsFramesNumber.get(integer)) {
                                        stringBuilder.append("(").append(percent).append(")");
                                    } else {
                                        stringBuilder.append(percent);
                                    }
                                    if (iCount != eventsFramesNumber.size()) {
                                        stringBuilder.append(",");
                                    }
                                }

                                eventsFramesNumber.clear();
                                stringBuilder.append("]");
                                String eventPercent = stringBuilder.toString();

                                String path = MainFrame.getPath() + "\\buff\\bytes\\" + date.getTime() +
                                        "-" + cameraGroupNumber + "(" + totalFPSForFile + ")"
                                        + eventPercent + ".tmp";

                                File videoFile = new File(path);
                                try {
                                    if (videoFile.createNewFile()) {
                                        FileOutputStream fileOutputStream = new FileOutputStream(videoFile, true);
                                        int size = fileDeque.size();
                                        for (int i = 0; i < size; i++) {

                                            if (i % 2 == 0) {
                                                MainFrame.showInformMassage("Зберігаемо файли", true);
                                            } else {
                                                MainFrame.showInformMassage("Зберігаемо файли", false);
                                            }

                                            File fileToSave = fileDeque.pollLast();
                                            System.out.println("Пишем файл " + fileToSave.getAbsolutePath());
                                            byte[] buff = new byte[1024];
                                            FileInputStream fileInputStream = new FileInputStream(fileToSave);
                                            while (fileInputStream.read(buff) > 0) {
                                                fileOutputStream.write(buff);
                                            }
                                            fileInputStream.close();
                                            fileToSave.delete();

                                            Integer remove = buffFilesSize.remove(fileToSave);
                                            totalCountImages -= remove;
                                        }

                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                MainFrame.showInformMassage("Збережено файл -" + cameraGroupNumber, true);
                            }
                        } else {
                            int i = (timeToSave / 5) + 1;
                            while (fileDeque.size() > i) {
                                File fileToDel = fileDeque.pollLast();
                                fileToDel.delete();
                                Integer remove = buffFilesSize.remove(fileToDel);
                                totalCountImages -= remove;
                            }
                        }
                        time = 0;
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveBytesThread.setName("Video Creator SaveBytesThread "+cameraGroupNumber);
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
        if (!startSaveVideo) {
            startSaveVideo = true;
            this.date = date;
        } else {
            stopSaveVideoInt = 0;
        }

        int imageNumber = totalCountImages;
        System.out.println("Событие, кадр номер " + imageNumber);
        eventsFramesNumber.put(imageNumber, programSave);
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

//    private void connectVideoBytesFromMap() {
//
//        Map<Long, byte[]> map1 = mapOfMapByte.get(1);
//        Map<Long, byte[]> map2 = mapOfMapByte.get(2);
//        percentEventOfFrames = new HashMap<>();
//        Map<Integer, Boolean> integerBooleanMap = listArr.get(0);
//        for (Integer integer : integerBooleanMap.keySet()) {
//            Boolean aBoolean = integerBooleanMap.get(integer);
//            double d1 = (double) integer / map1.size();
//            percentEventOfFrames.put((int) (d1 * 100), aBoolean);
//        }
//
//        List<Long> listOfLongs = new ArrayList<>();
//
//        if (map1 != null) {
//            for (Long l : map1.keySet()) {
//                if (!listOfLongs.contains(l)) {
//                    listOfLongs.add(l);
//                }
//            }
//        }
//
//        if (map2 != null) {
//            for (Long l : map2.keySet()) {
//                if (!listOfLongs.contains(l)) {
//                    listOfLongs.add(l);
//                }
//            }
//        }
//
//        System.out.println("Размер листа - " + listOfLongs.size());
//        Collections.sort(listOfLongs);
//
//        for (Long l : listOfLongs) {
//
//            if (map1 != null && map1.containsKey(l)) {
//                byte[] bytes = map1.get(l);
//                if (bytes != null) {
//                    list.add(bytes);
//                }
//            }
//
//            if (map2 != null && map2.containsKey(l)) {
//                byte[] bytes = map2.get(l);
//                if (bytes != null) {
//                    list.add(bytes);
//                }
//            }
//        }
//        mapOfMapByte.clear();
//    }
}
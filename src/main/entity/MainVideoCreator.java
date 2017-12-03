package entity;

import javafx.scene.Camera;
import ui.camera.CameraPanel;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import entity.sound.SoundSaver;
import org.apache.log4j.Logger;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * class for start/stop save bytes from cameras, creating video files and save it to disk
 */
public class MainVideoCreator {
    private static Logger log = Logger.getLogger(MainVideoCreator.class);
    /**
     * date for saving time, when lightning was
     */
    private static Date date;

    /**
     * this boolean mark saving video now
     */
    private static boolean saveVideoEnable;
    /**
     * Thread run the starting video from cameras
     */
    private static Thread startSaveVideoForAllCreatorsThread;
    /**
     * Thread start work when have one more lightning during saving video, mark it,
     * and block marking lightnings more then one for one second.
     */
    private static Thread continueSaveVideoThread;
    /**
     * int to show on inform panel, how many seconds already saved
     */
    private static int secondVideoAlreadySave = 1;

    /**
     * @param programingLightCatch - program or sensor catch lightning
     */
    public static void startCatchVideo(boolean programingLightCatch) {
        boolean anyCameraEnable = false;

        Map<Integer, CameraPanel> cameras = MainFrame.getCameras();
        for (Integer integer : cameras.keySet()) {
            CameraPanel cameraPanel = cameras.get(integer);
            anyCameraEnable = cameraPanel.getVideoCatcher().isCatchVideo();
            if (anyCameraEnable) {
                break;
            }
        }

        if (anyCameraEnable) {
            SoundSaver soundSaver = MainFrame.getMainFrame().getSoundSaver();
            if (soundSaver != null) {
                soundSaver.startSaveAudio();
            }

            String event;
            if (programingLightCatch) {
                event = ". Сработка - програмная.";
            } else {
                event = ". Сработка - аппаратная.";
            }

            if (!isSaveVideoEnable()) {
                date = new Date(System.currentTimeMillis());
                log.info("Событие " + date.toString() + event + ". Сохраняем секунд - " + MainFrame.getSecondsToSave());
                startSaveVideoForAllCreatorsThread = new Thread(() -> {
                    MainVideoCreator.setSaveVideo();
                    while (saveVideoEnable) {
                        MainFrame.showSecondsAlreadySaved(MainFrame.getBundle().getString("savedword") +
                                (secondVideoAlreadySave++) + MainFrame.getBundle().getString("seconds"));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    secondVideoAlreadySave = 1;
                    startSaveVideoForAllCreatorsThread = null;
                });
                startSaveVideoForAllCreatorsThread.start();
            } else {
                log.info("Еще одна сработка, продолжаем событие " + date.toString() + event);
                secondVideoAlreadySave = 1;
            }

            if (continueSaveVideoThread == null) {
                continueSaveVideoThread = new Thread(() -> {
                    for (Integer creator : MainFrame.videoSaversMap.keySet()) {
                        MainFrame.videoSaversMap.get(creator).startSaveVideo(programingLightCatch, date);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continueSaveVideoThread = null;
                });
                continueSaveVideoThread.start();
            } else {
                log.info("С прошлой сработки не прошло 2 секунды.");
            }
        }
    }

    /**
     * stop catch bytes from cameras
     */
    public static void stopCatchVideo() {
        SoundSaver soundSaver = MainFrame.getMainFrame().getSoundSaver();
        if (soundSaver != null) {
            soundSaver.stopSaveAudio();
        }
        MainFrame.showSecondsAlreadySaved(MainFrame.getBundle().getString("endofsaving"));
        saveVideoEnable = false;
    }

    /**
     * @param map -map with bytes from rtp packets from audio module and time, when it was saved to RAM
     */
    public static void saveAudioBytes(Map<Long, byte[]> map) {
        int size = 0;
        List<Long> longList = new ArrayList<>();

        for (Long l : map.keySet()) {
            longList.add(l);
        }

        Collections.sort(longList);
        for (Long integer : map.keySet()) {
            byte[] bytes = map.get(integer);
            size = size + bytes.length;
        }

        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(35535);
        for (Long l : longList) {
            byte[] bytes = map.get(l);
            try {
                temporaryStream.write(bytes);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        ByteArrayInputStream interleavedStream = new ByteArrayInputStream(temporaryStream.toByteArray());
        final AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.ULAW,
                8000f, // sample rate - you didn't specify, 44.1k is typical
                8, // how many bits per sample, i.e. per value in your byte array
                1,       // you want two channels (stereo)
                1,      // number of bytes per frame (frame == a sample for each channel)
                8000f,  // frame rate
                true);  // byte order

        final int numberOfFrames = size;
        File audioFile = new File(MainFrame.getPath() + "\\bytes\\" + date.getTime() + ".wav");
        final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);

        try {
            if (audioFile.createNewFile()) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            log.error(e1.getLocalizedMessage());
        }
    }

    /**
     * encoding and saving video file to from bytes
     *
     * @param folderWithTemporaryFiles - link to folder with bytes
     */
    public static void encodeVideoXuggle(File folderWithTemporaryFiles) {
        String name = folderWithTemporaryFiles.getName();
        String[] split = name.split("-");
        long dateLong = Long.parseLong(split[0]);

        Date date = new Date(dateLong);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("dd MMMM yyyy,HH-mm-ss");
        String dateString = dateFormat.format(date);

        String audioPath = MainFrame.getPath() + "\\bytes\\" + dateLong + ".wav";
        File audioFile = new File(audioPath);
        if (audioFile.exists()) {
            File newAudioFile = new File(MainFrame.getPath() + "\\" + dateString + ".wav");
            try {
                if (newAudioFile.createNewFile()) {
                    log.info("Сохраняем аудиофайл " + newAudioFile.getAbsolutePath());
                    FileInputStream fileInputStream = new FileInputStream(audioFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(newAudioFile);
                    byte[] buff = new byte[1024];
                    while (fileInputStream.read(buff) > 0) {
                        fileOutputStream.write(buff);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    fileInputStream.close();
                    audioFile.delete();
                    log.info("Аудиофайл сохранен. " + newAudioFile.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] fpsSplit = split[1].split("\\.");
        String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
        int integer = 0;

        try {
            integer = Integer.parseInt(numberOfGroupCameraString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = fpsSplit[0].indexOf(")");
        String totalFpsString = fpsSplit[0].substring(2, i);
        int totalFPS = Integer.parseInt(totalFpsString);


        String path = MainFrame.getPath() + "\\" + dateString + ", group -" + numberOfGroupCameraString + ".mp4";
        log.info("Сохраняем видеофайл " + path);
        float opacity = 0f;
        BufferedImage imageToConnect = null;
        boolean connectImage = false;


        String absolutePathToImage = folderWithTemporaryFiles.getAbsolutePath().replace(".tmp", ".jpg");
        File imageFile = new File(absolutePathToImage);
        if (imageFile.exists()) {
            try {
                imageToConnect = ImageIO.read(new FileInputStream(imageFile));
                opacity = CameraPanel.getOpacity();
                connectImage = true;
                log.info("Накладываем изображение на файл " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        File videoFile = new File(path);
        if (videoFile.exists()) {
            videoFile.delete();
        }

        try {
            if (videoFile.createNewFile()) {
                final IMediaWriter writer = ToolFactory.makeWriter(path);
                boolean addVideoStream = false;
                long nextFrameTime = 0;
                final long frameRate = (1000 / totalFPS);
                int count = 0;
                int countImageNotSaved = 0;

                FileInputStream fileInputStream = null;
                File[] temporaryFiles = folderWithTemporaryFiles.listFiles();


                int heightStream = 0;
                int weightStream = 0;

                for (File file : temporaryFiles) {
                    try {
                        fileInputStream = new FileInputStream(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream bufferedInputStream = null;
                    if (fileInputStream != null) {
                        bufferedInputStream = new BufferedInputStream(fileInputStream);
                        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);

                        int x = 0;
                        int t;

                        BufferedImage image = null;
                        while (x >= 0) {
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
                                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

                                try {
                                    image = ImageIO.read(inputStream);
                                } catch (Exception ignored) {
                                }

                                if (image != null) {
                                    if (!addVideoStream) {
                                        heightStream = image.getHeight();
                                        weightStream = image.getWidth();

                                        writer.addVideoStream(0, 0,
                                                ICodec.ID.CODEC_ID_MPEG4,
                                                image.getWidth(), image.getHeight());
                                        addVideoStream = true;
                                    }

                                    try {
                                        if (connectImage) {
                                            BufferedImage bufferedImage = connectImage(image, imageToConnect, opacity);
                                            writer.encodeVideo(0, bufferedImage, nextFrameTime, TimeUnit.MILLISECONDS);
                                        } else {
                                            writer.encodeVideo(0, image, nextFrameTime,
                                                    TimeUnit.MILLISECONDS);
                                        }
                                    } catch (Exception e) {

                                        count--;
                                        countImageNotSaved++;
                                        System.out.println("Размер потока - " + heightStream + " : " + weightStream);
                                        System.out.println("Размер ИЗОБРАЖЕНИЯ - " + image.getHeight() + " : " + image.getWidth());
                                        System.out.println("Изображение другого размера ");
                                        System.out.println("Сохранено - " + count);
                                        System.out.println("НЕ СОХРАНЕНО " + countImageNotSaved);
                                    }


                                    image = null;
                                    if (count % 2 == 0) {
                                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveframenumber") +
                                                count++, new Color(23, 114, 26));
                                    } else {
                                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveframenumber") +
                                                count++, new Color(181, 31, 27));
                                    }
                                    nextFrameTime += frameRate;
                                } else {
                                    countImageNotSaved++;
                                }
                            }
                        }
                        temporaryStream.close();
                        fileInputStream.close();
                        bufferedInputStream.close();
                    }
                }

                writer.flush();
                writer.close();
                MainFrame.showInformMassage(MainFrame.getBundle().getString("encodingdone") + count, new Color(23, 114, 26));

                Date videoLenght = new Date(nextFrameTime);
                dateFormat.applyPattern("mm:ss");
                log.info("Видеофайл сохранен - " + path +
                        ". Сохранено кадров - " + count +
                        ". Не сохранено кадров - " + countImageNotSaved +
                        ". Длинна видео - " + dateFormat.format(videoLenght));
                System.out.println("Видеофайл сохранен - " + path +
                        ". Сохранено кадров - " + count +
                        ". Не сохранено кадров - " + countImageNotSaved +
                        ". Длинна видео - " + dateFormat.format(videoLenght));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * * encoding and saving part of video file to disk from bytes
     *
     * @param pathToFileToSave  - path to file, to save data to
     * @param filesListToEncode - links for files with bytes from camera
     * @param totalFPS          - FPS for video file
     * @param backgroundImage   - image for connecting background for video (if exist)
     */
    public static void savePartOfVideoFile(String pathToFileToSave, List<File> filesListToEncode, int totalFPS, BufferedImage backgroundImage) {
        File videoFile = new File(pathToFileToSave);
        if (videoFile.exists()) {
            videoFile.delete();
        }
        boolean connectImage = false;
        float opacity = 0;
        if (backgroundImage != null) {
            connectImage = true;
            opacity = CameraPanel.getOpacity();
        }

        try {
            if (videoFile.createNewFile()) {
                final IMediaWriter writer = ToolFactory.makeWriter(pathToFileToSave);
                boolean addVideoStream = false;
                long nextFrameTime = 0;
                final long frameRate = (1000 / totalFPS);
                int count = 0;
                int countImageNotSaved = 0;

                FileInputStream fileInputStream = null;

                for (File file : filesListToEncode) {
                    try {
                        fileInputStream = new FileInputStream(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream bufferedInputStream = null;
                    if (fileInputStream != null) {
                        bufferedInputStream = new BufferedInputStream(fileInputStream);
                        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);

                        int x = 0;
                        int t;

                        BufferedImage image = null;
                        while (x >= 0) {
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
                                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

                                try {
                                    image = ImageIO.read(inputStream);
                                } catch (Exception ignored) {
                                }

                                if (image != null) {
                                    if (!addVideoStream) {
                                        writer.addVideoStream(0, 0,
                                                ICodec.ID.CODEC_ID_MPEG4,
                                                image.getWidth(), image.getHeight());
                                        addVideoStream = true;
                                    }

                                    if (connectImage) {
                                        writer.encodeVideo(0, connectImage(image, backgroundImage, opacity), nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    } else {
                                        writer.encodeVideo(0, image, nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    }
                                    image = null;


                                    if (count % 2 == 0) {
                                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveframenumber") +
                                                count++, new Color(23, 114, 26));
                                    } else {
                                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveframenumber") +
                                                count++, new Color(181, 31, 27));
                                    }
                                    nextFrameTime += frameRate;
                                } else {
                                    countImageNotSaved++;
                                }
                            }
                        }
                        temporaryStream.close();
                        fileInputStream.close();
                        bufferedInputStream.close();
                    }
                }

                writer.flush();
                writer.close();
                MainFrame.showInformMassage(MainFrame.getBundle().getString("encodingdone") + count, new Color(23, 114, 26));

                log.info("Видеофайл сохранен - " + pathToFileToSave +
                        ". Сохранено кадров - " + count +
                        ". Не сохранено кадров - " + countImageNotSaved);
                System.out.println("Видеофайл сохранен - " + pathToFileToSave +
                        ". Сохранено кадров - " + count +
                        ". Не сохранено кадров - " + countImageNotSaved);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * connecting image
     *
     * @param sourceImage    - source image
     * @param imageToConnect - image to connect
     * @param opacity        - opacity of image to connect
     * @return - image, which was created from two images
     */
    public static BufferedImage connectImage(BufferedImage sourceImage, BufferedImage imageToConnect, float opacity) {
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        graphics.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.drawImage(imageToConnect, 0, 0, imageToConnect.getWidth(), imageToConnect.getHeight(), null);
        graphics.dispose();
        return image;
    }

    public static boolean isSaveVideoEnable() {
        return saveVideoEnable;
    }

    private static void setSaveVideo() {
        MainVideoCreator.saveVideoEnable = true;
    }
}
package entity;

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

public class MainVideoCreator {
    private static Logger log = Logger.getLogger(MainVideoCreator.class);
    private static Date date;
    private static boolean saveVideo;
    private static Thread continueVideoThread;
    private static Thread startSaveVideoThread;
    private static int secondVideoSave = 1;

    public static void startCatchVideo(boolean programingLightCatch) {

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

        if (!isSaveVideo()) {
            date = new Date(System.currentTimeMillis());
            log.info("Событие " + date.toString() + event);
            continueVideoThread = new Thread(() -> {
                MainVideoCreator.setSaveVideo(true);
                while (saveVideo) {
                    MainFrame.showSecondsAlreadySaved(MainFrame.getBundle().getString("savedword") +
                            (secondVideoSave++) + MainFrame.getBundle().getString("seconds"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                secondVideoSave = 1;
                continueVideoThread = null;
            });
            continueVideoThread.start();
        } else {
            log.info("Продолжаем событие " + date.toString() + event);
            secondVideoSave = 1;
        }


        if (startSaveVideoThread == null) {
            startSaveVideoThread = new Thread(() -> {
                for (Integer creator : MainFrame.creatorMap.keySet()) {
                    MainFrame.creatorMap.get(creator).startSaveVideo(programingLightCatch, date);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startSaveVideoThread = null;
            });
            startSaveVideoThread.start();
        }
    }

    public static void stopCatchVideo() {

        SoundSaver soundSaver = MainFrame.getMainFrame().getSoundSaver();
        if (soundSaver != null) {
            soundSaver.stopSaveAudio();
        }
        MainFrame.showSecondsAlreadySaved(MainFrame.getBundle().getString("endofsaving"));
        saveVideo = false;
    }

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
                8,      // how many bits per sample, i.e. per value in your byte array
                1,      // you want two channels (stereo)
                1,      // number of bytes per frame (frame == a sample for each channel)
                8000f, // frame rate
                true);  // byte order

        final int numberOfFrames = size;
        File audioFile = new File(MainFrame.getPath() + "\\bytes\\" + date.getTime() + ".wav");
        final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);

        try {
            if (audioFile.createNewFile()) {//TODO vkjnguygyfgiyhfgnykhfgiykj
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            log.error(e1.getLocalizedMessage());
        }
//        String path = MainFrame.getPath()+"\\buff\\bytes\\" + date.getTime() + ".sound";
//        File file = new File(path);
//        try {
//            if (file.createNewFile()) {
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
//                for (long l : map.keySet()) {
//                    byte[] bytes = map.get(l);
//                    try {
//                        if (bytes != null) {
//                            fileOutputStream.write(bytes);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void encodeVideoXuggle(File folderWithTempraryFiles) {

        String name = folderWithTempraryFiles.getName();
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


        String absolutePathToImage = folderWithTempraryFiles.getAbsolutePath().replace(".tmp", ".jpg");
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
                File[] temporaryFiles = folderWithTempraryFiles.listFiles();

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
                                        writer.addVideoStream(0, 0,
                                                ICodec.ID.CODEC_ID_MPEG4,
                                                image.getWidth(), image.getHeight());
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            writer.addAudioStream(ICodec.ID.CODEC_ID_AAC)
//                            byte[] audioBytes = new byte[line.getBufferSize() / 2]; // best size?
//                            int numBytesRead = 0;
//                            numBytesRead = line.read(audioBytes, 0, audioBytes.length);
//                            // convert to signed shorts representing samples
//                            int numSamplesRead = numBytesRead / 2;
//                            short[] audioSamples = new short[numSamplesRead];
//                            if (format.isBigEndian()) {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i] << 8) | audioBytes[2 * i + 1]);
//                                }
//                            } else {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i + 1] << 8) | audioBytes[2 * i]);
//                                }
//                            }
//                            writer.encodeAudio(0,audioSamples);?????????/
//                             use audioSamples in Xuggler etc
////                            ========================================================
////                            ========================================================
////                            ========================================================
                                        addVideoStream = true;
                                    }

                                    if (connectImage) {
                                        writer.encodeVideo(0, connectImage(image, imageToConnect, opacity), nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    } else {
                                        writer.encodeVideo(0, image, nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    }
                                    image = null;
                                    if (count % 2 == 0) {
                                        MainFrame.showInformMassage(MainFrame.getBundle().getString("saveframenumber")+
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

    public static void savePartOfVideoFile(String pathToFileToSave, List<File> filesToEncodeToVideo, int totalFPS, BufferedImage imageToConnect) {

        File videoFile = new File(pathToFileToSave);
        if (videoFile.exists()) {
            videoFile.delete();
        }

        boolean connectImage = false;
        float opacity = 0;
        if (imageToConnect != null) {
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

                for (File file : filesToEncodeToVideo) {
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
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            writer.addAudioStream(ICodec.ID.CODEC_ID_AAC)
//                            byte[] audioBytes = new byte[line.getBufferSize() / 2]; // best size?
//                            int numBytesRead = 0;
//                            numBytesRead = line.read(audioBytes, 0, audioBytes.length);
//                            // convert to signed shorts representing samples
//                            int numSamplesRead = numBytesRead / 2;
//                            short[] audioSamples = new short[numSamplesRead];
//                            if (format.isBigEndian()) {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i] << 8) | audioBytes[2 * i + 1]);
//                                }
//                            } else {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i + 1] << 8) | audioBytes[2 * i]);
//                                }
//                            }
//                            writer.encodeAudio(0,audioSamples);?????????/
//                             use audioSamples in Xuggler etc
////                            ========================================================
////                            ========================================================
////                            ========================================================
                                        addVideoStream = true;
                                    }

                                    if (connectImage) {
                                        writer.encodeVideo(0, connectImage(image, imageToConnect, opacity), nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    } else {
                                        writer.encodeVideo(0, image, nextFrameTime,
                                                TimeUnit.MILLISECONDS);
                                    }
                                    image = null;
                                    if (count % 2 == 0) {
                                        MainFrame.showInformMassage("Зберігаем кадр - " + count++, new Color(23, 114, 26));
                                    } else {
                                        MainFrame.showInformMassage("Зберігаем кадр - " + count++, new Color(181, 31, 27));
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
                MainFrame.showInformMassage("Збережено. Кадрів - " + count, new Color(23, 114, 26));

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

    private static BufferedImage connectImage(BufferedImage sourceImage, BufferedImage imageToConnect, float opacity) {
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        graphics.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.drawImage(imageToConnect, 0, 0, imageToConnect.getWidth(), imageToConnect.getHeight(), null);
        graphics.dispose();
        return image;
    }

    public static boolean isSaveVideo() {
        return saveVideo;
    }

    private static void setSaveVideo(boolean saveVideo) {
        MainVideoCreator.saveVideo = saveVideo;
    }
}
package entity;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import org.apache.log4j.Logger;
import ui.camera.CameraPanel;
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
    private static int secondVideoSave;

    public static void startCatchVideo(boolean programingLightCatch) {
        if (!isSaveVideo()) {
            date = new Date(System.currentTimeMillis());
            log.info("Событие "+date.toString());
            continueVideoThread = new Thread(() -> {
                MainVideoCreator.setSaveVideo(true);
                while (saveVideo) {
                    MainFrame.showInformMassage("Збережено " + (secondVideoSave++) + " сек.", true);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                secondVideoSave = 0;
                continueVideoThread = null;
            });
            continueVideoThread.start();
        } else {
            log.info("Продолжаем событие "+date.toString());
            secondVideoSave = 0;
        }




        for(Integer creator:MainFrame.creatorMap.keySet()){
            MainFrame.creatorMap.get(creator).startSaveVideo(programingLightCatch,date);
        }
    }

    public static void stopCatchVideo() {
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
        File audioFile = new File("C:\\ipCamera\\bytes\\" + date.getTime() + ".wav");
        final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);

        try {
            if (audioFile.createNewFile()) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        String path = MainFrame.getPath()+"\\buff\\bytes\\" + date.getTime() + ".sound";
        System.out.println("Создаем аудио файл - " + path);

        File file = new File(path);
        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                for (long l : map.keySet()) {
                    byte[] bytes = map.get(l);
                    try {
                        if (bytes != null) {
                            fileOutputStream.write(bytes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encodeVideoXuggle(File file) {
        String name = file.getName();
        String[] split = name.split("-");
        long dateLong = Long.parseLong(split[0]);
        Date date = new Date(dateLong);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("dd MMMM yyyy,HH-mm-ss");
        String dateString = dateFormat.format(date);
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

        System.out.println("Общий FPS String = " + totalFpsString);
        System.out.println("Общий FPS = " + totalFPS);

        String path = MainFrame.getPath() +"\\"+ dateString + ", Группа камер -" + numberOfGroupCameraString + ".mp4";
        float opacity = 0f;
        BufferedImage imageToConnect = null;
        boolean connectImage = false;
        if (MainFrame.imagesForBlock.containsKey(integer)) {
            imageToConnect = MainFrame.imagesForBlock.get(integer);
            opacity = CameraPanel.getOpacity();
            connectImage = true;
            System.out.println("Соединяем изображения - " + connectImage + ". Прозрачность - " + opacity);
        }

        File videoFile = new File(path);
        if (videoFile.exists()) {
            videoFile.delete();
        }
        try {
            videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final IMediaWriter writer = ToolFactory.makeWriter(path);


        boolean addVideoStream = false;
        long nextFrameTime = 0;
        final long frameRate = 1000 / totalFPS;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedInputStream bufferedInputStream = null;
        if (fileInputStream != null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
            int count = 0;
            int x = 0;
            int t;
            int countImageNotSaved = 0;
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
//                            BufferedImage conImage = connectImage(image, imageToConnect, opacity);
                            writer.encodeVideo(0, connectImage(image, imageToConnect, opacity), nextFrameTime,
                                    TimeUnit.MILLISECONDS);
                        } else {
                            writer.encodeVideo(0, image, nextFrameTime,
                                    TimeUnit.MILLISECONDS);
                        }
                        image = null;
                        MainFrame.showInformMassage("Зберігаем кадр - " + count++, true);
                        nextFrameTime += frameRate;
                    } else {
                        countImageNotSaved++;
                    }
                }
            }
            writer.flush();
            writer.close();
            MainFrame.showInformMassage("Збережено. Кадрів - " + count, true);
            System.out.println("Сохранено кадров " + count + ". Не сохранено " + countImageNotSaved);
        }

        try {
            fileInputStream.close();
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    //    public static void putVideoFromCameraGroup(Integer numberOfGroup, List<byte[]> list, int totalFps, Map<Integer, Boolean> percentOfFramesEvent, int videoNumber) {
//        MainFrame.showInformMassage("Зберігаем файл - " + numberOfGroup, true);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Thread thread = new Thread(() -> saveBytes(numberOfGroup, list, totalFps, percentOfFramesEvent, videoNumber));
//        thread.setName("SaveBytesThread. Number " + numberOfGroup);
//        thread.start();
//    }
//    private static void saveBytes(Integer numberOfGroup, List<byte[]> list, int totalFPS, Map<Integer, Boolean> percentOfFramesEvent, int videoNumber) {
//
////        StringBuilder stringBuilder = new StringBuilder();
////        stringBuilder.append("[");
////        int i = 0;
////
////        for (Integer integer : percentOfFramesEvent.keySet()) {
////            i++;
////            if (percentOfFramesEvent.get(integer)) {
////                stringBuilder.append("(").append(integer).append(")");
////            } else {
////                stringBuilder.append(integer);
////            }
////            if (i != percentOfFramesEvent.size()) {
////                stringBuilder.append(",");
////            }
////        }
////
////        stringBuilder.append("]");
////        String eventPercent = stringBuilder.toString();
//        String path;
////        = "C:\\ipCamera\\bytes\\" + (date.getTime() + videoNumber) + "-" + numberOfGroup + "(" + totalFPS + ")" + eventPercent + "{" + videoNumber + "}.tmp";
//        File file;
////        = new File(path);
//
//
//
//        boolean newFile = false;
//        if (videoNumber == 0) {
//            System.out.println("ZERO part " + numberOfGroup);
//            path = "C:\\ipCamera\\bytes\\" + numberOfGroup + ".tmp";
////            path = "C:\\ipCamera\\bytes\\" + date.getTime()  + "-" + numberOfGroup+ ".tmp";
//            file = new File(path);
//            if(file.exists()){
//                file.delete();
//            }
//            newFile = true;
//        } else {
//
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("[");
//            int i = 0;
//
//            for (Integer integer : percentOfFramesEvent.keySet()) {
//                i++;
//                if (percentOfFramesEvent.get(integer)) {
//                    stringBuilder.append("(").append(integer).append(")");
//                } else {
//                    stringBuilder.append(integer);
//                }
//                if (i != percentOfFramesEvent.size()) {
//                    stringBuilder.append(",");
//                }
//            }
//
//            stringBuilder.append("]");
//            String eventPercent = stringBuilder.toString();
//
//            path = "C:\\ipCamera\\bytes\\" + (date.getTime() + videoNumber) + "-" + numberOfGroup + "(" + totalFPS + ")" + eventPercent + "{" + videoNumber + "}.tmp";
//            String pathZero = "C:\\ipCamera\\bytes\\" +numberOfGroup + ".tmp";
//            file = new File(pathZero);
//            if (!file.exists()) {
//                file = new File(path);
//                newFile = true;
//            }
////            System.out.println("NEXT part " + numberOfGroup);
////            path = "C:\\ipCamera\\bytes\\" + (date.getTime() + videoNumber) + "-" + numberOfGroup + "(" + totalFPS + ")" + eventPercent + "{" + videoNumber + "}.tmp";
////            if (videoNumber != 0) {
//////            if (videoNumber == 1) {
////                String pathZero = "C:\\ipCamera\\bytes\\" +numberOfGroup + ".tmp";
//////                String pathZero = "C:\\ipCamera\\bytes\\" + date.getTime() + "-" + numberOfGroup + ".tmp";
////                file = new File(pathZero);
////                if (!file.exists()) {
////                    file = new File(path);
////                    newFile = true;
////                }
////            } else {
////                file = new File(path);
////                newFile = true;
////            }
//        }
//        try {
//            if (newFile) {
//                file.createNewFile();
//            }
//
////            if(file.createNewFile()){
//                System.out.println("Пишем в файл - " + file.getAbsolutePath());
//                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
//                for (byte[] arr : list) {
//                    try {
//                        if (arr != null) {
//                            fileOutputStream.write(arr);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (videoNumber == 2) {
//            file.renameTo(new File(path));
//        }
//
//        log.info("Сохранили байты с потока группы - "+numberOfGroup);
//        MainFrame.showInformMassage("Блок - " + numberOfGroup + " збережено.", true);
//    }

//    public static void encodeVideo(File file) {
//        try {
//            String name = file.getName();
//            String[] split = name.split("-");
//            long dateLong = Long.parseLong(split[0]);
//            Date date = new Date(dateLong);
//            SimpleDateFormat dateFormat = new SimpleDateFormat();
//            dateFormat.applyPattern("dd MM yyyy_HH-mm-ss");
//            String dateString = dateFormat.format(date);
//            String[] fpsSplit = split[1].split("\\.");
//            String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
//            String totalFpsString = fpsSplit[0].substring(2, 4);
//            int totalFPS = Integer.parseInt(totalFpsString);
//            String path = "C:\\ipCamera\\" + dateString + "_" + numberOfGroupCameraString + ".mov";
//
//            File videoFile = new File(path);
//            if (!videoFile.exists()) {
//                try {
//                    videoFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            SeekableByteChannel out = null;
//            try {
//                out = NIOUtils.writableFileChannel(path);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            AWTSequenceEncoder encoder = null;
//            try {
//                encoder = new AWTSequenceEncoder(out, Rational.R(totalFPS, 1));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            FileInputStream fileInputStream = null;
//            try {
//                fileInputStream = new FileInputStream(file);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//            if (bufferedInputStream != null) {
//                ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//                int count = 0;
//                int x = 0;
//                int t = 0;
//
//                BufferedImage image = null;
//                while (x >= 0) {
//                    t = x;
//                    try {
//                        x = bufferedInputStream.read();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    temporaryStream.write(x);
//                    if (x == 216 && t == 255) {// начало изображения
//                        temporaryStream.reset();
//
//                        temporaryStream.write(t);
//                        temporaryStream.write(x);
//                    } else if (x == 217 && t == 255) {//конец изображения
//                        byte[] imageBytes = temporaryStream.toByteArray();
//                        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//
//                        try {
//                            image = ImageIO.read(inputStream);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (image != null) {
//                            try {
//                                encoder.encodeImage(image);
////                            encoder.encodeImage(imageToConnect);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            System.out.println("Пишем изображение - " + count++);
//                            MainFrame.showInformMassage("Зберігаем кадр - " + count++, true);
//                            image = null;
//                        }
//                    }
//                }
//
//                try {
//                    encoder.finish();
//                    bufferedInputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                NIOUtils.closeQuietly(out);
//                System.out.println("Сохраняем файл. Количесво изображений - " + count);
//                MainFrame.showInformMassage("Файл збережено. Всьго кадрів - " + count, true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public static boolean isProgramingLightCatch() {
//        return programingLightCatch;
//    }
}
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

//    private static Map<Integer,Thread> threadMap;
//    private static List<Integer> saveVideoGroupsNumbers = new ArrayList<>();
//    private static Thread mainCreatorThread = new Thread(()->{
//        while (true){
//            try{
//                if(saveVideoGroupsNumbers.size()>0){
//                    Integer integer = saveVideoGroupsNumbers.get(0);
//                    Thread thread = threadMap.get(integer);
//                    thread.start();
//
//                    while (thread.isAlive()){
//                        Thread.sleep(100);
//                    }
//
//                    saveVideoGroupsNumbers.remove(integer);
//                    threadMap.remove(integer);
//                } else {
//                  Thread.sleep(1000);
//                }
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    });
//    private void saveBytesFromVideoCreator(int numberGroup){
//        if(threadMap==null){
//            threadMap = new HashMap<>();
//        }
//
//        if(!threadMap.containsKey(numberGroup)){
//            Thread thread = new Thread(()->{
//
//            });
//            threadMap.put(numberGroup,thread);
//        }
//
//        if(threadMap.size()>0){
//
//        }
//    }

    public static void startCatchVideo(boolean programingLightCatch) {
//        if(!mainCreatorThread.isAlive()){
//            mainCreatorThread.start();
//        }
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
        File audioFile = new File(MainFrame.getPath()+"\\buff\\bytes\\" + date.getTime() + ".wav");
        final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);

        try {
            if (audioFile.createNewFile()) {//TODO vkjnguygyfgiyhfgnykhfgiykj
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
}
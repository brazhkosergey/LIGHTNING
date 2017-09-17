package entity;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import ui.video.VideoFilesPanel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainVideoCreator {
    private static Date date;
    private static MainVideoCreator mainVideoCreator;
    private static boolean saveVideo = false;

    private MainVideoCreator() {
    }

    public static MainVideoCreator getMainVideoCreator() {
        if (mainVideoCreator != null) {
            return mainVideoCreator;
        } else {
            mainVideoCreator = new MainVideoCreator();
            return mainVideoCreator;
        }
    }

    public static void startCatchVideo(Date date) {
        MainVideoCreator.date = date;
        saveVideo = true;
    }

    public static void stopCatchVideo() {
        saveVideo = false;
    }

    private static void saveBytes(Integer numberOfGroup,List<byte[]>list){
        String path = "C:\\ipCamera\\"+date.getTime()+"-"+numberOfGroup+".tmp";
        System.out.println("Создаем файл - "+path);
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for(byte[] arr:list){
                try {
                    fileOutputStream.write(arr);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveVideo(Integer numberOfGroup,List<BufferedImage>list){

//        String path = "C:\\ipCamera\\"+date.getTime()+"-"+numberOfGroup+".mp4";
        String path = null;
        if(date!=null){
            path = "C:\\ipCamera\\"+date.getTime()+"-"+numberOfGroup+".mpeg";
        }else {
            path = "C:\\ipCamera\\"+System.currentTimeMillis()+"-"+numberOfGroup+".mpeg";
        }



        System.out.println("Создаем файл - "+path);
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(path);
            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(10, 1));
            for (int i=0;i<list.size();i++) {
                BufferedImage image = list.get(i);
                encoder.encodeImage(image);
            }
            encoder.finish();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            NIOUtils.closeQuietly(out);
        }
    }

    public static void putVideoFromCameraGroup(Integer numberOfGroup, List<byte[]> list) {
        saveBytes(numberOfGroup,list);
        System.out.println("Номер группы - " + numberOfGroup + ". Размер листа в изображениями - " + list.size());
        System.out.println("размер общей мапы равен: " + VideoFilesPanel.map.size());
    }

    public static boolean isSaveVideo() {
        return saveVideo;
    }

    public static void setSaveVideo(boolean saveVideo) {
        MainVideoCreator.saveVideo = saveVideo;
    }
}

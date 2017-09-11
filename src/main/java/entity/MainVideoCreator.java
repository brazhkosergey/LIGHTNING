package entity;

import ui.main.MainFrame;
import ui.video.VideoPanel;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainVideoCreator {
    private static Date date;
    private static Map<Integer,List<BufferedImage>> map = new HashMap<>();
    private static MainVideoCreator mainVideoCreator;
    private static boolean saveVideo = false;
    private MainVideoCreator(){
        map = new HashMap<>();
    }

    public static MainVideoCreator getMainVideoCreator(){
        if(mainVideoCreator!=null){
            return mainVideoCreator;
        } else {
            mainVideoCreator = new MainVideoCreator();
            return mainVideoCreator;
        }
    }

    public static void startCatchVideo(Date date){
        MainVideoCreator.date = date;
        saveVideo = true;
    }

    public static void stopCatchVideo(){
        saveVideo = false;
    }

    public static void putVideoFromCameraGroup(Integer numberOfGroup,List<BufferedImage> list){
        MainVideoCreator.map.put(numberOfGroup,list);
        System.out.println("Номер группы - "+numberOfGroup+". Размер листа в изображениями - "+list.size());
        System.out.println("ДОбавили видео в мапу в главном классе создателе видео. Размер мапы "+map.size());
//        if(MainVideoCreator.map.size() >= MainFrame.cameras.size()/2){
            VideoPanel.map.put(date,map);
            System.out.println("размер общей мапы равен: "+VideoPanel.map.size());
            date = new Date();
            map = new HashMap<>();
            System.out.println("Добавили видео в видео панель.");
//        }
    }

    public static boolean isSaveVideo() {
        return saveVideo;
    }

    public static void setSaveVideo(boolean saveVideo) {
        MainVideoCreator.saveVideo = saveVideo;
    }
}

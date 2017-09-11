package ui.camera;

import entity.MainVideoCreator;
import ui.video.VideoPanel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCreator {
    int cameraGroupNumber = 0;
    private Map<Integer, List<BufferedImage>> map = new HashMap<>();
    List<BufferedImage> list;

    public VideoCreator(int cameraGroupNumber) {
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    public void addList(Integer integer, List<BufferedImage> list) {
        map.put(integer, list);
        if(map.size()==1){//Должно быть два.
            copyVideoToPanel();
        }
    }

    public void connectVideo() {
//==================================
//==================================
//==================================
        list = map.get(1);
    }


    private void copyVideoToPanel() {
        connectVideo();
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber,list);
        System.out.println(cameraGroupNumber+" номер.  добавляем лист размером  "+list.size());
        list = new ArrayList<>();
    }

}

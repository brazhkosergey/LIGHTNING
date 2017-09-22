package ui.camera;

import entity.MainVideoCreator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class VideoCreator {
    private int cameraGroupNumber;
    private Map<Integer, Map<Long,byte[]>> mapOfMapByte;//должно быть два видео, с дальше слепить их в один файл.
    private List<byte[]> list;
    private BufferedImage bufferedImageBack;
    int[] arr = new int[3];

    public VideoCreator(int cameraGroupNumber) {
        mapOfMapByte = new HashMap<>();
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    void addMapByte(Integer integer, Map<Long, byte[]> longMap, int fps) {
        arr[integer] = fps;
        mapOfMapByte.put(integer, longMap);
        if (mapOfMapByte.size() == 2) {//Должно быть два.

            saveBytesFromMapToFile(arr[1]+arr[2]);
        }
    }

    BufferedImage getBufferedImageBack() {
        return bufferedImageBack;
    }

    public void setBufferedImageBack(BufferedImage bufferedImageBack) {
        this.bufferedImageBack = bufferedImageBack;
    }

    private void connectVideoBytesFromMap(){
        Map<Long,byte[]> map1 = mapOfMapByte.get(1);
        Map<Long,byte[]> map2 = mapOfMapByte.get(2);

        List<Long> listOfLongs = new ArrayList<>();

        for(Long l: map1.keySet()){
            if(!listOfLongs.contains(l)){
                listOfLongs.add(l);
            }
        }

        for(Long l: map2.keySet()){
            if(!listOfLongs.contains(l)){
                listOfLongs.add(l);
            }
        }

        Collections.sort(listOfLongs);

        for(Long l:listOfLongs){
            if(map1.containsKey(l)){
                byte[] bytes = map1.get(l);
                if(bytes!=null){
                    list.add(bytes);
                }
            }
            if(map2.containsKey(l)){
                byte[] bytes = map2.get(l);
                if(bytes!=null){
                    list.add(bytes);
                }
            }
        }

        System.out.println("Размер первой коллекции: " + map1.size());
        System.out.println("Размер второй коллекции: " + map2.size());
        System.out.println("Размер общего листа: " + list.size());
    }

    private void saveBytesFromMapToFile( int totalFPS){
        connectVideoBytesFromMap();
        System.out.println("Общая частота кадров - " + totalFPS);
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber, list,totalFPS);
        System.out.println(cameraGroupNumber + " номер.  добавляем лист размером  " + list.size());
        list = new ArrayList<>();
    }
}

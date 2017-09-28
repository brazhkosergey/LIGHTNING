package ui.camera;

import entity.MainVideoCreator;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class VideoCreator {

    private int cameraGroupNumber;
    private int videoNumber;
    private Map<Integer, Map<Long,byte[]>> mapOfMapByte;
    private List<byte[]> list;
    private Map<Integer,Boolean> percentEventOfFrames;
    private BufferedImage bufferedImageBack;
    private int[] arr = new int[3];
    private int[] countOfVideoNumber = new int[3];
    private List<Map<Integer,Boolean>> listArr;

    private static Thread programCatchThread = null;

    public VideoCreator(int cameraGroupNumber) {
        listArr = new ArrayList();
        mapOfMapByte = new HashMap<>();
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    void addMapByte(Integer integer, Map<Long, byte[]> longMap, int fps, Map<Integer,Boolean> framesNumberEvent,int countPartsOfVideo) {
        listArr.add(framesNumberEvent);
        arr[integer] = fps;
        countOfVideoNumber[integer] = countPartsOfVideo;
        mapOfMapByte.put(integer, longMap);

        System.out.println("Добавляем часть видео файла. Номер камеры "+arr[integer]+". Количество кадров "+mapOfMapByte.get(integer).size()+". Часть номер "+countOfVideoNumber[integer]);
        if (mapOfMapByte.size() == 2
                && listArr.size()==2
                && countOfVideoNumber[1]==countOfVideoNumber[2]) {//Должно быть два.

            System.out.println("Сохраняем видео файл");
            videoNumber = countOfVideoNumber[1];
            saveBytesFromMapToFile(arr[1]+arr[2]);
        }
    }

    BufferedImage getBufferedImageBack() {
        return bufferedImageBack;
    }

    static void startSaveVideoProgram(){
        if(programCatchThread==null){
            programCatchThread = new Thread(() -> {
                MainVideoCreator.startCatchVideo(true);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                programCatchThread = null;
            });
        }
    }

    public void setBufferedImageBack(BufferedImage bufferedImageBack) {
        this.bufferedImageBack = bufferedImageBack;
    }

    private void connectVideoBytesFromMap(){
        Map<Long,byte[]> map1 = mapOfMapByte.get(1);
        Map<Long,byte[]> map2 = mapOfMapByte.get(2);

        percentEventOfFrames = new HashMap<>();
        if(listArr.size() == 2){
            Map<Integer, Boolean> integerBooleanMap = listArr.get(0);
            for(Integer integer:integerBooleanMap.keySet()){
                Boolean aBoolean = integerBooleanMap.get(integer);
                double d1 = (double)integer/map1.size();
                percentEventOfFrames.put((int) (d1*100),aBoolean);
            }

        } else {
            System.out.println("нет листов... "+ listArr.size());
        }

        listArr = new ArrayList();

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
        mapOfMapByte.clear();
    }

    private void saveBytesFromMapToFile( int totalFPS){
        connectVideoBytesFromMap();
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber, list,totalFPS,percentEventOfFrames,videoNumber);
        list = new ArrayList<>();
        percentEventOfFrames = new HashMap<>();
    }
}

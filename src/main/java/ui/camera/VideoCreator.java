package ui.camera;

import entity.MainVideoCreator;
import org.jcodec.common.DictionaryCompressor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class VideoCreator {
    private int cameraGroupNumber;
    private Map<Integer, Map<Long,byte[]>> mapOfMapByte;//должно быть два видео, с дальше слепить их в один файл.
    private List<byte[]> list;
    private ArrayList<Integer> percentEventOfFrames;
    private BufferedImage bufferedImageBack;
    private int[] arr = new int[3];
    private List<List<Integer>> listArr;

    public VideoCreator(int cameraGroupNumber) {
        listArr = new ArrayList();
        mapOfMapByte = new HashMap<>();
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    void addMapByte(Integer integer, Map<Long, byte[]> longMap, int fps, List<Integer> framesNumberEvent) {
        listArr.add(framesNumberEvent);
        arr[integer] = fps;
        mapOfMapByte.put(integer, longMap);
        if (mapOfMapByte.size() == 2&&listArr.size()==2) {//Должно быть два.
            saveBytesFromMapToFile(arr[1]+arr[2]);
        }
    }

    BufferedImage getBufferedImageBack() {
        return bufferedImageBack;
    }

    public void startSaveVideoProgram(){



    }

    public void setBufferedImageBack(BufferedImage bufferedImageBack) {
        this.bufferedImageBack = bufferedImageBack;
    }

    private void connectVideoBytesFromMap(){
        Map<Long,byte[]> map1 = mapOfMapByte.get(1);
        Map<Long,byte[]> map2 = mapOfMapByte.get(2);

        percentEventOfFrames = new ArrayList<>();
        if(listArr.size() == 2){
                int size=listArr.get(0).size();
                for(int i=0;i<size;i++){
                    double d1 = (double)listArr.get(0).get(i)/map1.size();
                    percentEventOfFrames.add((int) (d1*100));
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
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber, list,totalFPS,percentEventOfFrames);
        list = new ArrayList<>();
        percentEventOfFrames = new ArrayList<>();
    }
}

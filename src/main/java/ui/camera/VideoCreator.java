package ui.camera;

import entity.MainVideoCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCreator {
    int cameraGroupNumber = 0;
//    private Map<Integer, List<BufferedImage>> map = new HashMap<>();
    private Map<Integer, List<byte[]>> map = new HashMap<>();//должно быть два видео, с дальше слепить их в один файл.
    List<byte[]> list;
    int [] arr = new int[3];//Массив с ориентировочным центром видео.

    public VideoCreator(int cameraGroupNumber) {
        list = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    public void addList(Integer integer, List<byte[]> list, int centerEventFrameNumber) {
        arr[integer] = centerEventFrameNumber;
        map.put(integer, list);
        if(map.size()==2){//Должно быть два.
            saveBytesToFile();
        }
    }

    public void connectVideoBytes() {
        List<Integer> sizeList = new ArrayList<>();

        List<byte[]> bytesFirstCam = map.get(1);
        List<byte[]> bytesSecondCam = map.get(2);

        int firstCamSize = bytesFirstCam.size();
        int secondCamSize = bytesSecondCam.size();
//        int size = firstCamSize;
        int firstCamCenter = arr[1];
//        sizeList.add(firstCamCenter);
        int secondCamCenter = arr[2];
//        sizeList.add(secondCamCenter);
//
//
//        sizeList.add(firstCamSize-firstCamCenter);
//        sizeList.add(secondCamSize - secondCamCenter);
//
//        for(Integer integer:sizeList){
//            if(integer < size){
//                size = integer;
//            }
//        }


        List<byte[]> bytesFromEvent = new ArrayList<>();
        List<byte[]> bytesToEvent = new ArrayList<>();

//        int size = 0;
        int breakInt=0;
        byte[] firstCamBytes=null;
        byte[] secondCamBytes=null;

        for(int i=0;;i++){
            try{
                firstCamBytes = bytesFirstCam.get(firstCamCenter+i);
            }catch (Exception e){
                if(breakInt==0){
                    breakInt++;
                }
            }
            try{
                secondCamBytes = bytesSecondCam.get(secondCamCenter+i);
            } catch (Exception e){
                if(breakInt>0){
                    breakInt = 0;
                    break;
                }
            }

            if(firstCamBytes!= null){
                bytesFromEvent.add(firstCamBytes);
                firstCamBytes = null;
            }

            if(secondCamBytes!=null){
                bytesFromEvent.add(secondCamBytes);
                secondCamBytes = null;
            }

            System.out.println("Добавили изображение в лист ПОСЛЕ "+i);
        }

        for(int i=0;;i++){
            try{
                firstCamBytes = bytesFirstCam.get(firstCamCenter-i);
            }catch (Exception e){
                if(breakInt == 0){
                    breakInt++;
                }
            }

            try{
                secondCamBytes = bytesSecondCam.get(secondCamCenter-i);
            } catch (Exception e){
                if(breakInt>0){
                    breakInt = 0;
                    break;
                }
            }

            if(firstCamBytes!=null){
                bytesToEvent.add(firstCamBytes);
                firstCamBytes = null;
            }

            if(secondCamBytes!=null){
                bytesToEvent.add(secondCamBytes);
                secondCamBytes = null;
            }

            System.out.println("Добавили изображение в лист ДО "+i);
        }

        System.out.println("Размер первого листа: " + firstCamSize);
        System.out.println("Центр первого листа: " + firstCamCenter);
        System.out.println("Размер второго листа: " + secondCamSize);
        System.out.println("Центр второго листа: " + secondCamCenter);
        System.out.println("Размер листа ОТ события: "+bytesFromEvent.size());
        System.out.println("Размер листа ДО события: "+bytesToEvent.size());

        for(int i = bytesToEvent.size()-1;i >= 0;i--){
            list.add(bytesToEvent.get(i));
        }

        for(int i = 0;i<bytesFromEvent.size();i++){
            list.add(bytesFromEvent.get(i));
        }

        System.out.println("Размер общего листа: "+list.size());
    }

    private void saveBytesToFile() {
        connectVideoBytes();
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber,list);
        System.out.println(cameraGroupNumber+" номер.  добавляем лист размером  "+list.size());
        list = new ArrayList<>();
    }
}

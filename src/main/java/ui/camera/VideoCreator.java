package ui.camera;

import entity.MainVideoCreator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoCreator {
    int cameraGroupNumber;
    private Map<Integer, List<byte[]>> mapByte;//должно быть два видео, с дальше слепить их в один файл.
    private Map<Integer, List<BufferedImage>> mapImage;//должно быть два видео, с дальше слепить их в один файл.
    List<byte[]> list;
    List<BufferedImage> listImage;
    BufferedImage bufferedImageBack;
    int[] arr = new int[3];//Массив с ориентировочным центром видео.

    public VideoCreator(int cameraGroupNumber) {
        mapImage = new HashMap<>();
        mapByte = new HashMap<>();
        list = new ArrayList<>();
        listImage = new ArrayList<>();
        this.cameraGroupNumber = cameraGroupNumber;
    }

    public void addListByte(Integer integer, List<byte[]> list, int centerEventFrameNumber) {
        arr[integer] = centerEventFrameNumber;
        mapByte.put(integer, list);
        if (mapByte.size() == 2) {//Должно быть два.
            saveBytesToFile();
        }
    }

    public BufferedImage getBufferedImageBack() {
        return bufferedImageBack;
    }

    public void setBufferedImageBack(BufferedImage bufferedImageBack) {
        this.bufferedImageBack = bufferedImageBack;
    }

    public void addListImage(Integer integer, List<BufferedImage> list, int centerEventFrameNumber) {
        arr[integer] = centerEventFrameNumber;


        mapImage.put(integer, list);
        if (mapImage.size() == 2) {//Должно быть два.
            saveVideoToFile();
            System.out.println("Сохраняем видео");
        }
    }

    public void connectVideoBytes() {

        List<byte[]> bytesFirstCam = mapByte.get(1);
        List<byte[]> bytesSecondCam = mapByte.get(2);

        int firstCamSize = bytesFirstCam.size();
        int secondCamSize = bytesSecondCam.size();
        int firstCamCenter = arr[1];
        int secondCamCenter = arr[2];

        boolean firstBig = false;
        double diff = bytesFirstCam.size() / bytesSecondCam.size();
        int diffInt;
        if (diff > 0) {
            firstBig = true;
            diff = 1 / diff;
        }


        for (int i = 1; ; i++) {
            if ((diff * i) % 1 == 0) {
                diffInt = i;
                break;
            }
        }

        List<byte[]> bytesFromEvent = new ArrayList<>();
        List<byte[]> bytesToEvent = new ArrayList<>();

        int breakInt = 0;
        byte[] firstCamBytes = null;
        byte[] secondCamBytes = null;

        List<byte[]> listOneBuffer = new ArrayList<>();
        List<byte[]> listTwoBuffer = new ArrayList<>();


        for (int i = 0; ; i++) {

            if (firstBig)
                for (int k = 0; k < diffInt * diff; k++) {
//                listOneBuffer.add(bytesFirstCam.get(firstCamCenter + k))
                }

            try {
                firstCamBytes = bytesFirstCam.get(firstCamCenter + i);
            } catch (Exception e) {
                if (breakInt == 0) {
                    breakInt++;
                }
            }

            try {
                secondCamBytes = bytesSecondCam.get(secondCamCenter + i);
            } catch (Exception e) {
                if (breakInt > 0) {
                    breakInt = 0;
                    break;
                }
            }

            if (firstCamBytes != null) {
                bytesFromEvent.add(firstCamBytes);
                firstCamBytes = null;
            }

            if (secondCamBytes != null) {
                bytesFromEvent.add(secondCamBytes);
                secondCamBytes = null;
            }

            System.out.println("Добавили изображение в лист ПОСЛЕ " + i);
        }

        for (int i = 0; ; i++) {
            try {
                firstCamBytes = bytesFirstCam.get(firstCamCenter - i);
            } catch (Exception e) {
                if (breakInt == 0) {
                    breakInt++;
                }
            }

            try {
                secondCamBytes = bytesSecondCam.get(secondCamCenter - i);
            } catch (Exception e) {
                if (breakInt > 0) {
                    breakInt = 0;
                    break;
                }
            }


            if (firstCamBytes != null) {
                bytesToEvent.add(firstCamBytes);
                firstCamBytes = null;
            }

            if (secondCamBytes != null) {
                bytesToEvent.add(secondCamBytes);
                secondCamBytes = null;
            }

            System.out.println("Добавили изображение в лист ДО " + i);
        }

        System.out.println("Размер первого листа: " + firstCamSize);
        System.out.println("Центр первого листа: " + firstCamCenter);
        System.out.println("Размер второго листа: " + secondCamSize);
        System.out.println("Центр второго листа: " + secondCamCenter);
        System.out.println("Размер листа ОТ события: " + bytesFromEvent.size());
        System.out.println("Размер листа ДО события: " + bytesToEvent.size());

        for (int i = bytesToEvent.size() - 1; i >= 0; i--) {
            list.add(bytesToEvent.get(i));
        }

        for (int i = 0; i < bytesFromEvent.size(); i++) {
            list.add(bytesFromEvent.get(i));
        }

        System.out.println("Размер общего листа: " + list.size());
    }

    public void connectVideoImages() {

        System.out.println("Соединяем видео.");

        List<BufferedImage> imagesFirstCam = mapImage.get(1);
        List<BufferedImage> imagesSecondCam = mapImage.get(2);

        int firstCamSize = imagesFirstCam.size();
        int secondCamSize = imagesSecondCam.size();
        int firstCamCenter = arr[1];
        int secondCamCenter = arr[2];

        boolean firstBig = false;
        double diff = imagesFirstCam.size() / imagesSecondCam.size();
        int diffInt;
        if (diff > 0) {
            firstBig = true;
            diff = 1 / diff;
        }


        for (int i = 1; ; i++) {
            if ((diff * i) % 1 == 0) {
                diffInt = i;
                break;
            }
        }

        List<BufferedImage> bytesFromEvent = new ArrayList<>();
        List<BufferedImage> bytesToEvent = new ArrayList<>();

        int breakInt = 0;
        BufferedImage firstCamImage = null;
        BufferedImage secondCamImage = null;

//        List<byte[]> listOneBuffer = new ArrayList<>();
//        List<byte[]> listTwoBuffer = new ArrayList<>();


        for (int i = 0; ; i++) {

            if (firstBig)
                for (int k = 0; k < diffInt * diff; k++) {
//                listOneBuffer.add(bytesFirstCam.get(firstCamCenter + k))
                }

            try {
                firstCamImage = imagesFirstCam.get(firstCamCenter + i);
            } catch (Exception e) {
                if (breakInt == 0) {
                    breakInt++;
                }
            }

            try {
                secondCamImage = imagesSecondCam.get(secondCamCenter + i);
            } catch (Exception e) {
                if (breakInt > 0) {
                    breakInt = 0;
                    break;
                }
            }

            if (firstCamImage != null) {
                bytesFromEvent.add(firstCamImage);
                firstCamImage = null;
            }

            if (secondCamImage != null) {
                bytesFromEvent.add(secondCamImage);
                secondCamImage = null;
            }
            System.out.println("Добавили изображение в лист ПОСЛЕ " + i);
        }

        for (int i = 0; ; i++) {
            try {
                firstCamImage = imagesFirstCam.get(firstCamCenter - i);
            } catch (Exception e) {
                if (breakInt == 0) {
                    breakInt++;
                }
            }

            try {
                secondCamImage = imagesSecondCam.get(secondCamCenter - i);
            } catch (Exception e) {
                if (breakInt > 0) {
                    breakInt = 0;
                    break;
                }
            }


            if (firstCamImage != null) {
                bytesToEvent.add(firstCamImage);
                firstCamImage = null;
            }

            if (secondCamImage != null) {
                bytesToEvent.add(secondCamImage);
                secondCamImage = null;
            }

            System.out.println("Добавили изображение в лист ДО " + i);
        }

        System.out.println("Размер первого листа: " + firstCamSize);
        System.out.println("Центр первого листа: " + firstCamCenter);
        System.out.println("Размер второго листа: " + secondCamSize);
        System.out.println("Центр второго листа: " + secondCamCenter);
        System.out.println("Размер листа ОТ события: " + bytesFromEvent.size());
        System.out.println("Размер листа ДО события: " + bytesToEvent.size());

        for (int i = bytesToEvent.size() - 1; i >= 0; i--) {
            listImage.add(bytesToEvent.get(i));
        }

        for (int i = 0; i < bytesFromEvent.size(); i++) {
            listImage.add(bytesFromEvent.get(i));
        }

        System.out.println("Размер общего листа: " + listImage.size());
    }

    private void saveVideoToFile() {
        connectVideoImages();
        MainVideoCreator.putVideoImagesFromCameraGroup(cameraGroupNumber, listImage);
        System.out.println(cameraGroupNumber + " номер.  добавляем лист картинок размером  " + listImage.size());

    }


    private void saveBytesToFile() {
        connectVideoBytes();
        MainVideoCreator.putVideoFromCameraGroup(cameraGroupNumber, list);
        System.out.println(cameraGroupNumber + " номер.  добавляем лист размером  " + list.size());
        list = new ArrayList<>();
    }
}

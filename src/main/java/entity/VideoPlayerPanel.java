package entity;

import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class VideoPlayerPanel extends JPanel {
    int width = 520;
    int height = 230;
    private int numberVideoPanel;


    private boolean mainPanel;
    private long currentByteNumber = -1;
    private long fileSize;
    private int position ;

    private int FPS;

    private File file;
    private Thread thread;

    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    JLayer<JPanel> videoStreamLayer;
    private JLabel label;
    private boolean blockHaveVideo = true;
    private boolean videoPlay = true;
    private BufferedInputStream bufferedInputStream = null;

    VideoPlayerPanel(File file, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.file = file;
        this.setPreferredSize(new Dimension(540, 237));

        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        if (file != null) {
            fileSize = file.length();
            System.out.println(" файл есть. ");

            videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
            videoPlayerToShowOneVideo.setPreferredSize(new Dimension(520, 230));

            JPanel testPane = new JPanel();
            testPane.add(videoPlayerToShowOneVideo);
            LayerUI<JPanel> layerUI = new VideoPlayerPanel.MyLayer();
            videoStreamLayer = new JLayer<JPanel>(testPane, layerUI);


            label = new JLabel("Натистіть PLAY");


//            Thread threadToBufferFile = new Thread(() -> {
//                FileInputStream fileInputStream = null;
//                try {
//                    fileInputStream = new FileInputStream(file);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//
////                if (fileInputStream != null) {
//                if (bufferedInputStream != null) {
//                    ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//                    int x = 0;
//                    int t = 0;
//                    BufferedImage image = null;
//                    while (x >= 0) {
//                        t = x;
//                        try {
////                            x = fileInputStream.read();
//                            x = bufferedInputStream.read();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        temporaryStream.write(x);
//                        if (x == 216 && t == 255) {// начало изображения
//                            temporaryStream.reset();
//
//                            temporaryStream.write(t);
//                            temporaryStream.write(x);
//                        } else if (x == 217 && t == 255) {//конец изображения
//                            byte[] imageBytes = temporaryStream.toByteArray();
//                            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//                            try {
//                                image = ImageIO.read(inputStream);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            if (!list.contains(image)) {
//                                list.add(image);
//                                System.out.println("Добавили изображение " + list.size());
//                            }
//                        }
//
//                        if (!VideoPlayer.isShowVideoPlayer()) {
//                            x = -1;
//                        }
//                    }
//                    videoAlreadyBuffered = true;
//                }
//            });
//            threadToBufferFile.start();
        } else {
            System.out.println("Фаайла нет....");
            blockHaveVideo = false;
            label = new JLabel("Камери не працювали");
            this.add(label);
        }
        createThread();
    }




    class MyLayer extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            if (MainFrame.imagesForBlock.get(numberVideoPanel)!=null) {
                g.drawImage(CameraPanel.animateCircle(VideoCatcher.processImage(MainFrame.imagesForBlock.get(numberVideoPanel),width,height),BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }


    private void createThread() {
        if (blockHaveVideo) {
            thread = new Thread(() -> {
                System.out.println("Запускаем поток проигрывания.");
                while (blockHaveVideo) {

                    if (!VideoPlayer.isShowVideoPlayer()) {
                        blockHaveVideo = false;
                    }

                    if (VideoPlayer.isPLAY()) {

                        System.out.println("НАчали воспроизводить видео. ");
                        if (!videoPlay) {
                            videoPlay = true;
                        }

                        if (videoPlay) {
                            this.remove(label);
//                            this.add(videoPlayerToShowOneVideo);
                            this.add(videoStreamLayer);
                            this.revalidate();
                            this.repaint();
                        }

                        if (file != null) {
                            FileInputStream fileInputStream = null;
                            try {
                                fileInputStream = new FileInputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            bufferedInputStream = new BufferedInputStream(fileInputStream);
                        }
                        showFrames(0);
//                        int i = 0;
//                        if (bufferedInputStream != null) {
//                            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//                            int x = 0;
//                            int t = 0;
//                            BufferedImage image = null;
//                            while (x >= 0) {
//                                t = x;
//                                try {
//                                    x = bufferedInputStream.read();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//
//                                temporaryStream.write(x);
//                                if (x == 216 && t == 255) {// начало изображения
//                                    temporaryStream.reset();
//
//                                    temporaryStream.write(t);
//                                    temporaryStream.write(x);
//                                } else if (x == 217 && t == 255) {//конец изображения
//                                    byte[] imageBytes = temporaryStream.toByteArray();
//                                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//                                    try {
//                                        image = ImageIO.read(inputStream);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    System.out.println("Показываем изображение - " + i++);
//                                    videoPlayerToShowOneVideo.setBufferedImage(VideoCatcher.processImage(image, 390, 260));
//                                    videoPlayerToShowOneVideo.repaint();
//                                    this.repaint();
//                                }
//                            }
//                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (videoPlay) {
                            label = new JLabel("Натистіть PLAY");
                            videoPlay = false;

                            this.remove(videoStreamLayer);
//                            this.remove(videoPlayerToShowOneVideo);
                            this.add(label);
                            this.revalidate();
                            this.repaint();
                        }
                    }
                    if(!VideoPlayer.isShowVideoPlayer()){
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public void showFrames(int startBytePercent) {

        if (bufferedInputStream != null) {
            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
            int x = 0;
            int t = 0;
            BufferedImage image = null;
            if (startBytePercent > 0) {
                long startByte = (long) (startBytePercent/100)*fileSize;
                int buffSize= (int)startByte;//TODO to large buff
                try {
                    bufferedInputStream.read(new byte[buffSize]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentByteNumber = startByte;
            }

            while (x >= 0) {
                if(!VideoPlayer.isShowVideoPlayer()){
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }else if(VideoPlayer.isSetPOSITION()){
                    System.out.println("SETPOSSITION");
                    if (file != null) {
                        FileInputStream fileInputStream = null;
                        try {
                            fileInputStream = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bufferedInputStream = new BufferedInputStream(fileInputStream);
                    }
                    startBytePercent = VideoPlayer.getPosition();
                    if (startBytePercent > 0) {
                        long startByte = (long)startBytePercent*(fileSize/100);
                        int buffSize= (int)startByte;//TODO to large buff
                        try {
                            bufferedInputStream.read(new byte[buffSize]);
                            System.out.println("Вычитали байт - " +buffSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        currentByteNumber = startByte;
                    }
                    VideoPlayer.setSetPOSITION(false);
                    VideoPlayer.setPLAY(true);
                }else if (VideoPlayer.isPLAY()) {
                    t = x;
                    try {
                        x = bufferedInputStream.read();
                        currentByteNumber ++;
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
                            FPS++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        videoPlayerToShowOneVideo.setBufferedImage(VideoCatcher.processImage(image, 390, 260));
                        videoPlayerToShowOneVideo.repaint();
                        this.repaint();

                        if(mainPanel){
                            double percent = (double) currentByteNumber/fileSize;
                            percent = percent*100.0;
//                            System.out.println(percent);
                            position = (int) percent;
//                            System.out.println("всего байтов - "+fileSize+". Сейчас читаем байт номер - "+currentByteNumber);
//                            System.out.println("Позиция слайдера - "+position);
                            VideoPlayer.setSliderPosition(position);
                        }

                        try {
                            Thread.sleep(VideoPlayer.getSpeed());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (VideoPlayer.isPAUSE()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (VideoPlayer.isSTOP()) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    currentByteNumber = 0L;
                    position = 0;
                    VideoPlayer.setSliderPosition(position);
                    return;
                }
            }
            if(x==-1){

                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentByteNumber = 0L;
                position = 0;
                VideoPlayer.setSTOP(true);
                VideoPlayer.setPLAY(false);
                VideoPlayer.setPAUSE(false);
            }
        }
    }

    public void setMainPanel(boolean mainPanel) {
        if(mainPanel){
            Thread thread = new Thread(() -> {
                while (true){
                    MainFrame.showInformMassage("Швидкість відео: FPS - " + FPS, false);
                    FPS = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!VideoPlayer.isShowVideoPlayer()){
                        break;
                    }
                }
            });

            thread.start();
        }

        this.mainPanel = mainPanel;
    }

    Thread getThread() {
        return thread;
    }

    class VideoPlayerToShowOneVideo extends JPanel {
        private BufferedImage bufferedImage;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }

        private void setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }
    }
}

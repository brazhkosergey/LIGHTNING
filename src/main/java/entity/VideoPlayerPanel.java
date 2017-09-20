package entity;

import ui.camera.VideoCatcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayerPanel extends JPanel {

    List<BufferedImage> list = new ArrayList<>();
    boolean videoAlreadyBuffered = false;
    int currentFrameNumber = -1;

    Thread thread;
//    VideoPlayer mainPlayer;

    VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    JLabel label;
    boolean blockHaveVideo = true;
    boolean videoPlay = true;

    VideoPlayerPanel(File file, VideoPlayer videoPlayer) {
        this.setPreferredSize(new Dimension(540, 250));
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        if (file != null) {
            System.out.println(" файл есть. ");
            videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
            videoPlayerToShowOneVideo.setPreferredSize(new Dimension(520, 240));
            label = new JLabel("Натистіть PLAY");
            Thread threadToBufferFile = new Thread(() -> {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

//                if (fileInputStream != null) {
                if (bufferedInputStream != null) {
                    ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
                    int x = 0;
                    int t = 0;
                    BufferedImage image = null;
                    while (x >= 0) {
                        t = x;
                        try {
//                            x = fileInputStream.read();
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
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (!list.contains(image)) {
                                list.add(image);
                                System.out.println("Добавили изображение " + list.size());
                            }
                        }

                        if(!VideoPlayer.isShowVideoPlayer()){
                            x=-1;
                        }
                    }
                    videoAlreadyBuffered = true;
                }
            });
            threadToBufferFile.start();
        } else {
            System.out.println("Фаайла нет....");
            blockHaveVideo = false;
            label = new JLabel("Камери не працювали");
            this.add(label);
        }
        createThread();
    }

    private void createThread() {
        if (blockHaveVideo) {
            thread = new Thread(() -> {
                System.out.println("Запускаем поток проигрывания.");
                while (blockHaveVideo) {

                    if(!VideoPlayer.isShowVideoPlayer()){
                        blockHaveVideo = false;
                    }

                    if (VideoPlayer.isPLAY()) {
                        System.out.println("НАчали воспроизводить видео. ");

                        if (!videoPlay) {
                            videoPlay = true;
                        }

                        if(videoPlay){
                            this.remove(label);
                            this.add(videoPlayerToShowOneVideo);
                            this.revalidate();
                            this.repaint();
                        }

                        while (VideoPlayer.isPLAY()) {
                            int size = 1;
                            System.out.println("Размер равен: " + size);
                            for (int i = 0; i < size; i++) {
                                if(!VideoPlayer.isShowVideoPlayer()){
                                    VideoPlayer.setPLAY(false);
                                    break;
                                }
                                if(!VideoPlayer.isPLAY()){
                                    break;
                                }
                                System.out.println("Будем показывать изображение номер: - " + i);
                                try {
                                    Thread.sleep(VideoPlayer.speed);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                BufferedImage bufferedImage = list.get(i);
                                if (bufferedImage != null) {
                                    System.out.println("Показываем изображение - " + i);
                                    videoPlayerToShowOneVideo.setBufferedImage(VideoCatcher.processImage(bufferedImage, 390, 260));
                                    videoPlayerToShowOneVideo.repaint();
                                    this.repaint();
                                }

                                currentFrameNumber = i;
                                VideoPlayer.slider.setValue(i*100/list.size());

                                if (videoAlreadyBuffered) {
                                    size = list.size();
                                } else {
                                    size++;
                                }

                                if (i == list.size()-1) {
                                    VideoPlayer.setPLAY(false);
                                }
                            }
                        }

                    } else {
                        if (videoPlay) {
                            label = new JLabel("Натистіть PLAY");
                            videoPlay = false;

                            this.remove(videoPlayerToShowOneVideo);
                            this.add(label);
                            this.revalidate();
                            this.repaint();
                        }
                    }
                }
            });
        }
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

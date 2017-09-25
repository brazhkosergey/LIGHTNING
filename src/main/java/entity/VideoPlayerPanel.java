package entity;

import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class VideoPlayerPanel extends JPanel {
    private boolean isFullSize;

    private int width;
    private int height;
    private int numberVideoPanel;

    private boolean mainPanel;
    private long currentByteNumber = -1;
    private long fileSize;
    private int position;

    private int FPS;
    private File file;
    private Thread thread;

    private int changeImage;
    private int nextImageInt;
    private int prewImageIng;
    private int countImageFramesChangeIng;

    private VideoPlayerToShowOneVideo videoPlayerToShowOneVideo;
    private JLayer<JPanel> videoStreamLayer;
    private JLabel label;
    private boolean blockHaveVideo = true;
    private boolean videoPlay = true;
    private BufferedInputStream bufferedInputStream = null;
    private FileInputStream fileInputStream = null;
    private ByteArrayOutputStream temporaryStream = null;

    private Map<Integer, byte[]> buffMap;
    private int numberImageInBuffer;


    VideoPlayerPanel(File file, int numberVideoPanel) {
        this.numberVideoPanel = numberVideoPanel;
        this.file = file;
        buffMap = new HashMap<>();
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        if (file != null) {
            fileSize = file.length();
            videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
            LayerUI<JPanel> layerUI = new VideoPlayerPanel.MyLayer();
            videoStreamLayer = new JLayer<JPanel>(videoPlayerToShowOneVideo, layerUI);
            label = new JLabel("Натистіть PLAY");
        } else {
            blockHaveVideo = false;
            label = new JLabel("Камери не працювали");
            this.add(label);
        }
        createThread();
        setWidthAndHeight(535,222);
    }

    void setWidthAndHeight(int width, int height) {
        isFullSize = width > 535;

        this.setPreferredSize(new Dimension(width+5,height+5));
        if(videoPlayerToShowOneVideo!=null){
            videoPlayerToShowOneVideo.setPreferredSize(new Dimension(width,height));
        }
        this.width = width-4;
        this.height = height-4;
    }

    public boolean isFullSize() {
        return isFullSize;
    }

    class MyLayer extends LayerUI<JPanel> {
        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (MainFrame.imagesForBlock.get(numberVideoPanel) != null) {
                g.drawImage(CameraPanel.animateCircle(processImage(MainFrame.imagesForBlock.get(numberVideoPanel), width, height), BufferedImage.TYPE_INT_ARGB), 0, 0, null);
                g.dispose();
            }
        }
    }

    private void createThread() {
        label = new JLabel("Натистіть PLAY");
        if (blockHaveVideo) {
            thread = new Thread(() -> {
                while (blockHaveVideo) {

                    if (!VideoPlayer.isShowVideoPlayer()) {
                        blockHaveVideo = false;

                        if(fileInputStream!=null){
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }finally {
                                fileInputStream = null;
                            }
                        }

                        if(bufferedInputStream!=null){
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }finally {
                                bufferedInputStream = null;
                            }
                        }

                        if(temporaryStream!=null){
                            try {
                                temporaryStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            temporaryStream = null;
                        }
                        break;
                    }

                    if (VideoPlayer.isPLAY()) {
                        if (!videoPlay) {
                            videoPlay = true;
                            this.remove(label);
                            videoStreamLayer.repaint();
                            this.add(videoStreamLayer);
                            this.revalidate();
                            this.repaint();
                        }

                        if (file != null) {
                            if(fileInputStream!=null){
                                try {
                                    fileInputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }finally {
                                    fileInputStream = null;
                                }
                            }

                            if(bufferedInputStream!=null){
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }finally {
                                    bufferedInputStream = null;
                                }
                            }

                            try {
                                fileInputStream = new FileInputStream(file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            bufferedInputStream = new BufferedInputStream(fileInputStream);

                            if(temporaryStream!=null){
                                try {
                                    temporaryStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                temporaryStream = null;
                            }
                        }

                        if (VideoPlayer.isSetPOSITION()) {
                            showFrames(VideoPlayer.getPosition());
                        } else {
                            showFrames(0);
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (videoPlay) {
                            videoPlay = false;
                            this.remove(videoStreamLayer);
                            this.add(label);
                            this.revalidate();
                            this.repaint();
                        }
                    }
                }
            });
        }
    }

    private void showFrames(int startBytePercent) {

        if (bufferedInputStream != null) {
            temporaryStream = new ByteArrayOutputStream(65535);
            int x = 0;
            int t = 0;
            BufferedImage image = null;
            if (startBytePercent > 0) {
                long startByte = (long) (startBytePercent * fileSize) / 100;
                int buffSize = (int) startByte;//TODO to large buff
                try {
                    bufferedInputStream.read(new byte[buffSize]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                currentByteNumber = startByte;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Прыгнули на " + startBytePercent + " процентов. ВЫчитали байт " + buffSize + " всего байт " + fileSize);
                VideoPlayer.setSetPOSITION(false);
            }

            while (x >= 0) {
                ByteArrayInputStream inputImageStream = null;
                BufferedInputStream bufferedInputImageStream = null;
                if (!VideoPlayer.isShowVideoPlayer()){
                    currentByteNumber = 0L;
                    position = 0;
                    changeImage = 0;
                    if(mainPanel){
                        VideoPlayer.setSTOP(true);
                        VideoPlayer.setPLAY(false);
                        VideoPlayer.setPAUSE(false);
                        VideoPlayer.setSliderPosition(position);
                        VideoPlayer.setCountDoNotShowImage(0);
                        VideoPlayer.setSpeed(0);
                    }
                    return;
                }else if(VideoPlayer.isSetPOSITION()) {
                    return;
                } else if (VideoPlayer.isPLAY()) {
                    if (countImageFramesChangeIng == 0) {
                        t = x;
                        try {
                            x = bufferedInputStream.read();
                            currentByteNumber++;
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

                            buffMap.put(numberImageInBuffer++, imageBytes);
                            if (changeImage == VideoPlayer.getCountDoNotShowImage()) {

                                try {
                                    inputImageStream = new ByteArrayInputStream(imageBytes);
                                    bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                    image = ImageIO.read(bufferedInputImageStream);
                                    inputImageStream.close();
                                    bufferedInputImageStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (image != null) {
                                    videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
//                                videoPlayerToShowOneVideo.repaint();

                                    videoStreamLayer.repaint();
                                    changeImage = 0;
                                }
                            } else {
                                changeImage++;
                            }

                            if (mainPanel) {
                                FPS++;
                            }

                            try {
                                Thread.sleep(VideoPlayer.getSpeed());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng--));
                        if (bytes != null) {
                            currentByteNumber = currentByteNumber+bytes.length;
                            try {
                                inputImageStream = new ByteArrayInputStream(bytes);
                                bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                image = ImageIO.read(bufferedInputImageStream);
                                inputImageStream.close();
                                bufferedInputImageStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (image != null) {
                                videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                videoPlayerToShowOneVideo.repaint();
                            }
                        }
                    }
                } else if (VideoPlayer.isPAUSE()) {
                    if(VideoPlayer.isSaveIMAGE()){
                        byte[] bytes = buffMap.get(numberImageInBuffer - countImageFramesChangeIng-1);
//                        try {
//                            inputImageStream = new ByteArrayInputStream(bytes);
//                            bufferedInputImageStream = new BufferedInputStream(inputImageStream);
//                            image = ImageIO.read(bufferedInputImageStream);
//                            inputImageStream.close();
//                            bufferedInputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        String path = "C:\\ipCamera\\"+System.currentTimeMillis()+"-"+numberVideoPanel+".jpg";
                        File file = new File(path);
                        try {
                            if(file.createNewFile()){
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                fileOutputStream.write(bytes);
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(VideoPlayer.isSaveIMAGE()){
                            VideoPlayer.setSaveIMAGE(false);
                        }
                    }else if (VideoPlayer.isNextIMAGE()) {
                        if (nextImageInt != VideoPlayer.getNextImagesInt()) {
                            nextImageInt = VideoPlayer.getNextImagesInt();
                            if (countImageFramesChangeIng == 0) {
                                while (x >= 0) {
                                    t = x;
                                    try {
                                        x = bufferedInputStream.read();
                                        currentByteNumber++;
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
                                        buffMap.put(numberImageInBuffer++, imageBytes);

                                        try {
                                            inputImageStream = new ByteArrayInputStream(imageBytes);
                                            bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                            image = ImageIO.read(bufferedInputImageStream);
                                            inputImageStream.close();
                                            bufferedInputImageStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        if (image != null) {
                                            videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                            videoPlayerToShowOneVideo.repaint();
                                        }
                                        break;
                                    }
                                }
                            } else {
                                byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng--));
                                if (bytes != null) {
                                    currentByteNumber = currentByteNumber+bytes.length;
                                    try {
                                        inputImageStream = new ByteArrayInputStream(bytes);
                                        bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                        image = ImageIO.read(bufferedInputImageStream);
                                        inputImageStream.close();
                                        bufferedInputImageStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (image != null) {
                                        videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width,height));
                                        videoPlayerToShowOneVideo.repaint();
                                    }
                                }
                            }
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (VideoPlayer.isPrewIMAGE()) {
                        if (prewImageIng != VideoPlayer.getPrewImagesInt()) {
                                prewImageIng = VideoPlayer.getPrewImagesInt();
                            byte[] bytes = buffMap.get(numberImageInBuffer - (countImageFramesChangeIng++));
                                if (bytes != null) {
                                    currentByteNumber = currentByteNumber-bytes.length;

                                    try {
                                        inputImageStream = new ByteArrayInputStream(bytes);
                                        bufferedInputImageStream = new BufferedInputStream(inputImageStream);
                                        image = ImageIO.read(bufferedInputImageStream);
                                        inputImageStream.close();
                                        bufferedInputImageStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if (image != null) {
                                        videoPlayerToShowOneVideo.setBufferedImage(processImage(image, width, height));
                                        videoPlayerToShowOneVideo.repaint();
                                    }
                                }
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (VideoPlayer.isSTOP()) {
                    currentByteNumber = 0L;
                    position = 0;
                    VideoPlayer.setSliderPosition(position);
                    return;
                }

                if (mainPanel) {
                    double percent = (double) currentByteNumber / fileSize;
                    percent = percent * 100.0;
                    if (position != (int) percent) {
                        position = (int) percent;
                        VideoPlayer.setSliderPosition(position);
                        System.out.println(position);
                    }
                }
            }

            if (x == -1) {
                currentByteNumber = 0L;
                position = 0;
                changeImage = 0;

                if(mainPanel){
                    VideoPlayer.setSTOP(true);
                    VideoPlayer.setPLAY(false);
                    VideoPlayer.setPAUSE(false);
                    VideoPlayer.setSliderPosition(position);
                    VideoPlayer.setCountDoNotShowImage(0);
                    VideoPlayer.setSpeed(0);
                }
            }
        }
    }

    void setMainPanel(boolean mainPanel) {
        this.mainPanel = mainPanel;
        if (this.mainPanel) {

            Thread thread = new Thread(() -> {
                while (mainPanel) {
                    VideoPlayer.FPSLabel.setText("FPS - " + FPS);
                    VideoPlayer.FPSLabel.repaint();

                    FPS = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!VideoPlayer.isShowVideoPlayer()) {
                        break;
                    }
                }
            });
            thread.start();
            this.setBorder(BorderFactory.createLineBorder(Color.green));
        } else {
            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    private BufferedImage processImage(BufferedImage bi, int maxWidth, int maxHeight) {
        BufferedImage bi2 = null;
        double max;
        int size;
        int ww = maxWidth - bi.getWidth();
        int hh = maxHeight - bi.getHeight();

        if (ww < 0 || hh < 0) {
            if (ww < hh) {
                max = maxWidth;
                size = bi.getWidth();
            } else {
                max = maxHeight;
                size = bi.getHeight();
            }

            if (size > 0 && size > max) {
                double trans = 1.0 / (size / max);
                AffineTransform tr = new AffineTransform();
                tr.scale(trans, trans);
                AffineTransformOp op = new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR);
                Double w = bi.getWidth() * trans;
                Double h = bi.getHeight() * trans;
                bi2 = new BufferedImage(w.intValue(), h.intValue(), bi.getType());
                op.filter(bi, bi2);
            }
        } else {
            return bi;
        }
        return bi2;
    }


    Thread getThread() {
        return thread;
    }

    public String getFileName(){
        if(file!=null){
            return file.getName();
        } else {
            return null;
        }
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

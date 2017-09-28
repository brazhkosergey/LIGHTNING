import entity.MainVideoCreator;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Test{
    static ImagePanel mainPanel = new ImagePanel();
    static List<BufferedImage> list = new ArrayList<>();
    static boolean videoAlreadyBuffered = false;

    public static void main(String[] args) {
//
//        InputStream inputStream = null;
//
//        try {
//            AudioInputStream ais = AudioSystem.getAudioInputStream(inputStream);
//
//            while (true){
//                if(ais.available()!=0){
//                }
//            }
//
//
//        } catch (UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        кнопки убирать с рабочего стола.

//        rtp - поток ;
        String urlString = "http://192.168.3.221/axis-media/media.amp";
        String username = "root";
        String password = "PASS";
        URL url = null;
        try {
            url = new URL(urlString);
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        try {
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
//            int x = 0;
//            while (x>-1){
//                x = inputStream.read();
//                System.out.println(x);
//            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            StringBuilder b = new StringBuilder();
            String s;
            while ((s = bufferedReader.readLine()) != null){
                b.append(s);
            }
            System.out.println(b.toString());
// <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
// <HTML>
// <HEAD>
// <meta http-equiv="Expires" content="Tue, 12 May 1962 1:00:00 GMT">
// <meta http-equiv="Pragma" content="no-cache">
// <meta http-equiv="Cache-Control" content="no-cache">
// <meta http-equiv="Content-type" CONTENT="text/html; charset=iso-8859-1">
// <meta http-equiv="Content-language" CONTENT="en">
// <META HTTP-EQUIV="Refresh" CONTENT="0; URL=/view/viewer_index.shtml?id=61">
// <title>Index page</title>
// <noscript>Your browser has JavaScript turned off.
// <br>For the user interface to work, you must enable JavaScript in your browser and reload/refresh this page.
// </noscript></HEAD>
// <BODY>
// </BODY>
// </HTML>

        } catch (IOException e) {
            e.printStackTrace();
        }
//        File imageFile = new File("C:\\ipCamera\\1.jpg");
//        File imageFile1 = new File("C:\\ipCamera\\2.jpg");
//        BufferedImage bufferedImage = null;
//        BufferedImage bufferedImage1 = null;
//        try {
//            bufferedImage = ImageIO.read(imageFile);
//            bufferedImage1 = ImageIO.read(imageFile1);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        System.out.println(bufferedImage);
//        System.out.println(bufferedImage1);
//
//
//        BufferedImage twoImages = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = twoImages.createGraphics();
//        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
//        graphics.drawImage(bufferedImage, 0, 0, bufferedImage.getWidth(),bufferedImage.getHeight() , null);
//        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));
//        graphics.drawImage(bufferedImage1, 0, 0, bufferedImage1.getWidth(),bufferedImage1.getHeight() , null);
//        graphics.dispose();
//
//        JFrame frame = new JFrame("Test");
//        frame.setPreferredSize(new Dimension(700,600));
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//        VideoPlayerToShowOneVideo videoPlayerToShowOneVideo = new VideoPlayerToShowOneVideo();
//
////        videoPlayerToShowOneVideo.setBufferedImage(bufferedImage);
////        videoPlayerToShowOneVideo.setBufferedImage(twoImages);
//        videoPlayerToShowOneVideo.setBufferedImage(MainVideoCreator.connectImage(bufferedImage,bufferedImage1,0.5f));
////        videoPlayerToShowOneVideo.setBufferedImage(bufferedImage1);
//
//        frame.getContentPane().add(videoPlayerToShowOneVideo);
//        frame.pack();





//        for(int i=0;i<16;i++){
//            for(int j = 0;j<16;j++){
//                for(int k = 0;k<16;k++){
//                 Color color = new Color(200+i,200+j,200+k);
//                    System.out.println(color.getRGB());
//                }
//                System.out.println("=========================================");
//            }
//            System.out.println("=========================================");
//        }
//
//        Color color = new Color(42,84,24);




//        JFrame frame = new JFrame("TESST");
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setPreferredSize(new Dimension(1150, 720));
//        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
//        frame.setVisible(true);
//        frame.pack();
//
//        Thread threadSave = new Thread(()->{
//
//            File file = new File("C:\\ipCamera\\1505382999165-1.tmp");
//
//            FileInputStream fileInputStream = null;
//            try {
//                fileInputStream = new FileInputStream(file);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            if(fileInputStream!=null){
//                ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//                int x = 0;
//                int t = 0;
//                BufferedImage image = null;
//                while (x>=0){
//                    t = x;
//                    try {
//                        x = fileInputStream.read();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    temporaryStream.write(x);
//                    if (x == 216 && t == 255) {// начало изображения
//                        temporaryStream.reset();
//
//                        temporaryStream.write(t);
//                        temporaryStream.write(x);
//                    } else if (x == 217 && t == 255) {//конец изображения
//                        byte[] imageBytes = temporaryStream.toByteArray();
//                        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//                        try {
//                            image = ImageIO.read(inputStream);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        if(!list.contains(image)){
//                            list.add(image);
//                            System.out.println("Добавили изображение "+list.size());
//                        }
//                    }
//                }
//                videoAlreadyBuffered = true;
//            }
//        });
//        Thread threadShow = new Thread(()->{
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            int size = 1;
//            for(int i = 0;i < size;i++){
//
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                BufferedImage bufferedImage = list.get(i);
//                if(bufferedImage!=null){
//            System.out.println(bufferedImage);
//            mainPanel.setBufferedImage(bufferedImage);
//            mainPanel.repaint();
//        }
//        System.out.println("Показываем изображение номер - "+i);
//
//        if(videoAlreadyBuffered){
//            size = list.size();
//        } else {
//            size++;
//        }
//    }
//});
//        threadSave.start();
//        threadShow.start();
    }




    static class VideoPlayerToShowOneVideo extends JPanel {
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


    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    static class ImagePanel extends JPanel {
        private BufferedImage bufferedImage;

        public ImagePanel() {

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bufferedImage != null) {
                g.drawImage(bufferedImage, 0, 0, null);
            }
        }

        public void setBufferedImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
        }
    }
}

   
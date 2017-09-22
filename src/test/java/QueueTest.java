
import ui.camera.VideoCatcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class QueueTest {
    static BufferedImage bufferedImage;

    public static void main(String[] args) {

        Deque<Integer> deque = new ConcurrentLinkedDeque<>();

        for(int i=0;i<10;i++){
            System.out.println("Добавили "+i);
            deque.addFirst(i);
        }


        int size = deque.size();
        for (int i =0;i<size;i++){
            System.out.println(deque.pollLast());
            System.out.println("Размер очереди - "+deque.size());
        }

        Map<Long,Integer> map1 = new HashMap<>();
        Map<Long,Integer> map2 = new HashMap<>();

        List<Long> list = new ArrayList<>();

        for(Long l: map1.keySet()){
            if(!list.contains(l)){
             list.add(l);
            }
        }

        for(Long l: map2.keySet()){
            if(!list.contains(l)){
                list.add(l);
            }
        }
    }

//
//        bufferedImage = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
//        try {
//            bufferedImage = ImageIO.read(new File("C:\\ipCamera\\auto.jpg"));
//            int transparency = bufferedImage.getTransparency();
//            System.out.println("Прозрачность равна - " + transparency);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        JFrame frame = new JFrame("RESR");
//        frame.setPreferredSize(new Dimension(500, 500));
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//        frame.pack();
//
//        frame.getContentPane().setBackground(new Color(81, 150, 36,50));
//
//        JPanel firstPane = new JPanel();
//        firstPane.setBackground(new Color(90, 137, 200));
//        firstPane.setPreferredSize(new Dimension(300, 300));
//
//        LayerUI<JPanel> layerUI = new MyLayer();
//        JLayer<JPanel> layer = new JLayer<JPanel>(firstPane, layerUI);
//
//        frame.getContentPane().add(layer);
//   static public BufferedImage animateCircle(BufferedImage originalImage, int type){
//
//        //The opacity exponentially decreases
//        float opacity = 0.5F;
//
//        BufferedImage resizedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), type);
//        Graphics2D g = resizedImage.createGraphics();
//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
//        g.drawImage(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
//        g.dispose();
//
//        return resizedImage;
//    }
//
//
//    static class MyLayer extends LayerUI<JPanel> {
//
//        @Override
//        public void paint(Graphics g, JComponent c) {
//            super.paint(g, c);
//            if (bufferedImage != null) {
//                g.drawImage(animateCircle(VideoCatcher.processImage(bufferedImage,300,300),BufferedImage.TYPE_INT_ARGB), 100, 100, null);
//                g.dispose();
//            }
//        }
//    }
}

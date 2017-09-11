
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;



import javax.imageio.ImageIO;
import javax.sound.midi.Soundbank;
import javax.swing.*;

public class CatchImage implements Runnable {
    public static int fps = 0;
    public static boolean WRITE = false;
    public static Deque<BufferedImage> deque = new ConcurrentLinkedDeque<BufferedImage>();

    private URL url;
    private URLConnection connection;
    private BufferedInputStream inputStream;
    ImagePanel panel;
    boolean RUN = true;

    public CatchImage(ImagePanel panel) {
        this.panel = panel;
        try {
            url = new URL("http://195.235.198.107:3346/axis-cgi/mjpg/video.cgi?size=640x480");
//            video.cgi/mjpg/video.cgi?resolution=640×480
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
        int x = 0;
        int t = 0;

        while (RUN) {
            try {
                t = x;
                x = inputStream.read();
            } catch (Exception e) {
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
                    BufferedImage image = ImageIO.read(inputStream);
                    CatchImage.deque.addFirst(image);
                    panel.setBufferedImage(image);
                    fps++;
                    panel.repaint();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


class ImagePanel extends JPanel {
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

class Saver implements Runnable {

    String path = "C:\\chromedriver\\test.mp4";
    File file = new File(path);

    @Override
    public void run() {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            if(CatchImage.WRITE){
                System.out.println("Start making");
                List<BufferedImage> imagesList = new ArrayList<>();
                for(int i=0;i<100;i++){
                    BufferedImage image = CatchImage.deque.pollFirst();
                    if(image!=null) {
                        imagesList.add(image);
                    } else {
                      i--;
                    }
                }
                SeekableByteChannel out = null;
                try {
                    out = NIOUtils.writableFileChannel(path);
                    // for Android use: AndroidSequenceEncoder
                    AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(10, 1));
                    for (int i=0;i<100;i++) {
                        // Generate the image, for Android use Bitmap
                        BufferedImage image = imagesList.get(i);
                        // Encode the image
                        encoder.encodeImage(image);
                    }
                    // Finalize the encoding, i.e. clear the buffers, write the header, etc.
                    encoder.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    NIOUtils.closeQuietly(out);
                }

                CatchImage.WRITE = false;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


class Main {

    public static JLabel label = new JLabel("TEST FPS: ");

    public static void main(String[] args) {

        ImagePanel imagePanel = new ImagePanel();

        CatchImage catchImage = new CatchImage(imagePanel);

        Thread thread = new Thread(catchImage);
        thread.start();

        Thread saverThread = new Thread(new Saver());
        saverThread.start();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Main.label.setText("TEST FPS: " + CatchImage.fps);
                    CatchImage.fps = 0;
                    i++;
                    if (i == 10) {
                        CatchImage.WRITE = true;
                    } else if (i>20) {
                        CatchImage.WRITE = false;
                        System.out.println("STOP============================================");
                    }
                }
            }
        });
        thread1.start();
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(500, 500));

        frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
        frame.getContentPane().add(label, BorderLayout.NORTH);
        frame.pack();

    }
}

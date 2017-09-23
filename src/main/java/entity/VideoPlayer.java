package entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoPlayer extends JPanel {
    private static boolean showVideoPlayer = false;
    private static boolean STOP;
    private static boolean PAUSE;
    private static boolean PLAY;
    private static boolean SetPOSITION;
    private static boolean NextIMAGE;
    private static boolean PrewIMAGE;

    private static int nextImagesInt;
    private static int prewImagesInt;

    private static int position;

    private static List<JLabel> list;
    //    private static List<JPanel> listP;
    private static JPanel sliderPanel;

    private static int countDoNotShowImage;

    private static int speed = 0;

    public static JLabel informLabel;
    private static JLabel sliderLabel;

    public VideoPlayer(Map<Integer, File> map, String date) {
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setPreferredSize(new Dimension(1100, 550));
        this.setLayout(new BorderLayout());
        JPanel mainVideoPane = new JPanel(new FlowLayout());
        mainVideoPane.setPreferredSize(new Dimension(800, 510));
        boolean setMainPane = false;

        list = new ArrayList<>();

        List<Thread> threadList = new ArrayList<>();
        for (int j = 1; j < 5; j++) {
            File file = map.get(j);
            VideoPlayerPanel videoPlayer = new VideoPlayerPanel(file, j);
            if (!setMainPane) {
                if (file != null) {
                    videoPlayer.setMainPanel(true);
                    setMainPane = true;
                }
            }
            threadList.add(videoPlayer.getThread());
            mainVideoPane.add(videoPlayer);
        }

        for (Thread thread : threadList) {
            if (thread != null) {
                thread.start();
            }
        }

        this.add(mainVideoPane, BorderLayout.CENTER);
        JPanel buttonsPane = new JPanel(new FlowLayout());
        buttonsPane.setPreferredSize(new Dimension(1090, 35));

        JButton nextImage = new JButton("NEXT");
        nextImage.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(true);
            setPrewIMAGE(false);

            informLabel.setText("NEXT");
            prewImagesInt = 0;
            nextImagesInt++;
        });

        JButton previousImage = new JButton("PREV");
        previousImage.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(false);
            setPrewIMAGE(true);
            informLabel.setText("PREV");
            prewImagesInt++;
            nextImagesInt = 0;
        });

        JButton slowerButton = new JButton("<<");
        slowerButton.addActionListener((e) -> {
            if (countDoNotShowImage == 0) {
                informLabel.setText("<<");
                VideoPlayer.speed = ((VideoPlayer.speed + 1) * 20);
            } else {
                countDoNotShowImage--;
                informLabel.setText((countDoNotShowImage * 2) + "X");
                if (countDoNotShowImage < 1) {
                    countDoNotShowImage = 0;
                }
            }

            if (speed > 1000) {
                speed = 1000;
            }


            System.out.println("Пропускаем кадров - " + countDoNotShowImage);
            System.out.println("Засыпаем на милисекунд - " + speed);
        });

        JButton fasterButton = new JButton(">>");
        fasterButton.addActionListener((e) -> {
            if (speed == 0) {
                countDoNotShowImage++;
                informLabel.setText((countDoNotShowImage * 2) + "X");
                if (countDoNotShowImage > 9) {
                    countDoNotShowImage = 10;
                }
            } else {
                informLabel.setText(">>");
                VideoPlayer.speed = (int) ((VideoPlayer.speed + 1) * 0.05);
            }

            if (speed < 1) {
                speed = 0;
            }

            System.out.println("Пропускаем кадров - " + countDoNotShowImage);
            System.out.println("Засыпаем на милисекунд - " + speed);
        });

        JButton playButton = new JButton("PLAY");
        playButton.addActionListener(actionEvent -> {
            setPLAY(true);
            setSTOP(false);
            setPAUSE(false);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("PLAY");
        });

        JButton pauseButton = new JButton("PAUSE");
        pauseButton.addActionListener((e) -> {
            setPLAY(false);
            setSTOP(false);
            setPAUSE(true);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("PAUSE");
        });
        JButton stopButton = new JButton("STOP");
        stopButton.addActionListener(actionEvent -> {
            setPLAY(false);
            setSTOP(true);
            setPAUSE(false);
            setNextIMAGE(false);
            setPrewIMAGE(false);
            informLabel.setText("STOP");
        });


        JLabel label1 = new JLabel(date);
        sliderLabel = new JLabel("0 %");
        sliderLabel.setPreferredSize(new Dimension(50, 10));
        informLabel = new JLabel("STOP");
        informLabel.setPreferredSize(new Dimension(50, 10));
        buttonsPane.add(label1);
        buttonsPane.add(Box.createRigidArea(new Dimension(20, 10)));
        buttonsPane.add(informLabel);
        buttonsPane.add(Box.createRigidArea(new Dimension(20, 10)));
        buttonsPane.add(sliderLabel);
        buttonsPane.add(Box.createRigidArea(new Dimension(20, 10)));
        buttonsPane.add(previousImage);
        buttonsPane.add(slowerButton);
        buttonsPane.add(playButton);
        buttonsPane.add(pauseButton);
        buttonsPane.add(stopButton);
        buttonsPane.add(fasterButton);
        buttonsPane.add(nextImage);
        buttonsPane.add(Box.createRigidArea(new Dimension(250, 10)));
        JPanel southPane = new JPanel();

        sliderPanel = new JPanel();
        GridLayout layout = new GridLayout(1, 100, 0, 0);
        sliderPanel.setLayout(layout);
        sliderPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        sliderPanel.setPreferredSize(new Dimension(800, 30));
        for (int i = 1; i < 100; i++) {
//            JPanel panel = new JPanel();
//            panel.setPreferredSize(new Dimension(10,10));
//            panel.setBackground(Color.cyan);
//            int finalI = i;
//            panel.addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    System.out.println("Значение на слайдере изменилось на: " + finalI);
//                    setSetPOSITION(true);
//                    position = finalI;
//                    System.out.println("Установили SETPOSSITION - "+finalI);
//                }
//            });
//
//            sliderPanel.add(panel);
//            listP.add(panel);

//            if(i==99){
//                sliderPanel.add(new JLabel("!"));
//            }

//            JLabel label = new JLabel(String.valueOf((char) 8623));
            JLabel label = new JLabel(String.valueOf((char) 9899));
            label.setForeground(Color.LIGHT_GRAY);
//            JLabel label = new JLabel(String.valueOf((char) 8226));
//            label.setFont(new Font("Comic Sans MS",Font.BOLD,12));
            int finalI = i;
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Значение на слайдере изменилось на: " + finalI);
                    setSetPOSITION(true);
                    position = finalI;
                    System.out.println("Установили SETPOSSITION - " + finalI);
                }
            });
            sliderPanel.add(label);
            list.add(label);
        }
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
        southPane.add(sliderPanel);
        southPane.add(buttonsPane);

        this.add(southPane, BorderLayout.SOUTH);
        previousImage.addKeyListener(new KeyAdapter() {//TODO ikjhnbgvfgbhnjmk,l.,kmjnhbg
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 37) {
                    System.out.println("Нажали клавишу Предидущий кадр");
                    setPLAY(false);
                    setSTOP(false);
                    setPAUSE(true);
                    setNextIMAGE(false);
                    setPrewIMAGE(true);
                    informLabel.setText("PREV");
                    prewImagesInt++;
                    nextImagesInt = 0;
                }
            }
        });

        nextImage.addKeyListener(new KeyAdapter() {//TODO ikjhnbgvfgbhnjmk,l.,kmjnhbg
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 39) {
                    System.out.println("Нажали клавишу СЛЕДУЮЩИЙ КАДР");
                    setPLAY(false);
                    setSTOP(false);
                    setPAUSE(true);
                    setNextIMAGE(true);
                    setPrewIMAGE(false);

                    informLabel.setText("NEXT");
                    prewImagesInt = 0;
                    nextImagesInt++;
                }
            }
        });

    }

    static int getPosition() {
        return position;
    }

    static boolean isPLAY() {
        return PLAY;
    }

    static void setPLAY(boolean PLAY) {
        VideoPlayer.PLAY = PLAY;
    }

    static boolean isSTOP() {
        return STOP;
    }

    static boolean isPAUSE() {
        return PAUSE;
    }

    static void setSTOP(boolean STOP) {
        VideoPlayer.STOP = STOP;
    }

    static void setPAUSE(boolean PAUSE) {
        VideoPlayer.PAUSE = PAUSE;
    }

    static void setSliderPosition(int position) {

        for (int i = 0; i < position - 1; i++) {
            list.get(i).setForeground(new Color(4, 12, 247));
        }

        for (int i = position; i < list.size(); i++) {
            list.get(i).setForeground(Color.LIGHT_GRAY);
        }
        sliderPanel.repaint();
        sliderLabel.setText(position + "%");
        sliderLabel.repaint();
    }

    static boolean isSetPOSITION() {
        return SetPOSITION;
    }

    static void setSetPOSITION(boolean setPOSITION) {
        System.out.println("possition " + setPOSITION);
        SetPOSITION = setPOSITION;
    }

    static int getSpeed() {
        return speed;
    }

    static int getCountDoNotShowImage() {
        return countDoNotShowImage;
    }

    public static boolean isShowVideoPlayer() {
        return showVideoPlayer;
    }

    public static void setShowVideoPlayer(boolean showVideoPlayer) {
        VideoPlayer.showVideoPlayer = showVideoPlayer;
    }

    static boolean isPrewIMAGE() {
        return PrewIMAGE;
    }

    private static void setNextIMAGE(boolean nextIMAGE) {
        NextIMAGE = nextIMAGE;
    }

    private static void setPrewIMAGE(boolean prewIMAGE) {
        PrewIMAGE = prewIMAGE;
    }

    static boolean isNextIMAGE() {
        return NextIMAGE;
    }

    static int getNextImagesInt() {
        return nextImagesInt;
    }

    static int getPrewImagesInt() {
        return prewImagesInt;
    }
}

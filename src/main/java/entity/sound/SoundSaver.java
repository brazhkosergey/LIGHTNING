package entity.sound;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SoundSaver extends Thread {
    private DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
    private static int RTP_RCV_PORT = 25002; //port where the client will receive the RTP packets 25000

    private Timer timer; //timer used to receive data from the UDP socket
    private byte[] buf; //buffer used to store data received from the server

    private final static int INIT = 0;
    private final static int READY = 1;
    private final static int PLAYING = 2;

    static int state; //RTSP state == INIT or READY or PLAYING
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;

    static String VideoFileName; //video file to request to the server
    private int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
    private String RTSPid; //ID of the RTSP session (given by the RTSP Server)

    Map<Long, byte[]> map = new HashMap<>();
    final static String CRLF = "\r\n";

    private int soundPacketNumber;
    private int audioFPS = 0;
    private int fpsNotZero;
    private boolean hearSound;
    private boolean saveSound;

    private int stopSaveAudioInt;
    private int sizeAudioSecond;

    private boolean stopSaveAudio;
    private boolean startSaveAudio;
    private boolean delBytes;

    private boolean connect = false;


    Deque<Long> deque;
    Map<Long, byte[]> mainMapSaveFile;

    public SoundSaver(String addressName) {
        try{
            System.out.println("Исходная строка - " + addressName);
            int i = addressName.indexOf("://");
            String substring = addressName.substring(i+3, addressName.length());
            System.out.println("Первая строка -"+substring);
            int i1 = substring.indexOf("/");
            String address = substring.substring(0, i1);
            String fileName = substring.substring(i1, substring.length());

            System.out.println("Адресс - "+address);
            System.out.println("Имя файла - "+fileName);

            VideoFileName = fileName; //"/axis-media/media.amp"     rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov
            try {
                Socket RTSPsocket = new Socket(address, 554);
                RTSPBufferedReader = new BufferedReader(new InputStreamReader(RTSPsocket.getInputStream()));
                RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(RTSPsocket.getOutputStream()));
                connect = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(connect){
                deque = new ConcurrentLinkedDeque<>();
                mainMapSaveFile = new HashMap<>();

                Thread thread = new Thread(() -> {
                    int updateRequest = 0;
                    while (hearSound) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Пакетов в секунду - " + audioFPS);
                        if (audioFPS != 0) {
                            fpsNotZero = audioFPS;
                        }

                        if (!startSaveAudio) {
                            if (MainVideoCreator.isSaveVideo()) {
                                startSaveAudio = true;
                            }

                            delBytes = deque.size() > sizeAudioSecond * fpsNotZero;

                            if (sizeAudioSecond != MainFrame.getTimeToSave()) {
                                sizeAudioSecond = MainFrame.getTimeToSave();
                                while (true) {
                                    if (deque.size() > sizeAudioSecond * fpsNotZero) {
                                        map.remove(deque.pollLast());
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }

                        if (startSaveAudio) {
                            stopSaveAudioInt++;
                            if (stopSaveAudioInt == sizeAudioSecond) {
                                stopSaveAudio = true;
                            }
                        }

                        audioFPS = 0;
                        if (updateRequest > 40) {
                            send_RTSP_request("PLAY");
                            updateRequest = 0;
                        } else {
                            updateRequest++;
                        }
                    }
                });
                hearSound = true;
                thread.start();
                state = INIT;
                timer = new Timer(20, new timerListener());
                timer.setInitialDelay(0);
                timer.setCoalesce(true);
                try {
                    RTPsocket = new DatagramSocket(RTP_RCV_PORT);
                    RTPsocket.setSoTimeout(5);
                } catch (SocketException se) {
                    se.printStackTrace();
                }
                buf = new byte[15000];
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SETUP() {
        if(connect){
            RTSPSeqNb = 1;
            send_RTSP_request("SETUP");
            if (parse_server_response() != 200)
                System.out.println("Invalid Server Response");
            else {
                state = READY;
                System.out.println("New RTSP state: READY");
            }
        }
    }

    public void PLAY() {
        if (connect&&state == READY) {
            RTSPSeqNb++;
            send_RTSP_request("PLAY");
            if (parse_server_response() != 200) {
                System.out.println("Invalid Server Response");
            } else {
                state = PLAYING;
                timer.start();
            }
        }
    }

    public void TEARDOWN() {
        if(connect){
            hearSound = false;
            RTSPSeqNb++;
            //Send TEARDOWN message to the server
            send_RTSP_request("TEARDOWN");

            if (parse_server_response() != 200)
                System.out.println("Invalid Server Response");
            else {
                timer.stop();
            }
        }
    }

    class timerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            DatagramPacket rcvdp = new DatagramPacket(buf, buf.length);
            try {
                RTPsocket.receive(rcvdp);
                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());
                byte[] bytes = new byte[rtp_packet.getpayload_length()];
                rtp_packet.getpayload(bytes);
                audioFPS++;
                long l = System.currentTimeMillis();
                deque.addFirst(l);
                map.put(l, checkSound(bytes));

                if (!startSaveAudio) {
                    if (delBytes) {
                        map.remove(deque.pollLast());
                    }
                } else {
                    if (stopSaveAudio) {
                        MainVideoCreator.stopCatchVideo();
                        int size = deque.size();
                        for (int i = 0; i < size; i++) {
                            Long timeLong = deque.pollLast();
                            mainMapSaveFile.put(timeLong, map.get(timeLong));
                            map.remove(timeLong);
                        }

                        saveSoundToFile(mainMapSaveFile);
                        map = new HashMap<>();
                        mainMapSaveFile = new HashMap<>();
                        stopSaveAudioInt = 0;
                        stopSaveAudio = false;
                        startSaveAudio = false;
                    }
                }
            } catch (InterruptedIOException iioe) {
//                iioe.printStackTrace();
//                System.out.println("Nothing to read");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("Exception caught: " + ioe);
            }
        }
    }

    private byte[] checkSound(byte[] totalArr) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(totalArr);
        int count = 0;
        byte[] buffer = new byte[4];
        try {
            while (byteArrayInputStream.read(buffer) > 0) {
                int value = ((buffer[0] & 0xff) | (buffer[1] << 8)) << 16 >> 16;//-32640
                if (value == -32640) {
                    count++;
                } else {
                    count = 0;
                }
                if (count > 50) {
                    MainVideoCreator.startCatchVideo(false);
                }
//                if (Math.abs(value) > maxAmpl) {//32768
//                    maxAmpl = Math.abs(value);
//                    System.out.println("Max Value: " + maxAmpl);
//                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return totalArr;
    }

    private void saveSoundToFile(Map<Long, byte[]> mainMapSaveFile) {
        MainVideoCreator.saveAudioBytes(mainMapSaveFile);
    }

    private int parse_server_response() {
        int reply_code = 0;
        try {
            System.out.println("===============================================");
            String StatusLine = RTSPBufferedReader.readLine();
            while (!StatusLine.contains("RTSP/1.0")) {
                System.out.println("Status Line - " + StatusLine);
                StatusLine = RTSPBufferedReader.readLine();
            }

            System.out.println("Status Line - " + StatusLine);
            StringTokenizer tokens = new StringTokenizer(StatusLine);
            tokens.nextToken(); //skip over the RTSP version
            try {
                reply_code = Integer.parseInt(tokens.nextToken());
                System.out.println("Reply CODE:" + reply_code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (reply_code == 200) {
                String SeqNumLine = RTSPBufferedReader.readLine();
                System.out.println(SeqNumLine);

                String SessionLine = RTSPBufferedReader.readLine();
                System.out.println("Session line-" + SessionLine);

                String transportLine = RTSPBufferedReader.readLine();
                System.out.println(transportLine);
                String stringDate = RTSPBufferedReader.readLine();
                System.out.println(stringDate);
                System.out.println("Session line is: " + SessionLine);

                tokens = new StringTokenizer(SessionLine);
                tokens.nextToken();//skip over the RTSP version
                RTSPid = tokens.nextToken();
                System.out.println("Session is: " + RTSPid);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
        return (reply_code);
    }

    private void send_RTSP_request(String request_type) {
        System.out.println("===========================================");
        try {
            RTSPBufferedWriter.write(request_type + " " + VideoFileName + " " + "RTSP/1.0" + CRLF);
            System.out.print(request_type + " " + VideoFileName + " " + "RTSP/1.0" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
            System.out.print("CSeq: " + RTSPSeqNb + CRLF);
            if (request_type.equals("SETUP")) {
                RTSPBufferedWriter.write("Transport: RTP/AVP;unicast;client_port=" + RTP_RCV_PORT + CRLF + CRLF);//Transport: RTP/AVP;unicast;client_port=49501-49502
                System.out.print("Transport: RTP/AVP; unicast; client_port=" + RTP_RCV_PORT + CRLF);
            } else {
                RTSPBufferedWriter.write("Session: " + RTSPid + CRLF + CRLF);
                System.out.print(RTSPid + CRLF + CRLF);
            }
            RTSPBufferedWriter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception caught: " + ex);
        }
        System.out.println("===========================================");
    }
}

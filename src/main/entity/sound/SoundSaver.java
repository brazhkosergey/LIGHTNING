package entity.sound;

import entity.MainVideoCreator;
import ui.main.MainFrame;

import javax.sound.sampled.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SoundSaver extends Thread {
    private DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
    private static int RTP_RCV_PORT = 25002; //port where the client will receive the RTP packets 25000

    private byte[] buf; //buffer used to store data received from the server

    private final static int INIT = 0;
    private final static int READY = 1;
    private final static int PLAYING = 2;

    private static int state; //RTSP state == INIT or READY or PLAYING
    private static BufferedReader RTSPBufferedReader;
    private static BufferedWriter RTSPBufferedWriter;

    private static String FileName;
    private int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
    private String RTSPid; //ID of the RTSP session (given by the RTSP Server)

    private Map<Long, byte[]> map = new HashMap<>();
    private final static String CRLF = "\r\n";

    private int audioFPS = 0;

    private boolean hearSound;
    private boolean playSound;

    private int sizeAudioSecond;
    private SourceDataLine clipSDL = null;
    private boolean stopSaveAudio;
    private boolean startSaveAudio;

    private boolean connect = false;

    private Deque<Long> deque;
    private Deque<Integer> FPSDeque;
    private Map<Long, byte[]> mainMapSaveFile;

    private Thread mainThread;
    private Thread updateDataThread;

    public SoundSaver(String addressName) {
        try {
            int i = addressName.indexOf("://");
            String substring = addressName.substring(i + 3, addressName.length());
            int i1 = substring.indexOf("/");
            String address = substring.substring(0, i1);
            String fileName = substring.substring(i1, substring.length());
            System.out.println("Адресс аудио потока - " + address);
            System.out.println("Имя файла аудио потока - " + fileName);

            FileName = fileName; //"/axis-media/media.amp"     rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov
            try {
                Socket RTSPsocket = new Socket(address, 554);
                RTSPBufferedReader = new BufferedReader(new InputStreamReader(RTSPsocket.getInputStream()));
                RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(RTSPsocket.getOutputStream()));
                connect = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (connect) {
                deque = new ConcurrentLinkedDeque<>();
                mainMapSaveFile = new HashMap<>();
                FPSDeque = new ConcurrentLinkedDeque<>();

                hearSound = true;
                mainThread = new Thread(() -> {
                    while (hearSound) {
                        DatagramPacket rcvdp = new DatagramPacket(buf, buf.length);
                        try {
                            RTPsocket.receive(rcvdp);
                            RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());
                            byte[] bytes = new byte[rtp_packet.getpayload_length()];
                            rtp_packet.getpayload(bytes);

                            audioFPS++;
                            long l = System.currentTimeMillis();
                            deque.addFirst(l);
                            map.put(l, bytes);
                        } catch (InterruptedIOException iioe) {
//                            countHaveNotDataToRead++;
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            System.out.println("Exception caught: " + ioe);
                        }
                    }
                });

                mainThread.setName("Save Audio Thread");
                mainThread.setPriority(MIN_PRIORITY);

                updateDataThread = new Thread(() -> {
                    int updateRequest = 0;
                    while (hearSound) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        MainFrame.audioPacketCount.setText(audioFPS + " : " + FPSDeque.size());
                        FPSDeque.addFirst(audioFPS);
                        if (!startSaveAudio) {
                            sizeAudioSecond = MainFrame.getSecondsToSave();
                            while (FPSDeque.size() > sizeAudioSecond) {
                                Integer integer = FPSDeque.pollLast();
                                for (int j = 0; j < integer; j++) {
                                    Long aLong = deque.pollLast();
                                    map.remove(aLong);
                                }
                            }
                        } else {
                            if (stopSaveAudio) {
                                int size = FPSDeque.size();
                                for (int j = 0; j < size; j++) {
                                    Integer integer = FPSDeque.pollLast();
                                    for (int k = 0; k < integer; k++) {
                                        Long timeLong = deque.pollLast();
                                        if (map.containsKey(timeLong)) {
                                            byte[] bytes1 = map.get(timeLong);
                                            if (bytes1 != null) {
                                                mainMapSaveFile.put(timeLong, bytes1);
                                            }
                                            map.remove(timeLong);
                                        }
                                    }
                                }

                                saveSoundToFile(mainMapSaveFile);
                                map = new HashMap<>();
                                mainMapSaveFile = new HashMap<>();
                                stopSaveAudio = false;
                                startSaveAudio = false;
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

                    if (clipSDL != null) {
                        clipSDL.drain();
                        clipSDL.stop();
                        clipSDL.close();
                    }
                });

                updateDataThread.setName("Update Audio Thread");
                updateDataThread.setPriority(MIN_PRIORITY);

                state = INIT;
                try {
                    RTPsocket = new DatagramSocket(RTP_RCV_PORT);
                    RTPsocket.setSoTimeout(5);
                } catch (SocketException se) {
                    hearSound = false;
                    se.printStackTrace();
                }
                buf = new byte[15000];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSaveAudio() {
        startSaveAudio = true;
    }

    public void stopSaveAudio() {
        stopSaveAudio = true;
    }

    public void SETUP() {
        if (connect) {
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
        if (connect && state == READY) {
            RTSPSeqNb++;
            send_RTSP_request("PLAY");
            if (parse_server_response() != 200) {
                System.out.println("Invalid Server Response");
            } else {
                state = PLAYING;
                mainThread.start();
                updateDataThread.start();
            }
        }
    }

    public void TEARDOWN() {
        hearSound = false;
        if (connect) {
            RTSPSeqNb++;
            //Send TEARDOWN message to the server
            send_RTSP_request("TEARDOWN");
        }
    }

    private void saveSoundToFile(Map<Long, byte[]> mainMapSaveFile) {
        MainVideoCreator.saveAudioBytes(mainMapSaveFile);
    }

    private int parse_server_response() {
        int reply_code = 0;
        try {
            String StatusLine = RTSPBufferedReader.readLine();
            while (!StatusLine.contains("RTSP/1.0")) {
                StatusLine = RTSPBufferedReader.readLine();
            }

            StringTokenizer tokens = new StringTokenizer(StatusLine);
            tokens.nextToken(); //skip over the RTSP version
            try {
                reply_code = Integer.parseInt(tokens.nextToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (reply_code == 200) {
                String SeqNumLine = RTSPBufferedReader.readLine();

                String SessionLine = RTSPBufferedReader.readLine();

                String transportLine = RTSPBufferedReader.readLine();
                String stringDate = RTSPBufferedReader.readLine();

                tokens = new StringTokenizer(SessionLine);
                tokens.nextToken();//skip over the RTSP version
                RTSPid = tokens.nextToken();
//                System.out.println("Session is: " + RTSPid);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
        return (reply_code);
    }

    private void send_RTSP_request(String request_type) {
        try {
            RTSPBufferedWriter.write(request_type + " " + FileName + " " + "RTSP/1.0" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);
            if (request_type.equals("SETUP")) {
                RTSPBufferedWriter.write("Transport: RTP/AVP;unicast;client_port=" + RTP_RCV_PORT + CRLF + CRLF);//Transport: RTP/AVP;unicast;client_port=49501-49502
            } else {
                RTSPBufferedWriter.write("Session: " + RTSPid + CRLF + CRLF);
            }
            RTSPBufferedWriter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

package entity.sound;

import com.xuggle.xuggler.IAudioSamples;
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

    static int state; //RTSP state == INIT or READY or PLAYING
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;

    static String FileName; //video file to request to the server
    private int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
    private String RTSPid; //ID of the RTSP session (given by the RTSP Server)

    Map<Long, byte[]> map = new HashMap<>();
    final static String CRLF = "\r\n";

    private int audioFPS = 0;
    private int fpsNotZero;

    private boolean hearSound;
    private boolean playSound;

    private int stopSaveAudioInt;
    private int sizeAudioSecond;
    private int countHaveNotDataToRead;
    private SourceDataLine clipSDL = null;
    private boolean stopSaveAudio;
    private boolean startSaveAudio;
    private boolean delBytes;

    private boolean connect = false;

    private Deque<Long> deque;
    private Map<Long, byte[]> mainMapSaveFile;
    List<Long> list;

    private Thread mainThread;
    private Thread updateDataThread;
    private Thread playThread;

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

                list = new ArrayList<>();

                hearSound = true;
                mainThread = new Thread(() -> {
                    while (hearSound) {
                        DatagramPacket rcvdp = new DatagramPacket(buf, buf.length);
                        try {
                            RTPsocket.receive(rcvdp);
                            RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());
                            byte[] bytes = new byte[rtp_packet.getpayload_length()];
                            rtp_packet.getpayload(bytes);

//                            clipSDL.write(bytes,0,bytes.length);

                            audioFPS++;
                            long l = System.currentTimeMillis();
                            deque.addFirst(l);
                            map.put(l, bytes);
                            list.add(l);

                            if (!startSaveAudio) {
                                if (delBytes) {
                                    Long aLong = deque.pollLast();
                                    if(map.containsKey(aLong)){
                                        map.remove(aLong);
                                    }

                                    if(mainMapSaveFile.containsKey(aLong)){
                                        mainMapSaveFile.remove(aLong);
                                    }
                                }
                            } else {
                                if (stopSaveAudio) {
                                    int size = deque.size();
                                    for (int j = 0; j < size; j++) {
                                        Long timeLong = deque.pollLast();
                                        if(map.containsKey(timeLong)){
                                            byte[] bytes1 = map.get(timeLong);
                                            if (bytes1 != null) {
                                                mainMapSaveFile.put(timeLong, bytes1);
                                            }
                                            map.remove(timeLong);
                                        }
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
                            countHaveNotDataToRead++;
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

                        MainFrame.audioPacketCount.setText(audioFPS + " : " + countHaveNotDataToRead);

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
                        countHaveNotDataToRead = 0;
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

                playThread = new Thread(() -> {
                    while (true) {
                        if (playSound) {
                            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(35535);

//                            Collections.sort(list);
                            for(int j=0;j<list.size();j++){
                                Long time = list.get(j);
                                if(map.containsKey(time)){
                                    byte[] bytes = map.get(time);
                                    if(bytes!=null){
                                        try {
                                            temporaryStream.write(bytes );
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            list.clear();

                            if (clipSDL != null) {
                                byte[] bytes = temporaryStream.toByteArray();
                                clipSDL.write(bytes, 0, bytes.length);
                                try {
                                    temporaryStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
//                           List<Long> longList = new ArrayList<>();
//                           int count=0;
//                           for(Long l:deque){
//                               count++;
//                               longList.add(l);
//                               if(count>fpsNotZero){
//                                   break;
//                               }
//                           }
//
//                           Collections.sort(longList);
//
//                           int size = 0;
//
//                           for (Long integer : map.keySet()) {
//                               byte[] bytes = map.get(integer);
//                               size = size + bytes.length;
//                           }
//
//                           ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(35535);
//                           for (Long l:longList) {
//                               byte[] bytes = map.get(l);
//                               if(bytes!=null){
//                                   try {
//                                       temporaryStream.write(bytes);
//                                   } catch (IOException e1) {
//                                       e1.printStackTrace();
//                                   }
//                               }
//                           }
//
//                           System.out.println("Количество байт - " + size);
//                           ByteArrayInputStream interleavedStream = new ByteArrayInputStream(temporaryStream.toByteArray());
////                           final AudioFormat audioFormat = new AudioFormat(
////                                   AudioFormat.Encoding.ULAW,
////                                   8000f, // sample rate - you didn't specify, 44.1k is typical
////                                   8,      // how many bits per sample, i.e. per value in your byte array
////                                   1,      // you want two channels (stereo)
////                                   1,      // number of bytes per frame (frame == a sample for each channel)
////                                   8000f, // frame rate
////                                   true);  // byte order
//
//                           final AudioFormat audioFormat = new AudioFormat(8000.0f,8,1,true,true);
//             //            final int numberOfFrames = size/2; // one frame contains both a left and a right sample
//                           final int numberOfFrames = size; // one frame contains both a left and a right sample
//                           final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);
//
//                           if(clip.isActive()){
//                               clip.stop();
//                           }
//
//                           try {
//                               clip.open(audioStream);
//                           } catch (LineUnavailableException e) {
//                               e.printStackTrace();
//                           } catch (IOException e) {
//                               e.printStackTrace();
//                           }
//
//                           clip.setFramePosition(0); //устанавливаем указатель на старт
//                           clip.start(); //Поехали!!!
//
////                           clip.stop(); //Останавливаем
////                           clip.close(); //Закрываем
//                       }
//                       try {
//                           Thread.sleep(1000);
//                       } catch (InterruptedException e) {
//                           e.printStackTrace();
//                       }
                        }
                    }
                });
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
                playAudio();
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


    private void playAudio() {

        try {
////            final AudioFormat audioFormat = new AudioFormat(
////                    AudioFormat.Encoding.ULAW,
////                    8000f,// sample rate - you didn't specify, 44.1k is typical
////                    8,// how many bits per sample, i.e. per value in your byte array
////                    2,     // you want two channels (stereo)
////                    1,     // number of bytes per frame (frame == a sample for each channel)
////                    8000f,// frame rate
////                    true);// byte ordzer
//
////            AudioFormat audioFormat = new AudioFormat(32000.0f,
//////                (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
////                    (int) IAudioSamples.findSampleBitDepth(IAudioSamples.Format.FMT_DBLP),
////                    1,
////                    true, /* xuggler defaults to signed 16 bit samples */
////                    false);
//
//            AudioFormat audioFormat = new AudioFormat(8000.0f, 8, 1, false, true);
//            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
//            clipSDL = (SourceDataLine) AudioSystem.getLine(info);
//
//
////            clipSDL = AudioSystem.getSourceDataLine(audioFormat);
//            clipSDL.open(audioFormat);
//            clipSDL.start();
//            playSound = true;
//            playThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

package entity;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
//import org.jcodec.api.awt.AWTSequenceEncoder;
//import org.jcodec.common.io.NIOUtils;
//import org.jcodec.common.io.SeekableByteChannel;
//import org.jcodec.common.model.Rational;
import ui.camera.CameraPanel;
import ui.camera.VideoCatcher;
import ui.main.MainFrame;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainVideoCreator {
    private static Date date;
    private static boolean saveVideo = false;
    private static boolean programingLightCatch;
    private static Thread continueVideoThread;
    private static int secondVideoSave;

    public static void startCatchVideo(boolean programingLightCatch) {
        MainVideoCreator.programingLightCatch = programingLightCatch;
        if (!saveVideo) {
            MainVideoCreator.date = new Date(System.currentTimeMillis());
            saveVideo = true;
            continueVideoThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        MainFrame.showInformMassage("Збережено " + (secondVideoSave++) + " сек.", true);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (secondVideoSave == MainFrame.getTimeToSave()) {
                            break;
                        }
                    }
                    secondVideoSave = 0;
                    continueVideoThread = null;
                }
            });
            continueVideoThread.start();
        } else {
            secondVideoSave = 0;
            for (Integer integer : MainFrame.getCameras().keySet()) {
                VideoCatcher videoCatcher = MainFrame.getCameras().get(integer).getVideoCatcher();
                if (videoCatcher.isCatchVideo()) {
                    videoCatcher.continueSaveVideo();
                }
            }
        }
    }

    public static void stopCatchVideo() {
        saveVideo = false;
    }

    public static void saveAudioBytes(Map<Long, byte[]> map) {

        int size = 0;
        List<Long> longList = new ArrayList<>();

        for(Long l:map.keySet()){
            longList.add(l);
        }

        Collections.sort(longList);

        for (Long integer : map.keySet()) {
            byte[] bytes = map.get(integer);
            size = size + bytes.length;
        }
        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(35535);
        for (Long l:longList) {
            byte[] bytes = map.get(l);
            try {
                temporaryStream.write(bytes);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        ByteArrayInputStream interleavedStream = new ByteArrayInputStream(temporaryStream.toByteArray());
        final AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.ULAW,
                8000f, // sample rate - you didn't specify, 44.1k is typical
                8,      // how many bits per sample, i.e. per value in your byte array
                1,      // you want two channels (stereo)
                1,      // number of bytes per frame (frame == a sample for each channel)
                8000f, // frame rate
                true);  // byte order

        final int numberOfFrames = size;
        File audioFile = new File("C:\\ipCamera\\bytes\\"+date.getTime()+".wave");
        final AudioInputStream audioStream = new AudioInputStream(interleavedStream, audioFormat, numberOfFrames);

        try {
            if (audioFile.createNewFile()) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        String path = "C:\\ipCamera\\bytes\\" + date.getTime() + ".sound";
        System.out.println("Создаем аудио файл - " + path);

        File file = new File(path);
        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                for (long l : map.keySet()) {
                    byte[] bytes = map.get(l);
                    try {
                        if (bytes != null) {
                            fileOutputStream.write(bytes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveBytes(Integer numberOfGroup, List<byte[]> list, int totalFPS, Map<Integer, Boolean> percentOfFramesEvent, int videoNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int i = 0;

        for (Integer integer : percentOfFramesEvent.keySet()) {
            i++;
            if (percentOfFramesEvent.get(integer)) {
                stringBuilder.append("(").append(integer).append(")");
            } else {
                stringBuilder.append(integer);
            }
            if (i != percentOfFramesEvent.size()) {
                stringBuilder.append(",");
            }
        }

        stringBuilder.append("]");
        String eventPercent = stringBuilder.toString();

        String path = "C:\\ipCamera\\bytes\\" + (date.getTime() + videoNumber) + "-" + numberOfGroup + "(" + totalFPS + ")" + eventPercent + "{" + videoNumber + "}.tmp";
        System.out.println("Создаем файл - " + path);

        File file = new File(path);

        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                for (byte[] arr : list) {
                    try {
                        if (arr != null) {
                            fileOutputStream.write(arr);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MainFrame.showInformMassage("Блок - " + numberOfGroup + " збережено.", true);
    }

//    public static void encodeVideo(File file) {
//        try {
//            String name = file.getName();
//            String[] split = name.split("-");
//            long dateLong = Long.parseLong(split[0]);
//            Date date = new Date(dateLong);
//            SimpleDateFormat dateFormat = new SimpleDateFormat();
//            dateFormat.applyPattern("dd MM yyyy_HH-mm-ss");
//            String dateString = dateFormat.format(date);
//            String[] fpsSplit = split[1].split("\\.");
//            String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
//            String totalFpsString = fpsSplit[0].substring(2, 4);
//            int totalFPS = Integer.parseInt(totalFpsString);
//            String path = "C:\\ipCamera\\" + dateString + "_" + numberOfGroupCameraString + ".mov";
//
//            File videoFile = new File(path);
//            if (!videoFile.exists()) {
//                try {
//                    videoFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            SeekableByteChannel out = null;
//            try {
//                out = NIOUtils.writableFileChannel(path);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            AWTSequenceEncoder encoder = null;
//            try {
//                encoder = new AWTSequenceEncoder(out, Rational.R(totalFPS, 1));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            FileInputStream fileInputStream = null;
//            try {
//                fileInputStream = new FileInputStream(file);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//            if (bufferedInputStream != null) {
//                ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//                int count = 0;
//                int x = 0;
//                int t = 0;
//
//                BufferedImage image = null;
//                while (x >= 0) {
//                    t = x;
//                    try {
//                        x = bufferedInputStream.read();
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
//
//                        try {
//                            image = ImageIO.read(inputStream);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (image != null) {
//                            try {
//                                encoder.encodeImage(image);
////                            encoder.encodeImage(imageToConnect);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            System.out.println("Пишем изображение - " + count++);
//                            MainFrame.showInformMassage("Зберігаем кадр - " + count++, true);
//                            image = null;
//                        }
//                    }
//                }
//
//                try {
//                    encoder.finish();
//                    bufferedInputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                NIOUtils.closeQuietly(out);
//                System.out.println("Сохраняем файл. Количесво изображений - " + count);
//                MainFrame.showInformMassage("Файл збережено. Всьго кадрів - " + count, true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void encodeVideoXuggle(File file) {
        String name = file.getName();
        String[] split = name.split("-");
        long dateLong = Long.parseLong(split[0]);
        Date date = new Date(dateLong);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("dd MMMM yyyy,HH-mm-ss");
        String dateString = dateFormat.format(date);
        String[] fpsSplit = split[1].split("\\.");
        String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
        int integer = 0;

        try {
            integer = Integer.parseInt(numberOfGroupCameraString);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String totalFpsString = fpsSplit[0].substring(2, 4);
        String totalFpsString = fpsSplit[0].substring(2, 3);
        int totalFPS = Integer.parseInt(totalFpsString);
        String path = "C:\\ipCamera\\" + dateString + ", Группа камер -" + numberOfGroupCameraString + ".mp4";

        float opacity = 0f;
        BufferedImage imageToConnect = null;
        boolean connectImage = false;
        if (MainFrame.imagesForBlock.containsKey(integer)) {
            imageToConnect = MainFrame.imagesForBlock.get(integer);
            opacity = CameraPanel.getOpacity();
            connectImage = true;
            System.out.println("Соединяем изображения - " + connectImage + ". Прозрачность - " + opacity);
        }

        File videoFile = new File(path);
        if (!videoFile.exists()) {
            try {
                videoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final IMediaWriter writer = ToolFactory.makeWriter(path);
        boolean addVideoStream = false;

        long nextFrameTime = 0;

        final long frameRate = 1000 / totalFPS;

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        if (bufferedInputStream != null) {
            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
            int count = 0;
            int x = 0;
            int t = 0;
            BufferedImage image = null;
            while (x >= 0) {
                t = x;
                try {
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

                    if (image != null) {

                        if (!addVideoStream) {
                            writer.addVideoStream(0, 0,
                                    ICodec.ID.CODEC_ID_MPEG4,
                                    image.getWidth(), image.getHeight());
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            ========================================================
//                            writer.addAudioStream()
//                            byte[] audioBytes = new byte[line.getBufferSize() / 2]; // best size?
//                            int numBytesRead = 0;
//                            numBytesRead = line.read(audioBytes, 0, audioBytes.length);
//                            // convert to signed shorts representing samples
//                            int numSamplesRead = numBytesRead / 2;
//                            short[] audioSamples = new short[numSamplesRead];
//                            if (format.isBigEndian()) {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i] << 8) | audioBytes[2 * i + 1]);
//                                }
//                            } else {
//                                for (int i = 0; i < numSamplesRead; i++) {
//                                    audioSamples[i] = (short) ((audioBytes[2 * i + 1] << 8) | audioBytes[2 * i]);
//                                }
//                            }
//                            writer.encodeAudio(0,audioSamples);?????????/
//                             use audioSamples in Xuggler etc
////                            ========================================================
////                            ========================================================
////                            ========================================================



                            addVideoStream = true;
                        }

                        BufferedImage conImage;
                        if (connectImage) {
                            conImage = connectImage(image, imageToConnect, opacity);
                            writer.encodeVideo(0, conImage, nextFrameTime,
                                    TimeUnit.MILLISECONDS);
                            nextFrameTime += frameRate;
                            System.out.println("Пишем изображение - " + count++);
//                            MainFrame.showInformMassage("Зберігаем кадр - " + count++, true);
                        } else {
                            writer.encodeVideo(0, image, nextFrameTime,
                                    TimeUnit.MILLISECONDS);
                            nextFrameTime += frameRate;
                            System.out.println("Пишем изображение - " + count++);
                            MainFrame.showInformMassage("Зберігаем кадр - " + count++, true);
                            image = null;
                        }
                    }
                }
            }

            writer.flush();
            writer.close();
            MainFrame.showInformMassage("Збережено. Кадрів - " + count, true);
        }

        try {
            fileInputStream.close();
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage connectImage(BufferedImage sourceImage, BufferedImage imageToConnect, float opacity) {
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        graphics.drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        graphics.drawImage(imageToConnect, 0, 0, imageToConnect.getWidth(), imageToConnect.getHeight(), null);
        graphics.dispose();
        return image;
    }

    public static void putVideoFromCameraGroup(Integer numberOfGroup, List<byte[]> list, int totalFps, Map<Integer, Boolean> percentOfFramesEvent, int videoNumber) {
        MainFrame.showInformMassage("Зберігаем файл - " + numberOfGroup, true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(() -> saveBytes(numberOfGroup, list, totalFps, percentOfFramesEvent, videoNumber));
        thread.start();
        System.out.println("Номер группы - " + numberOfGroup + ". Размер листа в изображениями - " + list.size());
    }

    public static boolean isSaveVideo() {
        return saveVideo;
    }

    public static boolean isProgramingLightCatch() {
        return programingLightCatch;
    }
}


//        public static void encodeVideoHumble(File file) {
//
//        String name = file.getName();
//        String[] split = name.split("-");
//        long dateLong = Long.parseLong(split[0]);
//        Date date = new Date(dateLong);
//        SimpleDateFormat dateFormat = new SimpleDateFormat();
//        dateFormat.applyPattern("dd MM yyyy_HH-mm-ss");
//        String dateString = dateFormat.format(date);
//        String[] fpsSplit = split[1].split("\\.");
//        String numberOfGroupCameraString = fpsSplit[0].substring(0, 1);
//        String totalFpsString = fpsSplit[0].substring(2, 4);
//        int totalFPS = Integer.parseInt(totalFpsString);
//        String path = "C:\\ipCamera\\" + dateString + "_" + numberOfGroupCameraString + ".mov";
//
//        File videoFile = new File(path);
//        if (!videoFile.exists()) {
//            try {
//                videoFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println("Сохраняем видео в файл " + path);
//        if (!videoFile.exists()) {
//            try {
//                videoFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        /**
//         * Set up the AWT infrastructure to take screenshots of the desktop.
//         */
////        final Robot robot = new Robot();
////        final Toolkit toolkit = Toolkit.getDefaultToolkit();
////        final Rectangle screenbounds = new Rectangle(toolkit.getScreenSize());
//
//        final io.humble.video.Rational framerate = io.humble.video.Rational.make(1, 20);
//
//        /** First we create a muxer using the passed in filename and formatname if given. */
////        final Muxer muxer = Muxer.make(path, null, formatname);
//        final Muxer muxer = Muxer.make(path, null, "mp4");
////        final Muxer muxer = Muxer.make(path, null, null);
//
//        /** Now, we need to decide what type of codec to use to encode video. Muxers
//         * have limited sets of codecs they can use. We're going to pick the first one that
//         * works, or if the user supplied a codec name, we're going to force-fit that
//         * in instead.
//         */
//        final MuxerFormat format = muxer.getFormat();
//        final Codec codec;
//
////        if (codecname != null) {
////            codec = Codec.findEncodingCodecByName(codecname);
////        } else {
//        codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
////        }
//
//        /**
//         * Now that we know what codec, we need to create an encoder
//         */
//        Encoder encoder = Encoder.make(codec);
//
//        /**
//         * Video encoders need to know at a minimum:
//         *   width
//         *   height
//         *   pixel format
//         * Some also need to know frame-rate (older codecs that had a fixed rate at which video files could
//         * be written needed this). There are many other options you can set on an encoder, but we're
//         * going to keep it simpler here.
//         */
//        encoder.setWidth(1920);//1920, 1080
//        encoder.setHeight(1080);
//        // We are going to use 420P as the format because that's what most video formats these days use
//        final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
//        encoder.setPixelFormat(pixelformat);
////        encoder.setTimeBase(framerate);
//
//        /** An annoynace of some formats is that they need global (rather than per-stream) headers,
//         * and in that case you have to tell the encoder. And since Encoders are decoupled from
//         * Muxers, there is no easy way to know this beyond
//         */
//        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
//            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
//
//        /** Open the encoder. */
//        encoder.open(null, null);
//
//
//        /** Add this stream to the muxer. */
//        muxer.addNewStream(encoder);
//
//        /** And open the muxer for business. */
//        try {
//            muxer.open(null, null);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        /** Next, we need to make sure we have the right MediaPicture format objects
//         * to encode data with. Java (and most on-screen graphics programs) use some
//         * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
//         * codecs use some variant of YCrCb formatting. So we're going to have to
//         * convert. To do that, we'll introduce a MediaPictureConverter object later. object.
//         */
//
//        MediaPictureConverter converter = null;
//        final MediaPicture picture = MediaPicture.make(encoder.getWidth(), encoder.getHeight(), pixelformat);
////        picture.setTimeBase(framerate);
//
//        /** Now begin our main loop of taking screen snaps.
//         * We're going to encode and then write out any resulting packets. */
//
//        final MediaPacket packet = MediaPacket.make();
//
//
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//
//        if (bufferedInputStream != null) {
//            ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream(65535);
//            int count = 0;
//            int x = 0;
//            int t = 0;
//            BufferedImage image = null;
//            while (x >= 0) {
//                t = x;
//                try {
//                    x = bufferedInputStream.read();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                temporaryStream.write(x);
//                if (x == 216 && t == 255) {// начало изображения
//                    temporaryStream.reset();
//
//                    temporaryStream.write(t);
//                    temporaryStream.write(x);
//                } else if (x == 217 && t == 255) {//конец изображения
//                    byte[] imageBytes = temporaryStream.toByteArray();
//                    ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
//
//                    try {
//                        image = ImageIO.read(inputStream);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (image != null) {
//                        if (converter == null)
//                            converter = MediaPictureConverterFactory.createConverter(image, picture);
//                        converter.toPicture(picture, image, count++);
//                        do {
//                            encoder.encode(packet, picture);
//                            if (packet.isComplete())
//                                muxer.write(packet, false);
//                        } while (packet.isComplete());
//
//                        System.out.println("Пишем изображение - " + count);
//                        image = null;
//                    }
//                }
//            }
//
//            /** Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
//             * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
//             * input until the output is not complete.
//             */
//            do {
//                encoder.encode(packet, null);
//                if (packet.isComplete())
//                    muxer.write(packet, false);
//            } while (packet.isComplete());
//
//            /** Finally, let's clean up after ourselves. */
//            muxer.close();
//        }
//    }

//        final IMediaWriter writer = ToolFactory.makeWriter(path);
//        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
//                720, 304);
//        long nextFrameTime = 0;
//
//        final long frameRate = 25/1000;
//        long startTime = System.nanoTime();
//        for (int i=0;i<list.size();i++) {
//            writer.encodeVideo(0, list.get(i),nextFrameTime,
//                    TimeUnit.MILLISECONDS);
//            nextFrameTime += frameRate;
//            System.out.println("Пишем изображение - "+i);
//        }
//
//        writer.close();

//        SeekableByteChannel out = null;
//        try {
//            out = NIOUtils.writableFileChannel(path);
//            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(fps, 1));
////            AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(10, 1));
//            for (int i=0;i<list.size();i++) {
//                BufferedImage image = list.get(i);
//                encoder.encodeImage(image);
//                System.out.println("Пишем изображение - "+i);
//            }
//            encoder.finish();
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            NIOUtils.closeQuietly(out);
//        }
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGImageDecoder;
//
//public class IPCameraConnection extends Applet implements Runnable
//{
//    public boolean useMJPGStream = true;
//    String appletToLoad;
//    Thread appletThread;
//
//    public String mjpgURL ="http://192.168.1.10";
//
//    DataInputStream dis;
//    private Image image=null;
//    public Dimension imageSize = null;
//    public boolean connected = false;
//    private boolean initCompleted = false;
//    HttpURLConnection huc=null;
//    Component parent;
//
//    public IPCameraConnection (Component par)
//    {
//        parent = par;
//    }
//
//
//    public void connect()
//    {
//        Socket socket = null;
//        HttpURLConnection httpConn= null;
//        try
//        {
//            URL u = new URL(mjpgURL);
//
//            huc = (HttpURLConnection) u.openConnection();
//            String encoding = new
//                    sun.misc.BASE64Encoder().encode("admin:admin".getBytes());
//            huc.setRequestProperty ("Authorization", "Basic " +
//                    encoding);
//            System.out.println("con open complete");
//            //System.out.println(huc.getContentType());
//            a
//            System.out.println("input stream got");
//            connected = true;
//            //BufferedInputStream bis = new BufferedInputStream(is);
//            dis= new DataInputStream(is);
//
//            if(!initCompleted)
//                System.out.println("!init complete");
//            initDisplay();
//        }
//        catch(IOException e)
//        {
//            //incase no connection exists wait and try again,
//            instead of printing the error
//            try
//            {
//                System.out.println("Exception :
//                        "+e.getMessage());
//                        System.out.println("no con");
//                huc.disconnect();
//                Thread.sleep(60);
//            }
//            catch(InterruptedException ie)
//            {
//                System.out.println("no con excp");
//                huc.disconnect();
//                connect();
//            }
//            connect();
//        }
//        catch(Exception e){;}
//    }
//    public void initDisplay()
//    {
//        //setup the display
//        if (useMJPGStream)
//        {
//            System.out.println("mpg-------------------------------");
//            readMJPGStream();
//        }
//        else
//        {
//            System.out.println("jpg");
//            readJPG();
//            disconnect();
//        }
//        imageSize = new Dimension(image.getWidth(this),
//                image.getHeight(this));
//        //setPreferredSize(imageSize);
//        //parent.setSize(imageSize);
//        //parent.validate();
//        initCompleted = true;
//    }
//    public void disconnect()
//    {
//        try
//        {
//            if(connected)
//            {
//                dis.close();
//                connected = false;
//            }
//        }
//        catch(Exception e){;}
//    }
//    public void init()
//    {
//        System.out.println("Starting Applet");
//        appletToLoad = getParameter("appletToLoad");
//        setBackground(Color.white);
//    }
//    public void paint(Graphics g)
//    {
//        //used to set the image on the panel
//        if (image != null)
//            g.drawImage(image, 0, 0, this);
//        else
//            System.out.println("empty frame");
//    }
//    public void run()
//    {
//        try
//        {
//            connect();
//            readStream();
//
//            Class appletClass = Class.forName(appletToLoad);
//            Applet realApplet = (Applet)appletClass.newInstance();
//            //realApplet.setStub(this);
//            setLayout( new GridLayout(1,0));
//            add(realApplet);
//            realApplet.init();
//            realApplet.start();
//        }
//        catch (Exception e)
//        {
//            System.out.println( e );
//        }
//        validate();
//    }
//    public void start()
//    {
//        appletThread = new Thread(this);
//        appletThread.start();
//    }
//    public void stop()
//    {
//        appletThread.stop();
//        appletThread = null;
//    }
//    public void readStream()
//    {
//        //the basic method to continuously read the stream
//        try
//        {
//            if (useMJPGStream)
//            {
//                while(true)
//                {
//                    readMJPGStream();
//                    //parent.repaint();
//                }
//            }
//            else
//            {
//                while(true)
//                {
//                    connect();
//                    readJPG();
//                    //parent.repaint();
//                    disconnect();
//                }
//            }
//
//        }
//        catch(Exception e){;}
//    }
//    public void readMJPGStream()
//    {
//        //preprocess the mjpg stream to remove the mjpg encapsulation
//
//        //Following commented on 07/08/2006
//        //readLine(3,dis); //discard the first 3 lines
//
//        //Following added on 07/08/2006
//        readLine(4, dis); //discard the first 4 lines for D-Link
//        DCS-900
//
//        readJPG();
//        readLine(2,dis); //discard the last two lines
//    }
//
//    public void readLine(int n, DataInputStream dis)
//    {
//        //used to strip out the header lines
//        for (int i=0; i<n ;i++)
//        {
//            readLine(dis);
//        }
//    }
//    public void readLine(DataInputStream dis)
//    {
//        try
//        {
//            boolean end = false;
//            String lineEnd = "\n"; //assumes that the end of the line
//            is marked with this
//            byte[] lineEndBytes = lineEnd.getBytes();
//            System.out.println("lineEndBytes....."+lineEndBytes);
//            byte[] byteBuf = new byte[lineEndBytes.length];
//            System.out.println("byteBuf......." + byteBuf);
//            byte[] bf = new byte[1024];
//            StringBuffer sb = new StringBuffer();
//            while(!end)
//            {
//                //dis.read(byteBuf,0,lineEndBytes.length);
//                //String t = "";
//                int i = dis.read();
//                sb.append((char)i);
//                if( i == -1)
//                    end = true;
//                //if(byteBuf != null)
//                //{
//                //dis.read(byteBuf,0,lineEndBytes.length);
//
//                //t = dis.readLine();
//                //t = new String(byteBuf);
//                //}
//                //System.out.print(t); //uncomment if you want to see what
//                the lines actually look like
//                //if(t.equals(lineEnd))
//                //end=true;
//            }
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
////    public void run()
////    {
////    connect();
////    readStream();
////    }
//
//    private void readJPG()
//    {
//        try
//        {
//            JPEGImageDecoder decoder =
//
//                    JPEGCodec.createJPEGDecoder(dis);
//            image = decoder.decodeAsBufferedImage();
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();disconnect();
//        }
//
//    }
package nodes;

import brokerdata.BrokerData;
import brokerdata.KnownBrokers;
import filesystem.FileLoader;
import filesystem.VideoLoader;
import hashing.Hasher;
import metadata.Channel;
import metadata.VideoFile;
import threads.BrokerServiceCode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Publisher extends Client {
    private ArrayList<Channel> channels = new ArrayList<>(); // list of all channels of the specific publisher
    private ArrayList<VideoFile> videos = new ArrayList<>(); // list of all video files of the specific publisher
    private String myIP;
    private int myPort;
    private ServerSocket serverSocket = null;
    private int BUFFER_SIZE = 4096;

    private boolean channelExists(String t) {
        for (Channel c : channels) {
            if (c.getName().equals(t)) {
                return true;
            }
        }
        return false;
    }

    public Publisher(String IP, BrokerData firstBroker, String filepath) {
        super(new Random().nextInt(), filepath);
        myIP = IP;
        myPort = new Random().nextInt()%10000+30000; // 30000-40000


        Random random = new Random();
        System.out.println("Starting publisher at port:" + myPort + " with first broker: " + firstBroker);

        brokers.getAddresses().add(firstBroker);

        System.out.println("PUBLISHER ID: " + this.ID);
        System.out.println("Working path: " + filepath);
    }

    public void initialize() {
        System.out.println("Scanning disk for files in directory: " + filepath);

        File parent = new File(filepath);

        if (!parent.isDirectory()) {
            System.out.println("Directory not found or not accessible");
            System.exit(1);
        }

        FileLoader loader = new FileLoader();
        VideoLoader vloader = new VideoLoader();

        try {
            for (File child : parent.listFiles()) {
                if (!child.isDirectory() && child.getName().endsWith(".mp4")) {
                    // Video found ...
                    String tagfile = child.getName().replace(".mp4", ".tags").trim();
                    String channelfile = child.getName().replace(".mp4", ".channels").trim();

                    File f1 = new File(filepath + "/" + tagfile);
                    File f2 = new File(filepath + "/" + channelfile);

                    boolean tagfileExists = f1.exists() && f1.isFile();
                    boolean channelfileExists = f2.exists() && f2.isFile();

                    System.out.println("Video discovered: \"" + child.getName() + "\", tags defined:" + tagfileExists + ", channels defined: " + channelfileExists );

                    List<String> tags = loader.loadFile(f1);
                    List<String> videoChannels = loader.loadFile(f2);

                    VideoFile vf = vloader.load(child);

                    for (String tag : tags) {
                        vf.getTags().add(tag.trim());
                    }

                    for (String channelName : videoChannels) {
                        vf.getChannels().add(channelName);

                        if (!channelExists(channelName)) {
                            Channel ch = new Channel(channelName);
                            for (String tag : tags) {
                                ch.getTags().add(tag);
                            }
                            channels.add(ch);
                        } else {
                            for (Channel ch : channels) {
                                if (ch.getName().equals(channelName)) {
                                    for (String tag : tags) {
                                        ch.getTags().add(tag);
                                    }
                                }
                            }
                        }
                    }

                    videos.add(vf);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        System.out.println("===========================================================");
        for (Channel c : channels) {
            System.out.println("-" + c.getName());
        }
    }

    public void printMetadata() {
        System.out.println("----------------------------------------------");
        System.out.println("                Channels");
        System.out.println("----------------------------------------------");
        for (Channel c : channels) {
            System.out.println(c);
        }

        System.out.println("----------------------------------------------");
        System.out.println("                Videos");
        System.out.println("----------------------------------------------");
        for(VideoFile v : videos) {
            System.out.println(v);
        }
    }

    public void registerToFirstBroker() {
        int magicnumber = Protocol.MAGIC_NUMBER_PUBLISHER;
        super.registerToFirstBroker(myIP, ""+myPort, magicnumber);
    }

    public void connectToOtherBrokers() {
        int magicnumber = Protocol.MAGIC_NUMBER_PUBLISHER;
        super.connectToOtherBrokers(myIP, ""+myPort, magicnumber);
    }

    public void distributeVideos() throws IOException {
        Hasher hasher = new Hasher();

        try {
            System.out.println("===============================================");
            for (Channel ch : channels) { // <channel,tag>
                for (String tag : ch.getTags()) {
                    BrokerData broker = hasher.findResponsibleBroker(ch.getName(), brokers);
                    ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                    objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_CHANNEL_TAG); // magic number

                    objectOutputStream.writeUTF(ch.getName());
                    objectOutputStream.flush();

                    objectOutputStream.writeUTF(tag);
                    objectOutputStream.flush();

                    System.out.println(ch.getName() + " " + tag + "   assigned to: " + broker.getAddress());

                }
            }
            for (VideoFile vid : videos) { // video, tag
                for (String tag : vid.getTags()) {
                    BrokerData broker = hasher.findResponsibleBroker(tag, brokers);

                    ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                    objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_VIDEO_TAG);

                    objectOutputStream.writeUTF(vid.getVideoName());
                    objectOutputStream.flush();

                    objectOutputStream.writeUTF(tag);
                    objectOutputStream.flush();
                }
            }
            for (VideoFile vid : videos) { // video, channel
                for (String cha : vid.getChannels()) {
                    BrokerData broker = hasher.findResponsibleBroker(cha, brokers);

                    ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                    objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_CHANNEL_VIDEO);

                    objectOutputStream.writeUTF(cha);
                    objectOutputStream.flush();

                    objectOutputStream.writeUTF(vid.getVideoName());
                    objectOutputStream.flush();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void mainSocketLoop() { // service incoming connecitons

        Socket connectionSocket = null;

        int port = myPort;

        System.out.println("Thread started for mainSocketLoop");

        try {
            serverSocket = new ServerSocket(port, 100);

            while (true) {
                System.out.println("Waiting for a broker to connect to port " + port + " ....");
                connectionSocket = serverSocket.accept();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                BrokerServiceCode code = new BrokerServiceCode(this, objectOutputStream, objectInputStream);
                Thread t1 = new Thread(code);
                t1.start();
            }
        } catch (SocketException ioException) {
            return;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void serviceBroker(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        try {
            int magicnumber = objectInputStream.readInt();

            System.out.println("Magic number is: " + magicnumber);
            if (magicnumber == Protocol.MAGIC_NUMBER_PULL_VIDEO) {
                try {
                    String topic = objectInputStream.readUTF();
                    String name = objectInputStream.readUTF();

                    System.out.println("Publisher received request for:" + topic + "/" + name);

                    String inputFile = filepath + "/" +  name;

                    System.out.println("Video path is: " + inputFile);

                    Path filePath = Paths.get(inputFile);
                    long fileSize = Files.size(filePath);

                    try (InputStream diskStream = new FileInputStream(inputFile);) {
                        int byteRead;

                        byte []b = new byte[BUFFER_SIZE];

                        objectOutputStream.writeLong(fileSize);

                        while ((byteRead = diskStream.read(b)) != -1) {
                            objectOutputStream.write(b, 0, byteRead);
                            objectOutputStream.flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                }

                objectOutputStream.close();
                objectInputStream.close();
            }
        } catch (Exception ex) {

        }
    }

    public void disconnectFromNetwork() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.disconnectFromNetwork();

    }
}

package nodes;

import brokerdata.BrokerData;
import brokerdata.KnownBrokers;
import metadata.Channel;
import metadata.VideoFile;
import publisherdata.KnownPublishers;
import publisherdata.PublisherData;
import threads.PublisherServiceCode;
import threads.PublisherThreadInterface;
import threads.SubscriberServiceCode;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Broker {
    private BrokerData myIdentity;
    private int brokerId;
    private KnownBrokers brokers = new KnownBrokers();
    private ServerSocket serverSocket = null;

    private HashMap<Integer, PublisherData> dataset = new HashMap<>(); // ID of publisher with  Channels and videos for that publisher
    private HashMap<PublisherData, ObjectOutputStream> outputConnections = new HashMap<>(); // output for each publisher
    private HashMap<PublisherData, ObjectInputStream> inputConnections = new HashMap<>(); // input for each publisher
    private HashMap<PublisherData, String> ipConnections = new HashMap<>(); // input for each publisher
    private HashMap<PublisherData, String> portConnections = new HashMap<>(); // input for each publisher

    public Broker(int brokerId, BrokerData myIdentity, BrokerData ... brokerlist) {
        this.brokerId = brokerId;
        this.myIdentity = myIdentity;

        System.out.println("Starting BROKER #" + brokerId + " : " + myIdentity);

        for (BrokerData data : brokerlist) {
            brokers.getAddresses().add(data);
        }

        brokers.sort();
    }

    public void printMetadata() {
        for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
            ArrayList<Channel> channels = entry.getValue().getChannels();
            ArrayList<VideoFile> videos = entry.getValue().getVideos();

            System.out.println("----------------------------------------------");
            System.out.println("                Channels of " + entry.getValue().getID());
            System.out.println("----------------------------------------------");
            for (Channel c : channels) {
                System.out.println(c);
            }

            System.out.println("----------------------------------------------");
            System.out.println("                Videos of " + entry.getValue().getID());
            System.out.println("----------------------------------------------");
            for (VideoFile v : videos) {
                System.out.println(v);
            }
        }
    }

    public void sendIPandPorts(int id, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream ) throws IOException {
        int totalBrokers = brokers.getAddresses().size();

        objectOutputStream.writeInt(totalBrokers); // how many
        objectOutputStream.flush();

        System.out.println("Sending IP and ports to client ... ");

        for (int i=0;i<totalBrokers;i++) {
            String ip = brokers.getAddresses().get(i).getIp();
            String port = brokers.getAddresses().get(i).getPort();

            System.out.println("Sending: " + ip + ":" + port);

            objectOutputStream.writeUTF(ip);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(port);
            objectOutputStream.flush();
        }
    }

    public void sendChannelsAndTags(int id, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream ) {

    }

    private PublisherData findPublisherFromTopic(String topic, String name) {
    
    //if topic is a channel
        for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
            PublisherData data = entry.getValue();
            for (Channel c  : data.getChannels()) {
                if (c.getName().equals(topic)) {
                    for (VideoFile vf : data.getVideos()) {
                        if (vf.getChannels().contains(topic) && vf.getVideoName().equalsIgnoreCase(name)) {
                            return data;
                        }
                    }
                    return null;
                }
            }
	//if topic is a tag
            for (String s : data.getTags()) {
                if (s.equals(topic)) {
                    for (VideoFile vf : data.getVideos()) {
                        if (vf.getTags().contains(topic) && vf.getVideoName().equalsIgnoreCase(name)) {
                            return data;
                        }
                    }
                    return data;
                }
            }
        }
        return  null;
    }

    public void servicePublisher(int id, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream  ) {
        System.out.println("Publisher connected");
        PublisherData data = dataset.get(id);


        if (data == null) {
            data = new PublisherData();
            dataset.put(id, data);
        } else {
            dataset.remove(id);
            data = new PublisherData();
            dataset.put(id, data);
        }

        try {
        	//read ip and port of publisher
            String pubIP = objectInputStream.readUTF();
            String pubPort = objectInputStream.readUTF();
		//add them to the hashmap of the publisher
            ipConnections.put(data, pubIP);
            portConnections.put(data, pubPort);
		
		//add the input and output for the publisher in the map of publisher
            outputConnections.put(data, objectOutputStream);
            inputConnections.put(data, objectInputStream);

		//send the ip and ports of the other brokers to the publisher
            sendIPandPorts(id, objectOutputStream, objectInputStream);

            dataset.put(id, data);

            System.out.println("Waiting for a command from publisher");


            while (true) {
                int magicnumber = objectInputStream.readInt();

                System.out.println("Publisher requested operation with magic no: " + magicnumber);

                if (magicnumber == Protocol.MAGIC_NUMBER_CHANNEL_TAG) { // <channel,tag>
                    String channel = objectInputStream.readUTF();
                    String tag = objectInputStream.readUTF();

                    System.out.println("Received <channel,tag>: " + channel + "," + tag);

                    if (!data.channelExists(channel))  {
                        Channel ch = new Channel(channel); // create channel
                        ch.getTags().add(tag); // add tag to the channel
                        data.getChannels().add(ch); // add channel to the publisher data
                    } else {
                        Channel ch = data.findChannel(channel);
                        ch.getTags().add(tag);
                    }
                }

                if (magicnumber == Protocol.MAGIC_NUMBER_VIDEO_TAG) { // <video, tag>
                    String videoName = objectInputStream.readUTF();
                    String tag = objectInputStream.readUTF();

                    System.out.println("Received <videoName,tag>: " + videoName + "," + tag);

                    if (!data.videoExists(videoName))  {
                        VideoFile vf = new VideoFile(videoName, "","","","" ,"",null);
                        vf.getTags().add(tag); // add tag to the channel
                        data.getVideos().add(vf); // add channel to the publisher data
                    } else {
                        VideoFile vf = data.findVideo(videoName);
                        vf.getTags().add(tag);
                    }

                }

                if (magicnumber == Protocol.MAGIC_NUMBER_CHANNEL_VIDEO) { // <channel, video>
                    String channelName = objectInputStream.readUTF();
                    String videoName = objectInputStream.readUTF();
                    System.out.println("Received <videoName,channelName>: " + videoName + "," + channelName);

                    VideoFile vf;
                    if (!data.videoExists(videoName))  {
                        vf = new VideoFile(videoName, "","","","" ,"",null);
                        data.getVideos().add(vf); // add channel to the publisher data
                    } else {
                        vf = data.findVideo(videoName);
                    }

                    if (!data.channelExists(channelName))  {
                        Channel ch = new Channel(channelName); // create channel
                        vf.getChannels().add(ch.getName());
                    } else {
                        Channel ch = data.findChannel(channelName);
                        vf.getChannels().add(ch.getName());
                    }
                }
            }
        } catch (EOFException e) {
            e.printStackTrace();
            System.out.println("Client disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();

                outputConnections.remove(data);
                inputConnections.remove(data);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            dataset.remove(id);
        }
    }

    public void serviceSubscriber(int id, ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream) {
        try {
            System.out.println("Subscribed connected");
            sendIPandPorts(id, objectOutputStream, objectInputStream);

            String subIP = objectInputStream.readUTF();
            String subPORT = objectInputStream.readUTF();

            while (true) {
                int magicnumber = objectInputStream.readInt();

                System.out.println("Subscriber requested operation with magic no: " + magicnumber);

                if (magicnumber == Protocol.MAGIC_NUMBER_VIEW_CHANNELS) {
                    Set<String> results = new HashSet<>();

                    for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
                        for (Channel c : entry.getValue().getChannels()) {
                            String name = c.getName();
                            results.add(name);
                        }
                    }

                    for (String s : results) {
                        objectOutputStream.writeUTF(s);
                        objectOutputStream.flush();
                    }

                    objectOutputStream.writeUTF(Protocol.EOF);
                    objectOutputStream.flush();
                }
		//if sub wants to pull a video
                if (magicnumber == Protocol.MAGIC_NUMBER_PULL_VIDEO) {
                    String topic = objectInputStream.readUTF();//broker reads the topic(channel or tag)
                    String name =  objectInputStream.readUTF();//broker reads the name of video

                    System.out.println("Broker received request for:" + topic + "/" + name);

                    PublisherData publisherData = findPublisherFromTopic(topic, name);//finds the responsible publisher
                    if (publisherData == null) {
                        objectOutputStream.writeUTF("NOTHING FOUND");
                        objectOutputStream.flush();
                        System.out.println("No publisher found for this video");
                    } else {
                        objectOutputStream.writeUTF("FOUND");
                        objectOutputStream.flush();
                        String publisher_ip = ipConnections.get(publisherData);
                        String publisher_port = portConnections.get(publisherData);
                        System.out.println("Publisher for this video: " + publisherData + " " + publisher_ip + ":" + publisher_port);
			//making a socket to connect to publisher
                        Socket requestSocket = new Socket(publisher_ip, Integer.parseInt(publisher_port));
                        //creating the input and output stream 
                        ObjectOutputStream pubOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
                        ObjectInputStream pubInputStream = new ObjectInputStream(requestSocket.getInputStream());
			//saying to the responsible publisher that subscriber wants to pull a video 
                        pubOutputStream.writeInt(Protocol.MAGIC_NUMBER_PULL_VIDEO); // PUBLISHER
                        pubOutputStream.flush();
			//sending the topic and the video name 
                        pubOutputStream.writeUTF(topic);
                        pubOutputStream.flush();
                        pubOutputStream.writeUTF(name);
                        pubOutputStream.flush();

                        int BUFFER_SIZE = 4096;


                        try {
                            long fileSize = pubInputStream.readLong();
                            objectOutputStream.writeLong(fileSize);

                            System.out.println("File size: " + fileSize);

                            long totalRead = 0 ;
                            int byteRead;

                            while (totalRead < fileSize) {
                                long remaining = fileSize - totalRead;
                                byte []b = null;
                                if (remaining >= BUFFER_SIZE) {
                                    b = new byte[BUFFER_SIZE];
                                    byteRead = pubInputStream.read(b);
                                } else {
                                    b = new byte[(int)remaining];
                                    byteRead = pubInputStream.read(b);
                                }
                                objectOutputStream.write(b, 0, byteRead);
                                objectOutputStream.flush();
                                totalRead += byteRead;
                            }
                            System.out.println("Transfer complete");
                        } catch (EOFException ex) {
                            System.out.println("Transfer complete");
                        }

                        pubOutputStream.close();
                        pubInputStream.close();
                    }
                }

                if (magicnumber == Protocol.MAGIC_NUMBER_VIEW_TAGS) {
                    Set<String> results = new HashSet<>();

                    for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
                        for (String s : entry.getValue().getTags()) {
                            results.add(s);
                        }
                    }

                    for (String s : results) {
                        objectOutputStream.writeUTF(s);
                        objectOutputStream.flush();
                    }

                    objectOutputStream.writeUTF(Protocol.EOF);
                    objectOutputStream.flush();
                }

                if (magicnumber == Protocol.MAGIC_NUMBER_LIST_CHANNEL) { // <channel, video>
                    String name = objectInputStream.readUTF();

                    Set<String> results = new HashSet<>();

                    for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
                        for (VideoFile vf : entry.getValue().getVideos()) {
                            for (String c : vf.getChannels()) {
                                if (c.equals(name)) {
                                   results.add(vf.getVideoName());
                                   break;
                                }
                            }
                        }
                    }

                    for (String s : results) {
                        objectOutputStream.writeUTF(s);
                        objectOutputStream.flush();
                    }

                    objectOutputStream.writeUTF(Protocol.EOF);
                    objectOutputStream.flush();

                }

                if (magicnumber == Protocol.MAGIC_NUMBER_LIST_TAG) { // <channel, video>
                    String name = objectInputStream.readUTF();

                    Set<String> results = new HashSet<>();

                    for (Map.Entry<Integer, PublisherData> entry : dataset.entrySet()) {
                        for (VideoFile vf : entry.getValue().getVideos()) {
                            for (String c : vf.getTags()) {
                                if (c.equals(name)) {
                                    results.add(vf.getVideoName());
                                    break;
                                }
                            }
                        }
                    }

                    for (String s : results) {
                        objectOutputStream.writeUTF(s);
                        objectOutputStream.flush();
                    }

                    objectOutputStream.writeUTF(Protocol.EOF);
                    objectOutputStream.flush();

                }
            }

        } catch (EOFException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void mainSocketLoop() {
        Socket connectionSocket = null;

        int port = Integer.parseInt(myIdentity.getPort());

        try {
            serverSocket = new ServerSocket(port, 100);

            while (true) {
                System.out.println("Waiting for a client to connect to port " + port + " ....");
                connectionSocket = serverSocket.accept();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());

                int magicnumber = objectInputStream.readInt();
                int client_id = objectInputStream.readInt();

                System.out.println("Magic number is: " + magicnumber);
                if (magicnumber == 0) { // publisher
                    PublisherServiceCode code = new PublisherServiceCode(this, client_id, objectOutputStream, objectInputStream);
                    Thread t1 = new Thread(code);
                    t1.start();
                } else { // subscriber
                    SubscriberServiceCode code = new SubscriberServiceCode(this, client_id, objectOutputStream, objectInputStream);
                    Thread t2 = new Thread(code);
                    t2.start();
                }
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

    public void printBrokerFamily() {
        System.out.println("------------------------------------");
        System.out.println("Broker family:");
        System.out.println("------------------------------------");

        for (BrokerData data : brokers.getAddresses()) {
            if (data != myIdentity) {
                System.out.println("   " +data);
            } else {
                System.out.println(" * " +data);
            }
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
    }

    public void menu() {
        System.out.println("Broker thread started menu");

        System.out.println("What do you want to do?");
        System.out.println("\t 1. print structures");
        System.out.println("\t 9. exit");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Type your choice: ");
            String line = scanner.nextLine();

            if (line.equals("1")) {
                printMetadata();
            }

            if (line.equals("9")) {
                return;
            }
        }
    }
}

package nodes;

import brokerdata.BrokerData;
import brokerdata.KnownBrokers;
import hashing.Hasher;
import metadata.Channel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Client {
    protected final int ID;
    protected final String filepath;
    protected KnownBrokers brokers = new KnownBrokers(); // list of <IP,PORT,HASH> for each broker
    protected HashMap<BrokerData, ObjectOutputStream> outputConnections = new HashMap<>(); // output for each broker
    protected HashMap<BrokerData, ObjectInputStream> inputConnections = new HashMap<>(); // input for each broker
    protected HashMap<BrokerData, Socket> sockets = new HashMap<>(); // socket for each broker

    protected String firstBrokerIP;
    protected String firstBrokerPort;

    public Client(int ID, String filepath) {
        this.ID = ID;
        this.filepath = filepath;
    }

    public void registerToFirstBroker(String myIP, String myPORT, int magicnumber) {
        BrokerData firstBroker = brokers.getAddresses().get(0);

        System.out.println("Connecting to first broker: " + firstBroker.getIp() + ":" + firstBroker.getPort());

        try {
        
            //creating the socket to connect to firstbroker and the input and output of the firstbroker
            Socket requestSocket = new Socket(firstBroker.getIp(), firstBroker.getPort());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(requestSocket.getInputStream());

            sockets.put(firstBroker, requestSocket);
            outputConnections.put(firstBroker, objectOutputStream);
            inputConnections.put(firstBroker, objectInputStream);

            objectOutputStream.writeInt(magicnumber); // PUBLISHER
            objectOutputStream.flush();

            objectOutputStream.writeInt(ID);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(myIP);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(myPORT);
            objectOutputStream.flush();

            int totalBrokers = objectInputStream.readInt();

            for (int i = 0; i < totalBrokers; i++) {
                String ip = objectInputStream.readUTF();
                String port = objectInputStream.readUTF();

                boolean found = false;

                for (BrokerData data : brokers.getAddresses()) {
                    if (data.getIp().equals(ip) && data.getPort() == Integer.parseInt(port)) {
                        found = true;
                    }
                }

                if (!found) {
                    brokers.getAddresses().add(new BrokerData(ip, port));
                }
            }

            firstBrokerIP = firstBrokerIP;
            firstBrokerPort = firstBrokerPort;


            if (!Hasher.limitBrokers) {
                brokers.sort();
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void connectToOtherBrokers(String myIP, String myPORT, int magicnumber) {
        for (BrokerData broker : brokers.getAddresses()) {
            if (!broker.getIp().equals(firstBrokerIP) || broker.getPort() != Integer.parseInt(firstBrokerPort)) {
                System.out.println("Connecting to other broker: " + broker.getIp() + ":" + broker.getPort());

                try {
                    Socket requestSocket = new Socket(broker.getIp(), broker.getPort());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(requestSocket.getInputStream());

                    sockets.put(broker, requestSocket);
                    outputConnections.put(broker, objectOutputStream);
                    inputConnections.put(broker, objectInputStream);

                    objectOutputStream.writeInt(magicnumber); // PUBLISHER
                    objectOutputStream.flush();

                    objectOutputStream.writeInt(ID);
                    objectOutputStream.flush();

                    objectOutputStream.writeUTF(myIP);
                    objectOutputStream.flush();

                    objectOutputStream.writeUTF(myPORT);
                    objectOutputStream.flush();

                    int totalBrokers = objectInputStream.readInt();

                    for (int i = 0; i < totalBrokers; i++) {
                        objectInputStream.readUTF();
                        objectInputStream.readUTF();
                    }
                    System.out.println("Connection successful to " + broker.getIp() + ":" + broker.getPort());
                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                    System.out.println("Connection failed to " + broker.getIp() + ":" + broker.getPort());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    System.out.println("Connection failed to " + broker.getIp() + ":" + broker.getPort());
                }
            }
        }
    }

    public void printKnownBrokers() {
        System.out.println("Received brokers from first brokers:");

        for (BrokerData data : brokers.getAddresses()) {
            System.out.println(data);
        }
    }

    public void capBrokersToOne() {
        BrokerData firstbroker = brokers.getAddresses().get(0);
        brokers.getAddresses().clear();
        brokers.getAddresses().add(firstbroker);
    }

    public void disconnectFromNetwork() {
        System.out.println("Disconnecting from brokers ...");

        for (BrokerData broker : brokers.getAddresses()) {
            ObjectOutputStream objectOutputStream = outputConnections.get(broker);
            ObjectInputStream objectInputStream = inputConnections.get(broker);
            Socket requestSocket = sockets.get(broker);

            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (requestSocket != null) {
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                System.out.println("Error during disconnect:" + ioException.getMessage());
//                ioException.printStackTrace();
            }
        }

        outputConnections.clear();
        inputConnections.clear();
        sockets.clear();
    }

    public Set<String> viewChannels() {
        Set<String> results = new HashSet<>();
	
	//sending the magic number to all brokers and they send us back the channels 
        try {
            for (BrokerData broker : brokers.getAddresses()) { // <channel,tag>
                ObjectInputStream objectInputStream = inputConnections.get(broker);
                ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_VIEW_CHANNELS); // magic number
                objectOutputStream.flush();

                while (true) {
                    String s = objectInputStream.readUTF();
                    if (s.equals(Protocol.EOF)) {
                        break;
                    }
                    results.add(s);
                }
            }

            return results;
        } catch (Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public Set<String> viewTags() {
        Set<String> results = new HashSet<>();
	
	//sending the magic number to every broker and they send us back the tags
        try {
            for (BrokerData broker : brokers.getAddresses()) { // <channel,tag>
                ObjectInputStream objectInputStream = inputConnections.get(broker);
                ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_VIEW_TAGS); // magic number
                objectOutputStream.flush();

                while (true) {
                    String s = objectInputStream.readUTF();
                    if (s.equals(Protocol.EOF)) {
                        break;
                    }
                    results.add(s);
                }
            }

            return results;
        } catch (Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public Set<String> listChannel(String name) {
        Set<String> results = new HashSet<>();

        try {
            for (BrokerData broker : brokers.getAddresses()) { // <channel,tag>
                ObjectInputStream objectInputStream = inputConnections.get(broker);
                ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_LIST_CHANNEL); // magic number
                objectOutputStream.writeUTF(name); // magic number
                objectOutputStream.flush();

                while (true) {
                    String s = objectInputStream.readUTF();
                    if (s.equals(Protocol.EOF)) {
                        break;
                    }
                    results.add(s);
                }
            }

            return results;
        } catch (Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    public Set<String> listTag(String name) {
        Set<String> results = new HashSet<>();

        try {
            for (BrokerData broker : brokers.getAddresses()) { // <channel,tag>
                ObjectInputStream objectInputStream = inputConnections.get(broker);
                ObjectOutputStream objectOutputStream = outputConnections.get(broker);

                objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_LIST_TAG); // magic number
                objectOutputStream.writeUTF(name); // magic number
                objectOutputStream.flush();

                while (true) {
                    String s = objectInputStream.readUTF();
                    if (s.equals(Protocol.EOF)) {
                        break;
                    }
                    results.add(s);
                }
            }

            return results;
        } catch (Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }
}

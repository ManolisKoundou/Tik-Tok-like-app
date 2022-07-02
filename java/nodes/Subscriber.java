package nodes;

import brokerdata.BrokerData;
import com.uwyn.jhighlight.fastutil.Hash;
import hashing.Hasher;

import java.io.*;
import java.util.Random;
import java.util.Set;

public class Subscriber extends Client {
    public Subscriber(BrokerData firstBroker, String filepath) {
        super(new Random().nextInt(), filepath);

        brokers.getAddresses().add(firstBroker);

        System.out.println("SUBSCRIBER ID: " + this.ID);
        System.out.println("Download path: " + filepath);
    }

    public void initialize() {
        System.out.println("Scanning disk for files in directory: " + filepath);

        File dir = new File(filepath);

        if (!dir.exists()) {
            System.out.println("Directory: " + filepath + " does not exist");
            dir.mkdir();
        }
    }

    public void registerToFirstBroker() {
        int magicnumber = Protocol.MAGIC_NUMBER_SUBSCRIBER;
        super.registerToFirstBroker("-","-",magicnumber);
    }

    public void connectToOtherBrokers() {
        int magicnumber = Protocol.MAGIC_NUMBER_SUBSCRIBER;
        super.connectToOtherBrokers("-","-",magicnumber);
    }


    public void pull(String topic, String name) {
        Hasher hasher = new Hasher();

        try {
            BrokerData broker = hasher.findResponsibleBroker(topic, brokers);
            ObjectOutputStream objectOutputStream = outputConnections.get(broker);
            ObjectInputStream objectInputStream = inputConnections.get(broker);

            objectOutputStream.writeInt(Protocol.MAGIC_NUMBER_PULL_VIDEO); // magic number

            objectOutputStream.writeUTF(topic);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(name);
            objectOutputStream.flush();

            String s = objectInputStream.readUTF();
            if (s.equals("NOTHING FOUND")) {
                System.out.println("Video not found for this key");
            } else {
                System.out.println("Video found for this key");

                String outputFile = filepath + "/downloads/" + name;

                int BUFFER_SIZE = 4096;

                try (OutputStream diskStream = new FileOutputStream(outputFile);) {
                    long fileSize = objectInputStream.readLong();
                    long totalRead = 0 ;
                    int byteRead = 0;

                    while (totalRead < fileSize) {
                        long remaining = fileSize - totalRead;
                        byte []b = null;
                        if (remaining >= BUFFER_SIZE) {
                            b = new byte[BUFFER_SIZE];
                            byteRead = objectInputStream.read(b);
                        } else {
                            b = new byte[(int)remaining];
                            byteRead = objectInputStream.read(b);
                        }
                        diskStream.write(b, 0, byteRead);
                        diskStream.flush();
                        totalRead += byteRead;
                    }

                    System.out.println("Transfer complete");

                    System.out.println("Last bytes read: " + totalRead);
                } catch (EOFException ex) {
                    System.out.println("Transfer complete");
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception ex ) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

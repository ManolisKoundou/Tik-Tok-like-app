package threads;

import nodes.Broker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PublisherServiceCode implements PublisherThreadInterface {
    private Broker broker;
    private int client_id;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public PublisherServiceCode(Broker broker, int client_id, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.broker = broker;
        this.client_id = client_id;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {
        System.out.println("New thread started");
        broker.servicePublisher(client_id, objectOutputStream, objectInputStream);
    }
}

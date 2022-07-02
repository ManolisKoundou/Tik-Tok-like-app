package threads;

import nodes.Publisher;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BrokerServiceCode implements BrokerThreadInterface {
    private Publisher publisher;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public BrokerServiceCode(Publisher publisher,ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.publisher = publisher;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {
        System.out.println("New thread started");
        publisher.serviceBroker(objectOutputStream, objectInputStream);



    }
}

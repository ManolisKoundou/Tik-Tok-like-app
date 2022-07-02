
import brokerdata.BrokerData;
import nodes.Broker;

public class Main {
    public static final BrokerData b1 = new BrokerData("192.168.1.9", "22222");
    public static final BrokerData b2 = new BrokerData("192.168.1.9", "22223");
    public static final BrokerData b3 = new BrokerData("192.168.1.9", "22224");

    public static void main(String [] args) {
        int BROKER_ID;

        if (args.length == 0) {
            BROKER_ID = 1;
        } else {
            BROKER_ID = Integer.parseInt(args[0]);
        }

        BrokerData myIdentity = null;

        switch (BROKER_ID) {
            case 1:
                myIdentity = b1;
                break;
            case 2:
                myIdentity = b2;
                break;
            case 3:
                myIdentity = b3;
                break;
            default:
                System.out.println("Invalid BROKER_ID: " + BROKER_ID);
                System.exit(1);
        }

        Broker broker = new Broker(BROKER_ID, myIdentity, b1, b2, b3);
        broker.printBrokerFamily();

        Thread t = new Thread() {
            public void run() {
                broker.mainSocketLoop();
            }
        };

        t.start();

        broker.printMetadata();

        broker.menu();

        broker.disconnectFromNetwork();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Broker shutdown complete");
    }
}

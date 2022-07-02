package publisherdata;

import brokerdata.BrokerData;

import java.util.ArrayList;

public class KnownPublishers {
    ArrayList<PublisherData> addresses = new ArrayList<>();

    public ArrayList<PublisherData> getAddresses() {
        return addresses;
    }

    public void setAddresses(ArrayList<PublisherData> addresses) {
        this.addresses = addresses;
    }
}

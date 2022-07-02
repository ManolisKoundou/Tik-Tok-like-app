package brokerdata;

import hashing.Hasher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class KnownBrokers {
    ArrayList<BrokerData> addresses = new ArrayList<>();

    public ArrayList<BrokerData> getAddresses() {
        return addresses;
    }

    public void setAddresses(ArrayList<BrokerData> addresses) {
        this.addresses = addresses;
    }

    public void sort() {
        Collections.sort(addresses, new Comparator<BrokerData>() {
            @Override
            public int compare(BrokerData o1, BrokerData o2) {
                Hasher hasher = new Hasher();
                return hasher.compare(o1.getHash(), o2.getHash());
            }
        });
    }
}

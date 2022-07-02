package hashing;

import brokerdata.BrokerData;
import brokerdata.KnownBrokers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    public Hasher() {
//        System.out.println("Hasher initialized");
    }

    public String hash(String data) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");//selecting the algorithm
            byte[] messageDigest = md.digest(data.getBytes());
            BigInteger BI = new BigInteger(1, messageDigest);
            String hash = BI.toString(16);

            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String hash(String ip, String port) {
        String data = ip + ":" + port;
        return hash(data);
    }

    public int compare(String hash1, String hash2) {
        return hash1.compareTo(hash2);
    }

    public BrokerData findResponsibleBroker(String topic, KnownBrokers brokers) {
        Hasher hasher = new Hasher();
        String h = hash(topic);

        for (int i=0;i<brokers.getAddresses().size();i++) {
            if (hasher.compare(h, brokers.getAddresses().get(i).getHash()) <= 0) {
                return brokers.getAddresses().get(i);
            }
        }

        String n = String.valueOf(brokers.getAddresses().size());

        BigInteger x = new BigInteger(h, 16);
        BigInteger y = new BigInteger(n, 10);

        BigInteger mod = x.mod(y);

        int m = mod.intValue();

        return brokers.getAddresses().get(m);
    }
}
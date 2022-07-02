package brokerdata;

import hashing.Hasher;

public class BrokerData {
    String ip;
    String port;
    String hash;

    public BrokerData(String ip, String port) {
        this.ip = ip;
        this.port = port;

        Hasher hasher = new Hasher();

        this.hash = hasher.hash(ip, port);
    }

    @Override
    public String toString() {
        return "BrokerData{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}


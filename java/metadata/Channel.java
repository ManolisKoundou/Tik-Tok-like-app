package metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Channel {
    Set<String> tags = new HashSet<>();

    String name;

    public Channel(String name) {
        this.name = name;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "tags=" + tags +
                ", name='" + name + '\'' +
                '}';
    }
}

package publisherdata;

import metadata.Channel;
import metadata.VideoFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PublisherData {
    String ID;
    ArrayList<Channel> channels = new ArrayList<>();
    ArrayList<VideoFile> videos = new ArrayList<>();

    public boolean channelExists(String title) {
        for (Channel c : channels) {
            if (c.getName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public Channel findChannel(String title) {
        for (Channel c : channels) {
            if (c.getName().equals(title)) {
                return c;
            }
        }
        return null;
    }

    public boolean tagExists(String title) {
        for (Channel c : channels) {
            for (String tag : c.getTags()) {
                if (tag.equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    public ArrayList<VideoFile> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<VideoFile> videos) {
        this.videos = videos;
    }

    public boolean videoExists(String title) {
        for (VideoFile c : videos) {
            if (c.getVideoName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public VideoFile findVideo(String videoName) {
        for (VideoFile vf : videos) {
            if (vf.getVideoName().equals(videoName)) {
                return vf;
            }
        }
        return null;
    }

    public Set<String> getTags() {
        Set<String> results = new HashSet<>();

        for (VideoFile vf : videos) {
            for (String s : vf.getTags()) {
                results.add(s);
            }
        }

        for (Channel c : channels) {
            for(String s : c.getTags()) {
                results.add(s);
            }
        }

        return results;
    }
}

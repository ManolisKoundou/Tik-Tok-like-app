package metadata;

import java.util.HashSet;
import java.util.Set;

public class VideoFile {
    Set<String> tags = new HashSet<>();
    Set<String> channels = new HashSet<>();

    String videoName;
    String dateCreated;
    String length;
    String framerate;
    String frameWidth;
    String frameheight;
    byte [] blob;

    public VideoFile(String videoName, String dateCreated, String length, String framerate, String frameWidth, String frameheight, byte[] blob) {
        this.videoName = videoName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameheight = frameheight;
        this.blob = blob;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getChannels() {
        return channels;
    }

    public void setChannels(Set<String> channels) {
        this.channels = channels;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getFrameheight() {
        return frameheight;
    }

    public void setFrameheight(String frameheight) {
        this.frameheight = frameheight;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    @Override
    public String toString() {
        return "VideoFile{" +
                "tags=" + tags +
                ", channels=" + channels +
                ", videoName='" + videoName + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", length='" + length + '\'' +
                ", framerate='" + framerate + '\'' +
                ", frameWidth='" + frameWidth + '\'' +
                ", frameheight='" + frameheight + '\'' +
                ", blob=" + ((blob != null) ? blob.length : "?") + " bytes " +
                '}';
    }
}

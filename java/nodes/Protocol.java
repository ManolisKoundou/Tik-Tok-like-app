package nodes;

public class Protocol {
    public static final int MAGIC_NUMBER_PUBLISHER = 0;
    public static final int MAGIC_NUMBER_SUBSCRIBER = 1;
    public static final int MAGIC_NUMBER_VIEW_CHANNELS = 2;
    public static final int MAGIC_NUMBER_VIEW_TAGS = 6;
    public static final int MAGIC_NUMBER_LIST_CHANNEL = 7;
    public static final int MAGIC_NUMBER_LIST_TAG = 8;

    public static final int MAGIC_NUMBER_CHANNEL_TAG= 3;
    public static final int MAGIC_NUMBER_VIDEO_TAG= 4;
    public static final int MAGIC_NUMBER_CHANNEL_VIDEO = 5;

    public static final String EOF = "EOF";

    public static final int MAGIC_NUMBER_PULL_VIDEO = 9;
}

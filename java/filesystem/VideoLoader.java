package filesystem;


import metadata.VideoFile;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
//import org.apache.tika.metadata.PBCore;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;


public class VideoLoader {
    public VideoFile load(File file) {
        FileLoader loader = new FileLoader();

        try {
            Path filePath = file.toPath();
            FileTime creationTime = (FileTime) Files.getAttribute(filePath, "creationTime");
            System.out.println("mpike");
            System.out.println(filePath);

            String videoName = file.getName(); // including extension mp4
            System.out.println(videoName);
            String dateCreated = creationTime.toString();

            String length = "a";
            //System.out.println(file.size());
            String framerate = "a";
            String frameWidth = "a";
            String frameheight = "a";
            byte[] blob = loader.loadBlob(file);

//            AutoDetectParser parser = new AutoDetectParser();
//            BodyContentHandler handler = new BodyContentHandler(-1);
//            Metadata tikaMetadata = new Metadata();
//            InputStream input = TikaInputStream.get(file, tikaMetadata);
//            parser.parse(input, handler, tikaMetadata, new ParseContext());
//            String[] names = tikaMetadata.names();
//            Arrays.sort(names);
//            for (String name : names) {
//                System.out.println(name + ": " + tikaMetadata.get(name));
//            }
//            metadata.get(PBCore.ESSENCE_TRACK_FRAME_RATE(0));

            //BodyContentHandler handler = new BodyContentHandler(-1);
            //MP4Parser MP4Parser = new MP4Parser();
            //Metadata metadata = new Metadata();

            //ParseContext pcontext = new ParseContext();

            //FileInputStream inputstream = new FileInputStream(file);
            //try (InputStream stream = new FileInputStream(file)) {
            //MP4Parser.parse(stream, handler, metadata, pcontext);

            //MP4Parser.parse(inputstream, handler, metadata,pcontext);
            //System.out.println("Contents of the document:  :" + handler.toString());
            //System.out.println("Metadata of the document:");
            //String[] metadataNames = metadata.names();
            //System.out.println(metadata.names());
            //System.out.println("new: " + metadata.get("tiff:ImageWidth"));
            //for(String name : metadataNames) {
            //System.out.println(name + ": " + metadata.get(name));
            //}
            // TODO: find other values ... length, framerare, frameWidth, frameheight given file object
            //}catch(Exception e) {System.out.println("Exception message: "+ e.getMessage());}
            VideoFile v = new VideoFile(videoName, dateCreated, length, framerate, frameWidth, frameheight, blob);

            return v;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return null;
        }


    }
}
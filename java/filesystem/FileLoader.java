package filesystem;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileLoader {

    public List<String> loadFile(File file) {
        List<String> words = new ArrayList<>();

        String st;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            while ((st = br.readLine()) != null) {
                if (!st.isEmpty()) {
                    words.add(st);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        return words;
    }

    public byte[] loadBlob(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);

            long fileSize = file.length();

            byte[] blob = new byte[(int) fileSize];

            inputStream.read(blob);

            return blob;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}

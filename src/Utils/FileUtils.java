package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    public static String readFile(String inputPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inputPath));
        String oldLine;
        StringBuilder newLine = new StringBuilder();
        while ((oldLine = in.readLine()) != null) {
            newLine.append(oldLine);
            newLine.append("\n");
        }
        return newLine.toString();
    }

    public static void toFile(String content, String outPath) throws IOException {
        FileWriter writer = new FileWriter(outPath);
        writer.write(content);
        writer.flush();
        writer.close();
    }
}


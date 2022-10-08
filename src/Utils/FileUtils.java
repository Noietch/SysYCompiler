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

    public static boolean diff(String file1, String file2) throws IOException {
        BufferedReader in1 = new BufferedReader(new FileReader(file1));
        BufferedReader in2 = new BufferedReader(new FileReader(file2));
        String oldLine1, oldLine2;
        int lineNum = 1;
        while (true) {
            oldLine1 = in1.readLine();
            oldLine2 = in2.readLine();
            if (oldLine1 == null || oldLine2 == null) break;
            if (!oldLine1.equals(oldLine2)) {
                System.out.println(file1 + ": line:" + lineNum + " oldLine1: " + oldLine1 + " oldLine2: " + oldLine2);
            }
            lineNum++;
        }
        if (oldLine1 != null || oldLine2 != null) {
            System.out.println(file1 + ": line:" + lineNum);
            return false;
        }
        return true;
    }

    public static void toFile(String content, String outPath) throws IOException {
        FileWriter writer = new FileWriter(outPath);
        writer.write(content);
        writer.flush();
        writer.close();
    }
}


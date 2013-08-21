package aksw.org.doodle.silverStandardMP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SilverFileWriteOperations {

    public synchronized static File clear(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        return file;
    }

    public synchronized static void saveLines(ArrayList<ArrayList<String>> sameAs,
            Integer number) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(new File(
                    Constants.SAMEASPATH + number.toString() + ".txt"), true));
            for (ArrayList<String> row : sameAs) {
                Object[] r = row.toArray();
                bw.write(r[0] + Constants.SEPARATOR + r[1]);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void writeLinkCount(String theEndpoint,
            Map<String, Integer> theCount) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(new File(
                    Constants.LINKCOUNTPATH), true));
            for (String linkedEndpoint : theCount.keySet()) {
                if (theCount.get(linkedEndpoint) > 0) {
                    bw.write(theEndpoint + Constants.SEPARATOR);
                    bw.write(linkedEndpoint + Constants.SEPARATOR);
                    bw.write(theCount.get(linkedEndpoint).toString());
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

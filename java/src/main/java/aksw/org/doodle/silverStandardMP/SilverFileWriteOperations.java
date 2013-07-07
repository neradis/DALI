package aksw.org.doodle.silverStandardMP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SilverFileWriteOperations {

    public static File clear(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        return file;
    }

    public static ArrayList<String> readEndpoints() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Constants.ENDPOINTFILE));
            while (reader.ready()) {
                list.add(reader.readLine().split("\t")[0]);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveLines(ArrayList<ArrayList<String>> sameAs, int end)
            throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                Constants.SAMEASPATH), true));
        for (ArrayList<String> row : sameAs) {
            Object[] r = row.toArray();
            bw.write(r[0] + Constants.SEPARATOR + r[1]);
            bw.newLine();
        }
        bw.write("dummy"+Constants.SEPARATOR+"dummy");
        bw.close();
    }

    public static void proceedLines(String theEndpoint) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = cutEndpoints(readEndpoints());
        for (String endpoint : endPoints) {
            linkCount.put(endpoint, 0);
        }
//        System.out.println(endPoints);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Constants.SAMEASPATH));
            String cuttedEndPoint = theEndpoint.replaceAll("sparql", "");
            while (reader.ready()) {
                String linee = reader.readLine();
                String[] line = linee.split(Constants.SEPARATOR);
                try{
                    if (line[0].contains(cuttedEndPoint)) {
                        for (String endpoint : endPoints) {
                            if (line[1].contains(endpoint)) {
                                int i = linkCount.get(endpoint);
                                linkCount.put(endpoint, ++i);
                            }
                        }
                    } else if (line[1].contains(cuttedEndPoint)) {
                        for (String endpoint : endPoints) {
                            if (line[0].contains(endpoint)) {
                                int i = linkCount.get(endpoint);
                                linkCount.put(endpoint, ++i);
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println("line: "+linee);
                }
                
            }
            reader.close();
            writeLinkCount(theEndpoint, linkCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO better cutting
    public static List<String> cutEndpoints(List<String> theEndpoint) {
        List<String> newEndpoints = new ArrayList<String>();
        for (String endpoint : theEndpoint) {
            endpoint = endpoint.replaceAll("sparql", "");
            newEndpoints.add(endpoint);
        }
        return newEndpoints;
    }

    private static void writeLinkCount(String theEndpoint,
            Map<String, Integer> theCount) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                Constants.LINKCOUNTPATH), true));
        for (String linkedEndpoint : theCount.keySet()) {
            if (theCount.get(linkedEndpoint) > 0) {
                bw.write(theEndpoint + Constants.SEPARATOR);
                bw.write(linkedEndpoint + Constants.SEPARATOR);
                bw.write(theCount.get(linkedEndpoint).toString());
                bw.newLine();
            }
        }
        bw.write("end" + Constants.SEPARATOR + "end" + Constants.SEPARATOR + "0");
        bw.close();
    }
}

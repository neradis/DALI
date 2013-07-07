package aksw.org.doodle.silverStandardMP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SilverFileReadOperations {

    public static void proceedLines(String theEndpoint) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = cutEndpoints(readEndpoints());
        for (String endpoint : endPoints) {
            linkCount.put(endpoint, 0);
        }
        if(new File(Constants.SAMEASPATH).exists()){
            try {
                
                BufferedReader reader = new BufferedReader(new FileReader(
                        Constants.SAMEASPATH));
                String cuttedEndPoint = theEndpoint.replaceAll("sparql", "");
                while (reader.ready()) {
                    String linee = reader.readLine();
                    String[] line = linee.split(Constants.SEPARATOR);
                    if (!line.equals(null)) {
                        for (String endpoint : endPoints) {
                            if (line[0].contains(cuttedEndPoint)
                                    && !endpoint.isEmpty()
                                    && line[1].contains(endpoint)) {
                                int i = linkCount.get(endpoint);
                                linkCount.put(endpoint, ++i);
                            } else if (line[1].contains(cuttedEndPoint)
                                    && !endpoint.isEmpty()
                                    && line[0].contains(endpoint)) {
                                int i = linkCount.get(endpoint);
                                linkCount.put(endpoint, ++i);
                            }
                        }
                    }
                }
                reader.close();
                SilverFileWriteOperations.writeLinkCount(theEndpoint, linkCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else System.out.println("No sameAS Links detected.");
    }

    public static Map<String, Integer> readLinkCount(String theEndpoint) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = cutEndpoints(readEndpoints());
        for (String endpoint : endPoints) {
            linkCount.put(endpoint, 0);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Constants.LINKCOUNTPATH));
            String cuttedEndPoint = theEndpoint.replaceAll("sparql", "");
            while (reader.ready()) {
                String[] line = reader.readLine().split(Constants.SEPARATOR);
                if (!line.equals(null) && line[0].contains(cuttedEndPoint)) {
                    linkCount.put(line[1], new Integer(line[2]));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linkCount;
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

    private static List<String> cutEndpoints(List<String> theEndpoint) {
        List<String> newEndpoints = new ArrayList<String>();
        for (String endpoint : theEndpoint) {
            endpoint = endpoint.replaceAll("sparql", "");
            newEndpoints.add(endpoint);
        }
        return newEndpoints;
    }
}

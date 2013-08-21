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

    public static synchronized void proceedLines(String theEndpoint, Integer number, List<String> theEndPoints) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = cutEndpoints(theEndPoints);
        for (String endpoint : endPoints) {
            linkCount.put(endpoint, 0);
        }
        File sameAsFile = new File(Constants.SAMEASPATH + number.toString() + ".txt");
        if (sameAsFile.exists()) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(sameAsFile));
                String cuttedEndPoint = theEndpoint.replaceAll("sparql", "");
                while (reader.ready()) {
                    String linee = reader.readLine();
                    String[] line = linee.split(Constants.SEPARATOR);
                    if (!line.equals(null)) {
                        for (String endpoint : endPoints) {
                            try {
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
                            } catch (ArrayIndexOutOfBoundsException e) {
                            }
                        }
                    }
                }
                reader.close();
                SilverFileWriteOperations
                        .writeLinkCount(theEndpoint, linkCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static Map<String, Integer> readLinkCount(String theEndpoint, List<String> theEndPoints) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = cutEndpoints(theEndPoints);
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

    private synchronized static List<String> cutEndpoints(List<String> theEndpoint) {
        List<String> newEndpoints = new ArrayList<String>();
        for (String endpoint : theEndpoint) {
            endpoint = endpoint.replaceAll("sparql", "");
            newEndpoints.add(endpoint);
        }
        return newEndpoints;
    }
}

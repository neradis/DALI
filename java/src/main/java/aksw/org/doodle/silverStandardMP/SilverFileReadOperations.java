package aksw.org.doodle.silverStandardMP;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SilverFileReadOperations {

    public static Map<String, Integer> readLinkCount(String theEndpoint) {
        Map<String, Integer> linkCount = new HashMap<String, Integer>();
        List<String> endPoints = SilverFileWriteOperations
                .cutEndpoints(SilverFileWriteOperations.readEndpoints());
        for (String endpoint : endPoints) {
            linkCount.put(endpoint, 0);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Constants.LINKCOUNTPATH));
            String cuttedEndPoint = theEndpoint.replaceAll("sparql", "");
            while (reader.ready()) {
                String[] line = reader.readLine().split(Constants.SEPARATOR);
//                System.out.println(line);
//                System.out.println("http://dbpedia.org/sparql/thttp://dbpedia.org//t45");
//                for(String str : line){
//                    System.out.println(str);
//                }
                if (line[0].contains(cuttedEndPoint)) {
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
}

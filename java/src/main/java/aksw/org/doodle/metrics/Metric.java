package aksw.org.doodle.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import aksw.org.doodle.silverStandardMP.SilverFileReadOperations;

public abstract class Metric {

    public double computeMetric(List<String> theQuery, String theEndpoint){
        List<String> silverSet = computeCompareList(theEndpoint, theQuery.size());
        return computeValue(theQuery, silverSet);
    }

    private List<String> computeCompareList(String theEndpoint, int theNumber) {
        ArrayList<String> silverSet = new ArrayList<String>();
        Map<String, Integer> rank = SilverFileReadOperations
                .readLinkCount(theEndpoint);
        Integer[] values = (Integer[]) rank.values().toArray();
        Arrays.sort(values);
        int size = values.length;
        for (int count = 1; count <= size; count++) {
            for (String ep : SilverFileReadOperations.readEndpoints()) {
                if (rank.get(ep) == (values[size - count])) {
                    silverSet.add(ep);
                    rank.remove(ep);
                    if (silverSet.size() == theNumber) {
                        break;
                    }
                }
            }
        }
        return silverSet;
    }

    abstract double computeValue(List<String> theQuery, List<String> theSilverSet);
}

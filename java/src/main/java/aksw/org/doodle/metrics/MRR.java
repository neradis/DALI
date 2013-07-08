package aksw.org.doodle.metrics;

import java.util.List;

public class MRR extends Metric {

    @Override
    double computeValue(List<String> theQuery, List<String> theSilverSet) {
        while(theQuery.size() > theSilverSet.size()){
            theQuery.remove(theQuery.size()-1);
        }
        double mrr = (1/theQuery.size());
        double sum = 0;
        for(String hit : theQuery){
            int rank = theSilverSet.indexOf(hit) + 1;
            if(rank>0){
                sum += 1/rank;
            }
        }
        return mrr*sum;
    }

}

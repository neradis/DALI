package aksw.org.doodle.metrics;

import java.util.List;

public class MAP extends Metric {

    @Override
    double computeValue(List<String> theQuery, List<String> theSilverSet) {
        double map = 0;
        for(String hit: theQuery){
            map += avgPrec(theQuery, theSilverSet);
        }
        return map/theQuery.size();
    }
    
    private double avgPrec(List<String> theQuery, List<String> theSilverSet){
        double avgPrec = 0;
        for(String hit : theQuery){
            avgPrec += prec(hit, theQuery, theSilverSet)*rel(hit, theSilverSet);
        }
        return avgPrec/theSilverSet.size();
    }

    private int prec(String ep, List<String> theQuery, List<String> theSilverSet){
        int index = theQuery.indexOf(ep);
        while(theQuery.size() > index + 1){
            theQuery.remove(theQuery.size()-1);
        }
        int count = 0;
        for(String hit : theQuery){
            if(theSilverSet.contains(hit)){
                count++;
            }
        }
            return count/theQuery.size();
    }
    
    private int rel(String ep, List<String> theSilverSet){
        if(theSilverSet.contains(ep)){
            return 1;
        }else return 0;
    }
}

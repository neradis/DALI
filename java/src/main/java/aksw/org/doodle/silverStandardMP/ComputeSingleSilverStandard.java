package aksw.org.doodle.silverStandardMP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class ComputeSingleSilverStandard implements Runnable{

    private String endPoint;
    private Integer number;
    private List<String> endPoints;
    
    public ComputeSingleSilverStandard(String theEndpoint, int theNumber, List<String> theEndPoints){
        endPoint = theEndpoint;
        number = new Integer(theNumber);
        endPoints = theEndPoints;
    }
    
    public void run(){
          SilverFileWriteOperations.clear(Constants.SAMEASPATH + number.toString() + ".txt");
          query(endPoint);
          SilverFileReadOperations.proceedLines(endPoint, number, endPoints);
          SilverFileWriteOperations.clear(Constants.SAMEASPATH + number.toString() + ".txt");
    }
    
    private void query(String theEndpoint) {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        int offset = 10000;
        int count = 0;
        do {
            ArrayList<ArrayList<String>> resultLines = new ArrayList<ArrayList<String>>();
            String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> "
                    + "       SELECT DISTINCT ?s ?o "
                    + "       WHERE { ?s owl:sameAs ?o. } " + "OFFSET " + count
                    + " LIMIT " + offset;
            System.out.println("Detected sameAs: " + count);
            try {
                QueryExecutor exec = new QueryExecutor(query, theEndpoint);
                Future<ResultSet> future = pool.submit(exec);
                try {
                    ResultSet results = future.get(Constants.TIMEOUTMINUTES, TimeUnit.MINUTES);
                    if (!results.hasNext()) {
                        break;
                    }
                    while (results.hasNext()) {
                        ArrayList<String> line = new ArrayList<String>();
                        QuerySolution sln = results.next();
                        line.add(sln.get("?s").toString());
                        line.add(sln.get("?o").toString());
                        resultLines.add(line);
                    }
                    count += offset;
                    SilverFileWriteOperations.saveLines(resultLines, number);
                } catch (TimeoutException e) {
                    pool.shutdown();
                    break;
                } catch (ExecutionException e){
                    pool.shutdown();
                    break;
                } catch (NullPointerException e){
                    pool.shutdown();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                pool.shutdown();
                break;
            }
        } while (true);
        pool.shutdown();
    }
}

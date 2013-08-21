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

public class ComputeSilverStandardForAll {

    private List<String> endPoints;

    public static void main(String args[]) {
        ComputeSilverStandardForAll silver = new ComputeSilverStandardForAll();
        silver.fetchAllEndpoints();
        silver.computeRankingForAll();
    }

    private void fetchAllEndpoints() {
        ExecutorService pool = Executors.newCachedThreadPool();
        String query = "PREFIX void:<http://rdfs.org/ns/void#> "
                + " SELECT DISTINCT ?endpoint " + " WHERE{ "
                + "   ?dataset void:sparqlEndpoint ?endpoint. }";
        QueryExecutor exec = new QueryExecutor(query,
                "http://labs.mondeca.com/endpoint/ends");
        Future<ResultSet> future = pool.submit(exec);
        try {
            ResultSet results = future.get(Constants.TIMEOUTMINUTES,
                    TimeUnit.MINUTES);
            ArrayList<String> theEndPoints = new ArrayList<String>();
            while (results.hasNext()) {
                QuerySolution sln = results.next();
                theEndPoints.add(sln.get("?endpoint").toString());
            }
            if (theEndPoints.isEmpty()) {
                throw new Exception("No EndPoints fetched.");
            }
            endPoints = theEndPoints;
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Computes Ranking for all Endpoints of list at Constants.ENDPOINTFILE
     * 
     * @param theEndpoint
     */
    private void computeRankingForAll() {
        SilverFileWriteOperations.clear(Constants.LINKCOUNTPATH);
        ExecutorService pool = Executors.newCachedThreadPool();
        int count = 0;
        for (String endpoint : endPoints) {
            ComputeSingleSilverStandard single = new ComputeSingleSilverStandard(
                    endpoint, count, endPoints);
            pool.execute(single);
            count++;
        }
        pool.shutdown();
    }

}

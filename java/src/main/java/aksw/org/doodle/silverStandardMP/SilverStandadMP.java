package aksw.org.doodle.silverStandardMP;

import java.io.IOException;
import java.util.ArrayList;

import org.openrdf.repository.RepositoryException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

public class SilverStandadMP {

    public static void main(String args[]) throws RepositoryException,
            IOException {
        // computeRanking("http://labs.mondeca.com/endpoint/ends");
        // computeRanking("http://dbpedia.org/sparql");
        computeRankingForOne("http://linkedgeodata.org/sparql");
        SilverFileReadOperations
                .proceedLines("http://linkedgeodata.org/sparql");
    }

    /**
     * Computes Ranking for one Endpoint
     * 
     * @param theEndpoint
     */
    public static void computeRankingForOne(String theEndpoint) {
        SilverFileWriteOperations.clear(Constants.LINKCOUNTPATH);
        SilverFileWriteOperations.clear(Constants.SAMEASPATH);
        query(theEndpoint);
        SilverFileWriteOperations.clear(Constants.SAMEASPATH);
    }

    /**
     * Computes Ranking for all Endpoints of List at Constants.ENDPOINTFILE
     * 
     * @param theEndpoint
     */
    public static void computeRankingForAll(String theEndpoint) {
        SilverFileWriteOperations.clear(Constants.LINKCOUNTPATH);
        for (String endpoint : SilverFileReadOperations.readEndpoints()) {
            SilverFileWriteOperations.clear(Constants.SAMEASPATH);
            query(endpoint);
        }
        SilverFileWriteOperations.clear(Constants.SAMEASPATH);
    }

    private static void query(String theEndpoint) {
        // Do not change offset!!
        int offset = 1000;
        int count = 0;
        do {
            ArrayList<ArrayList<String>> resultLines = new ArrayList<ArrayList<String>>();
            // Do not change Limit and Offset!
            String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> "
                    + "       SELECT DISTINCT ?s ?o "
                    + "       WHERE { ?s owl:sameAs ?o. } " + "OFFSET " + count
                    + " LIMIT " + offset;
            System.out.println("Detected sameAs: " + count);
            try {
                Query sparqlQuery = QueryFactory
                        .create(query, Syntax.syntaxARQ);
                QueryExecution qexec = QueryExecutionFactory.sparqlService(
                        theEndpoint, sparqlQuery);
                // Sometimes not continuing here:
                ResultSet results = qexec.execSelect();
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
                SilverFileWriteOperations.saveLines(resultLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();
        } while (true);
    }
}

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
        computeRanking("http://dbpedia.org/sparql");
        // // computeRanking("http://linkedgeodata.org/sparql");
        SilverFileWriteOperations.proceedLines("http://dbpedia.org/sparql");
    }

    // TODO Iterate over all Endpoints
    private static void computeRanking(String theEndpoint) {
        SilverFileWriteOperations.clear(Constants.SAMEASPATH);
        SilverFileWriteOperations.clear(Constants.LINKCOUNTPATH);
        int end = 0;
        // for (String endpoint : readEndpoints()) {
        System.out.println(theEndpoint);
        query(theEndpoint);

        // ask for all owl:sameAs and save them
        // getSameAs("http://dbpedia.org/sparql/", end);
        end++;
        // }
    }

    // TODO Offset
    private static void query(String theEndpoint) {
        ArrayList<ArrayList<String>> resultLines = new ArrayList<ArrayList<String>>();
        int oldCount = 0;
        int newCount = 0;
        do {
            oldCount = newCount;
            String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> "
                    +
                    // "SELECT ?s ?o " +
                    // "WHERE { " +
                    "       SELECT DISTINCT ?s ?o "
                    + "       WHERE { ?s owl:sameAs ?o. } "
                    + "       ORDER BY ASC (?s) " +
                    // } " +
                    // "OFFSET " + oldCount + " " +
                    "LIMIT " + 10000;
            // String query =
            // "PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT ?s ?o WHERE { ?s owl:sameAs ?o } OFFSET "
            // + oldCount + " LIMIT  40000";
            // String query = "Select ?s ?o Where{ ?s ?p ?o } Limit 1000";
            // String prefVoid = "<http://rdfs.org/ns/void#";
            // String query = "SELECT ?s ?o WHERE{ ?s " + prefVoid
            // + "sparqlEndpoint> ?o.}";
            System.out.println(query);
            try {
                Query sparqlQuery = QueryFactory
                        .create(query, Syntax.syntaxARQ);
                QueryExecution qexec = QueryExecutionFactory.sparqlService(
                        theEndpoint, sparqlQuery);
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    ArrayList<String> line = new ArrayList<String>();
                    QuerySolution sln = results.next();
                    line.add(sln.get("?s").toString());
                    line.add(sln.get("?o").toString());
                    resultLines.add(line);
                }
                SilverFileWriteOperations.saveLines(resultLines, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // newCount = 40000 + oldCount;
            System.gc();
        } while (oldCount < newCount);
    }
}

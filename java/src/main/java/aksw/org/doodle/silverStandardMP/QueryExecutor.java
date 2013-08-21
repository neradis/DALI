package aksw.org.doodle.silverStandardMP;

import java.util.concurrent.Callable;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

public class QueryExecutor implements Callable<ResultSet> {

    private QueryExecution qexec;
    
    public QueryExecutor(String theQuery, String theEndpoint){
        Query sparqlQuery = QueryFactory.create(theQuery, Syntax.syntaxARQ);
        qexec = QueryExecutionFactory.sparqlService(
                theEndpoint, sparqlQuery);
    }
    
    @Override
    public ResultSet call() throws Exception {
        return qexec.execSelect();
    }

}

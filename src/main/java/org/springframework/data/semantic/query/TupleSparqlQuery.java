/**
 * Copyright (C) 2014 Ontotext AD (info@ontotext.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.semantic.query;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.UnsupportedQueryLanguageException;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.data.semantic.support.util.ValueUtils;


public class TupleSparqlQuery extends AbstractSparqlQuery implements TupleQuery {

	private TupleQuery query;
	
	private boolean count = false;
	
	private static final String COUNT_URI = "http://www.ontotext.com/count";
	
	public TupleSparqlQuery(String source, RepositoryConnection connection) throws MalformedQueryException, QueryEvaluationException, UnsupportedQueryLanguageException {
		super(source, connection);
		if (!(getParsedQuery() instanceof ParsedTupleQuery) ) {
			throw new QueryEvaluationException("Invalid tuple query.");
		}
	}
	
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		prepareTupleQuery();
		return query.evaluate();
	}

	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		prepareTupleQuery();
		query.evaluate(handler);
	}
	
	private void prepareTupleQuery() throws QueryEvaluationException {
		try {
			prePrepare();
			query = connection.prepareTupleQuery(QueryLanguage.SPARQL, str);
			postPrepare();
		} catch (RDF4JException e) {
			throw new QueryEvaluationException(e);
		}
	}

	protected TupleQuery getQuery() {
		return query;
	}
	
	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}

	@Override
	protected void postPrepare() {
		super.postPrepare();
		if (count) {			
			if (query.getDataset() == null) {
				query.setDataset(new SimpleDataset());
			}
			((SimpleDataset) query.getDataset()).addDefaultGraph(ValueUtils.createIRI(COUNT_URI));
		}		
	}
	
}

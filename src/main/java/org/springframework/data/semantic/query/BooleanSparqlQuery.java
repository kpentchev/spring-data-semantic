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
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;


public class BooleanSparqlQuery extends AbstractSparqlQuery implements BooleanQuery {

	private BooleanQuery query;
	
	public BooleanSparqlQuery(String source, RepositoryConnection connection) throws MalformedQueryException,
			QueryEvaluationException, RepositoryException {
		super(source, connection);
		if (!(getParsedQuery() instanceof ParsedBooleanQuery)) {
			throw new QueryEvaluationException("Invalid boolean query.");
		}
	}
	
	public boolean evaluate() throws QueryEvaluationException {
		prepareBooleanQuery();
		return query.evaluate();
	}
	
	private void prepareBooleanQuery() throws QueryEvaluationException {
		try {
			prePrepare();
			query = connection.prepareBooleanQuery(QueryLanguage.SPARQL, str);
			postPrepare();
		} catch (RDF4JException e) {
			throw new QueryEvaluationException(e);
		}
	}
	
	protected BooleanQuery getQuery() {
		return query;
	}

	@Override
	public void setLimit(long limit) {
	}
	
	protected void prePrepare() {	
	}
}

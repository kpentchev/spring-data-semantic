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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.UnsupportedQueryLanguageException;
import org.eclipse.rdf4j.query.algebra.Slice;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.impl.AbstractQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailQuery;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;


public abstract class AbstractSparqlQuery extends AbstractQuery {
	
	private static final String DISABLE_SAMEAS_URI = "http://www.ontotext.com/disable-sameAs";

	protected String source;
	protected String str;
	private long limit;
	private long offset;
	private boolean sameAs = false;
	private ParsedQuery parsedQuery;
	
	protected RepositoryConnection connection;
	
	private Log logger = LogFactory.getLog(AbstractSparqlQuery.class);
	
		
	public AbstractSparqlQuery(String source, RepositoryConnection connection) throws MalformedQueryException, UnsupportedQueryLanguageException {
		if (source != null && source.length() > 0) {
			this.source = source;
			this.connection = connection;
			str = normalize(source);
			parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, str, null);
			str = removeComments(str);
			dataset = parsedQuery.getDataset();
			TupleExpr expr = parsedQuery.getTupleExpr();
			if (expr instanceof Slice) {
				Slice slice = ((Slice) expr);
				offset = slice.getOffset();
				limit = slice.getLimit();
			} else {
				limit = -1;
			}
		} else {
			throw new MalformedQueryException();
		}
	}
	
	private String normalize(String source) {
		return source
			.replaceAll("\\u00a0", " ")
			.replaceAll("\\u00b0", "\t")
			.replaceAll("\\u2028", "\n");
	}
	
	private String removeComments(String text) {
		return Pattern.compile("^(\\s*)#.*$", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE)
			.matcher(text).replaceAll("");
	}
	
	private void setSailQueryOffset() {
		SailQuery query = ((SailQuery) getQuery());
		TupleExpr expr = query.getParsedQuery().getTupleExpr();
		Slice slice = null;
		if (expr instanceof Slice) {
			slice = (Slice) expr;
			slice.setOffset(offset);
		} else {
			slice = new Slice(query.getParsedQuery().getTupleExpr(), offset, limit);
		}
		query.getParsedQuery().setTupleExpr(slice);
	}
	
	private void setSailQueryLimit() {
		SailQuery query = ((SailQuery) getQuery());
		TupleExpr expr = query.getParsedQuery().getTupleExpr();
		Slice slice = null;
		if (expr instanceof Slice) {
			slice = (Slice) expr;
			slice.setLimit(limit);
		} else {
			slice = new Slice(query.getParsedQuery().getTupleExpr(), offset, limit);
		}
		query.getParsedQuery().setTupleExpr(slice);
	}
	
	private void setHTTPQueryOffset() {
		Pattern p = Pattern.compile("offset \\d+", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		if (offset > 0 && !m.find()) {
			str = str + " OFFSET " + offset;
		}
		else {
			str = m.replaceAll(offset > 0 ? "OFFSET " + offset : "");
		}
	}
	
	private void setHTTPQueryLimit() {
		Pattern p = Pattern.compile("limit\\s+\\d+", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		if (limit != -1 && !m.find()) {
			str = str + " LIMIT " + limit;
		}
		else {
			str = m.replaceAll(limit == -1 ? "" : "LIMIT " + limit);
		}
	}
	
	public void setSameAs(boolean sameAs) {
		this.sameAs = sameAs;
	}

	public boolean isSameAs() {
		return sameAs;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getLimit() {
		return limit;
	}

	public boolean hasLimit() {
		return limit >= 0;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getOffset() {
		return offset;
	}
	
	public Dataset getDataset() {
		if (getQuery() != null) {
			return getQuery().getDataset();
		}
		return dataset;
	}
	
	public BindingSet getBindings() {
		if (getQuery() != null) {
			return getQuery().getBindings();
		}
		return super.getBindings();
	}

	public List<String> getBindingNames() {
		if (parsedQuery != null) {
			return new ArrayList<String>(parsedQuery.getTupleExpr().getBindingNames());
		}
		return null;
	}
	
	public String getSource() {
		return source;
	}
	
	public String toString() {
		return source;
	}
	
	protected void prePrepare() {
		if (!(connection instanceof SailRepositoryConnection)) {
			setHTTPQueryOffset();
			setHTTPQueryLimit();
		}
	}
	
	protected void postPrepare() {
		
		Query query = getQuery();
		query.setIncludeInferred(includeInferred);
		query.setMaxExecutionTime(getMaxExecutionTime());
		
		
		for (Binding b : getBindings()) {
			query.setBinding(b.getName(), b.getValue());			
		}
		
		if (dataset != null) {
			if (query.getDataset() == null) {
				query.setDataset(new SimpleDataset());
			}
			for (IRI defaultGraph : dataset.getDefaultGraphs()) {
				((SimpleDataset) query.getDataset()).addDefaultGraph(defaultGraph);
			} 
			for (IRI defaultGraph : dataset.getNamedGraphs()) {
				((SimpleDataset) query.getDataset()).addNamedGraph(defaultGraph);
			}
		}
		
		if (sameAs) {
			if (query.getDataset() == null) {
				query.setDataset(new SimpleDataset());
			}
			((SimpleDataset) query.getDataset()).addDefaultGraph(connection.getValueFactory().createIRI(DISABLE_SAMEAS_URI));
		}
		
		if (connection instanceof SailRepositoryConnection) {
			setSailQueryLimit();
			setSailQueryOffset();
		}
	}
	
	public ParsedQuery getParsedQuery() {
		return parsedQuery;
	}
	
	protected abstract Query getQuery();
	
	@Override
	public int hashCode() {
		int hash = (int) (str.hashCode() + offset*13 + limit*17);
		if(str.length() > 1){
			if(sameAs){
				hash += 31*str.charAt(0);
			}
			if(includeInferred){
				hash += 7*str.charAt(str.length()-1);
			}
		}
		return hash;
	}
	
	public boolean equals(Object o){
		if(o == null) {
			return false;
		}
		else if(o.getClass().equals(this.getClass())){
			AbstractSparqlQuery q2 = (AbstractSparqlQuery) o;
			return q2.str.equals(this.str) && (q2.sameAs == this.sameAs) && (q2.includeInferred == this.includeInferred) && (q2.limit == this.limit) && (q2.offset == this.offset);
		}
		else{
			return false;
		}
	}
	
	public void close(){
		try {
			connection.close();
		} catch (RepositoryException e) {
			logger.warn(e.getMessage(), e);
		}
	}
}

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
package org.springframework.data.semantic.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.Test;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.util.ValueUtils;

public class TestSemanticTemplateStatementsCollector {
	
	private SemanticTemplateStatementsCollector collector; 
	
	private SemanticMappingContext mappingContext = new SemanticMappingContext((List<? extends Namespace>) new LinkedList<Namespace>(), new SimpleNamespace("", "urn:default:namespace:"), true);
	
	private Statement statement = SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:d"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:j"));
	
	@Test
	public void testAssembleStatements() throws RepositoryException{
		collector = new SemanticTemplateStatementsCollector(null, mappingContext, null);
		Model statements = new TreeModel();
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:a"), RDF.TYPE, ValueUtils.createIRI("urn:test:type")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:a"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:c")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:a"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:e")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:c"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:b")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:b"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:d")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:k"), RDF.TYPE, ValueUtils.createIRI("urn:test:type")));
		statements.add(SimpleValueFactory.getInstance().createStatement(ValueUtils.createIRI("urn:test:k"), ValueUtils.createIRI("urn:test:has"), ValueUtils.createIRI("urn:test:d")));
		statements.add(statement);
		
		Collection<Model> assembled = collector.assembleModels(ValueUtils.createIRI("urn:test:type"), statements);
		assertEquals(2, assembled.size());
		Iterator<Model> it = assembled.iterator();
		assertTrue(it.next().contains(statement) && it.next().contains(statement));
		
	}
	
	

}

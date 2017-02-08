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
package org.springframework.data.semantic.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.semantic.support.util.ValueUtils;
import org.springframework.data.semantic.testutils.Utils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/default-context.xml" })
public class SemanticDatabaseTest {
	@Autowired
	SemanticDatabase sdb;
	
	@Before
	public void prepareTestRepository() {
		Utils.populateTestRepository(sdb);
	}
	
	@Test
	public void testPagingQuery() {
		String source = "SELECT ?o WHERE " +
				"{ <http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Wine> ?p ?o } ORDER BY ?p";
		String orderLimit = " LIMIT 2 OFFSET 2";
		
		List<BindingSet> res1 = null;
		List<BindingSet> res2 = null;
		try {
			res1 = sdb.getQueryResults(source, 2L, 2L);
			res2 = sdb.getQueryResults(source + orderLimit);		
		} catch (Exception e) {
			assertTrue(false);
		}
		
		assertEquals(res1.size(), res2.size());
		for(int i = 0; i < res1.size(); i++) {
			
			String val1 = res1.get(i).getBinding("o").getValue().stringValue();
			String val2 = res2.get(i).getBinding("o").getValue().stringValue();
			assertEquals(val1, val2);
		}
	}
		
	@Test
	public void testDefaultNamespace() {
		try {
			Namespace ns = sdb.getDefaultNamespace();
			assertTrue("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#".equalsIgnoreCase(ns.getName()));
		} catch (RepositoryException e) {
			assertTrue(false);
		}
	}
	
	@Test
	public void testAddStatement(){
		long count = sdb.count();
		sdb.addStatement(ValueUtils.createIRI("urn:test:statement"),ValueUtils.createIRI(ValueUtils.RDF_TYPE_PREDICATE), ValueUtils.createIRI("unr:type:test-statement"));
		assertEquals(count+1, sdb.count());
	}
	
	@Test
	public void testAddStatements(){
		long count = sdb.count();
		Model model = new LinkedHashModel();
		model.add(ValueUtils.createIRI("urn:test:statement1"), ValueUtils.createIRI(ValueUtils.RDF_TYPE_PREDICATE), ValueUtils.createIRI("unr:type:test-statement"));
		model.add(ValueUtils.createIRI("urn:test:statement2"), ValueUtils.createIRI(ValueUtils.RDF_TYPE_PREDICATE), ValueUtils.createIRI("unr:type:test-statement"));
		sdb.addStatements(model);
		assertEquals(count+2, sdb.count());
	}
	
	@Test
	public void testDeleteStatements(){
		IRI uri = ValueUtils.createIRI("urn:test:statement2");
		sdb.removeStatements(uri, null, null);
		assertTrue(sdb.getStatementsForSubject(uri).isEmpty());
	}
}

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

import java.math.BigInteger;
import java.util.HashMap;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.repository.query.QueryCreationException;
import org.springframework.data.semantic.core.SemanticDatabase;
import org.springframework.data.semantic.core.SemanticOperationsCRUD;
import org.springframework.data.semantic.mapping.SemanticPersistentEntity;
import org.springframework.data.semantic.model.Merlot;
import org.springframework.data.semantic.model.vocabulary.WINE;
import org.springframework.data.semantic.support.convert.EntityToQueryConverter;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.util.ValueUtils;
import org.springframework.data.semantic.testutils.Utils;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



/*
 * TODO
 * When implemented, add test to check the behavior when the properties are associations, not simple literals
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/default-context.xml" })
public class SemanticEntityCreationTest {

	//------------Expected values------------
	private HashMap<String, Statement> expectedStatements;	
	private IRI expBody;
	private String expFlavor;
	private String expMaker;
	private String expSugar;
	private String expLocation;
	private int expYear;

	@Autowired
	SemanticDatabase sdb;

	SemanticMappingContext mappingContext;
	SemanticPersistentEntity<?> testEntityType;
	
	//these are not autowired because they depend on a configured repository
	SemanticOperationsCRUD operations;
	SemanticTemplateStatementsCollector statementsCollector;


	@Before
	public void prepare() throws RepositoryException {
		//populate repository with test data in order to have repeatable results
		Utils.populateTestRepository(sdb);	

		expectedStatements = new HashMap<String, Statement>();
				
		expectedStatements.put("urn:field:flavor",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(WINE.NAMESPACE+"LongridgeMerlot"),
						ValueUtils.createIRI("urn:field:flavor"), 
						ValueUtils.createIRI(WINE.NAMESPACE+"Moderate")));
		
		/*expectedStatements.put("urn:field:body",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(defaultNs.getName()+"LongridgeMerlot"), 
						ValueUtils.createIRI("urn:field:body"), 
						ValueUtils.createIRI(defaultNs.getName()+"Light")));*/
		
		expectedStatements.put("urn:field:maker",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(WINE.NAMESPACE+"LongridgeMerlot"),
						ValueUtils.createIRI("urn:field:maker"), 
						ValueUtils.createIRI(WINE.NAMESPACE+"Longridge")));
		
		expectedStatements.put("urn:field:sugar",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(WINE.NAMESPACE+"LongridgeMerlot"),
						ValueUtils.createIRI("urn:field:sugar"), 
						ValueUtils.createIRI(WINE.NAMESPACE+"Dry")));
		
		expectedStatements.put("urn:field:location",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(WINE.NAMESPACE+"LongridgeMerlot"),
						ValueUtils.createIRI("urn:field:location"), 
						ValueUtils.createIRI(WINE.NAMESPACE+"NewZealandRegion")));
		
		expectedStatements.put("urn:field:year",
				SimpleValueFactory.getInstance().createStatement(
						ValueUtils.createIRI(WINE.NAMESPACE+"LongridgeMerlot"),
						ValueUtils.createIRI("urn:field:year"), 
						new IntegerMemLiteral(null, BigInteger.valueOf(1998), XMLSchema.POSITIVE_INTEGER)));
		
		expBody = ValueUtils.createIRI(WINE.NAMESPACE + "Light");
		expFlavor = WINE.NAMESPACE + "Moderate";
		expMaker = WINE.NAMESPACE + "Longridge";
		expSugar = WINE.NAMESPACE + "Dry";
		expLocation = WINE.NAMESPACE + "NewZealandRegion";
		expYear = 1998;
		
		mappingContext = new SemanticMappingContext(sdb.getNamespaces(), sdb.getDefaultNamespace(), true);
		testEntityType = mappingContext.getPersistentEntity(ClassTypeInformation.from(Merlot.class));
		ConversionService conversionService = new DefaultConversionService();
		operations = new SemanticTemplateCRUD(sdb, conversionService, true);
		statementsCollector = new SemanticTemplateStatementsCollector(sdb, mappingContext, new EntityToQueryConverter(mappingContext));
		
	}
	
	@Test
	public void testStatementsIterator() throws QueryEvaluationException, RepositoryException, QueryCreationException, MalformedQueryException {

		/*Iterator<Statement> iter = getTestStatements().iterator();
		while(iter.hasNext()) {
			Statement statement = iter.next();
			Statement expStatement = expectedStatements.remove(statement.getPredicate().toString());
			assertEquals(expStatement, statement);
		}
		assertTrue(expectedStatements.isEmpty());*/
	}

	@Test
	public void testReadEntity() throws QueryInterruptedException, RepositoryException, QueryCreationException, QueryEvaluationException, MalformedQueryException { 
		Model iterator = getTestStatements();
		
		Merlot res = operations.createEntity(iterator, Merlot.class);
		
		assertEquals(expBody, res.getBody().getUri());
		assertEquals(expFlavor, res.getFlavor());
		assertEquals(expMaker, res.getMaker());
		assertEquals(expSugar, res.getSugar());
		assertEquals(expLocation, res.getLocation());
		assertEquals(expYear, res.getYear());
	}

	private Model getTestStatements() throws QueryInterruptedException, RepositoryException, QueryCreationException, QueryEvaluationException, MalformedQueryException {
		IRI resource = ValueUtils.createIRI("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#LongridgeMerlot");
		
		Model statements =  statementsCollector.getStatementsForResource(resource, Merlot.class, MappingPolicyImpl.ALL_POLICY);
		return statements;
		
		
	}

}

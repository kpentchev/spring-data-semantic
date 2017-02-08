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
package org.springframework.data.semantic.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.semantic.core.RDFState;
import org.springframework.data.semantic.mapping.SemanticPersistentEntity;
import org.springframework.data.semantic.model.ModelEntity;
import org.springframework.data.semantic.support.convert.SemanticEntityInstantiatorImpl;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.util.ValueUtils;
import org.springframework.data.util.ClassTypeInformation;

public class TestEntityInstantiator {
	
	private RDFState state;
	private IRI id = ValueUtils.createIRI("urn:default:id1");
	private IRI namePredicate = ValueUtils.createIRI("http://www.w3.org/2004/02/skos/core#prefLabel");
	private Literal name = SimpleValueFactory.getInstance().createLiteral("name");
	private SemanticEntityInstantiator instantiator = new SemanticEntityInstantiatorImpl();
	private SemanticMappingContext mappingContext;
	private SemanticPersistentEntity<ModelEntity> testEntityType;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setupTest(){
		state = new RDFState();
		mappingContext = new SemanticMappingContext(Arrays.asList(new SimpleNamespace("skos", "http://www.w3.org/2004/02/skos/core#")), new SimpleNamespace("", "urn:default:"), true);
		testEntityType = (SemanticPersistentEntity<ModelEntity>) mappingContext.getPersistentEntity(ClassTypeInformation.from(ModelEntity.class));
		state.addStatement(SimpleValueFactory.getInstance().createStatement(id, namePredicate, name));
		state.addStatement(SimpleValueFactory.getInstance().createStatement(id, RDF.TYPE, testEntityType.getRDFType()));
	}
	
	@Test
	public void testInstantiateDefaultCtor(){
		ModelEntity entity = instantiator.createInstanceFromState(testEntityType, state);
		assertNotNull(entity);
		assertNotNull(entity.getUri());
		assertEquals(id, entity.getUri());
	}

}

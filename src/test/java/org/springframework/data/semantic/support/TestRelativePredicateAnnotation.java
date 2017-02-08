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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.junit.Test;
import org.springframework.data.semantic.mapping.SemanticPersistentEntity;
import org.springframework.data.semantic.model.RelativePredicateEntity;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.util.ValueUtils;

/**
 * Tests for @Predicate annotation on classes with no @Namespace annotation
 * The cases with @Namespace are in TestNamespaceAnnotation
 * Created by itrend on 11/27/14.
 */
public class TestRelativePredicateAnnotation {
	private static final String DEFAULT_NS = "urn:default:namespace:";
	private SemanticMappingContext
			mappingContext = new SemanticMappingContext((List<? extends Namespace>) new LinkedList<Namespace>(), new SimpleNamespace("", DEFAULT_NS), true);

	@SuppressWarnings("unchecked")
	private SemanticPersistentEntity<RelativePredicateEntity> pe =
			(SemanticPersistentEntity<RelativePredicateEntity>) mappingContext.getPersistentEntity(RelativePredicateEntity.class);


	@Test
	public void testRelativePredicateWithNoNamespace() {
		assertEquals(
				ValueUtils.createIRI(DEFAULT_NS + "relative"),
				pe.getPersistentProperty("withRelativePredicate").getPredicate());
	}

	@Test
	public void testAbsolutePredicateWithNoNamespace() throws NoSuchFieldException {
		assertEquals(
				ValueUtils.createIRI("urn:really:absolute"),
				pe.getPersistentProperty("withAbsolutePredicate").getPredicate());
	}

}

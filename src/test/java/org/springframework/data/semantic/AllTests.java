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
package org.springframework.data.semantic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.data.semantic.config.ConfigTest;
import org.springframework.data.semantic.convert.TestEntityInstantiator;
import org.springframework.data.semantic.mapping.TestSemanticEntityState;
import org.springframework.data.semantic.mapping.TestSemanticPersistentEntity;
import org.springframework.data.semantic.mapping.TestStringSemanticPersistentEntity;
import org.springframework.data.semantic.repository.TestLazyInitializationDSP705;
import org.springframework.data.semantic.repository.TestSemanticRepository;
import org.springframework.data.semantic.support.TestNamespaceAnnotation;
import org.springframework.data.semantic.support.TestRelativePredicateAnnotation;
import org.springframework.data.semantic.support.TestSemanticTemplateStatementsCollector;
import org.springframework.data.semantic.support.TestValueUtils;

@RunWith(Suite.class)
@SuiteClasses({
	TestValueUtils.class
	, ConfigTest.class
	, TestEntityInstantiator.class
//	, TestMappingPolicy.class
	, TestSemanticEntityState.class
	, TestSemanticPersistentEntity.class
	, TestStringSemanticPersistentEntity.class
	, TestLazyInitializationDSP705.class
	, TestSemanticRepository.class 
	, TestNamespaceAnnotation.class
	, TestRelativePredicateAnnotation.class
	, TestSemanticTemplateStatementsCollector.class})
public class AllTests {

}

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

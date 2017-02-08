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
package org.springframework.data.semantic.model;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.springframework.data.semantic.repository.SemanticRepository;

public interface ModelEntityRepository extends SemanticRepository<ModelEntity> {
	
	List<ModelEntity> findByName(String name);
	
	List<ModelEntity> findBySynonyms(List<String> synonyms);
	
	List<ModelEntity> findByRelated(List<ModelEntity> related);
	
	List<ModelEntity> findByRelated(IRI related);
	
	Long countByName(String name);
	
	Long countBySynonyms(List<String> synonyms);
	
	Long countByRelated(List<ModelEntity> related);
	
	Long countByRelated(IRI related);
	
	ModelEntity findOneByRelated(IRI related);

}

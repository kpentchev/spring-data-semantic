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
package org.springframework.data.semantic.mapping;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.data.mapping.model.MutablePersistentEntity;
import org.springframework.data.semantic.core.RDFState;

/**
 * 
 * @author konstantin.pentchev
 *
 * @param <T>
 */
public interface SemanticPersistentEntity<T> extends MutablePersistentEntity<T, SemanticPersistentProperty> {
	
	
	Resource getContext(Object entity);
	
	 /**
	  * Creates and returns the MappingPolicy for this Semantic
	  * @return
	  */
	 MappingPolicy getMappingPolicy();
	 
	 /**
	  * Returns the {@link SemanticPersistentProperty} to be used as RDF context or null if none is defined.
	  * @return
	  */
	 SemanticPersistentProperty getContextProperty();
	 
	 /**
	  * Check if a context property is defined and set.
	  * @return
	  */
	 boolean hasContextProperty();
	 
	 /**
	  * Returns the {@link IRI} identifying this entity's RDF type.
	  * @return
	  */
	 IRI getRDFType();
	 
	 /**
	  * Returns the {@link List} of {@link IRI}s identifying this entity's superclass' RDF types.
	  * @return
	  */
	 List<IRI> getRDFSuperTypes();
	 
	 /**
	  * Retrieve the {@link IRI} of the given instance.
	  * @param entity
	  * @return
	  */
	 IRI getResourceId(Object entity);
	 
	 /**
	  * Sets the subject id of the entity from the given statements.
	  * @param entity
	  * @param statements
	  */
	 void setPersistentState(Object entity, RDFState statements);
	 
	 /**
	  * Set the given {@link IRI} as the id of the given instance.
	  * @param id
	  */
	 void setResourceId(Object entity, IRI id);
	 
	 /**
	  * Retrieve the namespace value.
	  * @return
	  */
	 IRI getNamespace();

}

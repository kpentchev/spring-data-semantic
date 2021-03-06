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
package org.springframework.data.semantic.support.convert.handlers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.springframework.data.semantic.core.RDFState;
import org.springframework.data.semantic.mapping.SemanticPersistentEntity;
import org.springframework.data.semantic.mapping.SemanticPersistentProperty;
import org.springframework.data.semantic.support.Direction;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.util.ValueUtils;

public class PropertiesToStatementsHandler extends AbstractPropertiesToStatementsHandlers {
	
	private URI resourceId;
	
	
	
	public PropertiesToStatementsHandler(RDFState statements, Object entity, SemanticMappingContext mappingContext){
		super(statements, entity, mappingContext);
		@SuppressWarnings("unchecked")
		SemanticPersistentEntity<Object> persistentEntity = (SemanticPersistentEntity<Object>) mappingContext.getPersistentEntity(entity.getClass());
		this.resourceId = persistentEntity.getResourceId(entity);
	}

	
	@Override
	protected void processPropertyStatement(SemanticPersistentProperty persistentProperty, Object value){
		SemanticPersistentEntity<?> persistentEntity = (SemanticPersistentEntity<?>) persistentProperty.getOwner();
		if(persistentProperty.isContext()){
			return;
		}
		Resource context = persistentEntity.getContext(entity);
		if(persistentProperty.isIdProperty()){
			addStatement(resourceId, new URIImpl(ValueUtils.RDF_TYPE_PREDICATE), persistentEntity.getRDFType(), context);
			for(URI supertype : persistentEntity.getRDFSuperTypes()){
				addStatement(resourceId, new URIImpl(ValueUtils.RDF_TYPE_PREDICATE), supertype, context);
			}
		}
		else{
			addStatement(resourceId, persistentProperty.getPredicate(), objectToLiteralConverter.convert(value), context);
		}
	}
	
	@Override
	protected void processAssociationStatement(SemanticPersistentProperty persistentProperty, Resource value) {
		SemanticPersistentEntity<?> persistentEntity = (SemanticPersistentEntity<?>) persistentProperty.getOwner();
		Resource context = persistentEntity.getContext(entity);
		if(Direction.OUTGOING.equals(persistentProperty.getDirection())){
			addStatement(resourceId, persistentProperty.getPredicate(), value, context);	
		}
		else if(Direction.INCOMING.equals(persistentProperty.getDirection())){
			SemanticPersistentProperty inverseProperty = persistentProperty.getInverseProperty();
			if(inverseProperty != null){
				addStatement(value, inverseProperty.getPredicate(), resourceId, context);
			}
			else{
				addStatement(value, persistentProperty.getPredicate(), resourceId, context);
			}
		}
		else{
			addStatement(resourceId, persistentProperty.getPredicate(), value, context);
			SemanticPersistentProperty inverseProperty = persistentProperty.getInverseProperty();
			if(inverseProperty != null){
				addStatement(value, inverseProperty.getPredicate(), resourceId, context);
			}
			else{
				addStatement(value, persistentProperty.getPredicate(), resourceId, context);
			}
		}
	}
	
	private void addStatement(Resource subject, URI predicate, Value object, Resource context){
		if(context == null){
			statements.addStatement(new StatementImpl(subject, predicate, object));
		}
		else{
			statements.addStatement(new ContextStatementImpl(subject, predicate, object, context));
		}
	}


	@Override
	protected AbstractPropertiesToStatementsHandlers getInstance(
			RDFState statements, Object entity,
			SemanticMappingContext mappingContext) {
		return new PropertiesToStatementsHandler(statements, entity, mappingContext);
	}


	@Override
	protected boolean allowEmpty() {
		return false;
	}





	
}

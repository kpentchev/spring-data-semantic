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
package org.springframework.data.semantic.support.convert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.semantic.convert.SemanticEntityConverter;
import org.springframework.data.semantic.convert.SemanticEntityInstantiator;
import org.springframework.data.semantic.convert.state.EntityState;
import org.springframework.data.semantic.core.RDFState;
import org.springframework.data.semantic.core.SemanticDatabase;
import org.springframework.data.semantic.mapping.MappingPolicy;
import org.springframework.data.semantic.mapping.SemanticPersistentEntity;
import org.springframework.data.semantic.mapping.SemanticPersistentProperty;
import org.springframework.data.semantic.support.Cascade;
import org.springframework.data.semantic.support.Direction;
import org.springframework.data.semantic.support.exceptions.RequiredPropertyException;
import org.springframework.data.semantic.support.mapping.SemanticMappingContext;
import org.springframework.data.semantic.support.mapping.SemanticPersistentEntityImpl;
import org.springframework.data.semantic.support.util.ValueUtils;

/**
 * Handles the logic for converting Statements to Entities
 * This service should not be used directly, but rather via a caching abstraction (SemanticEntityPersister)
 */
public class SemanticEntityConverterImpl implements SemanticEntityConverter {
	
	private Logger logger = LoggerFactory.getLogger(SemanticEntityConverterImpl.class);

	private final SemanticMappingContext mappingContext;
	private final ConversionService conversionService;
	private final SemanticEntityInstantiator entityInstantiator;
	private final SemanticSourceStateTransmitter sourceStateTransmitter;
	private final EntityToStatementsConverter toStatementsConverter;
	private final SemanticDatabase semanticDatabase;
	
	
	
	public SemanticEntityConverterImpl(SemanticMappingContext mappingContext, ConversionService conversionService, SemanticEntityInstantiator entityInstantiator, SemanticSourceStateTransmitter sourceStateTransmitter, EntityToStatementsConverter toStatementsConverter, SemanticDatabase semanticDatabase){
		this.mappingContext = mappingContext;
		this.conversionService = conversionService;
		this.entityInstantiator = entityInstantiator;
		this.sourceStateTransmitter = sourceStateTransmitter;
		this.toStatementsConverter = toStatementsConverter;
		this.semanticDatabase = semanticDatabase;
	}

	@Override
	public MappingContext<? extends SemanticPersistentEntity<?>, SemanticPersistentProperty> getMappingContext() {
		return mappingContext;
	}

	@Override
	public ConversionService getConversionService() {
		return conversionService;
	}

	@Override
	public <R> R read(Class<R> type, RDFState source) {
		
		@SuppressWarnings("unchecked")
		final SemanticPersistentEntityImpl<R> persistentEntity = (SemanticPersistentEntityImpl<R>) mappingContext.getPersistentEntity(type);
		R dao = entityInstantiator.createInstanceFromState(persistentEntity, source);
		loadEntity(dao, source, persistentEntity.getMappingPolicy(), persistentEntity);
		return dao;
	}

	@Override
	public void write(Object source, RDFState dbStatements) {
		final SemanticPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(source.getClass());
		final BeanWrapper<Object> wrapper = BeanWrapper.<Object>create(source, conversionService);
        RDFState currentState = toStatementsConverter.convertEntityToStatements(persistentEntity, source);
		if (dbStatements != null && !dbStatements.isEmpty()) {
			//TODO optimize conversion of alias statements to actual statements
			//Object dbObject = read(source.getClass(), dbStatements);
			//RDFState dbState = toStatementsConverter.convertEntityToStatements(persistentEntity, dbObject);
			//dbState.getCurrentStatements().removeAll(currentState.getCurrentStatements());
			dbStatements.getCurrentStatements().removeAll(currentState.getCurrentStatements());
        	currentState.setDeleteStatements(dbStatements.getCurrentStatements());
        }
		EntityState<Object, RDFState> state = sourceStateTransmitter.copyPropertiesTo(wrapper, currentState);
		state.persist();
	}
	
	@Override
	public void write(Map<Object, RDFState> objectsAndState) {
		RDFState mergedModel = new RDFState();
		for(Entry<Object, RDFState> entry : objectsAndState.entrySet()){
			try{
				Object source = entry.getKey();
				RDFState dbStatements = entry.getValue();
				final SemanticPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(source.getClass());
				final BeanWrapper<Object> wrapper = BeanWrapper.<Object>create(source, conversionService);
		        RDFState currentState = toStatementsConverter.convertEntityToStatements(persistentEntity, source);
				if (dbStatements != null && !dbStatements.isEmpty()) {
					//TODO optimize conversion of alias statements to actual statements
					//Object dbObject = read(source.getClass(), dbStatements);
					//RDFState dbState = toStatementsConverter.convertEntityToStatements(persistentEntity, dbObject);
					//dbState.getCurrentStatements().removeAll(currentState.getCurrentStatements());
					dbStatements.getCurrentStatements().removeAll(currentState.getCurrentStatements());
		        	currentState.setDeleteStatements(dbStatements.getCurrentStatements());
		        }
				EntityState<Object, RDFState> state = sourceStateTransmitter.copyPropertiesTo(wrapper, currentState);
				mergedModel.merge(state.getPersistentState());
			} catch(RequiredPropertyException e){
				logger.error(e.getMessage(), e);
			}
		}
		semanticDatabase.removeStatements(mergedModel.getDeleteStatements());
		semanticDatabase.addStatements(mergedModel.getCurrentStatements());
	}

	@Override
	public <R> R loadEntity(R entity, RDFState source,
			MappingPolicy mappingPolicy,
			SemanticPersistentEntity<R> persistentEntity) {
		
		final BeanWrapper<R> wrapper = BeanWrapper.<R>create(entity, conversionService);
        sourceStateTransmitter.copyPropertiesFrom(wrapper, source, persistentEntity, mappingPolicy);
        cascadeFetch(entity, persistentEntity, wrapper, source);
        
        return entity;
	}
	
	private <R> void cascadeFetch(final R entity, final SemanticPersistentEntity<R> persistentEntity, final BeanWrapper<R> wrapper, final RDFState source) {
		persistentEntity.doWithAssociations(new AssociationHandler<SemanticPersistentProperty>() {
            @Override
            public void doWithAssociation(Association<SemanticPersistentProperty> association) {
                final SemanticPersistentProperty property = association.getInverse();
                // MappingPolicy mappingPolicy = policy.combineWith(property.getMappingPolicy());
                final MappingPolicy mappingPolicy = property.getMappingPolicy();
                @SuppressWarnings("unchecked")
				SemanticPersistentEntity<Object> associatedPersistentEntity = (SemanticPersistentEntity<Object>) mappingContext.getPersistentEntity(property.getTypeInformation().getActualType());
            	Set<? extends Value> associatedEntityIds;
            	if(Direction.INCOMING.equals(property.getDirection())){
            		associatedEntityIds = source.getCurrentStatements().filter(null, ValueUtils.createUri(property.getInverseProperty().getAliasPredicate()), persistentEntity.getResourceId(entity)).subjects();
            	}
            	else{
            		associatedEntityIds = source.getCurrentStatements().filter(persistentEntity.getResourceId(entity), ValueUtils.createUri(property.getAliasPredicate()), null).objects();
            	}
            	if (property.getTypeInformation().isCollectionLike()) {
            		List<Object> associationValuesList = new LinkedList<Object>();
            		for(Value associatedEntityId : associatedEntityIds){
                		if(associatedEntityId instanceof URI){
                			URI associatedEntityURI = (URI) associatedEntityId;
                			Object associatedEntity = entityInstantiator.createInstance(associatedPersistentEntity, (URI) associatedEntityId);
                			associationValuesList.add(associatedEntity);
                			if (mappingPolicy.shouldCascade(Cascade.GET)) {
                                RDFState associatedEntityState = new RDFState(source.getCurrentStatements().filter(associatedEntityURI, null, null));
                                final BeanWrapper<Object> associatedWrapper = BeanWrapper.<Object>create(associatedEntity, conversionService);
                                sourceStateTransmitter.copyPropertiesFrom(associatedWrapper, associatedEntityState, associatedPersistentEntity, mappingPolicy);
                                cascadeFetch(associatedEntity, associatedPersistentEntity, associatedWrapper, source);
                            }
                		}
                	}
            		sourceStateTransmitter.setProperty(wrapper, property, associationValuesList);
            	}
            	else{
            		if(!associatedEntityIds.isEmpty()){
            			URI associatedEntityURI = (URI) associatedEntityIds.iterator().next();
            			Object associatedEntity = entityInstantiator.createInstance(associatedPersistentEntity, associatedEntityURI);
            			if (mappingPolicy.shouldCascade(Cascade.GET)) {
            				 RDFState associatedEntityState = new RDFState(source.getCurrentStatements().filter(associatedEntityURI, null, null));
                             final BeanWrapper<Object> associatedWrapper = BeanWrapper.<Object>create(associatedEntity, conversionService);
                             sourceStateTransmitter.copyPropertiesFrom(associatedWrapper, associatedEntityState, associatedPersistentEntity, mappingPolicy);
                             cascadeFetch(associatedEntity, associatedPersistentEntity, associatedWrapper, source);
                        }
            			sourceStateTransmitter.setProperty(wrapper, property, associatedEntity);
            			
            		}
            	}
            }
        });
	}

}

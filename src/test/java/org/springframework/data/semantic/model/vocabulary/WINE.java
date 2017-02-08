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
package org.springframework.data.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;

/**
 * Vocabulary constants for the wine ontology.
 * 
 * @author jeen
 * 
 */
public class WINE extends VOCABULARY {
	
	public static final String NAMESPACE = "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#";

	public static final IRI WINE = create("Wine");
	public static final IRI RED_WINE = create("RedWine");
	public static final IRI WHITE_WINE = create("WhiteWine");
	public static final IRI LIGHT = create("Light");
	public static final IRI RUBIN = create("Rubin");
	public static final IRI GAMZA = create("Gamza");
	public static final IRI KADARKA = create("Kadarka");
	
	public static final IRI VERDEJO = create("Verdejo");
	public static final IRI MACABEO = create("Macabeo");
	public static final IRI SAUVIGNON_BLANC = create("Sauvignon_blanc");
	
	private static IRI create(String localName){
		return create(NAMESPACE, localName);
	}

}

/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.IdentificationKey;
import org.ala.model.Image;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.model.PestStatus;
import org.ala.model.Publication;
import org.ala.model.Reference;
import org.ala.model.SimpleProperty;
import org.ala.model.SpecimenHolding;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Cassandra SubColumn name and data type.
 * 
 * @author mok011
 *
 */
public enum CassandraSubColumnType {
	TAXONCONCEPT_COL("taxonConcept", TaxonConcept.class, false),
	TAXONNAME_COL("hasTaxonName", TaxonName.class, true),
	IDENTIFIER_COL("sameAs", String.class, true),
	SYNONYM_COL("hasSynonym", TaxonConcept.class, true),
	IS_CONGRUENT_TO_COL("IsCongruentTo", TaxonConcept.class, true),
	VERNACULAR_COL("hasVernacularConcept", CommonName.class, true),
	CONSERVATION_STATUS_COL("hasConservationStatus", ConservationStatus.class, true),
	PEST_STATUS_COL("hasPestStatus", PestStatus.class, true),
	REGION_COL("hasRegion", OccurrencesInGeoregion.class, true),
	EXTANT_STATUS_COL("hasExtantStatus", ExtantStatus.class, true),
	HABITAT_COL("hasHabitat", Habitat.class, true),
	IMAGE_COL("hasImage", Image.class, true),
	DIST_IMAGE_COL("hasDistributionImage", Image.class, true),
	IS_CHILD_COL_OF("IsChildTaxonOf", TaxonConcept.class, true),
	IS_PARENT_COL_OF("IsParentTaxonOf", TaxonConcept.class, true),
    TEXT_PROPERTY_COL("hasTextProperty", SimpleProperty.class, true),
    CLASSIFICATION_COL("hasClassification", Classification.class, true),
    REFERENCE_COL("hasReference", Reference.class, true),
    EARLIEST_REFERENCE_COL("hasEarliestReference", Reference.class, false),
    PUBLICATION_REFERENCE_COL("hasPublicationReference", Reference.class, true),
    PUBLICATION_COL("hasPublication", Publication.class, true),
    IDENTIFICATION_KEY_COL("hasIdentificationKey", IdentificationKey.class, true),
    SPECIMEN_HOLDING_COL("hasSpecimenHolding", SpecimenHolding.class, true),
    IS_ICONIC("IsIconic", Boolean.class, false),
    IS_AUSTRALIAN("IsAustralian", Boolean.class, false),
    OCCURRENCE_RECORDS_COUNT_COL("hasOccurrenceRecords", Integer.class, false),
    GEOREF_RECORDS_COUNT_COL("hasGeoReferencedRecords", Integer.class, false);

	public static final String COLUMN_FAMILY_NAME = "tc";
	public static final String SUPER_COLUMN_NAME = "tc";
	
    private static final Map<String, CassandraSubColumnType> cassandraSubColumnTypeLookup = new HashMap<String, CassandraSubColumnType>();
    
    static {
         for (CassandraSubColumnType mt : EnumSet.allOf(CassandraSubColumnType.class)) {
        	 cassandraSubColumnTypeLookup.put(mt.getColumnName(), mt);
         }
    }

	private String columnName;
    private Class clazz;
    private boolean isList;
    
	private CassandraSubColumnType(String columnName, Class clazz, boolean isList){
    	this.columnName = columnName;
    	this.clazz = clazz;
    	this.isList = isList;
    }
    
    public String getColumnName() {
		return columnName;
	}

	public Class getClazz() {
		return clazz;
	}

	public boolean isList() {
		return isList;
	}
	
    public static CassandraSubColumnType getCassandraSubColumnType(String columnName) {
        return cassandraSubColumnTypeLookup.get(columnName);
    }
	
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }	
}

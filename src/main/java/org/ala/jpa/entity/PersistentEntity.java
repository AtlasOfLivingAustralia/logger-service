/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.jpa.entity;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Base class for persistent business objects.
 * 
 * @author waiman.mok@csiro.au
 */
@MappedSuperclass
public abstract class PersistentEntity implements Serializable {
    private static final long serialVersionUID = 286090534347625650L;

    /**
     * Identity relation.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof PersistentEntity) ? 
                EqualsBuilder.reflectionEquals((PersistentEntity)this, object) : false;
    }

    /**
     * HashCode method.
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    /**
     * To-string method.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

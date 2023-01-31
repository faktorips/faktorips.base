/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.jakarta3;

import java.util.UUID;

import org.faktorips.runtime.internal.AbstractModelObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Abstract base class for policy component types enabled for JAXB persistence.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public abstract class AbstractJaxbModelObject extends AbstractModelObject {

    /** Uniquely identifies this model object within the object graph it belongs to. */
    @XmlAttribute(name = "object.id")
    @XmlID
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "written and read by JAXB")
    private String jaxbId = "jaxbId-" + UUID.randomUUID().toString();

}

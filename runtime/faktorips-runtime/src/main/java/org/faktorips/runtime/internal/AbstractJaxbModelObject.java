/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstract base class for policy component types enabled for JAXB persistence.
 * 
 * @deprecated for removal since 23.6; use
 *                 {@code org.faktorips.runtime.xml.javax.AbstractJaxbModelObject} or
 *                 {@code org.faktorips.runtime.xml.jakarta.AbstractJaxbModelObject} instead
 */
@Deprecated
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public abstract class AbstractJaxbModelObject extends AbstractModelObject {

    /** Uniquely identifies this model object within the object graph it belongs to. */
    @XmlAttribute(name = "object.id")
    @XmlID
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "written and read by JAXB")
    private String jaxbId = "jaxbId-" + UUID.randomUUID().toString();

}

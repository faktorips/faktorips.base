/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Abstract implementation that can be used as a base class for real builder sets.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractBuilderSet implements IIpsArtefactBuilderSet {

    private String id;
    private String label;
    private IIpsProject ipsProject;
    private IIpsLoggingFrameworkConnector logStatementBuilder;

    public AbstractBuilderSet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector() {
        return logStatementBuilder;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasLogStatementBuilder() {
        return logStatementBuilder != null;
    }

    /**
     * {@inheritDoc}
     */
    public void setIpsLoggingFrameworkConnector(IIpsLoggingFrameworkConnector logStmtBuilder) {
        logStatementBuilder = logStmtBuilder;
    }

    public String toString() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsAggregateRootBuilder() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return false;
    }
}

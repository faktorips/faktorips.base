/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.io.Serializable;

import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * Instances of this class indicate a dependency between an IpsObject and a Datatype. It is assumed
 * that the source is an IpsObject and therefore the qualified name type is used to identify it. The
 * target is assumed to be a datatype where only the qualified name is known.
 * 
 * @author Peter Erzberger
 */
public class DatatypeDependency implements IDependency, Serializable{

    private static final long serialVersionUID = 6487956167551523725L;
    
    private QualifiedNameType source;
    private String target;
    private DependencyType type;
    private int hashCode;
    
    /**
     * Creates a new instance. The provided parameters must not be null.
     */
    public DatatypeDependency(QualifiedNameType source, String target) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        this.source = source;
        this.target = target;
        this.type = DependencyType.DATATYPE;
        calculateHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public QualifiedNameType getSource() {
        return source;
    }

    /**
     * Returns the target as string which is the actual datatype of the target property. 
     */
    public String getTargetAsQualifiedName() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public Object getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public DependencyType getType() {
        return type;
    }

    private void calculateHashCode(){
        int result = 17;
        result = result*37 + source.hashCode();
        result = result*37 + target.hashCode();
        hashCode = result;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj instanceof IDependency) {
            IDependency other = (IDependency)obj;
            return getSource().equals(other.getSource()) && getTarget().equals(other.getTarget())
                    && getType().equals(other.getType());
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode(){
        return hashCode;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString(){
        return "(" + source.toString() + " -> " + target.toString() + ", type: " + type + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}

/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.bf;

import org.eclipse.draw2d.geometry.Point;

/**
 * This enumeration defines the possible type of business function elements. Each instance of a
 * business function element has a reference to its type.
 * 
 * @author Peter Erzberger
 */
public enum BFElementType {

    ACTION_INLINE("inlineAction", Messages.BFElementType_inlineAction) { //$NON-NLS-1$  
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newOpaqueAction(location);
        }
    },

    ACTION_METHODCALL("methodCallAction", Messages.BFElementType_methodCallAction) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newMethodCallAction(location);
        }
    },

    ACTION_BUSINESSFUNCTIONCALL("businessFunctionCallAction", Messages.BFElementType_bfCallAction) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newBusinessFunctionCallAction(location);
        }
    },

    DECISION("decision", Messages.BFElementType_decision) { //$NON-NLS-1$  
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newDecision(location);
        }
    },

    DECISION_METHODCALL("methodCallDecision", Messages.BFElementType_methodCallDecision) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newMethodCallDecision(location);
        }
    },

    MERGE("merge", Messages.BFElementType_merge) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newMerge(location);
        }
    },

    END("end", Messages.BFElementType_end) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newEnd(location);
        }
    },

    START("start", Messages.BFElementType_start) { //$NON-NLS-1$ 
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newStart(location);
        }
    },

    PARAMETER("parameter", Messages.BFElementType_parameter) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Point location) {
            return businessFunction.newParameter();
        }
    };

    private BFElementType(String id, String name) {
        this.name = name;
        this.id = id;
    }

    private String name;
    private String id;

    /**
     * Returns the describing name of the business function element type.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the unique id of the business function element type.
     */
    public String getId() {
        return id;
    }

    /**
     * Creates a new instance of a business function element of this type.
     * 
     * @param businessFunction the business function to which the created type belongs to
     * @param location the graphical location of the display element of this type
     */
    public abstract IBFElement newBFElement(IBusinessFunction businessFunction, Point location);

    /**
     * Returns the type for the specified id. If none is found and {@link IllegalArgumentException}
     * will be thrown.
     * 
     * @throws IllegalArgumentException if no type is found for the specified id
     */
    public final static BFElementType getType(String id) {
        if (id.equals(ACTION_INLINE.id)) {
            return ACTION_INLINE;
        }
        if (id.equals(ACTION_BUSINESSFUNCTIONCALL.id)) {
            return ACTION_BUSINESSFUNCTIONCALL;
        }
        if (id.equals(ACTION_METHODCALL.id)) {
            return ACTION_METHODCALL;
        }
        if (id.equals(DECISION.id)) {
            return DECISION;
        }
        if (id.equals(DECISION_METHODCALL.id)) {
            return BFElementType.DECISION_METHODCALL;
        }
        if (id.equals(MERGE.id)) {
            return MERGE;
        }
        if (id.equals(END.id)) {
            return END;
        }
        if (id.equals(START.id)) {
            return START;
        }
        if (id.equals(PARAMETER.id)) {
            return PARAMETER;
        }
        throw new IllegalArgumentException("Unexpected type id: " + id); //$NON-NLS-1$
    }

}

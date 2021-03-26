/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.bf;

/**
 * This enumeration defines the possible type of business function elements. Each instance of a
 * business function element has a reference to its type.
 * 
 * @author Peter Erzberger
 */
@SuppressWarnings("deprecation")
public enum BFElementType {

    ACTION_INLINE("inlineAction", Messages.BFElementType_inlineAction) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newOpaqueAction(location);
        }
    },

    ACTION_METHODCALL("methodCallAction", Messages.BFElementType_methodCallAction) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newMethodCallAction(location);
        }
    },

    ACTION_BUSINESSFUNCTIONCALL("businessFunctionCallAction", Messages.BFElementType_bfCallAction) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newBusinessFunctionCallAction(location);
        }
    },

    DECISION("decision", Messages.BFElementType_decision) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newDecision(location);
        }
    },

    DECISION_METHODCALL("methodCallDecision", Messages.BFElementType_methodCallDecision) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newMethodCallDecision(location);
        }
    },

    MERGE("merge", Messages.BFElementType_merge) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newMerge(location);
        }
    },

    END("end", Messages.BFElementType_end) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newEnd(location);
        }
    },

    START("start", Messages.BFElementType_start) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
            return businessFunction.newStart(location);
        }
    },

    PARAMETER("parameter", Messages.BFElementType_parameter) { //$NON-NLS-1$
        @Override
        public IBFElement newBFElement(IBusinessFunction businessFunction, Location location) {
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
    public abstract IBFElement newBFElement(IBusinessFunction businessFunction, Location location);

    /**
     * Returns the type for the specified id. If none is found and {@link IllegalArgumentException}
     * will be thrown.
     * 
     * @throws IllegalArgumentException if no type is found for the specified id
     */
    public static final BFElementType getType(String id) {
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

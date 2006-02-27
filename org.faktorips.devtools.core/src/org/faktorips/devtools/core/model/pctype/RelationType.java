package org.faktorips.devtools.core.model.pctype;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * Describes the kind of relation, aggregation or assoziation. 
 */
public class RelationType extends DefaultEnumValue {
    
    public final static RelationType COMPOSITION;

    public final static RelationType REVERSE_COMPOSITION;
    
    public final static RelationType ASSOZIATION;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("RelationType", RelationType.class); //$NON-NLS-1$
        COMPOSITION = new RelationType(enumType, "comp", "Composition"); //$NON-NLS-1$ //$NON-NLS-2$
        REVERSE_COMPOSITION = new RelationType(enumType, "reverseComp", "Reverse Composition"); //$NON-NLS-1$ //$NON-NLS-2$
        ASSOZIATION = new RelationType(enumType, "ass", "Assoziation"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static RelationType getRelationType(String id) {
        if (id.equals("agg")) { //$NON-NLS-1$
            // renamed aggregation to composition (inkl the id!)
            return COMPOSITION;
        }
        return (RelationType)enumType.getEnumValue(id);
    }
    
    public boolean isComposition() {
        return this==COMPOSITION;
    }
    
    public boolean isReverseComposition() {
        return this==REVERSE_COMPOSITION;
    }
    
    public boolean isAssoziation() {
        return this==ASSOZIATION;
    }
    
    private RelationType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
    
}

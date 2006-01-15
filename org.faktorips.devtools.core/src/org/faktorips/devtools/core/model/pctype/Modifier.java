package org.faktorips.devtools.core.model.pctype;

import org.eclipse.jdt.core.Flags;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * The kind of modifiers. 
 */
public class Modifier extends DefaultEnumValue {
    
    public final static Modifier PUBLISHED;
    
    public final static Modifier PUBLIC;

    public final static Modifier PRIVATE;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("Modifier", Modifier.class);
        PUBLISHED = new Modifier(enumType, "published", Flags.AccPublic, java.lang.reflect.Modifier.PUBLIC);
        PUBLIC = new Modifier(enumType, "public", Flags.AccPublic, java.lang.reflect.Modifier.PUBLIC);
        PRIVATE = new Modifier(enumType, "private", Flags.AccPrivate, java.lang.reflect.Modifier.PRIVATE);
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static Modifier getModifier(String id) {
        return (Modifier)enumType.getEnumValue(id);
    }
    
    // flags constant according to org.eclipse.jdt.core.Flags 
    private int jdtFlags;
    
    // Modifier according to java.lang.reflect.Modifier
    private int javaModifier;
    
    /**
     * Returns the appropriate JDT Flags constants corresponding to the modifier.
     * 
     * @see org.eclipse.jdt.core.Flags
     */
    public int getJdtFlags() {
        return jdtFlags;
    }
    
    /**
     * Returns the Java modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier() {
    	return javaModifier;
    }
    
    private Modifier(DefaultEnumType type, String id, int jdtFlags, int javaModifier) {
        super(type, id);
        this.jdtFlags = jdtFlags;
        this.javaModifier = javaModifier;
    }
    
}

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public abstract class AbstractPageElement implements PageElement {

    protected Set<Style> styles = new LinkedHashSet<Style>();
    
    public AbstractPageElement() {
        super();
    }

    public AbstractPageElement(Set<Style> styles) {
        super();
        this.styles = styles;
    }

    public Set<Style> getStyles() {
    	if (styles == null) return Collections.emptySet();
        return styles;
    }

    public void addStyles(Style... style) {
        styles.addAll(Arrays.asList(style));
    }
    
    

    public void removeStyles(Style... style) {
    	styles.removeAll(Arrays.asList(style));
	}

	public void build() {
    }
    
    public abstract void acceptLayouter(ILayouter layouter);
}
package org.faktorips.devtools.htmlexport.generators;

/**
 * A LayoutResource is a part of a page, but not included in the page. <br/>
 * If the documentation is layouted in html, then e.g. images and stylesheets are external
 * resources, which are not part of the html-file. <br/>
 * A LayoutResource consists of an identifying and describing name and the content of this resource
 * as an byte[].
 * 
 * @author dicker
 * 
 */
public class LayoutResource {
    private String name;
    private byte[] content;

    public LayoutResource(String name, byte[] data) {
        super();
        this.name = name;
        this.content = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LayoutResource other = (LayoutResource)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}

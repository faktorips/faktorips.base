package org.faktorips.devtools.htmlexport.generators;

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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LayoutResource other = (LayoutResource) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}

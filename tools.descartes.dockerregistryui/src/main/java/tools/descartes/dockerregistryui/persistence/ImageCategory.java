package tools.descartes.dockerregistryui.persistence;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

@Entity
public class ImageCategory {

	@Transient
	public static final String DEFAULT_COLOR = "#42adf4";
	
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	private String color;
	
	@ManyToMany(cascade = CascadeType.REFRESH)
	@JoinTable
	private List<ImageDescription> descriptions;
	
	ImageCategory() {
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	void setColor(String color) {
		this.color = color;
	}

	public List<ImageDescription> getDescriptions() {
		return descriptions;
	}

	void setDescriptions(List<ImageDescription> descriptions) {
		this.descriptions = descriptions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageCategory other = (ImageCategory) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}

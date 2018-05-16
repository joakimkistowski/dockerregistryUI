package tools.descartes.dockerregistryui.persistence;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

@Entity
public class ImageDescription {

	@Id
	@GeneratedValue
	private long id;

	private String imageName;
	@Lob
	private String description;
	@Lob
	private String exampleCommand;

	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy="descriptions")
	private List<ImageCategory> categories;
	
	public long getId() {
		return id;
	}

	ImageDescription() {
		
	}
	
//	public void setId(long id) {
//		this.id = id;
//	}
	
	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	public String getExampleCommand() {
		return exampleCommand;
	}

	void setExampleCommand(String exampleCommand) {
		this.exampleCommand = exampleCommand;
	}

	public String getImageName() {
		return imageName;
	}

	void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public List<ImageCategory> getCategories() {
		return categories;
	}

	void setCategories(List<ImageCategory> categories) {
		this.categories = categories;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((imageName == null) ? 0 : imageName.hashCode());
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
		ImageDescription other = (ImageDescription) obj;
		if (id != other.id)
			return false;
		if (imageName == null) {
			if (other.imageName != null)
				return false;
		} else if (!imageName.equals(other.imageName))
			return false;
		return true;
	}
	

}

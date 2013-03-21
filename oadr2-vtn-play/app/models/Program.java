package models;

import javax.persistence.*;
import play.data.validation.Constraints.Required;

/**
 * A class to represent the Program creation form
 * for Play's automatic binding of form fields to Objects
 * 
 * @author Jeff LaJoie
 *
 */
@Entity(name="Program")
@Table(name="PROGRAMS")
public class Program{
	
	@Required(message = "Must enter a valid Program Name")
	private String programName;
	@Required(message = "Must enter a valid Program URI")
	private String programURI;

	@Id private long id;
	
	public Program(){
		
	}
	
	public Program(int programId){
		this.setId(programId);
	}

	@Column(name="PROJECTURI")
	public String getProgramURI() {
		return programURI;
	}

	public void setProgramURI(String programURI) {
		this.programURI = programURI;
	}

	@Column(name="PROJECTNAME")
	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	
}

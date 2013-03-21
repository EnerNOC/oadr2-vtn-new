package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.Required;

/**
 * A class to represent the VEN creation form
 * for Play's automatic binding of form fields to Objects
 * 
 * @author Jeff LaJoie
 *
 */
@Entity(name="VEN")
@Table(name="VEN")
public class VEN{
			
	@Required(message = "Must enter a valid VEN ID")
	@Column(name = "VENID")
	private String venID;
	
	//Must allow for multiple vens
	@Required(message = "Must select a Program")
	@Column(name = "PROGRAMID")
	private String programId;
	
	@Column(name = "VENNAME")
	private String venName;
	
	@Column(name = "CLIENTURI")
	private String clientURI;
	
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
		
	public VEN(){
		
	}		

	public String getVenID() {
		return venID;
	}

	public void setVenID(String userName) {
		this.venID = userName;
	}

	public String getProgramId() {
		return programId;
	}

	@Column(name = "PROJECTID")
	public void setProgramId(String programId) {
		this.programId = programId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
    public String getVenName() {
        return venName;
    }

    public void setVenName(String customerName) {
        this.venName = customerName;
    }

    public String getClientURI() {
        return clientURI;
    }

    public void setClientURI(String clientURI) {
        this.clientURI = clientURI;
    }

}

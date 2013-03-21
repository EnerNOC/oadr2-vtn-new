package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Persistence;

import models.Program;
import models.VEN;
import play.data.Form;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import protocol.ProtocolRegistry;

/**
 * Controller to handle the VEN creation, deletion and display
 * 
 * @author Jeff LaJoie
 *
 */
public class VENs extends Controller {	
    
    static ProtocolRegistry protocolRegistry = ProtocolRegistry.getInstance();

    /**
     * Base method called to access the default page for the VENs controller
     *  
     * @return a redirect to the vens() call as to render the default page
     */
    public static Result index() {
	    return redirect(routes.VENs.vens());
    }

    /**
     * Default method to render the page for the VEN table
     * 
     * @return the default render page for VEN display and deletion
     */
	@SuppressWarnings("unchecked")
	@Transactional
	public static Result vens(){		  		  
	    class VENFormComparator implements Comparator<VEN>{
	        public int compare(VEN userOne, VEN userTwo){
	            return userOne.getVenID().compareTo(userTwo.getVenID());
            }
        }		  
	    List<VEN> vens = JPA.em().createQuery("FROM VEN").getResultList();
	    Collections.sort(vens, new VENFormComparator());
	    return ok(views.html.vens.render(vens));
	}
	
	/**
	 * Creates a blank VEN form when the Create VEN form button is clicked
	 * 
	 * @return a rendering of an empty VEN creation form
	 */
	public static Result blankVEN(){
	    return ok(views.html.newVEN.render(form(VEN.class), makeProgramMap()));
	}
	
	/**
	 * Creates a VEN in the table from the submitted form
	 * 
	 * @return a redirect to the VEN display page with the added VEN
	 */
	@Transactional
    public static Result newVEN(){
	    Form<VEN> filledForm = form(VEN.class).bindFromRequest();
	    if(filledForm.hasErrors()){
	        addFlashError(filledForm.errors());
	        return badRequest(views.html.newVEN.render(filledForm, makeProgramMap()));
        }
	    else{
	        VEN newVEN = filledForm.get();
	        newVEN.setProgramId(JPA.em().find(Program.class, Long.parseLong(newVEN.getProgramId())).getProgramName());
	        JPA.em().persist(newVEN);
	        flash("success", "VEN as been created");
        }
	    return redirect(routes.VENs.vens());
    }
	
	/**
	 * Makes a map of programs to be selected from and used by the \@Select helper function
	 * 
	 * @return a Map<String, String> containing the ID and programName for a potential market context
	 */
	@SuppressWarnings("unchecked")
    public static Map<String, String> makeProgramMap(){
	    List<Program> programList = Persistence.createEntityManagerFactory("Events").createEntityManager().createQuery("FROM Program").getResultList();
		Map<String, String> programMap = new HashMap<String, String>();
		for(Program program : programList){
			programMap.put(program.getId() + "", program.getProgramName());
		}		
		return programMap;
	}
	
	/**
	 * Removes a VEN from the table
	 * 
	 * @param id - The database ID of the VEN to be deleted
	 * @return a redirect to the VEN display page without the deleted VEN
	 */
    @Transactional
    public static Result deleteVEN(Long id){
        JPA.em().remove(JPA.em().find(VEN.class, id));
        flash("success", "VEN has been deleted");
        return redirect(routes.VENs.vens());
    }
	
    /**
     * Specific validation errors for customized validation to be added to the Flash scope
     * 
     * @param errors - the Map containing the errors and their key
     */
    public static void addFlashError(Map<String, List<ValidationError>> errors){
        for(String key : errors.keySet()){
            List<ValidationError> currentError = errors.get(key);
            for(ValidationError error : currentError){
                flash(key, error.message());
            }
        }	  
    }
}
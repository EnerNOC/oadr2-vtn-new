package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import models.Program;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Programs controller to manage all Program objects created
 * and the display page for those objects
 * 
 * @author Jeff LaJoie
 *
 */
public class Programs extends Controller {
    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Events");
    static EntityManager entityManager = entityManagerFactory.createEntityManager();
    
    /**
     * Base method called to access the default page for the Programs controller
     * 
     * @return a redirect to the programs() call as to render the default page
     */
    public static Result index() {
        return redirect(routes.Programs.programs());
    }
  
    /**
     * Default method to render the page for the Program table
     * 
     * @return the default render page for Program display and deletion
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public static Result programs(){        
    	/**
    	 * Comparator to sort the Program table by name
    	 * 
    	 * @author Jeff LaJoie
    	 *
    	 */
    	class ProgramFormComparator implements Comparator<Program>{    	    
    	    public int compare(Program programOne, Program programTwo){
    			return programOne.getProgramName().compareTo(programTwo.getProgramName());
    		}
    	}
        List<Program> programs = JPA.em().createQuery("FROM Program").getResultList();   
    	Collections.sort(programs, new ProgramFormComparator());    	  
    	return ok(views.html.programs.render(programs));
    }
  
    /**
     * Called upon submission of the Create Program button
     * 
     * @return a rendering of the Program creation form with all fields blank
     */
    public static Result blankProgram(){
        return ok(views.html.newProgram.render(form(Program.class)));
    }
  
    /**
     * Persists the form from the newProgram page to the Program table
     * 
     * @return a redirect to the default display page with the Program added
     */
    @Transactional
    public static Result newProgram(){
        Form<Program> filledForm = form(Program.class).bindFromRequest();
        if(filledForm.hasErrors()){
            return badRequest();
        }
        else{
             Program newProgram = filledForm.get();
             JPA.em().persist(newProgram);
             flash("success", "Program as been created");
        }
        return redirect(routes.Programs.programs());
    }
  
    /**
     * 
     * @param id - the database ID of the Program to be deleted
     * @return a redirect to the default display page without the deleted Program
     */
    @Transactional
    public static Result deleteProgram(Long id){
        Program program = JPA.em().find(Program.class, id);
        flash("success", "Program has been deleted");
        JPA.em().remove(program);
        return redirect(routes.Programs.programs());
    }
}
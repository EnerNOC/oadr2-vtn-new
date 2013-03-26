package com.enernoc.oadr2.vtn


class ProgramController {
	/**
	 * Base method called to access the default page for the Programs controller
	 *
	 * @return a redirect to the programs() call as to render the default page
	 */
	def messageSource
	def index() {
		return redirect(action: "programs");
	}

	/**
	 * Default method to render the page for the Program table
	 *
	 * @return the default render page for Program display and deletion
	 */

	def programs() {
		/**
		 * Comparator to sort the Program table by name
		 *
		 * @author Jeff LaJoie
		 *
		 class ProgramFormComparator implements Comparator<Program>{
		 public int compare(Program programOne, Program programTwo){
		 return programOne.getProgramName().compareTo(programTwo.getProgramName());
		 }
		 }
		 List<Program> programs = JPA.em().createQuery("FROM Program").getResultList();
		 Collections.sort(programs, new ProgramFormComparator());
		 return ok(views.html.programs.render(programs));*/
		def results = Program.listOrderByProgramName(order:"desc")
		[programList: results]
	}

	/**
	 * Called upon submission of the Create Program button
	 *
	 * @return a rendering of the Program creation form with all fields blank
	 public static Result blankProgram(){
	 return ok(views.html.newProgram.render(form(Program.class)));
	 }*/

	def blankProgram() {
		[]
	}

	/**
	 * Persists the form from the newProgram page to the Program table
	 *
	 * @return a redirect to the default display page with the Program added
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
	 }*/

	def newProgram() {
		def program = new Program(params)
		def errorMessage = ""
		if (program.validate()) {
			program.save()
			flash.message = "Success"
		} else {
			flash.message = "Fail"
			program.errors.allErrors.each {
				errorMessage += messageSource.getMessage(it, null) + "</br>"
			}
			return chain(action:"blankProgram", model: [error: errorMessage])
		}
		chain(action:"programs", model: [error: errorMessage])
	}
	/**
	 *
	 * @param id - the database ID of the Program to be deleted
	 * @return a redirect to the default display page without the deleted Program
	 */
	/*@Transactional
	 public static Result deleteProgram(Long id){
	 Program program = JPA.em().find(Program.class, id);
	 flash("success", "Program has been deleted");
	 JPA.em().remove(program);
	 return redirect(routes.Programs.programs());
	 }*/

	def deleteProgram() {
		def program = Program.get(params.id)
		program.delete()
		redirect(action:"programs")
		//render(params.id)
	}

}

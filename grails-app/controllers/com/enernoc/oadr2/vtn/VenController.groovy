package com.enernoc.oadr2.vtn



class VenController {
    def messageSource
    def index() {
        redirect(action:"vens")
    }

    /**
     * Default method to render the page for the VEN table
     *
     * @return the default render page for VEN display and deletion
     *
     public static Result vens(){
     class VENFormComparator implements Comparator<VEN>{
     public int compare(VEN userOne, VEN userTwo){
     return userOne.getVenID().compareTo(userTwo.getVenID())
     }
     }
     List<VEN> vens = JPA.em().createQuery("FROM VEN").getResultList()
     Collections.sort(vens, new VENFormComparator())
     return ok(views.html.vens.render(vens))
     }*/

    def vens() {
        //def errorMessage = "bob"
        def results = Ven.listOrderByVenID(order:"desc")
        [venList: results]
        //chain(action:"newVEN")
        //render(view:"vens", model:[venList: results, errorMessage: errorMessage])
    }
    /**
     * Creates a blank VEN form when the Create VEN form button is clicked
     *
     * @return a rendering of an empty VEN creation form
     public static Result blankVEN(){
     return ok(views.html.newVEN.render(form(VEN.class), makeProgramMap()))
     }*/

    def blankVEN() {
        //def programs = Program.listOrderByProgramName(order:"desc")
        def programs = Program.executeQuery("SELECT distinct b.programName FROM Program b")
        [programsList: programs]
    }
    /**
     * Creates a VEN in the table from the submitted form
     *
     * @return a redirect to the VEN display page with the added VEN
     @Transactional
     public static Result newVEN(){
     Form<VEN> filledForm = form(VEN.class).bindFromRequest()
     if(filledForm.hasErrors()){
     addFlashError(filledForm.errors())
     return badRequest(views.html.newVEN.render(filledForm, makeProgramMap()))
     }
     else{
     VEN newVEN = filledForm.get()
     newVEN.setProgramId(JPA.em().find(Program.class, Long.parseLong(newVEN.getProgramId())).getProgramName())
     JPA.em().persist(newVEN)
     flash("success", "VEN as been created")
     }
     return redirect(routes.VENs.vens())
     }*/

    def newVEN() {
        def ven = new Ven(params)

        def program = Program.find("from Program as p where p.programName=?", [params.programID])
        def errorMessage = []
        if (program!=null) {
            program.addToVen(ven)
        }
        if (ven.validate()) {
            program.save()
            //	ven.save()
            flash.message="Success"
        } else {
            flash.message="Fail"
            ven.errors.allErrors.each {
                errorMessage << messageSource.getMessage(it, null)
            }
            return chain(action:"blankVEN", model:[error: errorMessage])
        }
        chain(action:"vens", model: [error: errorMessage])

    }

    /**
     * Removes a VEN from the table
     *
     * @param id - The database ID of the VEN to be deleted
     * @return a redirect to the VEN display page without the deleted VEN
     *
     @Transactional
     public static Result deleteVEN(Long id){
     JPA.em().remove(JPA.em().find(VEN.class, id))
     flash("success", "VEN has been deleted")
     return redirect(routes.VENs.vens())
     }*/
    def deleteVEN() {
        def ven = Ven.get(params.id)
        ven.delete()
        redirect(action:"vens")

    }

    /**
     * Specific validation errors for customized validation to be added to the Flash scope
     *
     * @param errors - the Map containing the errors and their key
     public static void addFlashError(Map<String, List<ValidationError>> errors){
     for(String key : errors.keySet()){
     List<ValidationError> currentError = errors.get(key)
     for(ValidationError error : currentError){
     flash(key, error.message())
     }
     }
     }*/
}

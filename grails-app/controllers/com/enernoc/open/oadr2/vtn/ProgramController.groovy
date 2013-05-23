package com.enernoc.open.oadr2.vtn


/**
 * Program controller to manage all Program objects created
 * and the display page for those objects
 *
 * @author Yang Xiang
 * 
 */
class ProgramController {
    def messageSource
    
    static defaultAction = 'programs'
    
    /**
     * Base method called to access the default page for the Programs controller
     *
     * @return a redirect to the programs() call as to render the default page
     */
    def index() {
        return redirect(action: "programs")
    }

    /**
     * Default method to render the page for the Program table
     *
     * @return the default render page for Program display, edit and deletion
     */
    def programs() {
        def results = Program.listOrderByName(order:"desc")
        [programList: results]
    }

    /**
     * The default page render for new program to be created based on
     * the file at program/blankProgram.gsp
     *
     * @return the rendered page to create an program
     */
    def blankProgram() {
        def model = [:]
        if ( ! flash.chainModel?.program ) 
            model.program = new Program()
        model
    }

    /**
     * Persists the form from the blankProgram page to the database
     *
     * @return on success: a redirect to the programs()
     * @return on fail: a chain to blankProgram with invalid program
     */
    def newProgram() {
        def program = new Program(params)
        if ( program.validate() ) {
            program.save(flush: true)
            flash.message = "Success, your Program has been created"
        } 
        else {
            flash.message="Please fix the errors below: "
            def errors = program.errors.allErrors.collect {
                messageSource.getMessage it, null
            }
            return chain(action:"blankProgram", model:[errors: errors, program: program])
        }
        redirect action: "programs"
    }
    
    /**
     * Removes the Program with the given id from the database
     *
     * @param id - the database ID of the Program to be deleted
     * @return a redirect to programs() without the deleted Program
     */
    def deleteProgram() {
        def program = Program.get(params.id)
        if ( ! program ) {
            response.sendError 404, "No program for ID $params.id"
            return
        }
        def venList = []
        venList.addAll(program.vens)
        venList.each { v ->
            v.removeFromPrograms(program)
            if( v.programs.size() == 0)
            v.delete(flush: true)
        }
        program.delete(flush: true)

        redirect(action:"programs")
    }
    
    /**
     * Edits the program with the given id
     * 
     * @param id - database ID of Program to be updated
     * @return renders program/editProgram.gsp for form submission
     */
    def editProgram() {
        def model = [:]
        if ( ! flash.chainModel?.program ) {
            def program = Program.get params.id
            if ( ! program ) {
                response.sendError 404, "No program for ID $params.id"
                return
            }
             model.program = program 
        }
        model
    }
    
    /**
     * Persists the form from the editProgram page to the database
     *
     * @return on success: a redirect to the programs()
     * @return on fail: a chain to editProgram with invalid program
     */
    def updateProgram() {
        def program = Program.get params.id     
//        bindData program, params, exclude:['id']
        program.properties = params
        if (program.validate()) {
            //TODO Once ven.programID is remove this loop will be removed
            program.save(flush: true)
            flash.message = "Success, your Program has been updated"
        }
        else {
            flash.message="Please fix the errors below: "
            def errors = program.errors.allErrors.collect {
                messageSource.getMessage it, null
            }
            return chain( action:"editProgram", model:[errors: errors, program: program] )
        }
        redirect action: "programs"
    }
}

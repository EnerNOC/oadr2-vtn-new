package com.enernoc.open.oadr2.vtn


/**
 * VEN controller to manage all VEN objects created
 * and the display page for those objects
 *
 * @author Yang Xiang
 * 
 */
class VenController {
    def messageSource

    static defaultAction = 'vens'

    /**
     * Base method called to access the default page for the Ven controller
     *
     * @return a redirect to the vens() call as to render the default page
     */
    def index() {
        redirect(action:"vens")
    }

    /**
     * Default method to render the page for the Ven table
     *
     * @return the default render page for Ven display, edit and deletion
     */
    def vens() {
        def results = Ven.listOrderByVenID(order:"desc")
        [venList: results]
    }

    /**
     * The default page render for new events to be created based on
     * the file at ven/blankVEN.gsp
     *
     * @return the rendered page to create an ven
     */
    def blankVEN() {
        def model = [:]
        model.programsList = Program.list()
        if ( ! flash.chainModel?.ven )
            model.ven = new Ven()
        model
    }

    /**
     * Persists the form from the blankVen page to the database
     *
     * @return on success: a redirect to the vens()
     * @return on fail: a chain to blankVEN() with invalid VEN
     */
    def newVEN() {
        def program = []
        params.programID.each { pID->
            def p =  Program.get( pID.toLong() )
            if ( ! p ) {
                response.sendError( 404, "No program for ID $pID" )
            }
            program << p
        }
        params.remove( 'programID' )
        def ven = new Ven(params)
        
        if (program!=[]) {
            program.each  { p->
                p.addToVen(ven)
            }
        }
        
        if (ven.validate()) {
            program.each  { p->
                p.save()
            }
            flash.message="Success, your VEN has been created"
        } else {
            ven.program = program
            flash.message="Please fix the errors below: "
            def errors = ven.errors.allErrors.collect {
                messageSource.getMessage(it, null)
            }
            return chain(action:"blankVEN", model:[errors: errors, ven: ven])
        }
        redirect(action:"vens")

    }

    /**
     * Removes the VEN with the given id from the database
     *
     * @param id - the database ID of the VEN to be deleted
     * @return a redirect to vens() without the deleted VEN
     */
    def deleteVEN() {
        def ven = Ven.get(params.id)
        if ( ! ven ) {
            response.sendError 404, "No ven for ID $params.id"
            return
        }
        ven.delete()
        redirect(action:"vens")

    }

    /**
     * Edits the VEN with the given id
     * 
     * @param id - database ID of VEN to be updated
     * @return renders ven/editVEN.gsp for form submission
     */
    def editVEN() {
        def model = [:]
        model.programsList = Program.list()
        if ( ! flash.chainModel?.currentVen) {
            model.currentVen = Ven.get(params.id)
            if ( ! model.currentVen ) {
                response.sendError 404, "No ven for ID $params.id"
                return
            }
        }
        model
    }

    /**
     * Persists the form from the editVEN.gsp page to the database
     *
     * @return on success: a redirect to the vens()
     * @return on fail: a chain to editVEN() with invalid VEN
     */
    def updateVEN() {
        def ven = Ven.get( params.id )
        if ( ! ven ) {
            response.sendError 404, "No ven for ID $params.id"
            return
        }
        
        def oldProgram = ven.program
        def newProgram = []
        params.programID.each { pID->
            def p =  Program.get( pID.toLong() )
            if ( ! p ) {
                response.sendError( 404, "No program for ID $pID" )
            }
            newProgram << p
        }
        
        //oldProgram.removeFromVen(ven)
        if (newProgram == []) {
            ven.program = null
        }
        params.remove('programID')
        ven.properties = params
        if (ven.validate()) {
            def tempOld = []
            def tempNew = []
            tempOld.addAll( oldProgram )
            tempOld.each { op ->
                if (!newProgram.contains( op )) {
                    println("old Loop: " + op.programName)
                    op.removeFromVen(ven)
                    op.save(flush:true)
                }
            }
            tempNew.addAll( newProgram )
            tempNew.each { np ->
                if (!oldProgram.contains( np )) {
                    println("new Loop: " + np.programName)
                    np.addToVen(ven)
                    np.save(flush:true)
                }
            }
            flash.message="Success, your VEN has been updated"
        } else {
            ven.program = newProgram
            flash.message="Please fix the errors below: "
            def errors = ven.errors.allErrors.collect {
                messageSource.getMessage(it, null)
            }
            return chain(action:"editVEN", model: [errors: errors, currentVen: ven])
        }
        redirect(action:"vens")
    }
}

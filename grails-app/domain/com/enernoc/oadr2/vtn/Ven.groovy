package com.enernoc.oadr2.vtn

class Ven {

    String venID;
    String programID;
    String venName;
    String clientURI;

    static belongsTo = [program: Program]

    static constraints = {
        venID(blank: false, unique: true)
        clientURI(nullable: true)

        //select a program id from one of the available programs

    }
}

package com.enernoc.open.oadr2.vtn

import com.enernoc.open.oadr2.model.SignalTypeEnumeratedType

public enum SignalType {
    
    LEVEL( "level", SignalTypeEnumeratedType.LEVEL ),
    PRICE_RELATIVE( "priceRelative", SignalTypeEnumeratedType.PRICE_RELATIVE);
    // TODO price,etc
    
    private final String xmlValue
    private final SignalTypeEnumeratedType xmlType
    
    public String getXmlValue() { return this.xmlValue }
    public SignalTypeEnumeratedType getXmlType() { this.xmlType }
    
    SignalType(final String val, final SignalTypeEnumeratedType xmlType) {
        this.xmlValue = val
        this.xmlType = xmlType
    }
    
    static findByStringValue( String val ) {
        SignalType.values().each { type ->
            if ( type.xmlValue == val ) return type
        }
        return null
    }
}

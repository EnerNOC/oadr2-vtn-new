package com.enernoc.open.oadr2.vtn

public enum SignalType {
    LEVEL("level"),
    PRICE_RELATIVE("priceRelative");
    // TODO price,etc
    
    private final String xmlValue
    
    public String getXmlValue() { return this.xmlValue }
    
    SignalType(final String val) {
        this.xmlValue = val
    }
}

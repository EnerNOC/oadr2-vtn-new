package com.enernoc.open.oadr2.vtn;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

public class DefaultMessageSource {

    MessageSource messageSource;
    
    String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        try {
            return this.messageSource.getMessage( resolvable, locale );
        }
        catch ( NullPointerException ex ) {
            return this.messageSource.getMessage( resolvable, Locale.getDefault() );
        }
    }

}

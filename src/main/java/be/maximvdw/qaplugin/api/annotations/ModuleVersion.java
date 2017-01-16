package be.maximvdw.qaplugin.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ModuleVersion
 *
 * Created by maxim on 15-Jan-17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleVersion {
    /**
     * Module version
     * @return value
     */
    String value();
}

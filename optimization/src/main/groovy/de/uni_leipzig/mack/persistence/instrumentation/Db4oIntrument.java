package de.uni_leipzig.mack.persistence.instrumentation;

import java.lang.annotation.*;

/**
 * Marks a class to be processed by the db4o bytecode enhancement process for transparent activation and transparent
 * persistence
 * <p/>
 * Created by Markus Ackermann.
 * No rights reserved.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Db4oIntrument {
}

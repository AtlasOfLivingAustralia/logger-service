package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class LogSourceType implements Serializable {

    String name;

    static constraints = {
        name maxSize: 255, nullable: true
    }

    static mapping = {
        table "log_source_type"
        version false

        id generator: "assigned", sqlType: "int(11)"
    }
}

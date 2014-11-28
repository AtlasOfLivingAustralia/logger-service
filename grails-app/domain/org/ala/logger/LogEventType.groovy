package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class LogEventType implements Serializable {

    String name

    static constraints = {
        id bindable: true
        name maxSize: 255, nullable: true
    }

    static mapping = {
        table "log_event_type"
        version false

        id generator: "assigned", sqlType: "int(11)"
    }
}

package au.org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class LogReasonType implements Serializable {

    String rkey;
    String name;

    static constraints = {
        id bindable: true
        name maxSize: 255, nullable: true
        rkey maxSize: 255, nullable: true
    }

    static mapping = {
        table "log_reason_type"
        version false

        id generator: "assigned", sqlType: "int(11)"
    }
}

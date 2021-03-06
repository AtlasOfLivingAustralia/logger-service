package au.org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class RemoteAddress implements Serializable {

    String ip;
    String hostName

    static constraints = {
        ip maxSize: 255, nullable: false
        hostName maxSize: 255, nullable: false
    }

    static mapping = {
        id bindable: true
        table "remote_address"
        version false

        id sqlType: "int(11)"
        hostName column: "host_name"
        ip column: "ip"
    }
}

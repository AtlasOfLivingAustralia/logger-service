package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Summary implements Serializable {
    String month
    String userEmail
    Integer reasonTypeId
    Integer eventTypeId
    int recordCount
    int eventCount
}

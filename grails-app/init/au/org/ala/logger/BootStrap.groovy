package au.org.ala.logger

class BootStrap {
    def init = { servletContext ->
        environments {
            test {
//                new LogReasonType(id: 1, name: "reason1", rkey: "rkey1", defaultOrder: 1).save(flush:true)
//                new LogReasonType(id: 10, name: "reason10", rkey: "rkey1", defaultOrder: 10).save(flush:true)
                new RemoteAddress(id: 13, ip: "123.123.123.123", hostName: "Valid 123").save(flush:true)
                new RemoteAddress(ip: "1.1.1.1", hostName: "Valid 1").save(flush:true)
//                new LogEventType(id: 1000, name: "event0").save(flush:true)
//                new LogEventType(id: 1001, name: "event1").save(flush:true)
//                new LogSourceType(id: 1, name: "source1").save(flush:true)
                new LogSourceType(id: 10, name: "source10").save(flush:true)

                new EventSummaryBreakdownReasonEntity(entityUid: "dp123", logEventTypeId: 1000, month: "202102", numberOfEvents: 2, recordCount: 1984, logReasonTypeId: 1).save()
                new LogEventType(id: 1000, name: "type1").save(flush:true)
                new LogEventType(id: 1001, name: "OCCURRENCE_RECORDS_VIEWED_ON_MAP").save(flush:true)
                new LogEventType(id: 1002, name: "OCCURRENCE_RECORDS_DOWNLOADED").save(flush:true)
                new LogEventType(id: 2000, name: "IMAGE_VIEWED").save(flush:true)

                new LogReasonType(id: 0, name: "conservation management/planning", rkey: "logger.download.reason.conservation", defaultOrder: 1).save()
                new LogReasonType(id: 1, name: "biosecurity management, planning", rkey: "logger.download.reason.biosecurity", defaultOrder: 10).save()
                new LogReasonType(id: 10, name: "testing", rkey: "logger.download.reason.testing", defaultOrder: 100).save()

                new LogSourceType(id: 0, name: "ALA").save()
                new LogSourceType(id: 1, name: "OZCAM").save()
                new LogSourceType(id: 2, name: "AVH").save()
            }
        }
    }
    def destroy = {
    }
}

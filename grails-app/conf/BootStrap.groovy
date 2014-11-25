import grails.util.Environment
import groovy.sql.Sql
import org.ala.logger.LogEventType
import org.ala.logger.LogReasonType
import org.ala.logger.RemoteAddress

class BootStrap {

    def init = { servletContext ->
        if (Environment.current == Environment.TEST) {
            String sqlFilePath = 'path/to/your/script.sql'
            String sqlString = new File(sqlFilePath).text
            def sql = Sql.newInstance(ConfigurationHolder.config.dataSource.url, ConfigurationHolder.config.dataSource.username, ConfigurationHolder.config.dataSource.password, ConfigurationHolder.config.dataSource.driverClassName)
            sql.execute(sqlString)

            new RemoteAddress(ip: "127.0.0.1", hostName: "localhost").save()

            LogEventType e = new LogEventType(id: 1000, name: "type1")
            e.setId(1000)
            e.save()


            LogReasonType r = new LogReasonType(name: "reason1")
            r.setId(10)
            r.save()
            r = new LogReasonType(name: "reason2")
            r.setId(2)
            r.save()
            r = new LogReasonType(name: "reason3")
            r.setId(3)
            r.save()
        }
    }

    def destroy = {
    }
}

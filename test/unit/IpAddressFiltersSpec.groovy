import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.ala.logger.LoggerController
import spock.lang.Specification

@TestFor(LoggerController)
//@Mock(IpAddressFilters)
class IpAddressFiltersSpec extends Specification {

    def "test filter"() {
        withFilters(controller: "logger", action: "") {

        }
    }
}

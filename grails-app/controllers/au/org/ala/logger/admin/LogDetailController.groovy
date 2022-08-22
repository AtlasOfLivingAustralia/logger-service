package au.org.ala.logger.admin
import au.org.ala.logger.LogDetail
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogDetailController {

    static scaffold = LogDetail

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        params.sort = params.sort ?: 'id'
        params.order = params.order ?: 'desc'

        def c = LogDetail.createCriteria()
        def r = c.list(max: params.max, offset: params.offset) {
            order(params.sort as String, params.order as String)
        }

        def model = [logDetailList : r,
                     logDetailCount: LogDetail.count(),
                     columns      : ['entityType', 'entityUid', 'logEvent', 'recordCount'],
                     entityName   : "LogDetail"]

        render(view: 'index', model: model)
    }

}

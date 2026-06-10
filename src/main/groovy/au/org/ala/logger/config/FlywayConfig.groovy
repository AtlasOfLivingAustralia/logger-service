package au.org.ala.logger.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.sql.DataSource

/**
 * Programmatic Flyway runner for PostgreSQL environment.
 * Only runs when grails.env=postgres system property is set.
 * This bypasses Spring Boot profile yml loading issues in Grails.
 */
@Configuration
class FlywayConfig {

    @Autowired
    DataSource dataSource

    @PostConstruct
    void runMigrations() {
        String grailsEnv = System.getProperty('grails.env', 'development')

        if (grailsEnv == 'postgres') {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations('classpath:db/migration')
                    .baselineOnMigrate(true)
                    .load()
            flyway.migrate()
        }
    }
}



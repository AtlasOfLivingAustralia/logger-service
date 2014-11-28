dataSource {
    pooled = true
    jmxExport = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
}

// environment specific settings
environments {

    development {
        dataSource {
            dbCreate = "validate" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://localhost/logger"
            driverClassName = "com.mysql.jdbc.Driver"
            username = "logger_user"
            password = "logger_user"
        }
    }
    test {
        dataSource {
            dialect = "org.hibernate.dialect.H2Dialect"
            dbCreate = "create-drop"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;"
        }
    }
    production {
        dataSource {
            // defined in external configuration file
        }
    }
}

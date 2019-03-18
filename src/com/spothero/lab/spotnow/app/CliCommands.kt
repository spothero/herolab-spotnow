package com.spothero.lab.spotnow.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.spothero.lab.spotnow.database.BaseRepository
import com.spothero.lab.spotnow.di.databaseModule
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module.module


class PostgreDbCommand : CliktCommand(help = "Postress repository setup", name = "dbSql") {
    val dbAddress: String? by argument(
        name = "dbAddress",
        help = "Address to Postgres repository with port(optional) - format address[:port]"
    )
    val dbUsername: String? by argument(name = "dbUser", help = "Database username.")
    val dbPassword: String? by argument(name = "dbPass", help = "Database password.")

    override fun run() {
        if (dbAddress != null && dbUsername != null && dbPassword != null) {
            databaseModule = module {
                single {
                    BaseRepository.postgreSql(dbAddress!!, "lab_spotnow")?.let { (dbUrl, driver) ->
                        Database.connect(dbUrl, driver, dbUsername!!, dbPassword!!)
                    }
                }
            }
        }
    }
}

class H2DbCommand : CliktCommand(help = "H2 repository setup", name = "h2Db") {
    val dbH2File: String? by option("-f", "--dbFile", help = "Provide full path to DB h2 file.")
    val dbMem: String? by option("-m", "--mem", help = "Name of in memory DB file")

    override fun run() {
        if (dbMem != null && dbH2File != null) {
            throw UsageError("Only one H2 database must be specified: --dbFile or --mem")
        } else {
            dbMem?.let { memDbName ->
                databaseModule = module {
                    single {
                        BaseRepository.h2DbMem(memDbName).let { (dbUrl, driver) ->
                            Database.connect(dbUrl, driver)
                        }
                    }
                }
            }
            dbH2File?.let { dbFile ->
                databaseModule = module {
                    single {
                        BaseRepository.h2DbFile(dbFile).let { (dbUrl, driver) ->
                            Database.connect(dbUrl, driver)
                        }
                    }
                }
            }
        }
    }
}

class OnDemandCommmand : CliktCommand(help = "SpotNow command to start ondemand feature", name = "SpotNow command") {
    val enable: String? by option("-d", "--ondemand", help = "On demand option of SpotNow - enabled by default")

    override fun run() {
        if (enable == "false") {
            //todo disable ondemand
        }
    }
}

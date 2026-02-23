/*
 * Copyright 2021 Kári Magnússon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package slinq.pg.ec.test

import scala.concurrent.ExecutionContext
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import slinq.pg.ec.api.db
import slinq.pg.ec.jdbc.JdbcExecutor

object TestHelpers {

  Class.forName("org.postgresql.Driver")

  implicit val ec: ExecutionContext = ExecutionContext.global

  private var initialized = false

  def loadConfig: HikariConfig = {
    val config = ConfigFactory.load()
    val dbConfig = config.getConfig("db")

    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(s"jdbc:postgresql://${dbConfig.getString("host")}:${dbConfig.getString("port")}/${dbConfig.getString("name")}")
    hikariConfig.setUsername(dbConfig.getString("user"))
    hikariConfig.setPassword(dbConfig.getString("pwd"))

    if (dbConfig.hasPath("poolSize"))
      hikariConfig.setMaximumPoolSize(dbConfig.getInt("poolSize"))

    if (dbConfig.hasPath("minPoolSize"))
      hikariConfig.setMinimumIdle(dbConfig.getInt("minPoolSize"))

    hikariConfig
  }

  def initDb(): Unit = {
    if (!initialized) {
      val config = loadConfig
      val pool = new JdbcExecutor(
        new com.zaxxer.hikari.HikariDataSource(config),
        ec
      )
      db.init(pool)
      initialized = true
    }
  }

  def closeDb(): Unit = {
    db.close()
  }
}

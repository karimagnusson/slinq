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

package slinq.pg.test

import com.typesafe.config.ConfigFactory
import zio.*
import slinq.pg.api.{SlinqPg, PgConfig}

object TestHelpers {

  def loadConfig: Task[PgConfig] = ZIO.attempt {
    val config = ConfigFactory.load()
    val dbConfig = config.getConfig("db")

    val pgConfig = PgConfig.forDb(dbConfig.getString("name"))
      .withUser(dbConfig.getString("user"))
      .withPassword(dbConfig.getString("pwd"))

    if (dbConfig.hasPath("host"))
      pgConfig.withHost(dbConfig.getString("host"))

    if (dbConfig.hasPath("port"))
      pgConfig.withPort(dbConfig.getString("port"))

    if (dbConfig.hasPath("poolSize"))
      pgConfig.withMaxPoolSize(dbConfig.getInt("poolSize"))

    if (dbConfig.hasPath("minPoolSize"))
      pgConfig.withMinPoolSize(dbConfig.getInt("minPoolSize"))

    pgConfig
  }

  val dbLayer: ZLayer[Any, Throwable, SlinqPg] =
    ZLayer.fromZIO(loadConfig).flatMap { confLayer =>
      SlinqPg.layer(confLayer.get).orDie
    }
}

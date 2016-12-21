package modules

import com.google.inject.AbstractModule
import services.{ AuthTokenService, AuthTokenServiceImpl }
import net.codingwell.scalaguice.ScalaModule
import providers.{ AuthTokenProvider, UserProvider }
import providers.impl.{ AuthTokenMongo, UserMongo }

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenProvider].to[AuthTokenMongo]
    bind[UserProvider].to[UserMongo]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}

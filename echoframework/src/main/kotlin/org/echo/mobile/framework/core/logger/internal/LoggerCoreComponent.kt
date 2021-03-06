package org.echo.mobile.framework.core.logger.internal

import org.echo.mobile.framework.core.logger.Logger

/**
 * Factory for library logger
 *
 * @author Dmitriy Bushuev
 */
object LoggerCoreComponent {

    internal var logLevel: LogLevel = LogLevel.INFO

    /**
     * Creates logger [Logger] with specified [LogLevel]
     */
    fun create(name: String): Logger = LoggerImpl(name)

    /**
     * Contains all possible configurations of events logging
     */
    enum class LogLevel {

        /**
         * All logs are disabled
         */
        NONE,

        /**
         * Logs event with information level
         */
        INFO

    }

}

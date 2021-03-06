package org.echo.mobile.framework.core.mapper.internal

import com.google.gson.Gson
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.mapper.MapperCoreComponent
import java.text.ParseException

/**
 * Implementation of [MapperCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class MapperCoreComponentImpl : MapperCoreComponent {

    val gson = Gson()

    override fun <T> map(json: String, type: Class<T>): T? {
        return try {
            gson.fromJson<T>(json, type)
        } catch (e: ParseException) {
            LOGGER.log(
                "Error occurred during json parsing. Json = $json. Required type = ${type.name}",
                e
            )
            null
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(MapperCoreComponentImpl::class.java.name)
    }

}

package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.framework.core.crypto.SeedProvider
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.support.Converter
import org.echo.mobile.framework.support.checkNotNull

/**
 * Provides seed for key creation with active user role
 *
 * @author Dmitriy Bushuev
 */
class RoleDependentSeedProvider : SeedProvider {

    override fun provide(name: String, password: String, authorityType: AuthorityType): String {
        val roleName = AuthorityTypeToRoleConverter().convert(authorityType)

        return name + roleName + password
    }

    private class AuthorityTypeToRoleConverter : Converter<AuthorityType, String> {

        private val roleNameRegistry = hashMapOf(
            AuthorityType.ACTIVE to "active"
        )

        override fun convert(source: AuthorityType): String {
            val roleName = roleNameRegistry[source]

            checkNotNull(roleName, "Unrecognized authority type: $source")

            return roleName!!
        }

    }

}

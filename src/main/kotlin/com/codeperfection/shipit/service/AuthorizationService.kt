package com.codeperfection.shipit.service

import com.codeperfection.shipit.exception.clienterror.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthorizationService {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val READ_SCOPE = "shipit:read"
        const val WRITE_SCOPE = "shipit:write"
    }

    fun checkReadAccess(userId: UUID) =
        checkAccess(userId, READ_SCOPE)

    fun checkWriteAccess(userId: UUID) =
        checkAccess(userId, WRITE_SCOPE)

    private fun checkAccess(userId: UUID, scope: String) {
        val claims = extractClaimsFromAuthentication()
        val scopes = extractFieldFromClaims<List<String>>(claims, fieldName = "scope")
        if (!scopes.contains(scope)) {
            logger.warn("Authorization error: missing scope $scope from JWT token for user $userId")
            throw UnauthorizedException
        }

        val subject = extractFieldFromClaims<String>(claims, fieldName = "sub")
        val flow = extractFieldFromClaims<String>(claims, fieldName = "flow")
        // Allow user access to only their data, i.e. subject field of the JWT token must match with provided user ID.
        // Except for client_credentials flow: machine-to-machine communication without user interaction involved.
        if (flow != AuthorizationGrantType.CLIENT_CREDENTIALS.value && userId.toString() != subject) {
            logger.warn("Authorization error: subject of JWT token $subject is different from user ID $userId")
            throw UnauthorizedException
        }
    }

    private fun extractClaimsFromAuthentication(): Map<String, Any> =
        try {
            val jwtAuthentication = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
            jwtAuthentication.token.claims
        } catch (e: Exception) {
            logger.warn("Error extracting claims from JWT token", e)
            throw UnauthorizedException
        }

    private inline fun <reified T> extractFieldFromClaims(claims: Map<String, Any>, fieldName: String): T =
        try {
            claims.getValue(fieldName) as T
        } catch (e: Exception) {
            logger.warn("Error extracting $fieldName from claims", e)
            throw UnauthorizedException
        }
}

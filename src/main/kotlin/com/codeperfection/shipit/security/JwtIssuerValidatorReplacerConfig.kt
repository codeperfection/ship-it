package com.codeperfection.shipit.security

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.JwkSetUriJwtDecoderBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import java.util.*

/**
 * This configuration class is used to replace validation of issuer to not based on the url
 * which is used to get public key,
 * Instead to do validation with preconfigured issuer name,
 * which was used to set 'iss' (issuer) claim in jwt tokens in auth server
 *
 * For details on what is being replaced please check how SupplierJwtDecoder bean is created in
 * OAuth2ResourceServerJwtConfiguration
 */
@Configuration
class JwtIssuerValidatorReplacerConfig(
    resourceServerProperties: OAuth2ResourceServerProperties,
    additionalValidatorsProvider: ObjectProvider<OAuth2TokenValidator<Jwt>>
) {

    private val properties = resourceServerProperties.jwt

    private val additionalValidators = additionalValidatorsProvider.orderedStream().toList()

    private fun getValidators(defaultValidator: OAuth2TokenValidator<Jwt>): OAuth2TokenValidator<Jwt> {
        val audiences: List<String?> = properties.audiences
        if (audiences.isEmpty() && additionalValidators.isEmpty()) {
            return defaultValidator
        }
        val validators: MutableList<OAuth2TokenValidator<Jwt>> = ArrayList()
        validators.add(defaultValidator)
        if (audiences.isNotEmpty()) {
            validators.add(
                JwtClaimValidator(JwtClaimNames.AUD) { aud: List<String?>? ->
                    aud != null && !Collections.disjoint(aud, audiences)
                }
            )
        }
        validators.addAll(additionalValidators)
        return DelegatingOAuth2TokenValidator(validators)
    }

    @Bean
    @Primary
    fun supplierJwtDecoder(
        customizers: ObjectProvider<JwkSetUriJwtDecoderBuilderCustomizer>,
        @Value("\${auth-server.issuer-name}") issuerName: String
    ) = SupplierJwtDecoder {
        val builder = NimbusJwtDecoder.withIssuerLocation(properties.issuerUri)
        customizers.orderedStream().forEach { it.customize(builder) }
        val jwtDecoder = builder.build()
        // We have implemented this configuration just to replace argument of the below createDefaultWithIssuer
        // method call from issuerUri (which is used to get public key) to issuerName
        // (which is set as claim in jwt token created by auth server)
        jwtDecoder.setJwtValidator(getValidators(JwtValidators.createDefaultWithIssuer(issuerName)))
        jwtDecoder
    }
}

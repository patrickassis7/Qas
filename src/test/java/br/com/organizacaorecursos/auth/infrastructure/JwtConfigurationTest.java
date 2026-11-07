package br.com.organizacaorecursos.auth.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtConfigurationTest {

    private final JwtConfiguration configuration = new JwtConfiguration();

    @Test
    void signingKeyIsRequired() {
        assertThatThrownBy(() -> JwtConfiguration.decodeSigningKey(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> JwtConfiguration.decodeSigningKey("  "))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void malformedBase64KeyIsRejected() {
        assertThatThrownBy(() -> JwtConfiguration.decodeSigningKey("%%%not-base64%%%"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decodedKeysShorterThan32BytesAreRejected() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[31]);
        assertThatThrownBy(() -> JwtConfiguration.decodeSigningKey(shortKey))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validBase64KeyDecodesToAtLeast32Bytes() {
        byte[] material = new byte[32];
        for (int i = 0; i < material.length; i++) {
            material[i] = (byte) (i + 1);
        }
        SecretKey key = JwtConfiguration.decodeSigningKey(Base64.getEncoder().encodeToString(material));
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(key.getEncoded()).containsExactly(material);
    }

    @Test
    void springPropertiesCommandLineAndJavaPropertiesCannotOverrideProcessEnvironment() {
        String processSecret = System.getenv("JWT_SECRET");
        assertThat(processSecret).isNotBlank();
        String springPropertySecret = Base64.getEncoder().encodeToString(filledBytes((byte) 2));
        String commandLineSecret = Base64.getEncoder().encodeToString(filledBytes((byte) 3));
        String systemPropertySecret = Base64.getEncoder().encodeToString(filledBytes((byte) 4));
        String previousJwtSystemProperty = System.getProperty("JWT_SECRET");
        System.setProperty("JWT_SECRET", systemPropertySecret);

        try {
            SpringApplication application = new SpringApplication(JwtConfiguration.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.setBannerMode(Banner.Mode.OFF);
            application.setLogStartupInfo(false);
            application.setDefaultProperties(Map.of(
                    "JWT_SECRET", springPropertySecret,
                    "app.auth.issuer", "jwt-config-test",
                    "app.auth.ttl-minutes", "30",
                    "logging.level.root", "OFF"));
            try (ConfigurableApplicationContext context = application.run("--JWT_SECRET=" + commandLineSecret)) {
                assertThat(context.getBean(SecretKey.class).getEncoded())
                        .containsExactly(Base64.getDecoder().decode(processSecret));
            }
        } finally {
            if (previousJwtSystemProperty == null) {
                System.clearProperty("JWT_SECRET");
            } else {
                System.setProperty("JWT_SECRET", previousJwtSystemProperty);
            }
        }
    }

    private byte[] filledBytes(byte value) {
        byte[] bytes = new byte[48];
        java.util.Arrays.fill(bytes, value);
        return bytes;
    }

    @Test
    void tokenLifetimeIsBounded() {
        assertThatThrownBy(() -> configuration.jwtLifetime(0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> configuration.jwtLifetime(61)).isInstanceOf(IllegalStateException.class);
    }

}

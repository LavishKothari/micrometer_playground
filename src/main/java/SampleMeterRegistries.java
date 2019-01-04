import com.netflix.spectator.atlas.AtlasConfig;
import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.lang.Nullable;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.time.Duration;

public class SampleMeterRegistries {

    private static final long DEFAULT_STEP_SIZE = 10; // in seconds

    private SampleMeterRegistries() {

    }

    public static LoggingMeterRegistry loggingMeterRegistry() {
        return new LoggingMeterRegistry(new LoggingRegistryConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(DEFAULT_STEP_SIZE);
            }
        }, Clock.SYSTEM);
    }

    public static JmxMeterRegistry jmxMeterRegistry() {
        return new JmxMeterRegistry(new JmxConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(DEFAULT_STEP_SIZE);
            }
        }, Clock.SYSTEM);
    }

    public static AtlasMeterRegistry atlasMeterRegistry() {
        return new AtlasMeterRegistry(new AtlasConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(DEFAULT_STEP_SIZE);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            @Nullable
            public String get(String k) {
                return null;
            }

            @Override
            public String uri() {
                return "http://localhost:7101";
            }
        }, Clock.SYSTEM);
    }
    public static PrometheusMeterRegistry prometheus() {
        return new PrometheusMeterRegistry(new PrometheusConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(DEFAULT_STEP_SIZE);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        });
    }
    public static SimpleMeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

}

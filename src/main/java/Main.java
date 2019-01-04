import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();

        LoggingMeterRegistry loggingMeterRegistry = SampleMeterRegistries.loggingMeterRegistry();
        JmxMeterRegistry jmxMeterRegistry = SampleMeterRegistries.jmxMeterRegistry();
//        AtlasMeterRegistry atlasMeterRegistry = SampleMeterRegistries.atlasMeterRegistry();
        PrometheusMeterRegistry prometheusMeterRegistry = SampleMeterRegistries.prometheus();

        compositeMeterRegistry.add(loggingMeterRegistry);
        compositeMeterRegistry.add(jmxMeterRegistry);
//        compositeMeterRegistry.add(atlasMeterRegistry);
        compositeMeterRegistry.add(prometheusMeterRegistry);

        exposeHTTPEndpointToPrometheus(prometheusMeterRegistry);

        AtomicInteger latencyForThisSecond = new AtomicInteger(0);
        Gauge gauge = Gauge.builder("my.gauge", latencyForThisSecond, n -> n.get())
                .register(compositeMeterRegistry);

        Counter counter = Counter
                .builder("my.counter")
                .description("some description")
                .tags("dev", "performance")
                .register(compositeMeterRegistry);

        Timer timer = Timer.builder("my.timer")
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(270))
                .register(compositeMeterRegistry);

        // colt/colt/1.2.0 is to be added for this.
        RandomEngine randomEngine = new MersenneTwister64(0);
        Normal incomingRequests = new Normal(0, 1, randomEngine);
        Normal duration = new Normal(250, 50, randomEngine);

        latencyForThisSecond.set(duration.nextInt());

        // For Flux you require io.projectreactor/reactor-core/3.2.3.RELEASE
        Flux.interval(Duration.ofSeconds(1))
                .doOnEach(d -> {
                    if (incomingRequests.nextDouble() + 0.4 > 0) {
                        timer.record(latencyForThisSecond.get(), TimeUnit.MILLISECONDS);
                        counter.increment();
                    }
                }).blockLast();

    }

    private static void exposeHTTPEndpointToPrometheus(PrometheusMeterRegistry prometheusMeterRegistry) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", httpExchange -> {
                String response = prometheusMeterRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

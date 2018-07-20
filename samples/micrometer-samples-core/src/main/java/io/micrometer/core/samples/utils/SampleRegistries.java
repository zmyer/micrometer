/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.samples.utils;

import com.netflix.spectator.atlas.AtlasConfig;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.lang.Nullable;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.dynatrace.DynatraceConfig;
import io.micrometer.dynatrace.DynatraceMeterRegistry;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import io.micrometer.ganglia.GangliaConfig;
import io.micrometer.ganglia.GangliaMeterRegistry;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.newrelic.NewRelicConfig;
import io.micrometer.newrelic.NewRelicMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.signalfx.SignalFxConfig;
import io.micrometer.signalfx.SignalFxMeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import io.micrometer.wavefront.WavefrontConfig;
import io.micrometer.wavefront.WavefrontMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

public class SampleRegistries {
    public static MeterRegistry pickOne() {
        throw new RuntimeException("Pick some other method on SampleRegistries to ship sample metrics to the system of your choice");
    }

    /**
     * To use pushgateway instead:
     * new PushGateway("localhost:9091").pushAdd(registry.getPrometheusRegistry(), "samples");
     *
     * @return A prometheus registry.
     */
    public static PrometheusMeterRegistry prometheus() {
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(new PrometheusConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        });

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });

            new Thread(server::start).run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return prometheusRegistry;
    }

    public static AtlasMeterRegistry atlas() {
        return new AtlasMeterRegistry(new AtlasConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static DatadogMeterRegistry datadog(String apiKey, String applicationKey) {
        DatadogConfig config = new DatadogConfig() {
            @Override
            public String apiKey() {
                return apiKey;
            }

            @Override
            public String applicationKey() {
                return applicationKey;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        };

        return new DatadogMeterRegistry(config, Clock.SYSTEM);
    }

    public static ElasticMeterRegistry elastic() {
        return new ElasticMeterRegistry(new ElasticConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static StatsdMeterRegistry datadogStatsd() {
        return new StatsdMeterRegistry(new StatsdConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }

            @Override
            public StatsdFlavor flavor() {
                return StatsdFlavor.DATADOG;
            }
        }, Clock.SYSTEM);
    }

    public static StatsdMeterRegistry telegrafStatsd() {
        return new StatsdMeterRegistry(new StatsdConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }

            @Override
            public StatsdFlavor flavor() {
                return StatsdFlavor.TELEGRAF;
            }
        }, Clock.SYSTEM);
    }

    public static StatsdMeterRegistry sysdigStatsd() {
        return new StatsdMeterRegistry(new StatsdConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }

            @Override
            public StatsdFlavor flavor() {
                return StatsdFlavor.SYSDIG;
            }
        }, Clock.SYSTEM);
    }

    public static GangliaMeterRegistry ganglia() {
        return new GangliaMeterRegistry(new GangliaConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static GraphiteMeterRegistry graphite() {
        return new GraphiteMeterRegistry(new GraphiteConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static JmxMeterRegistry jmx() {
        return new JmxMeterRegistry(new JmxConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static InfluxMeterRegistry influx() {
        return new InfluxMeterRegistry(new InfluxConfig() {
            @Override
            public String userName() {
                return "admin";
            }

            @Override
            public String password() {
                return "admin";
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static NewRelicMeterRegistry newRelic(String accountId, String apiKey) {
        return new NewRelicMeterRegistry(new NewRelicConfig() {
            @Override
            public String accountId() {
                return accountId;
            }

            @Override
            public String apiKey() {
                return apiKey;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static SignalFxMeterRegistry signalFx(String accessToken) {
        return new SignalFxMeterRegistry(new SignalFxConfig() {
            @Override
            public String accessToken() {
                return accessToken;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            @Nullable
            public String get(String k) {
                return null;
            }
        }, Clock.SYSTEM);
    }

    public static WavefrontMeterRegistry wavefront() {
        return new WavefrontMeterRegistry(WavefrontConfig.DEFAULT_PROXY, Clock.SYSTEM);
    }

    public static WavefrontMeterRegistry wavefrontDirect(String apiToken) {
        return new WavefrontMeterRegistry(new WavefrontConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String apiToken() {
                return apiToken;
            }

            @Override
            public String uri() {
                return "https://longboard.wavefront.com";
            }
        }, Clock.SYSTEM);
    }

    public static DynatraceMeterRegistry dynatrace(String apiToken, String uri) {
        return new DynatraceMeterRegistry(new DynatraceConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String apiToken() {
                return apiToken;
            }

            @Override
            public String uri() {
                return uri;
            }

            @Override
            public String deviceId() {
                return "sample";
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }
        }, Clock.SYSTEM);
    }
}

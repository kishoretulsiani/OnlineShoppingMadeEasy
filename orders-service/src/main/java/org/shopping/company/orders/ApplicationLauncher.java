

package org.shopping.company.orders;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.micrometer.backends.BackendRegistry;

public class ApplicationLauncher extends Launcher {

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        MicrometerMetricsOptions micrometerMetricsOptions = new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true)
                        .setStartEmbeddedServer(true)
                        .setEmbeddedServerOptions(new HttpServerOptions()
                                .setPort(3000)
                                .setSsl(false)
                                .setTcpKeepAlive(true))
                        .setEmbeddedServerEndpoint("/metrics"))
                .setEnabled(true);
        options.setMetricsOptions(micrometerMetricsOptions);
        BackendRegistry backendRegistry = BackendRegistries.setupBackend(micrometerMetricsOptions);
        MeterRegistry meterRegistry = backendRegistry.getMeterRegistry();
        //new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
    }

    public static void main(String[] args) {
        (new ApplicationLauncher()).dispatch(args);
    }

}

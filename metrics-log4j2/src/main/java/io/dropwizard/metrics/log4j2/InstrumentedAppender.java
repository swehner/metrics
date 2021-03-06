package io.dropwizard.metrics.log4j2;

import static io.dropwizard.metrics.MetricRegistry.name;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SharedMetricRegistries;

import java.io.Serializable;

/**
 * A Log4J 2.x {@link Appender} which has seven meters, one for each logging level and one for the total
 * number of statements being logged. The meter names are the logging level names appended to the
 * name of the appender.
 */
public class InstrumentedAppender extends AbstractAppender {
    private final MetricRegistry registry;

    private Meter all;
    private Meter trace;
    private Meter debug;
    private Meter info;
    private Meter warn;
    private Meter error;
    private Meter fatal;

    /**
     * Create a new instrumented appender using the given registry name.
     *
     * @param registryName the name of the registry in {@link SharedMetricRegistries}
     * @param filter The Filter to associate with the Appender.
     * @param layout The layout to use to format the event.
     * @param ignoreExceptions If true, exceptions will be logged and suppressed. If false errors will be
     * logged and then passed to the application.
     *
     */
    public InstrumentedAppender(String registryName, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        this(SharedMetricRegistries.getOrCreate(registryName), filter, layout, ignoreExceptions);
    }

    /**
     * Create a new instrumented appender using the given registry name.
     *
     * @param registryName the name of the registry in {@link SharedMetricRegistries}
     */
    public InstrumentedAppender(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * Create a new instrumented appender using the given registry.
     *
     * @param registry the metric registry
     */
    public InstrumentedAppender(MetricRegistry registry) {
        this(registry, null, null, true);
    }

    /**
     * Create a new instrumented appender using the given registry.
     *
     * @param registry the metric registry
     * @param filter The Filter to associate with the Appender.
     * @param layout The layout to use to format the event.
     * @param ignoreExceptions If true, exceptions will be logged and suppressed. If false errors will be
     * logged and then passed to the application.
     */
    public InstrumentedAppender(MetricRegistry registry, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(Appender.class.getName(), filter, layout, ignoreExceptions);
        this.registry = registry;
    }

    @Override
    public void start() {
        this.all = registry.meter(name(getName(), "all"));
        this.trace = registry.meter(name(getName(), "trace"));
        this.debug = registry.meter(name(getName(), "debug"));
        this.info = registry.meter(name(getName(), "info"));
        this.warn = registry.meter(name(getName(), "warn"));
        this.error = registry.meter(name(getName(), "error"));
        this.fatal = registry.meter(name(getName(), "fatal"));
        super.start();
    }

    @Override
    public void append(LogEvent event) {
        all.mark();
        switch (event.getLevel().getStandardLevel()) {
            case TRACE:
                trace.mark();
                break;
            case DEBUG:
                debug.mark();
                break;
            case INFO:
                info.mark();
                break;
            case WARN:
                warn.mark();
                break;
            case ERROR:
                error.mark();
                break;
            case FATAL:
                fatal.mark();
                break;
            default:
                break;
        }
    }
}

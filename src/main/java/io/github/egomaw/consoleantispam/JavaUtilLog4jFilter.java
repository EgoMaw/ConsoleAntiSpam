package io.github.egomaw.consoleantispam;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public final class JavaUtilLog4jFilter extends AbstractFilter implements Filter {
	public boolean isLoggable(@NotNull LogRecord record) {
		try {
			return !Main.shouldFilterMessage(record.getMessage());
		} catch (SerializationException e) {
			return false;
		}
	}

	public Result filter(@NotNull LogEvent event) {
		try {
			return Main.shouldFilterMessage("[" + event.getLoggerName() + "]: " + event.getMessage().getFormattedMessage()) ? Result.DENY : Result.NEUTRAL;
		} catch (SerializationException e) {
			return Result.NEUTRAL;
		}
	}
}

package io.github.egomaw.consoleantispam;


import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class Main implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger("consoleantispam");
    public static CommentedConfigurationNode CONFIG;

    @Override
    public void onPreLaunch() {
        try {
            Path path = Path.of("config", "consoleantispam.conf");
            var builder = HoconConfigurationLoader.builder().path(path).build();
            if (!path.toFile().exists()) {
                FileUtils.copyURLToFile(Objects.requireNonNull(getClass().getResource("/assets/consoleantispam/config")), path.toFile());
            }
            CONFIG = builder.load();
        } catch (Exception e) {
            LOGGER.error("An error occurred while loading the configuration: " + e.getMessage());
            LOGGER.error("Disabling mod...");
            return;
        }

        if (CONFIG.node("showinfo").getBoolean() && !CONFIG.node("phrases").empty()) {
            LOGGER.info("Logs containing the following phrases will be hidden:");
            try {
                var phrases = CONFIG.node("phrases").getList(String.class);
                assert phrases != null;
                for (var entry : phrases) LOGGER.info(entry);
            } catch (Exception e) {
                LOGGER.error("Failed to load messages");
            }

        }

        if (CONFIG.node("showinfo").getBoolean() && !CONFIG.node("regex").empty()) {
            LOGGER.info("Logs matching the following regex patterns will be hidden:");
            try {
                var regex = CONFIG.node("regex").getList(String.class);
                assert regex != null;
                for (var entry : regex) LOGGER.info(entry);
            } catch (Exception e) {
                LOGGER.error("Failed to load regex entries");
            }

        }

        final var filter = new JavaUtilLog4jFilter();
        System.setOut(new SystemPrintFilter(System.out));
        java.util.logging.Logger.getLogger("").setFilter(filter);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);
        var foundOffshootLog4jLoggers = new ArrayList<>();
        var logContext = (LoggerContext) LogManager.getContext(false);
        var map = logContext.getConfiguration().getLoggers();

        for (var logger : map.values()) {
            if (!foundOffshootLog4jLoggers.contains(logger)) {
                logger.addFilter(filter);
                foundOffshootLog4jLoggers.add(logger);
            }
        }
    }

    public static boolean shouldFilterMessage(String message) throws SerializationException {
        var stringIterator = Objects.requireNonNull(CONFIG.node("phrases").getList(String.class)).iterator();
        String phrase;

        var regexIterator = Objects.requireNonNull(CONFIG.node("regex").getList(String.class)).iterator();
        String regex;

        do {
            if (!stringIterator.hasNext()) {
                do {
                    if (!regexIterator.hasNext()) return false;
                    regex = regexIterator.next();
                } while (!message.matches(regex));
                return true;
            }
            phrase = stringIterator.next();
        } while (!message.contains(phrase));
        return true;
    }
}

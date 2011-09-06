/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.log4j;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.helpers.NameUtil;
import org.apache.logging.log4j.message.LocalizedMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;

import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 *
 */
public class Category {
    private static final Map<LoggerContext, ConcurrentMap<String, Logger>> contextMap =
        new WeakHashMap<LoggerContext, ConcurrentMap<String, Logger>>();

    private final org.apache.logging.log4j.core.Logger logger;

    private static final CategoryFactory FACTORY = new CategoryFactory();

    private static final String FQCN = Category.class.getName();

    protected ResourceBundle bundle = null;

    private static org.apache.log4j.LoggerFactory loggerFactory = new PrivateFactory();

    protected Category(LoggerContext context, String name) {
        this.logger = context.getLogger(getFactory(), name);
    }

    protected Category(String name) {
        this((LoggerContext)PrivateManager.getContext(), name);
    }

    private Category(org.apache.logging.log4j.core.Logger logger) {
        this.logger = logger;
    }

    public static Category getInstance(String name) {
        return getInstance((LoggerContext)PrivateManager.getContext(), name, loggerFactory);
    }

    static Category getInstance(LoggerContext context, String name) {
        return getInstance(context, name, loggerFactory);
    }

    static Category getInstance(LoggerContext context, String name, org.apache.log4j.LoggerFactory factory) {
        ConcurrentMap<String, Logger> loggers = getLoggersMap(context);
        Logger logger = loggers.get(name);
        if (logger != null) {
            return logger;
        }
        logger = factory.makeNewLoggerInstance(context, name);
        Logger prev = loggers.putIfAbsent(name, logger);
        return prev == null ? logger : prev;
    }

    public static Category getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }


    static Category getInstance(LoggerContext context, Class clazz) {
        return getInstance(context, clazz.getName());
    }

    public final String getName() {
        return logger.getName();
    }

    org.apache.logging.log4j.core.Logger getLogger() {
        return logger;
    }

    public final Category getParent() {
        org.apache.logging.log4j.core.Logger parent = logger.getParent();
        if (parent == null) {
            return null;
        }
        ConcurrentMap<String, Logger> loggers = getLoggersMap(logger.getContext());
        Logger l = loggers.get(parent.getName());
        return l == null ? new Category(parent) : l;
    }

    public static Category getRoot() {
        return getInstance("");
    }


    static Category getRoot(LoggerContext context) {
        return getInstance(context, "");
    }

    private static ConcurrentMap<String, Logger> getLoggersMap(LoggerContext context) {
        synchronized (contextMap) {
            ConcurrentMap<String, Logger> map = contextMap.get(context);
            if (map == null) {
                map = new ConcurrentHashMap<String, Logger>();
                contextMap.put(context, map);
            }
            return map;
        }
    }

    public final Level getEffectiveLevel() {
        org.apache.logging.log4j.Level level = logger.getLevel();

        switch (level) {
            case TRACE:
                return Level.TRACE;
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            default:
                return Level.ERROR;
        }
    }

    public final Priority getChainedPriority() {
        return getEffectiveLevel();
    }

    public final Level getLevel() {
        return getEffectiveLevel();
    }

    public void setLevel(Level level) {
        logger.setLevel(org.apache.logging.log4j.Level.toLevel(level.levelStr));
    }

    public final Level getPriority() {
        return getEffectiveLevel();
    }

    public void setPriority(Priority priority) {
        logger.setLevel(org.apache.logging.log4j.Level.toLevel(priority.levelStr));
    }

    public void debug(Object message) {
        logger.debug(message);
    }

    public void debug(Object message, Throwable t) {
        logger.debug(message, t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void error(Object message) {
        logger.error(message);
    }

    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void warn(Object message) {
        logger.warn(message);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void fatal(Object message) {
        logger.fatal(message);
    }

    public void fatal(Object message, Throwable t) {
        logger.fatal(message, t);
    }

    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    public void info(Object message) {
        logger.info(message);
    }

    public void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void trace(Object message) {
        logger.trace(message);
    }

    public void trace(Object message, Throwable t) {
        logger.trace(message, t);
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isEnabledFor(Priority level) {
        org.apache.logging.log4j.Level lvl = org.apache.logging.log4j.Level.toLevel(level.toString());
        return ((CategoryFactory.CategoryLogger) logger).isEnabledFor(lvl);
    }

    public void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
        org.apache.logging.log4j.Level lvl = org.apache.logging.log4j.Level.toLevel(level.toString());
        ((CategoryFactory.CategoryLogger) logger).log(null, fqcn, lvl, new ObjectMessage(message), t);
    }

    public boolean exists(String name) {
        return PrivateManager.getContext().hasLogger(name);
    }

    public boolean getAdditivity() {
        return logger.isAdditive();
    }

    public void setAdditivity(boolean additivity) {
        logger.setAdditive(additivity);
    }

    public void setResourceBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public ResourceBundle getResourceBundle() {
        if (bundle != null) {
            return bundle;
        }
        int i = 0;
        String name = logger.getName();
        ConcurrentMap<String, Logger> loggers = getLoggersMap(logger.getContext());
        while ((name = NameUtil.getSubName(name)) != null) {
            if (loggers.containsKey(name)) {
                ResourceBundle rb = loggers.get(name).bundle;
                if (rb != null) {
                    return rb;
                }
            }
        }
        return null;
    }

    public void l7dlog(Priority priority, String key, Throwable t) {
        if (isEnabledFor(priority)) {
            Message msg = new LocalizedMessage(bundle, key, null);
            forcedLog(FQCN, priority, msg, t);
        }
    }

    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
        if (isEnabledFor(priority)) {
            Message msg = new LocalizedMessage(bundle, key, params);
            forcedLog(FQCN, priority, msg, t);
        }
    }

    protected org.apache.logging.log4j.spi.LoggerFactory getFactory() {
        return FACTORY;
    }

    private static class PrivateFactory implements org.apache.log4j.LoggerFactory {

        public Logger makeNewLoggerInstance(LoggerContext context, String name) {
            return new Logger(context, name);
        }
    }

    private static class PrivateManager extends org.apache.logging.log4j.LogManager {
        private static final String FQCN = Category.class.getName();

        public static org.apache.logging.log4j.spi.LoggerContext getContext() {
            return getContext(FQCN, false);
        }

        public static org.apache.logging.log4j.Logger getLogger(String name) {
            return getLogger(FQCN, name);
        }
    }

    private static class CategoryFactory implements org.apache.logging.log4j.spi.LoggerFactory<LoggerContext> {

        public org.apache.logging.log4j.core.Logger newInstance(LoggerContext ctx, String name) {
            return new CategoryLogger(ctx, name);
        }

        public class CategoryLogger extends org.apache.logging.log4j.core.Logger {

            public CategoryLogger(LoggerContext ctx, String name) {
                super(ctx, name);
            }

            @Override
            public String getFQCN() {
               return FQCN;
            }

            @Override
            public void log(Marker marker, String fqcn, org.apache.logging.log4j.Level level,
                               Message data, Throwable t) {
                super.log(marker, fqcn, level, data, t);
            }

            public boolean isEnabledFor(org.apache.logging.log4j.Level level) {
                return isEnabled(level, null, null);
            }
        }
    }
}
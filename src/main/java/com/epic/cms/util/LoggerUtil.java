package com.epic.cms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logging utility for the CMS application.
 * Provides convenient methods for logging at different levels across separate log files.
 * 
 * Log Files:
 * - error.log: Contains only ERROR level logs
 * - warn.log: Contains only WARN level logs
 * - system.log: Contains INFO and above level logs (general application logs)
 */
public class LoggerUtil {
    
    /**
     * Get a logger for a specific class.
     * 
     * @param clazz The class requesting the logger
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Log an informational message (goes to system.log).
     * 
     * @param clazz The class logging the message
     * @param message The message to log
     */
    public static void info(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.info(message);
    }
    
    /**
     * Log an informational message with parameters (goes to system.log).
     * 
     * @param clazz The class logging the message
     * @param message The message template
     * @param args Message parameters
     */
    public static void info(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.info(message, args);
    }
    
    /**
     * Log a warning message (goes to warn.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The warning message
     */
    public static void warn(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.warn(message);
    }
    
    /**
     * Log a warning message with parameters (goes to warn.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The warning message template
     * @param args Message parameters
     */
    public static void warn(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.warn(message, args);
    }
    
    /**
     * Log a warning with exception (goes to warn.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The warning message
     * @param throwable The exception
     */
    public static void warn(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = getLogger(clazz);
        logger.warn(message, throwable);
    }
    
    /**
     * Log an error message (goes to error.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The error message
     */
    public static void error(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.error(message);
    }
    
    /**
     * Log an error message with parameters (goes to error.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The error message template
     * @param args Message parameters
     */
    public static void error(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.error(message, args);
    }
    
    /**
     * Log an error with exception (goes to error.log and system.log).
     * 
     * @param clazz The class logging the message
     * @param message The error message
     * @param throwable The exception
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = getLogger(clazz);
        logger.error(message, throwable);
    }
    
    /**
     * Log a debug message (only visible in DEBUG mode).
     * 
     * @param clazz The class logging the message
     * @param message The debug message
     */
    public static void debug(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.debug(message);
    }
    
    /**
     * Log a debug message with parameters (only visible in DEBUG mode).
     * 
     * @param clazz The class logging the message
     * @param message The debug message template
     * @param args Message parameters
     */
    public static void debug(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.debug(message, args);
    }
    
    /**
     * Log a trace message (only visible in TRACE mode).
     * 
     * @param clazz The class logging the message
     * @param message The trace message
     */
    public static void trace(Class<?> clazz, String message) {
        Logger logger = getLogger(clazz);
        logger.trace(message);
    }
    
    /**
     * Log a trace message with parameters (only visible in TRACE mode).
     * 
     * @param clazz The class logging the message
     * @param message The trace message template
     * @param args Message parameters
     */
    public static void trace(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.trace(message, args);
    }
}

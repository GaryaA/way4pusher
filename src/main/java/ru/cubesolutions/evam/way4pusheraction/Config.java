package ru.cubesolutions.evam.way4pusheraction;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Garya on 09.02.2018.
 */
public class Config {

    private final static Logger log = Logger.getLogger(Config.class);
    public final static String OUTPUT_FILE_NAME_PREFIX = "IIC_Documents_00010001";

    public final static String RABBITMQ_HOST;
    public final static int RABBITMQ_PORT;
    public final static String RABBITMQ_VHOST;
    public final static String RABBITMQ_USER;
    public final static String RABBITMQ_PASSWORD;
    public final static String RABBITMQ_QUEUE;
    public final static String JDBC_DRIVER;
    public final static String JDBC_URL;
    public final static String JDBC_USER;
    public final static String JDBC_PASSWORD;

    public final static String SMB_USER;
    public final static String SMB_PASSWORD;
    public final static String SMB_OUTPUT_PATH;
    public final static String SMP_DOMAIN;

    public final static String LOCAL_OUTPUT_FILE_PATH;

    public final static int TIME_READING_FOR_ONE_FILE_IN_SECONDS;
    public final static int TIME_PAUSE_BETWEEN_TASKS_IN_SECONDS;
    public final static int MAX_MESSAGES_IN_ONE_FILE;


    static {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream("way4pusher.properties")) {
            props.load(is);
            PropertyConfigurator.configure("log4j.properties");
        } catch (FileNotFoundException e) {
            try (InputStream input = Config.class.getResourceAsStream("/way4pusher.properties")) {
                PropertyConfigurator.configure(Config.class.getResourceAsStream("/log4j.properties"));
                props.load(input);
            } catch (Throwable t) {
                log.error("File config way4pusher.properties not found", e);
                System.out.println("File config way4pusher.properties not found");
                throw new RuntimeException(t);
            }
        } catch (Throwable t) {
            log.error("File config way4pusher.properties not found", t);
            System.out.println("File config way4pusher.properties not found");
            throw new RuntimeException(t);
        }
        RABBITMQ_HOST = props.getProperty("rabbitmq-host");
        RABBITMQ_PORT = Integer.parseInt(props.getProperty("rabbitmq-port"));
        RABBITMQ_VHOST = props.getProperty("rabbitmq-v-host");
        RABBITMQ_USER = props.getProperty("rabbitmq-user");
        RABBITMQ_PASSWORD = props.getProperty("rabbitmq-password");
        RABBITMQ_QUEUE = props.getProperty("rabbitmq-queue");

        JDBC_DRIVER = props.getProperty("jdbc-driver");
        JDBC_URL = props.getProperty("jdbc-url");
        JDBC_USER = props.getProperty("jdbc-user");
        JDBC_PASSWORD = props.getProperty("jdbc-password");

        SMB_USER = props.getProperty("smb-user");
        SMB_PASSWORD = props.getProperty("smb-password");
        SMB_OUTPUT_PATH = props.getProperty("smb-output-path");
        SMP_DOMAIN = props.getProperty("smb-domain");

        LOCAL_OUTPUT_FILE_PATH = props.getProperty("local-output-path");

        TIME_READING_FOR_ONE_FILE_IN_SECONDS = Integer.parseInt(props.getProperty("time-reading-queue-for-one-file-in-seconds"));
        TIME_PAUSE_BETWEEN_TASKS_IN_SECONDS = Integer.parseInt(props.getProperty("time-pause-between-tasks-in-seconds"));
        MAX_MESSAGES_IN_ONE_FILE = Integer.parseInt(props.getProperty("max-messages-in-one-file"));
    }


}

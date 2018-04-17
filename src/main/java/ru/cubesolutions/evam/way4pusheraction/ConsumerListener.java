package ru.cubesolutions.evam.way4pusheraction;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * Created by Garya on 09.02.2018.
 */
public class ConsumerListener extends DefaultConsumer {

    private final static Logger log = Logger.getLogger(ConsumerListener.class);
    private static int counter = 0;

    private final static Map<Long, String> docsBlocksWithDeliveryTags = new ConcurrentHashMap<>();

    private Lock lock;

    public ConsumerListener(Channel channel, Lock lock) {
        super(channel);
        this.lock = lock;
    }


    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String doc = new String(body, "UTF-8");
        docsBlocksWithDeliveryTags.put(envelope.getDeliveryTag(), doc);
        ++counter;
        log.debug("delivery tag:" + envelope.getDeliveryTag());
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        super.handleConsumeOk(consumerTag);
        log.info("Queue listening started, wait...");
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        lock.lock();
        try {
            try {
                log.debug(docsBlocksWithDeliveryTags.size() + " messages are consumed");
                if (!docsBlocksWithDeliveryTags.isEmpty()) {
                    Way4DocFileCreator.createWay4File(new ArrayList<>(docsBlocksWithDeliveryTags.values()), FileSequences.INSTANCE.getFileSeqNumber(), FileSequences.INSTANCE.getReceiver());
                    acknowledge();
                    FileSequences.INSTANCE.persist();
                } else {
                    log.info("0 messages");
                }
            } catch (IOException e) {
                log.error("Can't write file", e);
                negateAcknowledge();
            }
            log.info("Queue listening stoppped");
            super.handleCancelOk(consumerTag);
        } finally {
            lock.unlock();
        }
    }

    private synchronized void acknowledge() {
        try {
            acknowledge(4, 0);
        } catch (IOException e) {
            log.error("Internal error, can't acknowledge input docs", e);
            throw new RuntimeException(e);
        }
    }

    private synchronized void acknowledge(int attempts, int currentAttempt) throws IOException {
        try {
            log.debug("Acknowledgement: " + docsBlocksWithDeliveryTags.size() + " messages");
            for (Long tag : docsBlocksWithDeliveryTags.keySet()) {
                log.debug("tag to acknowledge: " + tag);
                this.getChannel().basicAck(tag, true);
                log.debug("success");
            }
            log.debug("Acknowledged " + docsBlocksWithDeliveryTags.size() + " messages");
            docsBlocksWithDeliveryTags.clear();
        } catch (IOException e) {
            ++currentAttempt;
            log.error("Can't acknowledge input docs, try " + currentAttempt, e);
            if (currentAttempt > attempts) {
                throw e;
            }
            acknowledge(attempts, currentAttempt);
        }
    }

    private synchronized void negateAcknowledge() {
        try {
            negateAcknowledge(4, 0);
        } catch (IOException e) {
            log.error("Internal error, can't restore input docs", e);
            throw new RuntimeException(e);
        }
    }

    private synchronized void negateAcknowledge(int attempts, int currentAttempt) throws IOException {
        try {
            for (Long tag : docsBlocksWithDeliveryTags.keySet()) {
                this.getChannel().basicNack(tag, true, true);
            }
            log.info("Restored " + docsBlocksWithDeliveryTags.size() + " messages");
            docsBlocksWithDeliveryTags.clear();
        } catch (IOException e) {
            ++currentAttempt;
            log.error("Can't restore input docs, try " + currentAttempt, e);
            if (currentAttempt > attempts) {
                throw e;
            }
            negateAcknowledge(attempts, currentAttempt);
        }
    }

}

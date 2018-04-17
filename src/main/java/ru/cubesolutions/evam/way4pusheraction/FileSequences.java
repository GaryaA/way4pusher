package ru.cubesolutions.evam.way4pusheraction;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Garya on 10.02.2018.
 */
public class FileSequences {

    private final static Logger log = Logger.getLogger(FileSequences.class);

    public final static FileSequences INSTANCE = new FileSequences();

    private AtomicInteger fileSeqNumber;
    private Long fileSeqNumberlastUpdate;
    private AtomicInteger receiver;
    private Long receiverLastUpdate;

    private FileSequences() {
        int fileSeqNumber = 0;
        int receiver = 10000;
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("select file_seq_number, " +
                    "file_seq_number_last_update, " +
                    "receiver, " +
                    "receiver_last_update " +
                    "from way4file_counters " +
                    "order by file_seq_number_last_update desc limit 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int fileSeqNumberDb = rs.getInt(1);
                long fileSeqNumberlastUpdate = rs.getLong(2);
                int receiverDb = rs.getInt(3);
                long receiverLastUpdate = rs.getLong(4);
                if (LocalDate.now().equals(dateFromMillis(fileSeqNumberlastUpdate))) {
                    fileSeqNumber = fileSeqNumberDb;
                }
                if (LocalDate.now().equals(dateFromMillis(receiverLastUpdate))) {
                    receiver = receiverDb;
                }
            }
        } catch (Exception e) {
            System.out.println("Can't read fileSeqNumber and receiver from db");
            log.error("Can't read fileSeqNumber and receiver from db", e);
            throw new RuntimeException("Can't read fileSeqNumber and receiver from db", e);
        }
        this.fileSeqNumber = new AtomicInteger(fileSeqNumber);
        this.receiver = new AtomicInteger(receiver);
        this.fileSeqNumberlastUpdate = System.currentTimeMillis();
        this.receiverLastUpdate = System.currentTimeMillis();
    }

    public int getFileSeqNumber() {
        if (!LocalDate.now().equals(dateFromMillis(fileSeqNumberlastUpdate))) {
            fileSeqNumber.set(0);
        }
        fileSeqNumberlastUpdate = System.currentTimeMillis();
        return fileSeqNumber.incrementAndGet();
    }

    public int getReceiver() {
        if (!LocalDate.now().equals(dateFromMillis(receiverLastUpdate))) {
            receiver.set(10000);
        }
        receiverLastUpdate = System.currentTimeMillis();
        return receiver.decrementAndGet();
    }

    private static LocalDate dateFromMillis(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public synchronized void persist() {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement("insert into way4file_counters(file_seq_number," +
                    "file_seq_number_last_update," +
                    "receiver," +
                    "receiver_last_update) " +
                    "VALUES (?,?,?,?)");
            ps.setInt(1, this.fileSeqNumber.get());
            ps.setLong(2, this.fileSeqNumberlastUpdate);
            ps.setInt(3, this.receiver.get());
            ps.setLong(4, this.receiverLastUpdate);
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("Can't persist fileSeqNumber and receiver to db", e);
            throw new RuntimeException("Can't persist fileSeqNumber and receiver to db", e);
        }
    }

}

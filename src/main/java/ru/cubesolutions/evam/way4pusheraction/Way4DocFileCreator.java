package ru.cubesolutions.evam.way4pusheraction;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import static ru.cubesolutions.evam.way4pusheraction.XmlStructureHelper.*;

public class Way4DocFileCreator {

    private final static Logger log = Logger.getLogger(Way4DocFileCreator.class);

    private static String generateDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String generateTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private static String createFileHeader(int fileSeqNumber, int receiver) {
        return String.format(fileHeader(),
                generateDate(),
                generateTime(),
                "" + fileSeqNumber,
                "" + receiver
        );
    }

    private static String createDocList(String docsBlocks) {
        return String.format(docList(), docsBlocks);
    }

    private static String createFileTrailer(int recsCount, BigDecimal hashTotalAmount) {
        return String.format(fileTrailer(),
                "" + recsCount,
                hashTotalAmount.toString()
        );
    }

    private static String createWay4FileContent(List<String> docs, int fileSeqNumber, int receiver) {
        if (docs == null || docs.isEmpty()) {
            return null;
        }
        int recsCount = docs.size();
        BigDecimal hashTotalAmount = BigDecimal.ZERO;

        StringBuilder docFileContent = new StringBuilder();
        StringBuilder docsBlocks = new StringBuilder();

        for (String doc : docs) {
            doc = new String(Base64.getDecoder().decode(doc), Charset.forName("UTF-8"));
            docsBlocks.append(doc);
            BigDecimal amount = new BigDecimal(doc.substring(doc.indexOf("<Amount>") + 8, doc.indexOf("</Amount>")).replace(",", "."));
            hashTotalAmount = hashTotalAmount.add(amount);
        }

        docFileContent.append(createFileHeader(fileSeqNumber, receiver))
                .append(createDocList(docsBlocks.toString()))
                .append(createFileTrailer(recsCount, hashTotalAmount));
        return String.format(allContent(), docFileContent.toString());
    }

    public static void createWay4File(List<String> docs, int fileSeqNumber, int receiver) throws IOException {
        if (docs == null || docs.isEmpty()) {
            log.info("Didn't consume any messages...");
            return;
        }
        String content = createWay4FileContent(docs, fileSeqNumber, receiver);
        String name;
        if (Config.SMB_OUTPUT_PATH == null || Config.SMB_OUTPUT_PATH.isEmpty()) {
            name = Config.LOCAL_OUTPUT_FILE_PATH
                    + (Config.LOCAL_OUTPUT_FILE_PATH.endsWith(File.separator) ? "" : File.separator)
                    + Config.OUTPUT_FILE_NAME_PREFIX
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyddMM")) + "_"
                    + receiver
                    + ".xml";
            if (Files.notExists(Paths.get(name).getParent())) {
                Files.createDirectories(Paths.get(name).getParent());
            }
            Files.write(Paths.get(name), content.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
        } else {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(Config.SMP_DOMAIN, Config.SMB_USER, Config.SMB_PASSWORD);
            name = Config.SMB_OUTPUT_PATH
                    + (Config.SMB_OUTPUT_PATH.endsWith(File.separator) ? "" : File.separator)
                    + Config.OUTPUT_FILE_NAME_PREFIX
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyddMM")) + "_"
                    + receiver
                    + ".xml";
            SmbFile sFile = new SmbFile(name, auth);
            try (SmbFileOutputStream sfos = new SmbFileOutputStream(sFile)) {
                sfos.write(content.getBytes(Charset.forName("UTF-8")));
                sfos.flush();
            }
        }
        log.info("file with name " + name + " and " + docs.size() + " records created");
    }

}

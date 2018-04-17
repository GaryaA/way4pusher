package ru.cubesolutions.evam.way4pusheraction;

/**
 * Created by Garya on 09.02.2018.
 */
public class XmlStructureHelper {

    public static String allContent() {
        return "<DocFile>\n" +
                "%s\n" +
                "</DocFile>";
    }

    public static String fileHeader() {
        return "<FileHeader>\n" +
                "        <FileLabel>PAYMENT</FileLabel>\n" +
                "        <FormatVersion>2.1</FormatVersion>\n" +
                "        <Sender>PAY0001</Sender>\n" +
                "        <CreationDate>%s</CreationDate>\n" +
                "        <CreationTime>%s</CreationTime>\n" +
                "        <FileSeqNumber>%s</FileSeqNumber>\n" +
                "        <Receiver>%s</Receiver>\n" +
                "    </FileHeader>\n";
    }

    public static String docList() {
        return "<DocList>\n" +
                "%s\n" +
                "</DocList>";
    }

    public static String fileTrailer() {
        return "<FileTrailer>\n" +
                "        <CheckSum>\n" +
                "            <RecsCount>%s</RecsCount>\n" +
                "            <HashTotalAmount>%s</HashTotalAmount>\n" +
                "        </CheckSum>\n" +
                "    </FileTrailer>";
    }

}

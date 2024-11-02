package serverLogic.dataStorage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DumpReader {
    // reads redis .rdb file, based on https://rdb.fnordig.de/file_format.html

    private DumpReader(){}

    public static DataStorage readRdbData(String filePath){
        DataStorage rdbData = new DataStorage();

        System.out.println("Trying to read dump from file: " + filePath);

        try (FileInputStream fis = new FileInputStream(filePath)) {

            // first 5 bytes should be string = "REDIS"
            byte[] magicStrArr = fis.readNBytes(5);
            System.out.println("magic string = " + new String(magicStrArr));

            // next 4 bytes represent redis version 
            byte[] versionArr = fis.readNBytes(4);
            System.out.println("version = " + new String(versionArr));

            // sections 
            // 0xFA - AUX - Auxiliary fields - key value pairs encoded as strings. Followed by 2 redis strings.
            // 0xFE - database selector - variable length field indicates db number.
            // 0xFB - resize section - followed by 2 length encoded integers indicating Database hash table size and Expiry hash table size.
            // 0xFD - expire time in seconds - The following 4 bytes represent the Unix timestamp as an unsigned integer.
            // 0xFE - expire time in miliseconds - The following 8 bytes represent the Unix timestamp as an unsigned long.

            int b;
            // move through header sections
            while ((b = fis.read()) != -1) {
                if (b == 0xFA) {
                    System.out.println("aux section");
                }

                if (b == 0xFE) {
                    System.out.println("database selector section");
                    int dbIdx = DumpReader.getStrLength(fis.read(), fis);
                    System.out.println("db index = " + dbIdx);
                }

                if (b == 0xFB) {
                    System.out.println("resize db section");
                    
                    int dbSize = DumpReader.getStrLength(fis.read(), fis); //fis.read();
                    System.out.println("db size = " + dbSize);

                    int expSize = DumpReader.getStrLength(fis.read(), fis); //fis.read();
                    System.out.println("exp size = " + expSize);

                    break; // done with header, move on to reading key-value pairs
                }

                if (b == 0xFD) {
                    System.out.println("expire time in seconds");
                }

                if (b == 0xFC) {
                    System.out.println("expire time in miliseconds");
                }

                if (b == 0xFF) {
                    System.out.println("EOF");
                }
            }

            // read key-value pairs
            while ((b = fis.read()) != -1) {

                LocalDateTime expTime = null;
                long milisToExpire = 0;

                if (b == 0xFE) {
                    System.out.println("database selector section");
                    break;
                }

                if (b == 0xFF) {
                    System.out.println("EOF");
                    break;
                }

                if (b == 0xFD) {
                    System.out.println("expire time in seconds");
                    int expSeconds = ByteBuffer.wrap(fis.readNBytes(4)).getInt();
                    System.out.println("expSeconds = " + expSeconds);

                    expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(expSeconds), ZoneId.of("UTC"));

                    milisToExpire = Duration.between(LocalDateTime.now(), expTime).toMillis();

                    // read value type flag
                    b = fis.read();
                }

                if (b == 0xFC) {
                    System.out.println("expire time in miliseconds");

                    byte[] expTimeBarr = fis.readNBytes(8);

                    ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.put(expTimeBarr);
                    bb.flip();
                    long expMiliseconds = bb.getLong();
                    //System.out.println("expMiliseconds = " + expMiliseconds);

                    expTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(expMiliseconds), ZoneId.of("UTC"));
                    //System.out.println("expiration time = " + expTime);

                    milisToExpire = Duration.between(LocalDateTime.now(), expTime).toMillis();

                    // read value type flag
                    b = fis.read();
                }

                // otherwise b is 1 byte flag indicating encoding used to save value
                // 0 = String Encoding
                // 1 = List Encoding
                // 2 = Set Encoding
                // 3 = Sorted Set Encoding
                // 4 = Hash Encoding
                // 9 = Zipmap Encoding
                // 10 = Ziplist Encoding
                // 11 = Intset Encoding
                // 12 = Sorted Set in Ziplist Encoding
                // 13 = Hashmap in Ziplist Encoding (Introduced in RDB version 4)
                // 14 = List in Quicklist encoding (Introduced in RDB version 7)

                System.out.println("value type flag = " + b);

                if (b != 0) {
                    System.out.println("unsupported value encoding");
                    return null;
                }

                int keyLength = DumpReader.getStrLength(fis.read(), fis);
                byte[] keyBarr = fis.readNBytes(keyLength);
                String keyStr = new String(keyBarr);

                int valLength = DumpReader.getStrLength(fis.read(), fis);
                byte[] valBarr = fis.readNBytes(valLength);
                String valStr = new String(valBarr);
                System.out.println("key = " + keyStr + " value = " + new String(valStr));

                if (expTime != null) {
                    System.out.println("expiration time = " + expTime);
                }

                if (milisToExpire >= 0) {
                    // expired keys should not be imported from rdb files

                    //rdbData.put(keyStr, valStr);
                    rdbData.addToStorage(keyStr, valStr, milisToExpire);
                }

            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("");
            System.out.println("Error reading dump file, application will start as empty database.");
        }

        return rdbData;
    }

    private static int getStrLength(int lengthByte, FileInputStream fis) throws IOException{

        // length encoding
        // first 2 bits inform us how to decode length
        // 00 - next 6 bits represent length
        // 01 - read additional byte, combined 14 bits represent length
        // 10 - discard next 6 bits, read 4 bytes which represent length
        // 11 - special format, remaining 6 bits indicate the format

        int first2bits = lengthByte & 11000000;

        //System.out.println("lengthByte = " + lengthByte); // LOG
        //System.out.println("first2bits = " + first2bits); // LOG

        if (first2bits == 0) {
            // 00
            return lengthByte;
        }

        if (first2bits == 64) {
            // 01
            int lengthByte1x = lengthByte & 0b00111111; // discard first 2 bits
            lengthByte1x = lengthByte1x << 8; // shift bits 8 positions to the left

            int lengthByte2 = fis.read(); // read second byte

            return lengthByte1x + lengthByte2;
        }

        if (first2bits == 128) {
            // 10
            byte[] arr = fis.readNBytes(4);
            ByteBuffer bb = ByteBuffer.wrap(arr);
            return bb.getInt();
        }

        if (first2bits == 192) {
            // 11

            //read remaining 6 bits
            // 0 indicates that an 8 bit integer follows
            // 1 indicates that a 16 bit integer follows
            // 2 indicates that a 32 bit integer follows
            int remaining6Bits = lengthByte & 0b00111111;

            //System.out.println("remaining 6 bits = " + remaining6Bits);  // LOG

            if (remaining6Bits == 0) {
                return 1;
            }

            if (remaining6Bits == 1) {
                return 2;
            }

            if (remaining6Bits == 2) {
                return 4;
            }

            System.out.println("encountered string in special format - unsupported");
            
        }

        return 0;
    }
}

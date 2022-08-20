package UnWrap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.InflaterInputStream;

public class UnWrap {

    private static final String HEX_FROM = "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F" +
            "202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F" +
            "404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F" +
            "606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F" +
            "808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9F" +
            "A0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF" +
            "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF" +
            "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF";
    private static final String HEX_TO = "3D6585B318DBE287F152AB634BB5A05F7D687B9B24C228678ADEA4261E03EB17" +
            "6F343E7A3FD2A96A0FE935561FB14D1078D975F6BC4104816106F9ADD6D5297E" +
            "869E79E505BA84CC6E278EB05DA8F39FD0A271B858DD2C38994C480755E4538C" +
            "46B62DA5AF322240DC50C3A1258B9C16605CCFFD0C981CD4376D3C3A30E86C31" +
            "47F533DA43C8E35E1994ECE6A39514E09D64FA5915C52FCABB0BDFF297BF0A76" +
            "B449445A1DF0009621807F1A82394FC1A7D70DD1D8FF139370EE5BEFBE09B977" +
            "72E7B254B72AC7739066200E51EDF87C8F2EF412C62B83CDACCB3BC44EC06936" +
            "6202AE88FCAA4208A64557D39ABDE1238D924A1189746B91FBFEC901EA1BF7CE";

    public static String Inflate(byte[] src) {
        try {
            InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(src));
            StringBuilder result = new StringBuilder();
            for (int c = iis.read(); c != -1; c = iis.read())
                result.append((char) c);
            return new String(result.toString().getBytes(StandardCharsets.ISO_8859_1), "cp1251");
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("-= UnWrap by Malgo =-");
        File[] filesList = new File(".").listFiles();
        String pattern = args.length > 0 ? args[0] : ".*\\.pbl";
        assert filesList != null;
        for (File file : filesList) {
            if (file.isFile() && file.getName().matches(pattern)) {
                Scanner fileScanner = new Scanner(new FileInputStream(file.getName()));
                BufferedWriter bw = Files.newBufferedWriter(Paths.get(file.getName() + ".sql"));

                while (fileScanner.hasNextLine()) {
                    String str = fileScanner.nextLine();
                    String NEW_LINE = "" + (char) 13 + (char) 10;
                    if (str.equals("a000000")) {
                        StringBuilder wrapText = new StringBuilder();
                        for (int i = 0; i < 18; i++)
                            str = fileScanner.nextLine();
                        int base64Length = Integer.parseInt(str.split(" ")[1], 16);
                        while (base64Length > 0) {
                            str = fileScanner.nextLine();
                            wrapText.append(str);
                            base64Length -= str.length() + 1;
                        }

                        byte[] base64Decode = Base64.getDecoder().decode(wrapText.toString().getBytes());
                        byte[] base64DecodeWithoutSHA1 = new byte[base64Decode.length - 20];
                        System.arraycopy(base64Decode, 20, base64DecodeWithoutSHA1, 0, base64Decode.length - 20);
                        byte[] translate = new byte[base64DecodeWithoutSHA1.length];
                        for (int i = 0; i < base64DecodeWithoutSHA1.length; i++) {
                            String hex = String.format("%02X", base64DecodeWithoutSHA1[i]);

                            for (int j = 0; j < HEX_FROM.length() / 2; j++) {
                                if (hex.equals(HEX_FROM.substring(j * 2, j * 2 + 2))) {
                                    hex = HEX_TO.substring(j * 2, j * 2 + 2);
                                    break;
                                }
                            }
                            translate[i] = (byte) ((Character.digit(hex.charAt(0), 16) << 4) + Character.digit(hex.charAt(1), 16));
                        }
                        bw.write(Inflate(translate) + NEW_LINE);
                    } else {
                        bw.write(str.replace("wrapped", "") + NEW_LINE);
                    }
                    bw.write(NEW_LINE);
                }
                fileScanner.close();
                bw.flush();
                bw.close();
                System.out.println(file.getName() + " -> " + file.getName() + ".sql");
            }
        }
    }
}
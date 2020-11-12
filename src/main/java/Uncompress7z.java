import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZFileOptions;
import org.tukaani.xz.CorruptedInputException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class Uncompress7z {

    public static void Uncompress(String inputFile) throws Exception {
        /**
         * zip解压
         * @param inputFile 待解压文件名
         * @param destDirPath  解压路径
         */
        File srcFile = new File(inputFile);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        List<Character> candidates = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            candidates.add(c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            candidates.add(c);
        }
        for (char i = '0'; i <= '9'; i++) {
            candidates.add(i);
        }
        List<Character> specialChars = Arrays.asList('!', '@', '?', ',', '\'', '"', '(', ')', '#', '+', '-', '=');
        candidates.addAll(specialChars);

        int N = candidates.size();
        long start = System.currentTimeMillis();
        for (int i = 0; i < Math.pow(N, 8); i++) {
            if (i % 10 == 0) {
                System.out.println("have tested " + i + "个" + ",耗时" + (System.currentTimeMillis() - start) / 60000 + "分");
            }
            String code = "";
            int num = i;
            int k = num % N;
            code = code + candidates.get(k);
            num = num / N;
            while (num > 0) {
                k = num % N;
                code = code + candidates.get(k);
                num = num / N;
            }
            int capitalCount = 0;
            int specialCount = 0;
            boolean isCaptialInFirstPlace = true;

            Map<Character, Integer> countMap = new HashMap<>();
            for (int j = 0; j < code.length(); j++) {
                char c = code.charAt(j);
                if (c >= 'A' && c <= 'Z') {
                    if (j != 0) {
                        isCaptialInFirstPlace = false;
                    }
                    capitalCount++;
                }
                if (specialChars.contains(c)) {
                    specialCount++;
                }
                if (countMap.containsKey(c)) {
                    countMap.put(c, 1);
                } else {
                    countMap.put(c, countMap.get(c) + 1);
                }
            }
            // 特殊字符和大写字符不应该出现超过二次
            if (specialCount > 2 || capitalCount > 2) {
                continue;
            }
            // 大写字符一般只出现在第一位
            if(!isCaptialInFirstPlace){
                continue;
            }
            // 任何字符不应该出现超过三次
            OptionalInt max = countMap.entrySet().stream().mapToInt(Map.Entry::getValue).max();
            if (max.isPresent() && max.getAsInt() > 3) {
                continue;
            }

//            System.out.println(code);
            SevenZFile zIn = new SevenZFile(srcFile, code.toCharArray());
            SevenZArchiveEntry entry = null;
            try {
                while ((entry = zIn.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        int len = -1;
//                        while ((len = zIn.read(buf)) != -1) {
//                        }
                        byte[] buf = new byte[1024];
                        len = zIn.read(buf);
                        if (len <= 0) {
                            throw new RuntimeException("wrong password");
                        }
                        break;
                    }
                }
                zIn.close();
                System.out.println("code is " + code);
                System.exit(0);
            } catch (CorruptedInputException e) {
                try {
                    zIn.close();
                } catch (Exception ex) {

                }
            } catch (Exception e) {
                try {
                    zIn.close();
                } catch (Exception ex) {

                }
            }
        }

    }

    public static void main(String[] args) throws Exception {

//        Uncompress("D:\\tst\\1.7z","D:\\tst2","221");

//        如有分卷先合并
//        copy /b 开发平台部署文件.7z.*  开发平台部署文件.7z
        try {
            Uncompress("D:\\tst\\开发平台部署文件.7z");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Uncompress(String inputFile, String destDirPath, String password) throws Exception {
        /**
         * zip解压
         * @param inputFile 待解压文件名
         * @param destDirPath  解压路径
         */
        File srcFile = new File(inputFile);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        //开始解压
        SevenZFile zIn = new SevenZFile(srcFile, password.toCharArray());
        SevenZArchiveEntry entry = null;
        File file = null;
        while ((entry = zIn.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                file = new File(destDirPath, entry.getName());
                if (!file.exists()) {
                    new File(file.getParent()).mkdirs();//创建此文件的上级目录
                }
                OutputStream out = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                int len = -1;
                byte[] buf = new byte[1024];
                while ((len = zIn.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                // 关流顺序，先打开的后关闭
                bos.close();
                out.close();
            }
        }
    }

}

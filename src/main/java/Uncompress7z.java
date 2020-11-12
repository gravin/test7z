import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZFileOptions;
import org.tukaani.xz.CorruptedInputException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
//        for (char c = 'a'; c <= 'z'; c++) {
//            candidates.add(c);
//        }
//        for (char c = 'A'; c <= 'Z'; c++) {
//            candidates.add(c);
//        }
        for (char i = '0'; i <= '9'; i++) {
            candidates.add(i);
        }
//        candidates.addAll(Arrays.asList('!', '@', '?', ',', '\'', '"', '(', ')', '#', '+', '-', '='));

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
//            System.out.println(code);
            SevenZFile zIn = new SevenZFile(srcFile, code.toCharArray(), SevenZFileOptions.builder().withMaxMemoryLimitInKb(8000).build());
            SevenZArchiveEntry entry = null;
            try {
                while ((entry = zIn.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        int len = -1;
                        byte[] buf = new byte[1];
//                        while ((len = zIn.read(buf)) != -1) {
//                        }
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
//        SevenZFile zIn = new SevenZFile(new File("D:\\tst\\新建.7z"), "303".toCharArray());
//        Uncompress("D:\\tst\\新建.7z","D:\\tst2","303");

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
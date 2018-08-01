import java.io.*;

public class Main {

    public static void main(String[] args)
    {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印

            /* 读入TXT文件 */
            String pathname = "data\\test\\test_data.txt";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine(); // 一次读入一行数据
            }

            /* 写入Txt文件 */
//            File writename = new File(".\\result\\en\\output.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
//            writename.createNewFile(); // 创建新文件
//            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
//            out.write("我会写入文件啦\r\n"); // \r\n即为换行
//            out.flush(); // 把缓存区内容压入文件
//            out.close(); // 最后记得关闭文件

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

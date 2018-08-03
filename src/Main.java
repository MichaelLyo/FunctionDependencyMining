import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args)
    {
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印

            /* 读入TXT文件 */
            String pathname = "data"+File.separator+"test"+File.separator+"test_data.txt";
            //String pathname = "data"+File.separator+"train"+File.separator+"data.txt";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            line = br.readLine();
            String[] firstLine = line.split(",");
            int columnNum = firstLine.length;
            char[] U = new char[columnNum];

            for (int i = 0; i<columnNum;i++)
            {
                U[i] = (char)('A'+i);
            }
            //HashMap<Character,List<String>> relation;
            List<String[]> relation = new ArrayList<>();
            while (line != null) {
                //System.out.println(line);
                String[] items = line.split(",");
                //for (String i :items)
                //{
                //    System.out.print(i+"  ");
                //}
                //System.out.print("\n");
                relation.add(items);

                line = br.readLine(); // 一次读入一行数据
            }
            String[][] newRelation  = new String[relation.size()][columnNum];
            for (int i = 0; i<relation.size();i++)
            {
                newRelation[i] = relation.get(i);
            }
            FDMine fdMine = new FDMine(newRelation,U);
            fdMine.runAlgorithm();

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

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args)
    {

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印

            //读取数据并存入内存

            //String pathname = "data"+File.separator+"test"+File.separator+"test_data.txt";
            String pathname = "data"+File.separator+"train"+File.separator+"data.txt";

            //String pathname = "data"+File.separator+"test"+File.separator+"mytest.txt";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            line = br.readLine();
            String[] firstLine = line.split(",");
            int columnNum = firstLine.length;
            char[] U = new char[columnNum];

            //处理列号
            for (int i = 0; i<columnNum;i++)
            {
                U[i] = (char)('A'+i);
            }
            List<String[]> relation = new ArrayList<>();
            while (line != null) {
                String[] items = line.split(",");
                relation.add(items);

                line = br.readLine();
            }


            String[][] newRelation  = new String[relation.size()][columnNum];
            for (int i = 0; i<relation.size();i++)
            {
                newRelation[i] = relation.get(i);
            }
            //初始化算法
            FDMine fdMine = new FDMine(newRelation,U);

            /**
            ** 选择是否修剪函数依赖
             * 修剪会极大提升算法性能，同时过滤掉因为等价等原因造成的重复、无意义的函数依赖
             * 不修剪可以提供直观的所有函数依赖的计算
             * 当数据量很大时强烈建议设置修剪为true
            **/
            fdMine.setPrune(true);

            //运行算法
            fdMine.runAlgorithm();
            //打印到控制台
            fdMine.ShowF();
            //存入文件
            fdMine.StoreIntoFile();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

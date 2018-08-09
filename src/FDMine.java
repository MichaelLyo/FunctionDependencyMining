
import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by LSL on 2018/8/2
 */
public class FDMine
{

	//存储数据
	private String[][] relation;
	//存储列名（根据数字相对位置生成的字母，方便组合）
	private char[] U;
	//保存函数依赖的list
	private List<String[]> F;
	//保存等价类的list
	private Set<String> E;
	//保存closure的map
	private HashMap<String,Set<Character>> FClosure;
	//算法是否修剪
	private boolean prune;


	public FDMine(String[][] relation, char[] u)
	{
		this.relation = relation;
		U = u;
		F= new ArrayList<>();
		E = new HashSet<>();
		FClosure = new HashMap<>();
		prune=true;
	}

	public FDMine()
	{
		F= new ArrayList<>();
		E = new HashSet<>();
		prune=true;
	}

	//算法运行的主体部分
	public void runAlgorithm()
	{

		//为简化操作并减少内存开销，每次维护两个level的计算
		//Ckm为ck的上一层
		HashMap<String,Object[]> Ck ;
		HashMap<String,Object[]> Ckm = new HashMap<>();

		long time1=System.currentTimeMillis();   //获取开始时间

		for (char c :U)
		{
			Object[] objects = new Object[2];
			String ch = String.valueOf(c);

			objects[0] = InitializeClosure(ch);
			objects[1] = InitializePartition(c);
			Ckm.put(ch,objects);
		}
		long time2=System.currentTimeMillis(); //获取结束时间
		System.out.println("init time： "+(time2-time1)+"ms");
		int k = 1;
		while (Ckm.size()>0)
		{
			System.out.println("第"+String.valueOf(k)+"层");
			Ck = GenerateNextLevel(Ckm);
			long time3=System.currentTimeMillis(); //获取结束时间
			System.out.println("GenerateNextLevel： "+(time3-time2)+"ms");

			ObtainFDs(Ck,Ckm);
			long time4=System.currentTimeMillis(); //获取结束时间
			System.out.println("ObtainFDs： "+(time4-time3)+"ms");

			ObtainEquivalences(Ckm);
			long time5=System.currentTimeMillis(); //获取结束时间
			System.out.println("ObtainEquivalences： "+(time5-time4)+"ms");

			//根据需要是否修剪
			if (prune)
			{
				Ck = Prune(Ck,Ckm);
				long time6=System.currentTimeMillis(); //获取结束时间
				System.out.println("Prune： "+(time6-time5)+"ms");
			}

			Ckm = Ck;
			k++;
		}


	}

	//单一属性的时候的partition，为初始状态的partition策略
	public List<List<Integer>> InitializePartition(char ch)
	{
		List<List<Integer>> partition = new ArrayList<>();
		int index = ch-'A';
		Map<String,Integer> pMap = new HashMap<>();
		int[] intRelation = new int[relation.length];
		int it = 0;
		for (int i = 0; i < relation.length;i++)
		{
			if(pMap.containsKey(relation[i][index]))
			{
				intRelation[i] = pMap.get(relation[i][index]);
				partition.get(intRelation[i]).add(i);
			}
			else
			{
				intRelation[i] = it;
				pMap.put(relation[i][index],it);
				List<Integer> newList = new ArrayList<>();
				newList.add(i);
				partition.add(newList);
				it++;
			}
		}
		//stripped partition
		Iterator<List<Integer>> iter = partition.iterator();
		while (iter.hasNext()) {
			List<Integer> item = iter.next();
			if (item.size()<=1) {
				iter.remove();
			}
		}
		return partition;
	}


	//从上一层中指定的两个partition中产生这一层的对应项目的partition，简化了partition的计算
	public List<List<Integer>> CalculatePartition(List<List<Integer>> p1,List<List<Integer>> p2)
	{
		//Map<Integer,Integer> T = new HashMap<>();
		int[] T = new int[relation.length+1];
		for (int i = 0; i< relation.length+1;i++)
		{
			T[i]=-1;
		}


		List<List<Integer>> newPartition = new ArrayList<>();
		List<List<Integer>> S = new ArrayList<>();
		for (int i = 0; i < p1.size();i++)
		{
			for (int t : p1.get(i))
			{
				//T.put(t,i);
				T[t]=i;
			}
			List<Integer> newList = new ArrayList<>();
			S.add(newList);

		}


		for (int i = 0; i < p2.size();i++)
		{


			for (int t : p2.get(i))
			{
				if (T[t]>=0)
				{
					S.get(T[t]).add(t);
				}
			}

			for (int t : p2.get(i))
			{
			    //stripped partition
				if (T[t]>=0)
				{
					if(S.get(T[t]).size()>1)
					{
						List<Integer> newList = new ArrayList<>();
						for (int in : S.get(T[t]))
						{
							newList.add(in);
						}
						newPartition.add(newList);
					}

					S.get(T[t]).clear();
				}


			}

		}


		return newPartition;
	}

	//用于在生成下一层级时合并attribute组合
	public String mergeString(String s1,String s2)
	{
		String result;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(s1);
		for (int i=0; i < s2.length();i++)
		{
			if (s1.indexOf(s2.charAt(i))<0)
			{
				stringBuilder.append(s2.charAt(i));
			}
		}
		result = stringBuilder.toString();
		char[] a = result.toCharArray();
		Arrays.sort(a);
		String rst = new String(a);
		return rst;
	}

	//生成下一层级
	public HashMap<String,Object[]> GenerateNextLevel(HashMap<String,Object[]> Ckm)
	{
		HashMap<String,Object[]> Ck = new HashMap<>();
		Set<String> newXs = new HashSet<>();

		Iterator<Map.Entry<String,Object[]>> iterator1 = Ckm.entrySet().iterator();
		int counter = 1;

		while (iterator1.hasNext())
		{
			Iterator<Map.Entry<String,Object[]>> iterator2 = Ckm.entrySet().iterator();
			for (int i = 0; i<counter;i++)
			{
				iterator2.next();
			}
			Map.Entry<String,Object[]> entry1 = iterator1.next();
			while (iterator2.hasNext())
			{
				Map.Entry<String,Object[]> entry2 = iterator2.next();
				String newX = mergeString(entry1.getKey(),entry2.getKey());
				if (newX.length() == (entry1.getKey()).length()+1 && !newXs.contains(newX))
				{
					Object[] objects = new Object[2];
//					System.out.println(newX);
                    objects[0] = InitializeClosure(newX);
                    long time2=System.currentTimeMillis();

					objects[1] = CalculatePartition((List<List<Integer>>)(entry1.getValue()[1]),(List<List<Integer>>)entry2.getValue()[1]);
                    long time3=System.currentTimeMillis();
//                    System.out.println("CalculatePartition： "+(time3-time2)+"ms");
					Ck.put(newX,objects);

					newXs.add(newX);
				}
			}

			counter+=1;
		}

		return Ck;

	}

	//初始化闭包
	public Set<Character> InitializeClosure(String X)
	{

		Set<Character> closure = new HashSet<>();
		for (int i =0;i< X.length();i++)
		{
			closure.add(X.charAt(i));
		}
		return closure;
	}

	//计算U-{X}
	public List<Character> UMinusX(Set<Character> X)
	{
		List<Character> result = new ArrayList<>();
		for (char ch : U)
		{
			if (!X.contains(ch))
			{
				result.add(ch);
			}
		}

		return result;
	}

	//获取Ckm层的functional dependency
	public void ObtainFDs(HashMap<String,Object[]> Ck,HashMap<String,Object[]> Ckm)
	{
		Iterator<Map.Entry<String,Object[]>> iterator = Ckm.entrySet().iterator();
		List<String[]> thisF = new ArrayList<>();
		while (iterator.hasNext())
		{
			Map.Entry<String,Object[]> entry = iterator.next();
			List<Character> umx = UMinusX((Set<Character>)entry.getValue()[0]);
			for (char ch : umx)
			{
				String merged = mergeString(entry.getKey(),String.valueOf(ch));
				if (Ck.containsKey(merged))
				{
					if (((List<List<Integer>>)entry.getValue()[1]).size() ==
							((List<List<Integer>>)Ck.get(merged)[1]).size())
					{
						((Set<Character>)entry.getValue()[0]).add(ch);
						String[] newF = new String[2];
						newF[0] = entry.getKey();
						newF[1] = String.valueOf(ch);
						thisF.add(newF);
					}
				}

			}
		}
		for (String[] fItem : thisF)
		{
			F.add(fItem);
			if (!FClosure.containsKey(fItem[0]))
			{
				FClosure.put(fItem[0],(Set<Character>)Ckm.get(fItem[0])[0]);
			}
		}
	}

	//判断A是否为B的子集
	public boolean judgeABelongsToB(String A, Set<Character> B)
	{
		for (int i = 0;i<A.length();i++)
		{
			if (!B.contains(A.charAt(i)))
				return false;
		}
		return true;
	}

	//判断闭包是否为B的子集
	public boolean judgeClosureBelongsToString(Set<Character> A, String B)
	{
		for (char ch : A)
		{
			if (B.indexOf(ch)<0)
				return false;
		}
		return true;
	}

	//获取等价类，用以修剪
	public void ObtainEquivalences(HashMap<String,Object[]> Ckm)
	{
		Iterator<Map.Entry<String,Object[]>> iterator = Ckm.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String,Object[]> entry = iterator.next();
			for (String[] f : F)
			{
				if(judgeABelongsToB(entry.getKey(),FClosure.get(f[0]))&&
						judgeABelongsToB(f[0],(Set<Character> )entry.getValue()[0]))
				{
					String[] newE = new String[2];
					newE[0] = entry.getKey();
					newE[1] = f[0];
					if (!newE[0].equals(newE[1]))
					{
						//E.add(newE);
						if (!E.contains(newE[1]+","+newE[0]))
						{
							E.add(newE[0]+","+newE[1]);
						}
					}
				}
			}
		}
	}

	//判断a是否是b的真子集
	public boolean judgeAReallyBelongsToB(String a, String b)
	{
		if (a.length()<=0)
			return false;
		if (a.length()<b.length())
		{
			for (int i = 0; i < a.length(); i++)
			{
				if (b.indexOf(a.charAt(i))<0)
					return false;
			}
			return true;
		}
		return false;
	}

	//判断等价类中是否包含X
	public boolean judgeEContainsX(String X)
	{

		for (String it : E)
		{
			String[] its = it.split(",");
			if (its[1].equals(X))
				return true;
		}
		return false;
	}

	//获取非平凡闭包
	public String getNontrivialClosure(Set<Character> closure,String X)
	{
		char[] nClo = new char[closure.size() - X.length()];
		int count = 0;
		for (Character ch : closure)
		{
			if (X.indexOf(ch)<0)
			{
				nClo[count++] = ch;
			}
		}
		Arrays.sort(nClo);
		String result = new String(nClo);
		return result;
	}

	//判断闭包与属性集是否构成全集的互补关系
	public boolean judgeComplementary(Set<Character> SClosure,String xnc)
	{
		for(int i =0 ; i<xnc.length();i++)
		{
			SClosure.add(xnc.charAt(i));
		}
		return SClosure.size() == U.length;
	}

	//合并两个closure
	public Set<Character> mergeClosure(Set<Character> s,String x)
	{
		Set<Character> result= new HashSet<>();
		for (char ch :s)
		{
			result.add(ch);
		}
		for (int i = 0; i<x.length();i++)
		{
			result.add(x.charAt(i));
		}

		return result;
	}

	//修剪Ck层
	public HashMap<String,Object[]> Prune(HashMap<String,Object[]> Ck,HashMap<String,Object[]> Ckm)
	{
		Iterator<Map.Entry<String,Object[]>> KIterator = Ck.entrySet().iterator();
		while (KIterator.hasNext())
		{
			Map.Entry<String,Object[]> KEntry = KIterator.next();
			String S = KEntry.getKey();
			Iterator<Map.Entry<String,Object[]>> MIterator = Ckm.entrySet().iterator();
			boolean stop = false;
			while (MIterator.hasNext()&&!stop)
			{
				Map.Entry<String,Object[]> MEntry = MIterator.next();
				String X = MEntry.getKey();

				if (judgeAReallyBelongsToB(X,S))
				{
					//prune rule 1
					if (judgeEContainsX(X))
					{
						//Ck.remove(KEntry.getKey());
						KIterator.remove();
						stop = true;
					}
					//prune rule 2
					//else if (judgeClosureBelongsToString((Set<Character>)MEntry.getValue()[0],S))
					else if (judgeAReallyBelongsToB(getNontrivialClosure((Set<Character>)MEntry.getValue()[0],X),S))
					{
						//Ck.remove(KEntry.getKey());
						KIterator.remove();
						stop = true;

					}

					if (!stop)
					{
						//prune rule 3
						KEntry.getValue()[0] = mergeClosure((Set<Character>)KEntry.getValue()[0],getNontrivialClosure((Set<Character>)MEntry.getValue()[0],X));
						//prune rule 4
						if (((Set<Character>)KEntry.getValue()[0]).size() == U.length)
						{
							KIterator.remove();
							stop = true;
						}
					}
					//else if (judgeComplementary(((Set<Character>)KEntry.getValue()[0]),getNontrivialClosure((Set<Character>)MEntry.getValue()[0],X)))
					//{
					//	//Ck.remove(KEntry.getKey());
					//	KIterator.remove();
					//	stop = true;
					//}
				}
			}
		}
		return Ck;
	}

	//控制台打印
	public void ShowF()
	{
		System.out.println("\n\nFunctional Dependency Mining Result:");

		SortF();
		for (int i = 0; i < F.size();i++)
		{
			System.out.println(TransformAttributeForm(F.get(i)[0])+ " -> " + TransformAttributeForm(F.get(i)[1]));
		}
	}
	//将字母属性列转换为数字
	public String TransformAttributeForm(String str)
	{
		StringBuilder stringBuilder = new StringBuilder();

		char[] chars = str.toCharArray();
		Arrays.sort(chars);
		int i = 0;
		for (; i<chars.length-1;i++)
		{
			stringBuilder.append(chars[i]-'A'+1);
			stringBuilder.append(" ");
		}
		stringBuilder.append(chars[i]-'A'+1);
		return stringBuilder.toString();
	}

	//按照字典序排列
	public void SortF()
	{
		Comparator c = new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				if(o1[0].compareTo(o2[0])<0)
					return -1;
				else if(o1[0].compareTo(o2[0])==0)
				{
					if(o1[1].compareTo(o2[1])<=0)
						return -1;
					else return 1;
				}
				else return 1;
			}
		};
		Collections.sort(F,c);
	}


	//将函数依赖存于文件中
	public void StoreIntoFile() throws IOException
	{
		String mid;
		if (prune)
		{
			mid = "pruned";
		}
		else {
			mid = "unpruned";
		}
        File writeName = new File("data"+File.separator+"result"+File.separator+"result_"+mid+".txt");
        writeName.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(writeName));

		for (int i = 0; i < F.size();i++)
		{
			out.write(TransformAttributeForm(F.get(i)[0])+ " -> " + TransformAttributeForm(F.get(i)[1])+'\n');
		}

        out.flush();
        out.close();
	}

	public void StoreDivideIntoFile(int mid) throws IOException
	{

		File writeName = new File("data"+File.separator+"merged"+File.separator+"result_"+String.valueOf(mid)+".txt");
		writeName.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(writeName));

		for (int i = 0; i < F.size();i++)
		{
			out.write(TransformAttributeForm(F.get(i)[0])+ " -> " + TransformAttributeForm(F.get(i)[1])+'\n');
		}

		out.flush();
		out.close();
	}

	// getters and setters
	public String[][] getRelation()
	{
		return relation;
	}

	public void setRelation(String[][] relation)
	{
		this.relation = relation;
	}

	public char[] getU()
	{
		return U;
	}

	public void setU(char[] u)
	{
		U = u;
	}

	public List<String[]> getF()
	{
		return F;
	}

	public void setF(List<String[]> f)
	{
		F = f;
	}

	public Set<String> getE()
	{
		return E;
	}

	public void setE(Set<String> e)
	{
		E = e;
	}

	public HashMap<String, Set<Character>> getFClosure()
	{
		return FClosure;
	}

	public void setFClosure(HashMap<String, Set<Character>> FClosure)
	{
		this.FClosure = FClosure;
	}

	public boolean isPrune()
	{
		return prune;
	}

	public void setPrune(boolean prune)
	{
		this.prune = prune;
	}
}

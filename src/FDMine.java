
import java.util.*;

/**
 * Created by LSL on 2018/8/2
 */
public class FDMine
{

	private String[][] relation;
	//private HashMap<Character,List<String>> relation;
	private char[] U;
	//private List<String[]> F;


	public FDMine(String[][] relation, char[] u)
	{
		this.relation = relation;
		U = u;
	}

	public FDMine()
	{
	}

	public void runAlgorithm()
	{
		List<String[]> F = new ArrayList<>();
		List<String[]> E = new ArrayList<>();
		List<Object[]> Ck ;
		List<Object[]> Ckm = new ArrayList<>();
		int k = 1;

		for (char c :U)
		{
			Object[] objects = new Object[3];
			String ch = String.valueOf(c);

			objects[0] = ch;
			objects[1] = InitializeClosure(ch);
			objects[2] = InitializePartition(c);
			Ckm.add(objects);
		}

		while (k <= U.length)
		{
			Ck = GenerateNextLevel(Ckm);



			Ckm = Ck;
			k++;
		}


	}

	//单一属性的时候的partition
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


		return partition;
	}


	//从上一层中指定的两个partition中产生这一层的对应项目的partition
	public List<List<Integer>> CalculatePartition(List<List<Integer>> p1,List<List<Integer>> p2)
	{
		Map<Integer,List<Integer>> S = new HashMap<>();
		Map<Integer,Integer> T = new HashMap<>();
		List<List<Integer>> newPartition = new ArrayList<>();
		for (int i = 0; i < p1.size();i++)
		{
			for (int t : p1.get(i))
			{
				T.put(t,i);
				List<Integer> newList = new ArrayList<>();
				S.put(i,newList);
			}
		}

		for (int i = 0; i < p2.size();i++)
		{
			for (int t : p2.get(i))
			{
				if (T.containsKey(t))
				{
					S.get(T.get(t)).add(t);
				}
			}

			for (int t : p2.get(i))
			{
				if (T.containsKey(t))
				{
					//S.get(T.get(t)).add(t);
					newPartition.add(S.get(T.get(t)));
				}
				S.get(T.get(t)).clear();
			}
		}
		return newPartition;
	}

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

	public List<Object[]> GenerateNextLevel(List<Object[]> Ckm)
	{
		List<Object[]> Ck = new ArrayList<>();
		Set<String> newXs = new HashSet<>();

		for (int i = 0; i <Ckm.size()-1;i++)
		{
			for (int j = i+1; j<Ckm.size();j++)
			{
				String newX = mergeString((String)(Ckm.get(i)[0]),(String)Ckm.get(j)[0]);
				if (newX.length() == ((String)(Ckm.get(i)[0])).length()+1 && !newXs.contains(newX))
				{
					Object[] objects = new Object[3];


					objects[0] = newX;
					objects[1] = InitializeClosure(newX);
					objects[2] = CalculatePartition((List<List<Integer>>)(Ckm.get(i)[2]),(List<List<Integer>>)Ckm.get(j)[2]);
					Ck.add(objects);

					newXs.add(newX);
				}
			}
		}

		return Ck;

	}

	public Set<Character> InitializeClosure(String X)
	{

		Set<Character> closure = new HashSet<>();
		for (int i =0;i< X.length();i++)
		{
			closure.add(X.charAt(i));
		}
		return closure;
	}


	public void ObtainFDs(List<Object[]> Ck,List<Object[]> Ckm)
	{

	}
	public void ObtainEquivalences()
	{

	}

	public void Prune()
	{

	}
}

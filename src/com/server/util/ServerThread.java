package com.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerThread implements Callable<String> {
	
	// ���嵱ǰ�߳��������Socket
	Socket s = null;
	// ���߳��������Socket����Ӧ��������
	private static BufferedReader br = null;
	private static Gson gson;
	private static String content;
	private static Map<String,String> apMacAddress=new HashMap<String,String>();
	
	private static final String fileName=GetDate.getTime();
//	private static List<Double> saveDouble1=new ArrayList<Double>();
//	private static List<Double> saveDouble2=new ArrayList<Double>();
//	private static List<Double> countDouble1=new ArrayList<Double>();
//	private static List<Double> countDouble2=new ArrayList<Double>();
	//���600�Լ�����ļ������
//	private static List<ArrayList<Double>> list=new ArrayList<ArrayList<Double>>();
	private static boolean isThreshold=true;
//	private static boolean hasStaticData=true;
	private static HandleMysql handleMysql;
	private static int timer=0;

//	private Iterator<Map<String, String>> iterator;
	
	//��̬��������apnamelist ����ִֻ��һ�� ����ȡ�����apname
	//�ж��Ƿ���ڱ�
	static{
		try {
			ReadProperties readProperties=new ReadProperties();
			//��ȡ����apMacAddress
			apMacAddress=readProperties.readAll("src\\com\\server\\util\\apMacAddress.properties");
			System.out.println("apMacAddress:"+apMacAddress.toString());
			
			handleMysql=new HandleMysql();//��������
			handleMysql.createTable(fileName);//����
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ServerThread(Socket s) throws IOException {
		this.s = s;
		// ��ʼ����Socket��Ӧ��������
		br = new BufferedReader(new InputStreamReader(s.getInputStream(),
				"utf-8"));
	}
	
	@Override
	public String call() throws Exception {
		
		try {
			
			//���ж��Ƿ��о�Ĭ��txt�ĵ�
			handleMysql.getStaticData();
			File file=new File("D:\\data.txt");
			if(file.exists()){
				//TODO ����
				System.out.println("�������о�Ĭ״̬����");
//					saveDouble1=ImportStaticData.readStaticData("Pear");
//					saveDouble2=ImportStaticData.readStaticData("Orange");
				
//					hasStaticData=false;
//					System.out.println("���÷�������data.txt�� saveDouble�Ĵ�С��"+saveDouble1.size()+":"+saveDouble2.size());
//					System.out.println(saveDouble1.toString());
//					System.out.println(saveDouble2.toString());
			}else{
				System.out.println("û��data.txt");
			}
			
			
			
			content = null;
			List<Map<String, String>> list = new ArrayList<Map<String,String>>();
			int numofSave=handleMysql.getColNumber(fileName,apMacAddress.get("apMacAddress1"),"save");
			int numofCount=handleMysql.getColNumber(fileName,apMacAddress.get("apMacAddress1"),"count");
			System.out.println("��ѯ�����"+numofSave);
			//����ѭ�����ϴ�Socket�ж�ȡ�ͻ��˷��͹��������� ѭ��
			while ((content = readFromClient()) != null) {
				//System.out.println(content);
				if(numofSave<20){
					if(!content.equals("count")){//�����ʱ�䲻Ϊcount
						list=readJsonString(content);
						savetList2db(list,"save");
						return "2���ڴ洢���ݼ�����ֵ ���� "+numofSave+"/200 ��";
					}else{
						return "2�����������Լ�����ֵ";
					}
				}else if(numofSave>=20 && numofCount<10){
					if(isThreshold){
						//TODO ������ֵ ���ý��д洢������saveֻ��Ҫ�洢��Ҫ��600��
						ArrayList<String> apList=new ArrayList<String>(apMacAddress.values());//�洢����properties�е�mac��ַ
						List<ArrayList<String>> saveDataList=new ArrayList<ArrayList<String>>(); 
						for(int i=0;i<apList.size();i++){
							saveDataList.add(handleMysql.getListFromeDB(fileName,apList.get(i), "save"));
						}
						System.out.println(saveDataList.toString());
//						Double[] d1=new Double[saveDouble1.size()];
//						saveDouble1.toArray(d1);
//					    //System.out.println(Arrays.toString(d1));
//					    Double[] d2=new Double[saveDouble2.size()];
//						saveDouble2.toArray(d2);
//					    //System.out.println(Arrays.toString(d2));
//						
//						upbound1=CountDouble.getMyupbound(d1);//����ֵΪ���飬�ں���4��ֵ(�����Ǿ�ֵ���ޣ��������ޣ���ʼ��ֵ����ʼ����)
//						upbound2=CountDouble.getMyupbound(d2);//����ֵΪ���飬�ں���4��ֵ
//						
						isThreshold=false;
						return "2*****�������ֵ******";
					}
					if(!content.equals("count") && timer>0){//�Ѿ�����
						list=readJsonString(content);
						savetList2db(list,"count");
						timer--;
						return "2���ڴ洢����ʵʩ��� ���� "+numofCount+"/10 ������";
					}else if(content.equals("count")){//���ڵ�
						timer=10;
						return "2���ڴ洢����ʵʩ��� ���� "+numofCount+"/10 ������";
					}else{//û�е� �ȴ����
						return "2 ����  "+numofSave+"/600 �����ȴ���ʼ���ָ��";
					}
				}else if(numofSave>=20 && numofCount >=10){
					if(!content.equals("count")){
						savetList2db(list,"count");
						
						ArrayList<String> apList=new ArrayList<String>(apMacAddress.values());//�洢����properties�е�mac��ַ
						List<ArrayList<String>> saveDataList=new ArrayList<ArrayList<String>>(); 
						for(int i=0;i<apList.size();i++){
							saveDataList.add(handleMysql.getListFromeDB(fileName,apList.get(i), "count"));
						}
						System.out.println(saveDataList.toString());
//						countDouble1.remove(0);
//						countDouble2.remove(0);
//						list=readJsonString(content);
//						handleListDouble(list,"count");
//						
//						//����
//						Double[] d3=new Double[countDouble1.size()];
//						countDouble1.toArray(d3);
//					   // System.out.println(Arrays.toString(d3));
//					    Double[] d4=new Double[countDouble2.size()];
//						countDouble2.toArray(d4);
//						//System.out.println(Arrays.toString(d4));
//						
//						System.out.println("****����RESULT******:                         ʱ�䣺"+GetDate.getTime());
//						//ֱ�ӵ�����ȫ���쳣ֵ������0��1
//						int at=CountDouble.getDetection(upbound1,upbound2,d3,d4);
//						savtList2db(content+" "+GetDate.getTime()+" "+at);
						return "0RESULT��";//+at;
					}
					return "2�Ѿ���ʼ��� ��Ч�ļ��ָ��";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("�����쳣ʱ ���յ��������ǣ� "+content);
		return "3�����쳣";
	}

	

	// �����ȡ�ͻ������ݵķ���
	private String readFromClient() {
		try {
			return br.readLine();
		}
		// �����׽���쳣��������Socket��Ӧ�Ŀͻ����Ѿ��ر�
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Map<String, String>> readJsonString(String gsonString) {
		List<Map<String, String>> readList = null;
		gson = new Gson();
		if (gson != null) {
			readList = gson.fromJson(gsonString,
					new TypeToken<List<Map<String, String>>>() {
					}.getType());
		}
		return readList;
	}
	
	private void savetList2db(List<Map<String,String>> list,String label) throws FileNotFoundException, IOException{
		Iterator<Map<String,String>> iterator=list.iterator();
		//TODO �ж�list�Ƿ������е�apMacAddress
		while(iterator.hasNext()){
			Map<String, String>map=iterator.next();
			map.toString();
			handleMysql.insert(fileName,map,label);
		}
	}
	
}

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
	
	// 定义当前线程所处理的Socket
	Socket s = null;
	// 该线程所处理的Socket所对应的输入流
	private static BufferedReader br = null;
	private static Gson gson;
	private static String content;
	private static Map<String,String> apMacAddress=new HashMap<String,String>();
	
	private static final String fileName=GetDate.getTime();
//	private static List<Double> saveDouble1=new ArrayList<Double>();
//	private static List<Double> saveDouble2=new ArrayList<Double>();
//	private static List<Double> countDouble1=new ArrayList<Double>();
//	private static List<Double> countDouble2=new ArrayList<Double>();
	//存放600以及后面的检测数据
//	private static List<ArrayList<Double>> list=new ArrayList<ArrayList<Double>>();
	private static boolean isThreshold=true;
//	private static boolean hasStaticData=true;
	private static HandleMysql handleMysql;
	private static int timer=0;

//	private Iterator<Map<String, String>> iterator;
	
	//静态代码块加载apnamelist 这样只执行一次 来获取不变的apname
	//判断是否存在表
	static{
		try {
			ReadProperties readProperties=new ReadProperties();
			//获取所有apMacAddress
			apMacAddress=readProperties.readAll("src\\com\\server\\util\\apMacAddress.properties");
			System.out.println("apMacAddress:"+apMacAddress.toString());
			
			handleMysql=new HandleMysql();//建立连接
			handleMysql.createTable(fileName);//建表
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ServerThread(Socket s) throws IOException {
		this.s = s;
		// 初始化该Socket对应的输入流
		br = new BufferedReader(new InputStreamReader(s.getInputStream(),
				"utf-8"));
	}
	
	@Override
	public String call() throws Exception {
		
		try {
			
			//先判断是否有静默的txt文档
			handleMysql.getStaticData();
			File file=new File("D:\\data.txt");
			if(file.exists()){
				//TODO 载入
				System.out.println("载入已有静默状态数据");
//					saveDouble1=ImportStaticData.readStaticData("Pear");
//					saveDouble2=ImportStaticData.readStaticData("Orange");
				
//					hasStaticData=false;
//					System.out.println("调用方法载入data.txt后 saveDouble的大小："+saveDouble1.size()+":"+saveDouble2.size());
//					System.out.println(saveDouble1.toString());
//					System.out.println(saveDouble2.toString());
			}else{
				System.out.println("没有data.txt");
			}
			
			
			
			content = null;
			List<Map<String, String>> list = new ArrayList<Map<String,String>>();
			int numofSave=handleMysql.getColNumber(fileName,apMacAddress.get("apMacAddress1"),"save");
			int numofCount=handleMysql.getColNumber(fileName,apMacAddress.get("apMacAddress1"),"count");
			System.out.println("查询结果："+numofSave);
			//采用循环不断从Socket中读取客户端发送过来的数据 循环
			while ((content = readFromClient()) != null) {
				//System.out.println(content);
				if(numofSave<20){
					if(!content.equals("count")){//大多数时间不为count
						list=readJsonString(content);
						savetList2db(list,"save");
						return "2正在存储数据计算阈值 已有 "+numofSave+"/200 条";
					}else{
						return "2数据量不足以计算阈值";
					}
				}else if(numofSave>=20 && numofCount<10){
					if(isThreshold){
						//TODO 计算阈值 不用进行存储操作，save只需要存储需要的600条
						ArrayList<String> apList=new ArrayList<String>(apMacAddress.values());//存储所有properties中的mac地址
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
//						upbound1=CountDouble.getMyupbound(d1);//返回值为数组，内含有4个值(依次是均值门限，方差门限，初始均值，初始方差)
//						upbound2=CountDouble.getMyupbound(d2);//返回值为数组，内含有4个值
//						
						isThreshold=false;
						return "2*****已算出阈值******";
					}
					if(!content.equals("count") && timer>0){//已经点了
						list=readJsonString(content);
						savetList2db(list,"count");
						timer--;
						return "2正在存储数据实施检测 已有 "+numofCount+"/10 条数据";
					}else if(content.equals("count")){//正在点
						timer=10;
						return "2正在存储数据实施检测 已有 "+numofCount+"/10 条数据";
					}else{//没有点 等待点击
						return "2 已有  "+numofSave+"/600 条，等待开始检测指令";
					}
				}else if(numofSave>=20 && numofCount >=10){
					if(!content.equals("count")){
						savetList2db(list,"count");
						
						ArrayList<String> apList=new ArrayList<String>(apMacAddress.values());//存储所有properties中的mac地址
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
//						//计算
//						Double[] d3=new Double[countDouble1.size()];
//						countDouble1.toArray(d3);
//					   // System.out.println(Arrays.toString(d3));
//					    Double[] d4=new Double[countDouble2.size()];
//						countDouble2.toArray(d4);
//						//System.out.println(Arrays.toString(d4));
//						
//						System.out.println("****计算RESULT******:                         时间："+GetDate.getTime());
//						//直接调用求全局异常值，返回0或1
//						int at=CountDouble.getDetection(upbound1,upbound2,d3,d4);
//						savtList2db(content+" "+GetDate.getTime()+" "+at);
						return "0RESULT：";//+at;
					}
					return "2已经开始检测 无效的检测指令";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("出现异常时 接收到的数据是： "+content);
		return "3出现异常";
	}

	

	// 定义读取客户端数据的方法
	private String readFromClient() {
		try {
			return br.readLine();
		}
		// 如果捕捉到异常，表明该Socket对应的客户端已经关闭
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
		//TODO 判断list是否有所有的apMacAddress
		while(iterator.hasNext()){
			Map<String, String>map=iterator.next();
			map.toString();
			handleMysql.insert(fileName,map,label);
		}
	}
	
}

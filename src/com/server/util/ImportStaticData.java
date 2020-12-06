package com.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ImportStaticData {
	@SuppressWarnings("resource")
	/**
	 * 
	 * @param apName
	 * @return 一个ap的静默数据 ArrayList<Double>()
	 */
	public static ArrayList<Double> readStaticData(String apName){
		ArrayList<Double> s=new ArrayList<Double>();
		try {
			File file=new File("D:\\data.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str =null;
//			Iterator<Map<String, String>> iterator;
//			List<Map<String, String>> list=new ArrayList<Map<String, String>>();
			while((str=br.readLine()) != null){
				if(str.endsWith("]")){
					s.add(getApFromList(str,apName));
				}
				else
					continue;
			}
		}catch(FileNotFoundException e){
			System.out.println("file not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;

	}
	//获取txt中每行的一个list 找到目标ap返回一个值
	private static double getApFromList(String str,String apname) {
		Iterator<Map<String, String>> iterator;
		List<Map<String, String>> list;
		list = HandleJsonString.readJsonString(str);
		iterator=list.iterator();
		while(iterator.hasNext()){
			Map<String, String>map=(Map<String, String>) iterator.next();
			if(map.get("wifiName").equals(apname)){
				return Double.parseDouble(map.get("wifiStrength"));
			}else{
				continue;
			}
		}
		return -1;
	}

}

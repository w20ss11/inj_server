package com.server.util;


//计算阈值
public class CountDouble {

	//计算门限
	public static Double[] getMyupbound(Double[] arry_test0){
		//去掉前10秒的数据
		int N0=arry_test0.length-30;
		double[] arry_test=new double[N0];
		for(int i=0;i<N0;i++){
			 arry_test[i]=arry_test0[i+30];
		 }

		//取静默状态的其中一部分，计算初始均值和标准差(该数据需要一直保留，重要)
		int N1=11;//截取的长度(可调)
		Double[] arry_t1=new Double[N1];
		for(int i=0;i<N1;i++){
			arry_t1[i]=arry_test[i+49];
		}
		double ave_0= getAverage(arry_t1);
		double std_0=getStandardDiviation(arry_t1);

		//使用滑动窗进行特征提取，均值和方差。定义两个数组用于存储
		int l=10;//滑动窗长度（可调）
		int N3=arry_test.length-l+1;
		//System.out.println(N3);
		Double[] arry_ave=new Double[N3];//存放均值特征
		Double[] arry_std=new Double[N3];//存放标准差特征
		for(int i=0;i<N3;i++){
			Double[] arry_t2=new Double[l];//临时矩阵
			for(int j=0;j<l;j++){
				arry_t2[j]=arry_test[i+j];
			}
			arry_ave[i]= Math.abs(getAverage(arry_t2)-ave_0);
			arry_std[i]= Math.abs(getStandardDiviation(arry_t2));
		}

		/*求置信度alf,根据静默和有人时的数据大小来决定，即alf=length(静默)/length(有人入侵)-0.02;
		 *这里直接给出
		 * 该值重要可调
		 */
		double alpha=0.1;

		//求门限
		double upbound_0=getUpbound(arry_ave,alpha);//均值的门限
		double upbound_1=getUpbound(arry_std,alpha);//方差的门限
		Double[] upbound=new Double[4];//定义门限数组
		upbound[0]=upbound_0;//均值门限
		upbound[1]=upbound_1;//方差门限
		upbound[2]=ave_0;//初始均值
		upbound[3]=std_0;//初始方差

		return upbound;
	}






	//进行验证
	public static int getDetection(Double[] a,Double[] b,Double[] c,Double[] d) {

		double arry1_ave= Math.abs(getAverage(c)-a[2]);
		double arry1_std=Math.abs(getStandardDiviation(c));
		//System.out.println("均值"+arry1_ave);
		//System.out.println("方差"+arry1_std);
		double arry2_ave= Math.abs(getAverage(d)-b[2]);
		double arry2_std=Math.abs(getStandardDiviation(d));
		//System.out.println("均值"+arry2_ave);
		//System.out.println("方差"+arry2_std);
		//定义异常值a0,a1,以及全局异常值at
		int at1_1=0,at1_2=0,at2_1=0,at2_2=0,ut=0,at=0;//at是全局异常值
		if(arry1_ave>a[0])at1_1=1;
		if(arry1_std>a[1]-1.5)at1_2=1;
		if(arry2_ave>b[0])at2_1=1;
		if(arry2_std>b[1]+1)at2_2=1;
		at=at1_2+at2_2;//将单条流的异常度相加
//		at=at1_2;//将单条流的异常度相加
		if(at>=1){
			ut=1;
			//System.out.print("at="+at);
			//System.out.print("ut="+ut);
			//System.out.println("有人入侵");
		}
		else{
			ut=0;
			//System.out.print("at="+at);
			//System.out.print("ut="+ut);
			//System.out.println("无人入侵");
		}

		return ut;
	}


	/**
	 * 求给定双精度数组的门限
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果,如果输入值不合法，返回为-1
	 */
	public static double getUpbound(Double[] inputData,double alf){
		double max=getMax(inputData);
		double min=getMin(inputData);
		double upbound=0;
		for(int i=0;i<inputData.length;i++){
			inputData[i]=(inputData[i]-min)/(max-min);
		}
		double max1=getMax(inputData);
		double min1=getMin(inputData);
		double std=getStandardDiviation(inputData);
		//计算宽度因子
		double h=2.45*std*Math.pow(inputData.length, -0.2);

		//计算概率密度以及概率密度分布函数
		double Fx=0,x=0;
		//double fx=0;
		for(int i=0;i<(max1-min1)/0.01;i++){
			double fx=0;
			x=min1-0.01+i*0.01;
			for(int j=0;j<inputData.length;j++){
				double xt=0;
				double q=(x-inputData[j])/h;
				if(Math.abs(q)<=1){
					xt=0.75*(1-Math.pow(q,2));
				}
				fx=fx+xt;

			}
			fx=fx/(h*inputData.length);
			Fx=Fx+fx*0.01;
			if(Fx>1-alf&upbound==0){
				upbound=x;
				break;
			}
		}
		upbound=upbound*(max-min)+min;
		System.out.println("门限"+upbound);
		return upbound;	
	}




	/**
	 * 求给定双精度数组中值的最大值
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果,如果输入值不合法，返回为-1
	 */
	public static double getMax(Double[] inputData) {
		if (inputData == null || inputData.length == 0)
			return -1;
		int len = inputData.length;
		double max = inputData[0];
		for (int i = 0; i < len; i++) {
			if (max < inputData[i])
				max = inputData[i];
		}
		return max;
	}


	/**
	 * 求求给定双精度数组中值的最小值
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果,如果输入值不合法，返回为-1
	 */
	public static double getMin(Double[] inputData) {
		if (inputData == null || inputData.length == 0)
			return -1;
		int len = inputData.length;
		double min = inputData[0];
		for (int i = 0; i < len; i++) {
			if (min > inputData[i])
				min = inputData[i];
		}
		return min;
	}

	/**
	 * 求给定双精度数组中值的和
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果
	 */
	public static double getSum(Double[] inputData) {
		if (inputData == null || inputData.length == 0)
			return -1;
		int len = inputData.length;
		double sum = 0;
		for (int i = 0; i < len; i++) {
			sum = sum + inputData[i];
		}

		return sum;

	}

	/**
	 * 求给定双精度数组中值的数目
	 * 
	 * @param input
	 *            Data 输入数据数组
	 * @return 运算结果
	 */
	public static int getCount(Double[] inputData) {
		if (inputData == null)
			return -1;

		return inputData.length;
	}

	/**
	 * 求给定双精度数组中值的平均值
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果
	 */
	public static double getAverage(Double[] inputData) {
		if (inputData == null || inputData.length == 0)
			return -1;
		int len = inputData.length;
		double result;
		result = getSum(inputData) / len;

		return result;
	}

	/**
	 * 求给定双精度数组中值的平方和
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果
	 */
	public static double getSquareSum(Double[] inputData) {
		if(inputData==null||inputData.length==0)
			return -1;
		int len=inputData.length;
		double sqrsum = 0.0;
		for (int i = 0; i <len; i++) {
			sqrsum = sqrsum + inputData[i] * inputData[i];
		}


		return sqrsum;
	}


	/**
	 * 求给定双精度数组中值的方差
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果
	 */
	public static double getVariance(Double[] inputData) {
		int count = getCount(inputData);
		//double sqrsum = getSquareSum(inputData);
		double average = getAverage(inputData);
		double result=0;
		for(int i=0;i<inputData.length;i++){
			result =result+ Math.pow((inputData[i]-average),2)/(inputData.length-1);
		}
		//result = (sqrsum - count * average * average) / count;

		return result; 
	}

	/**
	 * 求给定双精度数组中值的标准差
	 * 
	 * @param inputData
	 *            输入数据数组
	 * @return 运算结果
	 */
	public static double getStandardDiviation(Double[] inputData) {
		double result;
		//绝对值化很重要
		result = Math.sqrt(Math.abs(getVariance(inputData)));

		return result;

	}
}

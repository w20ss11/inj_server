package com.server.util;


//������ֵ
public class CountDouble {

	//��������
	public static Double[] getMyupbound(Double[] arry_test0){
		//ȥ��ǰ10�������
		int N0=arry_test0.length-30;
		double[] arry_test=new double[N0];
		for(int i=0;i<N0;i++){
			 arry_test[i]=arry_test0[i+30];
		 }

		//ȡ��Ĭ״̬������һ���֣������ʼ��ֵ�ͱ�׼��(��������Ҫһֱ��������Ҫ)
		int N1=11;//��ȡ�ĳ���(�ɵ�)
		Double[] arry_t1=new Double[N1];
		for(int i=0;i<N1;i++){
			arry_t1[i]=arry_test[i+49];
		}
		double ave_0= getAverage(arry_t1);
		double std_0=getStandardDiviation(arry_t1);

		//ʹ�û���������������ȡ����ֵ�ͷ�����������������ڴ洢
		int l=10;//���������ȣ��ɵ���
		int N3=arry_test.length-l+1;
		//System.out.println(N3);
		Double[] arry_ave=new Double[N3];//��ž�ֵ����
		Double[] arry_std=new Double[N3];//��ű�׼������
		for(int i=0;i<N3;i++){
			Double[] arry_t2=new Double[l];//��ʱ����
			for(int j=0;j<l;j++){
				arry_t2[j]=arry_test[i+j];
			}
			arry_ave[i]= Math.abs(getAverage(arry_t2)-ave_0);
			arry_std[i]= Math.abs(getStandardDiviation(arry_t2));
		}

		/*�����Ŷ�alf,���ݾ�Ĭ������ʱ�����ݴ�С����������alf=length(��Ĭ)/length(��������)-0.02;
		 *����ֱ�Ӹ���
		 * ��ֵ��Ҫ�ɵ�
		 */
		double alpha=0.1;

		//������
		double upbound_0=getUpbound(arry_ave,alpha);//��ֵ������
		double upbound_1=getUpbound(arry_std,alpha);//���������
		Double[] upbound=new Double[4];//������������
		upbound[0]=upbound_0;//��ֵ����
		upbound[1]=upbound_1;//��������
		upbound[2]=ave_0;//��ʼ��ֵ
		upbound[3]=std_0;//��ʼ����

		return upbound;
	}






	//������֤
	public static int getDetection(Double[] a,Double[] b,Double[] c,Double[] d) {

		double arry1_ave= Math.abs(getAverage(c)-a[2]);
		double arry1_std=Math.abs(getStandardDiviation(c));
		//System.out.println("��ֵ"+arry1_ave);
		//System.out.println("����"+arry1_std);
		double arry2_ave= Math.abs(getAverage(d)-b[2]);
		double arry2_std=Math.abs(getStandardDiviation(d));
		//System.out.println("��ֵ"+arry2_ave);
		//System.out.println("����"+arry2_std);
		//�����쳣ֵa0,a1,�Լ�ȫ���쳣ֵat
		int at1_1=0,at1_2=0,at2_1=0,at2_2=0,ut=0,at=0;//at��ȫ���쳣ֵ
		if(arry1_ave>a[0])at1_1=1;
		if(arry1_std>a[1]-1.5)at1_2=1;
		if(arry2_ave>b[0])at2_1=1;
		if(arry2_std>b[1]+1)at2_2=1;
		at=at1_2+at2_2;//�����������쳣�����
//		at=at1_2;//�����������쳣�����
		if(at>=1){
			ut=1;
			//System.out.print("at="+at);
			//System.out.print("ut="+ut);
			//System.out.println("��������");
		}
		else{
			ut=0;
			//System.out.print("at="+at);
			//System.out.print("ut="+ut);
			//System.out.println("��������");
		}

		return ut;
	}


	/**
	 * �����˫�������������
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������,�������ֵ���Ϸ�������Ϊ-1
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
		//����������
		double h=2.45*std*Math.pow(inputData.length, -0.2);

		//��������ܶ��Լ������ܶȷֲ�����
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
		System.out.println("����"+upbound);
		return upbound;	
	}




	/**
	 * �����˫����������ֵ�����ֵ
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������,�������ֵ���Ϸ�������Ϊ-1
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
	 * �������˫����������ֵ����Сֵ
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������,�������ֵ���Ϸ�������Ϊ-1
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
	 * �����˫����������ֵ�ĺ�
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������
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
	 * �����˫����������ֵ����Ŀ
	 * 
	 * @param input
	 *            Data ������������
	 * @return ������
	 */
	public static int getCount(Double[] inputData) {
		if (inputData == null)
			return -1;

		return inputData.length;
	}

	/**
	 * �����˫����������ֵ��ƽ��ֵ
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������
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
	 * �����˫����������ֵ��ƽ����
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������
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
	 * �����˫����������ֵ�ķ���
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������
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
	 * �����˫����������ֵ�ı�׼��
	 * 
	 * @param inputData
	 *            ������������
	 * @return ������
	 */
	public static double getStandardDiviation(Double[] inputData) {
		double result;
		//����ֵ������Ҫ
		result = Math.sqrt(Math.abs(getVariance(inputData)));

		return result;

	}
}

package com.rubika.aotalk.rkn;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.rubika.aotalk.util.Logging;

public class CallSoap {
	private static final String APP_TAG = "--> The Leet :: CallSoap";

	private String OPERATION_NAME = ""; 
	private String WSDL_TARGET_NAMESPACE = "http://rubi-ka.net/";
	private String SOAP_ADDRESS = "https://rubi-ka.net/API/v1/Feed.asmx";
	
	public CallSoap(String operation) 
	{
		this.OPERATION_NAME = operation;
	}
	
	public List<String> Call(List<PropertyInfo> properties)
	{
		List<String> resultData = new ArrayList<String>();
		SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
		
		for (PropertyInfo pi : properties) 
		{
		    request.addProperty(pi);
		}
	
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
	
		envelope.setOutputSoapObject(request);
	
		HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
		SoapObject response = new SoapObject();
		
		try
		{
			httpTransport.call(WSDL_TARGET_NAMESPACE + OPERATION_NAME, envelope);
			response = (SoapObject)envelope.bodyIn;
		}
		catch (Exception exception)
		{
			Logging.log(APP_TAG, exception.getMessage());
			return null;
		}
		
		Logging.log(APP_TAG, response.toString());
		
		if (response.getPropertyCount() > 0)
		{
			SoapObject data = null;
			
			try
			{
				data = (SoapObject) response.getProperty(0);
			} 
			catch (Exception exception)
			{
				Logging.log(APP_TAG, exception.getMessage());
			}
			
			if (data == null) 
			{
				resultData.add(response.getPropertyAsString(0));
			} 
			else 
			{
				for (int i = 0; i < data.getPropertyCount(); i++) 
				{
					resultData.add(data.getPropertyAsString(i));
				}
			}
			
			return resultData;
		}
		else 
		{
			return null;
		}
	}
}

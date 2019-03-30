package crawler;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;
import crawler.escalonadorCurtoPrazo.EscalonadorSimples;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class URLAddress {
	private URL address;
	private int depth;
	public URLAddress(String url,int depth) throws MalformedURLException
	{
		this.address =  new URL(formatURL(url));
		this.depth = depth;
	}
	public static String formatURL(String url)
	{
		if(!url.matches("[a-zA-Z]+://.*"))
		{
			url = "http://"+url;
		}
		
		return url;
	}
	public static String getDomain(String address) throws MalformedURLException
	{
		return new URL(formatURL(address)).getHost();
	}
	public String getProtocol()
	{
		return this.address.getProtocol();
	}
	public String getDomain()
	{
		return address.getHost();
	}
	public String getAddress() {
		return address.getProtocol()+"://"+address.getHost()+address.getFile();
	}
	public int getDepth() {
		return depth;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return address.getPath().length()==0?"/":"";
	}
	
	
	
	public static void main(String[] args) throws MalformedURLException, UnknownHostException, IOException
	{
            EscalonadorSimples escalonadorSimples = new EscalonadorSimples();
            escalonadorSimples.adicionaNovaPagina( new URLAddress("www.zerozero.pt", 0));
            escalonadorSimples.adicionaNovaPagina( new URLAddress("terra.com.br", 0));
            escalonadorSimples.adicionaNovaPagina( new URLAddress("www.forbes.com", 0));
            int numDeThreads = 1;             
            for(int i = 0; i < numDeThreads; i++){            
                PageFetcher pageFetcher = new PageFetcher(escalonadorSimples);
                Thread t1 = new Thread(pageFetcher);
                t1.start();
            }
	}
	public String toString()
	{
		return address.toString();
	}
}

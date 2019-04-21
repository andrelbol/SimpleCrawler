/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;
import crawler.escalonadorCurtoPrazo.Escalonador;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


/**
 *
 * @author aluno
 */
public class PageFetcher implements Runnable{

    Escalonador escalonador;
    String robotName;
    
    public PageFetcher(Escalonador escalonador) {
        this.robotName = "asyrobot";
        this.escalonador = escalonador;
    }

    public ArrayList<URLAddress> coletaLinksHtml(URLAddress urlAddr) throws MalformedURLException, IOException
    {
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        CleanerProperties props = htmlCleaner.getProperties();
        TagNode node = htmlCleaner.clean(new URL(urlAddr.getAddress()));
        TagNode[] anchorNodes = node.getElementsByName("a", true);
        ArrayList<URLAddress> urls = new ArrayList();
        for (TagNode anchorNode : anchorNodes) {            
            String link = anchorNode.getAttributeByName("href");            
            if(link != null && !link.contains(".php"))
            {
                URLAddress newURL = new URLAddress(link, urlAddr.getDepth() + 1); 
                if(! newURL.getDomain().isEmpty())
                {
                    if(newURL.getDomain().equals(urlAddr.getDomain()))
                    {
                        urls.add(new URLAddress(ConvertToAbsoluteUrl(urlAddr.getDomain(), newURL.getAddress()), urlAddr.getDepth() + 1));
                    }
                    else
                    {
                        urls.add(newURL);
                    }
                }
                
            }
        }
        return urls;
    }
    
    public void AddUrlsDescobertas(ArrayList<URLAddress> urlList, URLAddress urlSemente)
    {
        for(URLAddress urlAddress : urlList)
        {
            this.escalonador.adicionaNovaPagina(urlAddress);
        }
    }
    
    
    private String ConvertToAbsoluteUrl(String domain, String url) 
    {
        if(!ColetorUtil.isAbsoluteURL(url))
            return domain + url;
        return url;
    }
    
    public Record getRecord(URLAddress urlAdd) throws MalformedURLException {
        RobotExclusion robotExclusion = new RobotExclusion();
        String urlHttp = "http://" + urlAdd.getDomain()+"/robots.txt";
        String urlHttps = "https://" + urlAdd.getDomain()+"/robots.txt";
        
        Record httpRecord = robotExclusion.get(new URL(urlHttp), robotName);
        if(httpRecord == null){
            return robotExclusion.get(new URL(urlHttps), robotName);
        } else {
            return httpRecord;
        }
    }
    
    @Override
    public void run(){
        while(! escalonador.finalizouColeta()){
            try {
                URLAddress urlAdd = escalonador.getURL();
                while(urlAdd == null)
                {
                    Thread.sleep(1000);
                    urlAdd = escalonador.getURL();                    
                }
                //o Record é a classe do jrobotx que possui o robots.txt em forma de um objeto java
                Record record = escalonador.getRecordAllowRobots(urlAdd); //verifica se já existe um Record para a url
                if(record == null) { 
                    record = getRecord(urlAdd); //usa a API jrobotx para obter o record 
                    if(record == null) //sem lei
                    {
                        ArrayList<URLAddress> result = this.coletaLinksHtml(urlAdd);
                        if(result != null && result.size() > 0)
                            this.AddUrlsDescobertas(result, urlAdd);
                    }
                    else
                    {
                        escalonador.putRecorded(urlAdd.getDomain(), record);
                    }                    
                }
                else if(record.allows(urlAdd.getPath()))
                {
                    ArrayList<URLAddress> result = this.coletaLinksHtml(urlAdd);
                    if(result != null && result.size() > 0)
                        this.AddUrlsDescobertas(result, urlAdd);
                }
            } 
            catch (MalformedURLException ex) {
                //Logger.getLogger(PageFetcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                //Logger.getLogger(PageFetcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                //Logger.getLogger(PageFetcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

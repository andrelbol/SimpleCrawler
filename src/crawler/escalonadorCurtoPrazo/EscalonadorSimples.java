package crawler.escalonadorCurtoPrazo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;

import crawler.Servidor;
import crawler.URLAddress;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EscalonadorSimples implements Escalonador{
    
        private static int PROFUNDIDADE_MAXIMA = 5;
        private static int MAXIMO_DE_PAGINAS = 20;

        private int numeroDePaginasColetadas = 0;
    
        LinkedHashMap<Servidor, ArrayList<URLAddress>> filaDePaginas = new LinkedHashMap();
        HashSet<String> urlsConhecidas =  new HashSet();
        HashMap<Servidor, Record> listaDeRecords = new HashMap();
        List<String> urlColetadas = new ArrayList();

	@Override
	public synchronized URLAddress getURL() {            
            while(! this.filaDePaginas.isEmpty()){
                for (Servidor serv : filaDePaginas.keySet()) {
                    if(serv.isAccessible())
                    {
                        URLAddress urlAddress =  filaDePaginas.get(serv).get(0);                        
                        filaDePaginas.get(serv).remove(0);
                        countFetchedPage();
                        serv.acessadoAgora();
                        if(filaDePaginas.get(serv).isEmpty())
                            filaDePaginas.remove(serv);
                        urlColetadas.add(urlAddress.getAddress());
                       return urlAddress;
                    }

                }		            
                try {
                    this.wait(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(EscalonadorSimples.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return null;
	}

	@Override
	public synchronized boolean adicionaNovaPagina(URLAddress urlAdd) {
            Servidor serv = new Servidor(urlAdd.getDomain());
            
            if(this.urlsConhecidas.contains(urlAdd.getAddress()) || urlAdd.getDepth() > PROFUNDIDADE_MAXIMA)
                    return false;
            
            if(!filaDePaginas.containsKey(serv)){
                filaDePaginas.put(serv, new ArrayList<URLAddress>());
            } 
            urlsConhecidas.add(urlAdd.getAddress());
            filaDePaginas.get(serv).add(urlAdd);
            return true;
	}


	@Override
	public Record getRecordAllowRobots(URLAddress url) {
            Servidor serv = new Servidor(url.getDomain());
            return listaDeRecords.get(serv);
	}

	@Override
	public void putRecorded(String domain, Record domainRec) {
            Servidor serv = new Servidor(domain);
            this.listaDeRecords.put(serv, domainRec);		
	}
	@Override
	public boolean finalizouColeta() {
            if(numeroDePaginasColetadas >= MAXIMO_DE_PAGINAS)
            {
                for(String urlColetada : this.getUrlColetadas())
                {
                    System.out.println(urlColetada);
                }
                return true;
            }
            return false;
	}

	@Override
	public void countFetchedPage() {
            numeroDePaginasColetadas++;
	}

    public List<String> getUrlColetadas() {
        return urlColetadas;
    }
	
        
}

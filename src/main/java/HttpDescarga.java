
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nahuel
 */
public class HttpDescarga extends Descarga{

    public HttpDescarga(URL url, String carpetaSalida, int numConexiones) {
        super(url, carpetaSalida, numConexiones);
        descarga();
    }

    private void error(){
        establecerEstado(ERROR);
    }
    
    @Override
    public void run() {
        HttpURLConnection conn = null;
        
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            
            conn.connect();
            
            if (conn.getResponseCode() / 100 != 2){
                error();
            }
            
            int tamanoData = conn.getContentLength();
            if (tamanoData < 1){
                error();
            }
            
            if (tamano == -1){
                tamano = tamanoData;
                cambioEstado();
            }
            
            if (estado == DESCARGANDO){
                if (listaDescargaThread.size() == 0){
                    if (tamano > MIN_TAMANO_DESCARGA){
                        int tamanoParte =  Math.round(((float)tamano / numConexiones) / TAMANO_BLOQUE) * TAMANO_BLOQUE;
                        
                        int comienzoByte = 0;
                        int finByte = tamanoParte - 1;
                        
                        HttpDescargaThread  thread = new HttpDescargaThread(1, url, carpetaSalida + nombre, comienzoByte, finByte);
                        listaDescargaThread.add(thread);
                        
                        int i = 2;
                        
                        while (finByte < tamano){
                            comienzoByte = finByte + 1;
                            finByte += tamanoParte;
                            thread = new HttpDescargaThread(1, url, carpetaSalida + nombre, comienzoByte, finByte);
                            listaDescargaThread.add(thread);
                        }
                    }else {
                        HttpDescargaThread thread = new HttpDescargaThread(1, url, carpetaSalida + nombre, 0, tamano);
                    }
                }else {
                    for (int i = 0; i < listaDescargaThread.size(); ++i){
                        if (!listaDescargaThread.get(i).terminado())
                            listaDescargaThread.get(i).esperaTermina();
                    }
                }
                
                for (int i = 0; i < listaDescargaThread.size(); ++i){
                    listaDescargaThread.get(i).esperaTermina();
                }
                
                if (estado == DESCARGANDO)
                    establecerEstado(COMPLETADO);
            }
        } catch (IOException ex) {
            error();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }
    
    private class HttpDescargaThread extends DescargaThread {

        
        public HttpDescargaThread(int id, URL u, String aS, int cB, int fB){
            super(id, u, aS, cB, fB);
        }
        
        @Override
        public void run() {
            BufferedInputStream in = null;
            RandomAccessFile raf = null;
            
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                String rangoByte = comienzoByte + "-" + finByte;
                conn.setRequestProperty("Range", "bytes=" + rangoByte);
                
                conn.connect();
                
                if (conn.getResponseCode() / 100 != 2){
                    error();
                }
                
                in = new BufferedInputStream(conn.getInputStream());
                
                raf = new RandomAccessFile(nombre, "rw");
                raf.seek(comienzoByte);
                
                byte data[] = new byte[TAMANO_BUFFER];
                int numLeer;
                
                while ((estado == DESCARGANDO) && ((numLeer = in.read(data, 0, TAMANO_BUFFER)) != -1)){
                    raf.write(data, 0, numLeer);
                    comienzoByte += numLeer;
                    descargado(numLeer);
                }
                
                if (estado == DESCARGANDO){
                    termino = true;
                }
            } catch (IOException ex) {
                error();
            } finally {
                if (raf != null){
                    try {
                        raf.close();
                    } catch (IOException ex) {
                        
                    }
                }
                
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException ex) {
                        
                    }
                }
            }
        }
        
    }
}

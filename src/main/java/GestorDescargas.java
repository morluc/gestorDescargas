
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nahuel
 */
public class GestorDescargas {
    private static GestorDescargas instancia = null;
    
    private static final int DEFAULT_NUM_CON_POR_DESCARGA = 8; 
    public static final String DEFAULT_CARPETA_SALIDA = "";
    
    private int numConPorDescarga;
    private ArrayList<Descarga> listaDescarga;
    
    protected GestorDescargas(){
        numConPorDescarga = DEFAULT_NUM_CON_POR_DESCARGA;
        listaDescarga = new ArrayList<Descarga>();
    }
    
    /**
     * Obtener cantidad de conexiones por descarga
     * @return int
     */
    public int obtenerNumConPorDescarga(){
        return numConPorDescarga;
    }
    
    /**
     * Establece la cantidad de conexion por descarga
     * @param v
     */
    public void establecerNumConPorDescarga(int v){
        numConPorDescarga = v;
    }
    
    /**
     * Devuelve la descarga que coincide con el index
     * @param index
     * @return 
     */
    public Descarga obtenerDescarga(int index){
        return listaDescarga.get(index);
    }
    
    public void eliminarDescarga(int index){
        listaDescarga.remove(index);
    }
    
    /**
     * Devuelve la lista de descarga
     * @return 
     */
    public ArrayList<Descarga> obtenerListaDescarga(){
        return listaDescarga;
    }
    
    public Descarga crearDescarga(URL verificadaURL, String carpetaSalida){
        HttpDescarga aux = new HttpDescarga(verificadaURL, carpetaSalida, numConPorDescarga);
        listaDescarga.add(aux);
        
        return aux;
    }
    
    /**
     * Devuelve la unica instancia del gestor
     * @return 
     */
    public static GestorDescargas obtenerInstancia(){
        if (instancia == null)
            instancia = new GestorDescargas();
        
        return instancia;
    }
    
    /**
     * Verifica que la url sea valida
     * @param urlArchivo
     * @return 
     */
    public static URL verificarURL(String urlArchivo){
        URL aux = null;
        
        if (urlArchivo.toLowerCase().startsWith("http://")){
            try {
                aux = new URL(urlArchivo);
            } catch (MalformedURLException ex) {
                
            }
            
            if (aux != null && aux.getFile().length() < 2)
                aux = null;
        }
        
        return aux;
    }
}


import java.net.URL;
import java.util.ArrayList;

import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nahuel
 */
public abstract class Descarga extends Observable implements Runnable{
    
    protected static final int TAMANO_BLOQUE = 4096;
    protected static final int TAMANO_BUFFER = 4096;
    protected static final int MIN_TAMANO_DESCARGA = TAMANO_BLOQUE * 100;
    
    public static final int DESCARGANDO = 0;
    public static final int PAUSA = 1;
    public static final int COMPLETADO = 2;
    public static final int CANCELADO = 3;
    public static final int ERROR = 4;
    
    /* url del archivo */
    protected URL url;
    /* direccion de la salida del archivo */
    protected String carpetaSalida;
    /* numero de conexiones(thread) */
    protected int numConexiones;
    /* nombre del archivo */
    protected String nombre;
    /* tamaño del archivo */
    protected int tamano;
    /* estado de la descarga*/
    protected int estado;
    /* tamaño del archivo descargado */
    protected int tamanoDescargado;
    /* lista de los thread de descarga */
    protected ArrayList<DescargaThread> listaDescargaThread;
    
    /**
     * Constructor
     * @param url
     * @param carpetaSalida
     * @param numConexiones 
     */
    public Descarga(URL url, String carpetaSalida, int numConexiones){
        this.url = url;
        this.carpetaSalida = carpetaSalida;
        this.numConexiones = numConexiones;
        
        String urlArchivo = url.getFile();
        nombre = urlArchivo.substring(urlArchivo.lastIndexOf('/') + 1);
        
        tamano = -1;
        estado = DESCARGANDO;
        tamanoDescargado = 0;
        
        listaDescargaThread = new ArrayList<DescargaThread>();
    }
    
    /**
     * Pausa la descarga
     */
    public void pausa(){
        establecerEstado(PAUSA);
    }
    
    /**
     * Reanuda la descarga
     */
    public void resumir(){
        establecerEstado(DESCARGANDO);
        descarga();
    }
    
    /**
     * Cancela la descarga
     */
    public void cancelar(){
        establecerEstado(CANCELADO);
    }
    
    /**
     * Obtener url del archivo
     * @return url (String)
     */
    
    public String obtenerURL(){
        return url.toString();
    }
    
    /**
     * Obtener el Tamaño del archivo
     * @return tamano
     */
    public int obtenerTamano(){
        return tamano;
    }
    
    /**
     * Obtener el progreso de la descarga
     * @return progreso
     */
    public float obtenerProgreso(){
        return ((float)tamanoDescargado / tamano) * 100;
    }
    
    /**
     * Obtiene el estado actual de la descarga
     * @return estado
     */
    public int obtenerEstado(){
        return estado;
    }
    
    /**
     * Establece el estado
     * @param estado
     */
    protected void establecerEstado(int e){
        estado = e;
        cambioEstado();
    }
    
    /**
     * Emmpieza la descarga
     */
    protected void descarga(){
        Thread t = new Thread(this);
        t.start();
    }
    
    /**
     * Incrementa el tamaño descargado
     * @param tamanoDescargado
     */
    protected synchronized void descargado(int v){
        tamanoDescargado += v;
        cambioEstado();
    }
    
    /**
     * Establece que el estado cambio y avisa a los observadores
     */
    protected void cambioEstado(){
        setChanged();
        notifyObservers();
    }
    
    /**
     * Thread para descargar una parte del archivo
     */
    protected abstract class DescargaThread implements Runnable {
        protected int threadID;
        protected URL url;
        protected String archivoSalida;
        protected int comienzoByte;
        protected int finByte;
        protected boolean termino;
        protected Thread thread;
        
        public DescargaThread(int threadID, URL url, String archivoSalida, int comienzoByte, int finByte){
            this.threadID = threadID;
            this.url = url;
            this.archivoSalida = archivoSalida;
            this.comienzoByte = comienzoByte;
            this.finByte = finByte;
            termino = false;
            
            descarga();
        }
        
        /**
         * Devuelve si termino la descarga o no
         * @return termino
         */
        public boolean terminado(){
            return termino;
        }
        
        /**
         * Comienza o reanuda la descarga
         */
        public void descarga(){
            thread = new Thread(this);
            thread.start();
        }
        
        /**
         * Espera a que termine la descarga
         */
        public void esperaTermina() {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Descarga.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

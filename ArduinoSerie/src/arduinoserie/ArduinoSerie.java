/*
 * ArduinoSerie.java
 */
package arduinoserie;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**Programa para hacer eco a un mensaje enviado desde la pc hacia un arduino.
 * @author Ingeniero en Computación Ricardo Presilla.
 * @version 0.1 Beta
 */
public class ArduinoSerie implements SerialPortEventListener{
    //Indica si esta conectado ó no
    boolean conectado=false;
    /**Almacena los nombres de los puertos de comunicación.*/
    private static final String Puertos[] = {
        //"/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyUSB0", // Linux
        "/dev/ttyUSB1", // Linux
        "/dev/ttyACM0",
        "COM3", // Windows
        "COM7"
    };
    //Puerto serial
    SerialPort puertoSerial;
    /**Bufer de entrada para el puerto.*/
    private InputStream input;
    /**Bufer de salida para el puerto.*/
    private OutputStream output;
    /**Tiempo de espera de conección.*/
    private static final int TIME_OUT = 2000;
    /**Comunicación por defecto en bits por segundos*/
    private static final int DATA_RATE = 9600;
    /**Método para inicializar el programa.*/
    public void initialize(){
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : Puertos) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            conectado=false;
            System.out.println("El puerto de comunicación no pudo ser "
        +"encontrado.");
        }
        try{//Apertura del puerto serial
            puertoSerial = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            //Parametros de inicio
            puertoSerial.setSerialPortParams(DATA_RATE,SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            input = puertoSerial.getInputStream();
            output = puertoSerial.getOutputStream();
            //Agregar los eventos
            puertoSerial.addEventListener(this);
            puertoSerial.notifyOnDataAvailable(true);
            conectado=true;
        }catch(PortInUseException | UnsupportedCommOperationException | 
                IOException | TooManyListenersException ex){
            System.err.println(ex.toString());
        }
    }
    /**Método para detener el puerto en linux.*/
    public synchronized void close() {
        if (puertoSerial != null) {
            puertoSerial.removeEventListener();
            puertoSerial.close();
        }
    }
/**Este método indica si el arduino esta conectado.
 * @return Regresa verdadero si el arduino esta conectado sino regresa falso.
 */
    public synchronized boolean conectado() {
        return conectado;
    }
/**Handle an event on the serial port. Lee el dato y lo imprime
     * @param oEvent    Alamacena el evento del puerto serial.
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try{
                int available = input.available();
                byte chunk[] = new byte[available];
                input.read(chunk, 0, available);
                System.out.println("");//imprime los resultados
                System.out.println("Arduino dice: "+new String(chunk));
            }catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }
    /**Envia un mensaje al arduino
     * @param cadena    Alamacena un string.
     */
    public synchronized void sendMessage(String cadena) {
        byte[] asd;
        asd=cadena.getBytes();
        try{
            output.write(asd);
        }catch (IOException ex) {
            Logger.getLogger(ArduinoSerie.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    /**Constructor de la clase.*/
    public void ArduinoSerie(){}
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception Para obtener las excepciones.
     */
    public static void main(String[] args) throws Exception{
        boolean salir=false;
        BufferedReader buffer = new BufferedReader(
                                    new InputStreamReader(System.in));
        String linea="";
        ArduinoSerie objeto = new ArduinoSerie();
        while (salir==false) {
            objeto.initialize();
            if (objeto.conectado()) {
                System.out.println("Prueba de ECO");
                System.out.println("");
                System.out.println("Aduino debe devolver lo que se le mande por"
                        + " aqui.");
                System.out.println("");
                System.out.println("Ahora puedes enviar tu tus mensajes para "
                        + "probar : )");
                while (!linea.equals("fin")) {
                    try{
                        System.out.print("¿Qué le quieres decir a Arduino? :");
                        linea = buffer.readLine();
                        if (linea.equals("fin")) {
                            objeto.close();
                            salir=true;
                        }
                        else{
                            objeto.sendMessage(linea);
                        }
                    }catch(Exception e){
                        System.out.println("Error de salida.");
                    }
                }
            }
            else{
                System.out.println("No se ha podido conectar.");
                System.out.println(";¿Deseas volver a intentarlo? [s/n] :");
                linea =buffer.readLine();
                if(linea.equals("s") || linea.equals("S")) {
                    salir=false;
                }
                else{
                    salir=true;
                }
            }
        }
    }
    
}

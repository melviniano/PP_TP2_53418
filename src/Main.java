import java.io.*;
import java.util.*;
import java.time.LocalDate;

class CupoExcedidoException extends Exception {
    public CupoExcedidoException(String m) { super(m); }
}

interface Certificable {
    String ENTIDAD_EMISORA = "UTN - Facultad Regional Mendoza";
    String generarCertificadoEstudiante(Estudiante e);
}

class Sala implements Serializable {
    private int id; private String nombre;
    public Sala(int id, String n) { this.id = id; this.nombre = n; }
    public String getNombre() { return nombre; }
}

class Estudiante implements Serializable {
    private String legajo, nombre;
    public Estudiante(String l, String n) { this.legajo = l; this.nombre = n; }
    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
}

class Inscripcion implements Serializable {
    private LocalDate fecha = LocalDate.now();
    private String estado = "PENDIENTE";
    private Estudiante estudiante; private TicketDeAcceso ticket;

    public Inscripcion(Estudiante e) { this.estudiante = e; }
    public void confirmarInscripcion() {
        this.estado = "CONFIRMADA";
        this.ticket = new TicketDeAcceso("TK-" + estudiante.getLegajo() + "-" + (int)(Math.random()*1000));
    }
    public String getEstado() { return estado; }
    public Estudiante getEstudiante() { return estudiante; }
    public TicketDeAcceso getTicket() { return ticket; }

    class TicketDeAcceso implements Serializable {
        private String idTicket; private LocalDate fechaEmision = LocalDate.now();
        public TicketDeAcceso(String id) { this.idTicket = id; }
        public void enviarTicket() {
            System.out.println("   [HILO-ENVÍO] Enviando Ticket " + idTicket + " al alumno " + estudiante.getNombre());
        }
    }
}

abstract class Actividad implements Serializable {
    private int id, cupoMaximo; private String titulo;
    private List<Inscripcion> inscripciones = new ArrayList<>();

    public Actividad(int id, String t, int c) { this.id = id; this.titulo = t; this.cupoMaximo = c; }
    public Inscripcion inscribir(Estudiante e) throws CupoExcedidoException {
        if (inscripciones.size() >= cupoMaximo) throw new CupoExcedidoException("Error: Cupo agotado en '" + titulo + "'. Máximo: " + cupoMaximo);
        Inscripcion n = new Inscripcion(e); inscripciones.add(n); return n;
    }
    public abstract double calcularCostoMateriales();
    public abstract String getTipo();
    public String getTitulo() { return titulo; }
    public List<Inscripcion> getInscripciones() { return inscripciones; }
}

class Charla extends Actividad {
    private String disertante;
    public Charla(int id, String t, int c, String d) { super(id, t, c); this.disertante = d; }
    public double calcularCostoMateriales() { return 1500.0; }
    public String getTipo() { return "Charla"; }
}

class Taller extends Actividad implements Certificable {
    private boolean requiereNotebook;
    public Taller(int id, String t, int c, boolean r) { super(id, t, c); this.requiereNotebook = r; }
    public double calcularCostoMateriales() { return 4500.0; }
    public String getTipo() { return "Taller"; }
    public String generarCertificadoEstudiante(Estudiante e) { return "CERTIFICADO - Taller: " + getTitulo() + " | Alumno: " + e.getNombre() + " | Emite: " + ENTIDAD_EMISORA; }
}

class Curso extends Actividad implements Certificable {
    private int nivel;
    public Curso(int id, String t, int c, int n) { super(id, t, c); this.nivel = n; }
    public double calcularCostoMateriales() { return 8000.0; }
    public String getTipo() { return "Curso"; }
    public String generarCertificadoEstudiante(Estudiante e) { return "CERTIFICADO - Curso: " + getTitulo() + " | Alumno: " + e.getNombre() + " | Emite: " + ENTIDAD_EMISORA; }
}

class EventoUniversitario implements Serializable {
    private String id, titulo; private Sala sala; private List<Actividad> actividades = new ArrayList<>();
    public EventoUniversitario(String id, String t) { this.id = id; this.titulo = t; }
    public void asignarSala(Sala s) { this.sala = s; }
    public void agregarActividad(Actividad ac) { actividades.add(ac); }

    @SuppressWarnings("unchecked")
    public <T extends Actividad> List<T> filtrarActividadesPorTipo(Class<T> tipo) {
        List<T> fil = new ArrayList<>();
        for (Actividad a : actividades) { if (tipo.isInstance(a)) fil.add((T) a); }
        return fil;
    }
    public double calcularCostoMateriales(List<? extends Actividad> lista) {
        double tot = 0; for (Actividad a : lista) tot += a.calcularCostoMateriales(); return tot;
    }
    public List<Actividad> getActividades() { return actividades; }
    public String getTitulo() { return titulo; }
}

class EnvioTicketsThread extends Thread {
    private EventoUniversitario ev;
    public EnvioTicketsThread(EventoUniversitario ev) { this.ev = ev; }
    public void run() {
        System.out.println("\n--- [HILO-ENVÍO] INICIANDO ENVÍO CONCURRENTE ---");
        for (Actividad a : ev.getActividades()) {
            for (Inscripcion i : a.getInscripciones()) {
                if ("CONFIRMADA".equals(i.getEstado()) && i.getTicket() != null) {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    i.getTicket().enviarTicket();
                }
            }
        }
        System.out.println("--- [HILO-ENVÍO] PROCESO TERMINADO ---");
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("=== EJECUTANDO TRABAJO PRÁCTICO 2 ===");
        EventoUniversitario ev = new EventoUniversitario("EV-53418", "Congreso Tecnológico UTN");
        ev.asignarSala(new Sala(1, "Aula Magna"));

        Charla charla = new Charla(10, "Charla de IA", 1, "Dr. Gomez");
        Taller taller = new Taller(11, "Taller de Git", 5, true);
        ev.agregarActividad(charla); ev.agregarActividad(taller);

        Estudiante alum1 = new Estudiante("53418", "Alumno Principal");
        Estudiante alum2 = new Estudiante("11111", "Compañero Uno");

        try {
            System.out.println("\n[PROCESO] Inscripciones en Charla...");
            charla.inscribir(alum1).confirmarInscripcion();
            charla.inscribir(alum2).confirmarInscripcion();
        } catch (CupoExcedidoException e) {
            System.err.println("[CATCH CONTROLADO] " + e.getMessage());
        } finally {
            System.out.println("[FINALLY] Inscripciones de charla finalizadas.");
        }

        try { taller.inscribir(alum1).confirmarInscripcion(); } catch (Exception ignored) {}

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("evento.dat"))) {
            oos.writeObject(ev);
            System.out.println("\n[SERIALIZACIÓN] Guardado en: evento.dat");
        } catch (IOException e) { System.err.println("[ERROR SERIALIZACIÓN] " + e.getMessage()); }

        System.out.println("\n=== EMISIÓN DE CERTIFICADOS (EJERCICIO 2) ===");
        for (Actividad act : ev.getActividades()) {
            if (act instanceof Certificable) {
                System.out.println(((Certificable) act).generarCertificadoEstudiante(act.getInscripciones().get(0).getEstudiante()));
            } else { System.out.println("[-] '" + act.getTitulo() + "' no emite certificados."); }
        }

        System.out.println("\n=== PRUEBA DE FILTRADO Y WILDCARDS (EJERCICIO 3) ===");
        List<Taller> listaTalleres = ev.filtrarActividadesPorTipo(Taller.class);
        System.out.println("Talleres filtrados: " + listaTalleres.size() + " | Costo materiales: $" + ev.calcularCostoMateriales(listaTalleres));

        EnvioTicketsThread hilo = new EnvioTicketsThread(ev);
        hilo.start();

        System.out.println("\n[HILO PRINCIPAL] Renderizando de forma asíncrona...");
        for (int i = 1; i <= 3; i++) {
            System.out.println("[HILO PRINCIPAL] Log activo - Iteración " + i);
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
    }
}

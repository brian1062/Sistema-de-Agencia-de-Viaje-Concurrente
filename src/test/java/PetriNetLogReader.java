import java.io.*;
import java.util.*;

public class PetriNetLogReader {

    public static void main(String[] args) {
        String logFilePath = "/tmp/petriNetResults.txt";

        try {
            PetriNetConf petriNetConf = new PetriNetConf();
            validateLogFile(logFilePath, petriNetConf);
            System.out.println("Log file validation passed successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }
    }

    public static void validateLogFile(String logFilePath, PetriNetConf petriNetConf) throws IOException {
        List<LogEntry> logEntries = readLogFile(logFilePath);
        int[] marking = petriNetConf.getInitialMarking();
        int[] previousMarking;

        for (LogEntry entry : logEntries) {
            boolean isTransitionEnabled = isTransitionEnabled(entry.transition, marking, petriNetConf);
            if (!isTransitionEnabled) {
                throw new IllegalStateException(
                    "In line " + (logEntries.indexOf(entry) + 1) + ": Transition " 
                    + entry.transition + " is not enabled with marking " + Arrays.toString(marking));
            }

            previousMarking = marking;
            marking = entry.marking;

            boolean isMarkingCorrect = isMarkingCorrect(entry.transition, previousMarking, marking, petriNetConf);
            if (!isMarkingCorrect) {
                throw new IllegalStateException(
                    "In line " + (logEntries.indexOf(entry) + 1) + ": Marking " + Arrays.toString(marking) 
                    + " is not correct after transition " + entry.transition + ". Previous marking: " 
                    + Arrays.toString(previousMarking));
            }
        }
    }

    // Clase para almacenar una entrada de log
    static class LogEntry {
        String transition;
        int[] marking;

        LogEntry(String transition, int[] marking) {
            this.transition = transition;
            this.marking = marking;
        }
    }

    // Método para leer el archivo de log
    public static List<LogEntry> readLogFile(String filePath) throws IOException {
        List<LogEntry> logEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Extraer la transición y el marcado de la línea
                String transition = extractTransition(line);
                int[] marking = extractMarking(line);

                // Crear una nueva entrada de log y añadirla a la lista
                logEntries.add(new LogEntry(transition, marking));
            }
        }

        return logEntries;
    }

    // Método para extraer la transición de una línea
    private static String extractTransition(String line) {
        String prefix = "Transition fired: {";
        int startIndex = line.indexOf(prefix) + prefix.length();
        int endIndex = line.indexOf('}', startIndex);

        if (startIndex == -1 || endIndex == -1) {
            throw new IllegalArgumentException("Invalid line format for transition: " + line);
        }

        return line.substring(startIndex, endIndex);
    }

    // Método para extraer el marcado de una línea
    private static int[] extractMarking(String line) {
        String prefix = "Marking: {";
        int startIndex = line.indexOf(prefix) + prefix.length();
        int endIndex = line.lastIndexOf('}');

        if (startIndex == -1 || endIndex == -1) {
            throw new IllegalArgumentException("Invalid line format for marking: " + line);
        }

        // Substring con el contenido de Marking y dividir en pares clave:valor
        String markingContent = line.substring(startIndex, endIndex);
        String[] placeMarkings = markingContent.split(", ");

        int[] marking = new int[placeMarkings.length];
        for (int i = 0; i < placeMarkings.length; i++) {
            String[] parts = placeMarkings[i].split(": ");
            marking[i] = Integer.parseInt(parts[1]);
        }

        return marking;
    }

    private static boolean isTransitionEnabled(String transition, int[] marking, PetriNetConf petriNetConf) {
        int transitionIndex = Integer.parseInt(transition.substring(1));
        if (transitionIndex < 0 || transitionIndex >= petriNetConf.getIncidenceMatrixIn()[0].length) {
            throw new IllegalArgumentException("Invalid transition index: " + transitionIndex);
        }

        if (!transition.startsWith("T")) {
            throw new IllegalArgumentException("Invalid transition format: " + transition);
        }
        
        for (int i = 0; i < petriNetConf.getIncidenceMatrixIn()[0].length; i++) {
            if (petriNetConf.getIncidenceMatrixIn()[transitionIndex][i] > marking[i]) {
                return false;
            }
        }

        return true;
    }

    private static boolean isMarkingCorrect(String transition, int[] previousMarking, int[] marking, PetriNetConf petriNetConf) {
        int transitionIndex = Integer.parseInt(transition.substring(1));

        if (transitionIndex < 0 || transitionIndex >= petriNetConf.getIncidenceMatrixIn()[0].length) {
            throw new IllegalArgumentException("Invalid transition index: " + transitionIndex);
        }
        
        int[] expectedMarking = new int[marking.length];
        
        for (int i = 0; i < marking.length; i++) {
            expectedMarking[i] = previousMarking[i] + petriNetConf.getIncidenceMatrixOut()[i][transitionIndex] - petriNetConf.getIncidenceMatrixIn()[i][transitionIndex];
        }

        if (!Arrays.equals(marking, expectedMarking)) {
            return false;
        }

        return true;
    }


}

package pt.fcul.masters.statistics.gui.bridges;

public class LogBridge {
	
    public void log(String text) {
        System.out.println(text);
    }
    
    public void error(String text) {
        System.err.println(text);
    }
}

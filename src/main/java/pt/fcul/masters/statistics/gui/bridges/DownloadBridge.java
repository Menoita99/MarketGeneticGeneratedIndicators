package pt.fcul.masters.statistics.gui.bridges;


import lombok.Data;

@Data
public class DownloadBridge {

	public void dowload(Object s) {
		System.out.println(s);
	}
}

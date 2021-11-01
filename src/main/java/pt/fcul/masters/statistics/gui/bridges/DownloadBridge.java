package pt.fcul.masters.statistics.gui.bridges;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadBridge {

	private WebView view;
	private static final String ASSETS = "src/main/resources/META-INF/plot";

	public void dowloadAsHtml() throws IOException {
		String html = (String) view.getEngine().executeScript("document.documentElement.outerHTML");
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("html files (*.html)", "*.html");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showSaveDialog(new Stage());
		if (file != null) {
			String fileName = file.getName();
			if (!fileName.toUpperCase().endsWith(".HTML")) 
				file = new File(file.getAbsolutePath() + ".html");
			try(PrintWriter pw = new PrintWriter(file)){
				pw.println(html);
			}
			copyFolder(new File(ASSETS).toPath(),file.getParentFile().toPath());
		}
	}




	public void dowloadAsPng() throws IOException {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("image files (*.png)", "*.png");
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showSaveDialog(new Stage());
		if (file != null) {
			String fileName = file.getName();
			if (!fileName.toUpperCase().endsWith(".PNG")) 
				file = new File(file.getAbsolutePath() + ".png");
			ImageIO.write(SwingFXUtils.fromFXImage(view.snapshot(null, null), null), "png", file);
		}
	}

	public  void copyFolder(Path src, Path dest) throws IOException {
		try (Stream<Path> stream = Files.walk(src)) {
			stream.forEach(source -> {
				try {
					File f = new File(dest.resolve(src.relativize(source)).toString());
					if(!(f.isDirectory() && f.exists()) && !f.getName().endsWith(".html"))
						Files.copy(source, dest.resolve(src.relativize(source)), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
}

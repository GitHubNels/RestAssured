import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GetProperties {
	static Properties properties = new Properties();

	public void GetProperty(String Path) throws IOException, IOException {
		properties.load(new FileInputStream(Path));
	}

}

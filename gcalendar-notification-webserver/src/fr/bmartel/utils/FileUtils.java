package fr.bmartel.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File processing.
 *
 * @author Bertrand Martel
 */
public class FileUtils {

	/**
	 * Convert a file to String text with specific encoding.
	 *
	 * @param path the path
	 * @param encoding the encoding
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readFile(String path, String encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}

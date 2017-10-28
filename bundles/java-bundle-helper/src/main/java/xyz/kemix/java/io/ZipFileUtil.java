package xyz.kemix.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public class ZipFileUtil {

	/**
	 * 
	 * zip the source file or folder to target zip or folder, if source is folder,
	 * the folder name won't be used.
	 */
	public static void zip(File source, File target) throws IOException {
		if (source == null || target == null || !source.exists()) {
			throw new IOException("Parameter can't be null or not existed");
		}
		File targetZip = target;
		if (target.exists() && target.isDirectory()) {
			targetZip = new File(target, source.getName() + FileExts.ZIP.ext());
		}
		if (source.isDirectory()) {
			zip(source.listFiles(), targetZip);
		} else if (source.isFile()) {
			zip(new File[] { source }, targetZip);
		}
	}

	/**
	 * zip several files to target zip file
	 */
	public static void zip(File[] files, File target) throws IOException {
		if (files == null || files.length == 0) {
			throw new IOException("Can't zip empty list of files");
		}
		Map<String, File> map = new HashMap<String, File>();
		for (File f : files) {
			list(f, null, map);
		}
		if (map.isEmpty()) {
			throw new IOException("Can't zip empty list of files");
		}

		FileUtils.forceMkdir(target.getParentFile());

		boolean finished = false;
		ZipArchiveOutputStream zipArchiveOut = new ZipArchiveOutputStream(target);
		try {
			for (Map.Entry<String, File> entry : map.entrySet()) {
				File file = entry.getValue();
				ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, entry.getKey());
				zipArchiveOut.putArchiveEntry(zipEntry);
				InputStream is = new FileInputStream(file);
				try {
					byte[] b = new byte[1024 * 5];
					int i = -1;
					while ((i = is.read(b)) != -1) {
						zipArchiveOut.write(b, 0, i);
					}
				} finally {
					IOUtils.closeQuietly(is);
				}
				zipArchiveOut.closeArchiveEntry();
			}
			zipArchiveOut.finish();
			finished = true;
		} finally {
			IOUtils.closeQuietly(zipArchiveOut);
			if (!finished) { // have error
				target.delete();
			}
		}
	}

	private static void list(File f, String parent, Map<String, File> map) {
		String name = f.getName();
		if (parent != null) {
			name = parent + '/' + name; // path in zip
		}
		if (f.isFile()) {
			map.put(name, f);
		} else if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				list(file, name, map);
			}
		}
	}

	/**
	 * unzip the zip or jar to target folder
	 */
	public static final void unzip(File zip, File targetFolder) throws IOException {
		if (!zip.exists()) {
			throw new FileNotFoundException(zip.toString());
		}
		if (!zip.isFile()) {
			throw new IOException("Must be file:" + zip);
		}
		if (!FileExts.ZIP.of(zip.getName()) && !FileExts.JAR.of(zip.getName())) {
			throw new IOException("Invalid compress file:" + zip);
		}

		ZipFile zipfile = new ZipFile(zip);
		try {
			Enumeration<ZipArchiveEntry> entries = zipfile.getEntries();
			if (entries == null || !entries.hasMoreElements()) {
				throw new IOException("No any files in zip to unzip");
			}

			FileUtils.forceMkdir(targetFolder);

			while (entries.hasMoreElements()) {
				ZipArchiveEntry zipEntry = entries.nextElement();
				String fname = zipEntry.getName();

				if (zipEntry.isDirectory()) {
					String fpath = FilenameUtils.normalize(targetFolder + "/" + fname);
					FileUtils.forceMkdir(new File(fpath));
					continue;
				}

				if (StringUtils.contains(fname, "/")) {
					String tpath = StringUtils.substringBeforeLast(fname, "/");
					String fpath = FilenameUtils.normalize(targetFolder + "/" + tpath);
					FileUtils.forceMkdir(new File(fpath));
				}

				InputStream input = null;
				OutputStream output = null;
				try {
					input = zipfile.getInputStream(zipEntry);

					String file = FilenameUtils.normalize(targetFolder + "/" + fname);
					output = new FileOutputStream(file);

					IOUtils.copy(input, output);
				} finally {
					IOUtils.closeQuietly(input);
					IOUtils.closeQuietly(output);
				}
			}
		} finally {
			ZipFile.closeQuietly(zipfile);
		}
	}

}

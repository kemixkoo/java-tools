package xyz.kemix.java.bundle;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.io.UTFFileHandler;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public class ManifestUtil {
	/*
	 * copied from org.osgi.framework.Constants
	 */
	public static final String BUNDLE_SYMBOLICNAME = "Bundle-SymbolicName";
	public static final String BUNDLE_VERSION = "Bundle-Version";

	public static Manifest getZipJarManifest(ZipFile zipFile, ZipArchiveEntry zipEntry) throws IOException {
		if (zipFile == null || zipEntry == null) {
			return null;
		}
		if (zipEntry.isDirectory()) {
			return null;
		}

		if (!FileExts.JAR.of(zipEntry.getName())) {
			return null;
		}
		Manifest manifest = null;

		JarInputStream jarStream = null;
		try {
			jarStream = new JarInputStream(zipFile.getInputStream(zipEntry));
			manifest = jarStream.getManifest();
		} catch (IOException e) {
			// ignore
			// if (debug)
			// e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(jarStream);
		}

		if (manifest == null) {
			// maybe the jar package in win for winrar?
			// FIXME, try ZipInputStream
			manifest = getManifest(new ZipInputStream(zipFile.getInputStream(zipEntry)));

		}
		if (manifest == null)
			throw new IOException(
					"the entry " + zipEntry.getName() + " is not valid bundle in " + zipFile + ", maybe broken");
		return manifest;
	}

	public static Manifest getJarManifest(File file) throws IOException {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}

		if (!FileExts.JAR.of(file.getName())) {
			return null;
		}
		Manifest manifest = null;

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			// ignore
			// if (debug)
			// e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(jarFile);
		}

		// if because BOM
		try {
			jarFile = new JarFile(file);
			JarEntry jarEntry = jarFile.getJarEntry(JarFile.MANIFEST_NAME);
			if (jarEntry != null) {
				InputStream inputStreamWithoutBom = UTFFileHandler
						.getInputStreamWithoutBom(jarFile.getInputStream(jarEntry));
				manifest = new Manifest();
				manifest.read(inputStreamWithoutBom);

				IOUtils.closeQuietly(inputStreamWithoutBom);
			}
		} catch (IOException e) {
			// ignore
			// if (debug)
			// e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(jarFile);
		}

		//
		if (manifest == null) {
			// FIXME, try zip file way
			manifest = getManifest(new ZipInputStream(new BufferedInputStream(new FileInputStream(file))));

		}
		if (manifest == null)
			throw new IOException("Invalid jar file: " + file);

		return manifest;
	}

	public static Manifest getZipFileManifest(ZipFile zipFile, ZipArchiveEntry zipEntry) throws IOException {
		if (zipFile == null || zipEntry == null) {
			return null;
		}

		if (!JarFile.MANIFEST_NAME.equals(zipEntry.getName().toUpperCase(Locale.ENGLISH))) {
			return null;
		}

		InputStream manifestStream = null;
		try {
			manifestStream = UTFFileHandler.getInputStreamWithoutBom(zipFile.getInputStream(zipEntry));

			Manifest manifest = new Manifest();
			manifest.read(manifestStream);
			return manifest;
		} finally {
			IOUtils.closeQuietly(manifestStream);
		}
	}

	public static Manifest getFileManifest(File manifestFile) throws IOException {
		if (manifestFile == null || !manifestFile.exists() || !manifestFile.isFile()) {
			return null;
		}

		if (!FilenameUtils.getName(JarFile.MANIFEST_NAME).equals(manifestFile.getName().toUpperCase(Locale.ENGLISH))) {
			return null;
		}
		InputStream manifestStream = null;
		try {
			manifestStream = UTFFileHandler.getInputStreamWithoutBom(new FileInputStream(manifestFile));
			Manifest man = new Manifest();
			man.read(manifestStream);
			return man;
		} finally {
			IOUtils.closeQuietly(manifestStream);
		}
	}

	private static Manifest getManifest(ZipInputStream jarInputStream) throws IOException {
		if (jarInputStream == null) {
			return null;
		}
		// iterate to find the manifest
		ZipEntry zipEntry = null;
		try {
			while ((zipEntry = jarInputStream.getNextEntry()) != null) {
				if (JarFile.MANIFEST_NAME.equals(zipEntry.getName().toUpperCase(Locale.ENGLISH))) {
					InputStream manifestStream = null;
					try {
						int size = (int) zipEntry.getSize();
						if (size == -1) {
							// FIXME,
							// org.eclipse.m2m.atl.debug.core_3.5.0.v201405260755.jar,
							// the size is -1
							size = 2048; // same as JarInputStream.getBytes
						}
						byte bytes[] = new byte[size];
						IOUtils.readFully(jarInputStream, bytes);

						manifestStream = UTFFileHandler.getInputStreamWithoutBom(new ByteArrayInputStream(bytes));

						Manifest man = new Manifest();
						man.read(manifestStream);
						return man;
					} catch (Throwable t) {
						// if (debug)
						// t.printStackTrace();
						break;
					} finally {
						IOUtils.closeQuietly(manifestStream);
					}
				}

			}
		} finally {
			IOUtils.closeQuietly(jarInputStream);
		}
		return null;
	}

}

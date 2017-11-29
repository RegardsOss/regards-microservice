package fr.cnes.regards.framework.utils.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
public final class DownloadUtils {

    private DownloadUtils() {}

    /**
     * Get an InputStream on a source URL with no proxy used
     * @param source
     * @return
     * @throws IOException
     */
    public static InputStream getInputStream(URL source) throws IOException {
        return getInputStreamThroughProxy(source, Proxy.NO_PROXY);
    }

    /**
     * works as {@link DownloadUtils#downloadThroughProxy} without proxy.
     */
    public static String download(URL source, Path destination, String checksumAlgorithm)
            throws IOException, NoSuchAlgorithmException {
        return downloadThroughProxy(source, destination, checksumAlgorithm, Proxy.NO_PROXY, null);
    }

    /**
     * works as {@link DownloadUtils#downloadThroughProxy} without proxy.
     */
    public static String download(URL source, Path destination, String checksumAlgorithm, Integer pConnectTimeout)
            throws IOException, NoSuchAlgorithmException {
        return downloadThroughProxy(source, destination, checksumAlgorithm, Proxy.NO_PROXY, pConnectTimeout);
    }

    /**
     *
     * Download from the source and write it onto the file system at the destination provided.
     * Use the provided checksumAlgorithm to calculate the checksum at the end for further verification
     *
     * @param source
     * @param destination
     * @param checksumAlgorithm
     * @param proxy
     * @return checksum, computed using the provided algorithm, of the file created at destination
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String downloadThroughProxy(URL source, Path destination, String checksumAlgorithm, Proxy proxy,
            Integer pConnectTimeout) throws NoSuchAlgorithmException, IOException {
        try(OutputStream os = Files.newOutputStream(destination, StandardOpenOption.CREATE);
        InputStream sourceStream = DownloadUtils.getInputStreamThroughProxy(source, proxy, pConnectTimeout);
        // lets compute the checksum during the copy!
        DigestInputStream dis = new DigestInputStream(sourceStream, MessageDigest.getInstance(checksumAlgorithm))) {
            ByteStreams.copy(dis, os);
            return ChecksumUtils.getHexChecksum(dis.getMessageDigest().digest());
        }
    }

    /**
     * same than {@link DownloadUtils#downloadAndCheckChecksum(URL, Path, String, String, Proxy, Integer)} with {@link Proxy#NO_PROXY} as proxy
     */
    public static boolean downloadAndCheckChecksum(URL source, Path destination, String checksumAlgorithm,
            String expectedChecksum, Integer pConnectionTimeout) throws IOException, NoSuchAlgorithmException {
        return downloadAndCheckChecksum(source, destination, checksumAlgorithm, expectedChecksum, Proxy.NO_PROXY,
                                        pConnectionTimeout);
    }

    /**
     * same than {@link DownloadUtils#downloadAndCheckChecksum(URL, Path, String, String, Proxy, Integer)} with {@link Proxy#NO_PROXY} as proxy and default timeout
     */
    public static boolean downloadAndCheckChecksum(URL source, Path destination, String checksumAlgorithm,
            String expectedChecksum) throws IOException, NoSuchAlgorithmException {
        return downloadAndCheckChecksum(source, destination, checksumAlgorithm, expectedChecksum, Proxy.NO_PROXY, null);
    }

    /**
     * Download a source to the provided destination using the provided proxy.
     * Checks if the checksum computed thanks to checksumAlgorithm match to the expected checksum
     *
     * @param source
     * @param destination
     * @param checksumAlgorithm
     * @param expectedChecksum
     * @param proxy
     * @return checksum.equals(expectedChecksum)
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean downloadAndCheckChecksum(URL source, Path destination, String checksumAlgorithm,
            String expectedChecksum, Proxy proxy, Integer pConnectionTimeout)
            throws IOException, NoSuchAlgorithmException {
        String checksum = downloadThroughProxy(source, destination, checksumAlgorithm, proxy, pConnectionTimeout);
        return checksum.equals(expectedChecksum);
    }

    /**
     *
     * @param source
     * @param proxy
     * @return
     * @throws IOException
     */
    public static InputStream getInputStreamThroughProxy(URL source, Proxy proxy) throws IOException {
        URLConnection connection = source.openConnection(proxy);
        connection.setDoInput(true); //that's the default but lets set it explicitly for understanding
        connection.connect();
        return connection.getInputStream();
    }

    /**
    *
    * @param source
    * @param proxy
    * @param pConnectTimeout Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced by this URLConnection
    * @return
    * @throws IOException
    */
    public static InputStream getInputStreamThroughProxy(URL source, Proxy proxy, Integer pConnectTimeout)
            throws IOException {
        URLConnection connection = source.openConnection(proxy);
        connection.setDoInput(true); //that's the default but lets set it explicitly for understanding
        if (pConnectTimeout != null) {
            connection.setConnectTimeout(pConnectTimeout);
        }
        connection.connect();
        return connection.getInputStream();
    }

    public static Long getContentLength(URL source, Integer pConnectTimeout) throws IOException {
        URLConnection connection = source.openConnection();
        connection.setConnectTimeout(pConnectTimeout);
        return connection.getContentLengthLong();
    }

}
